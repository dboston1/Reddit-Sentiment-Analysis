package org.SentimentAnalysis;

// Java file IO 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.StringTokenizer;

// Java sets etc (used for distributed cache)
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

// Regular expression utility
import java.util.regex.Pattern;

// Hadoop file I/O
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

// Mapper and configuration class
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

// Hadoop Counters
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;


// Hadoop wrapper classes
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.StringUtils;


public class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
	
	//define counters
	public static enum CTR{
		TRUMP,
		HILLARY,
		BOTH
	}
	
	// Reusable variables
	private Text outKey = new Text();
	private String input;
	private IntWritable value = new IntWritable();
	
	// HashSets for filter terms.
	private Set<String> posWords = new HashSet<String>();
	private Set<String> negWords = new HashSet<String>();
	private Set<String> negationWords = new HashSet<String>();
	private Set<String> trumpContext = new HashSet<String>();
	private Set<String> hillaryContext = new HashSet<String>();
	private Set<String> adjectives = new HashSet<String>();
    
	protected void 
		setup(Mapper<LongWritable, Text, Text, IntWritable>.Context context)
		throws IOException, InterruptedException {
		// If the input for this mapper is a file reference, read from the
		// referenced file. Otherwise, read from the InputSplit itself.
		if (context.getInputSplit() instanceof FileSplit)
		{
			this.input = 
					((FileSplit) context.getInputSplit()).getPath().toString();
		} else {
			this.input = context.getInputSplit().toString();
		}
		
		URI[] localPaths = context.getCacheFiles();
		
		int uriCount = 0;
		
		parseNegation(localPaths[uriCount++]);
		parsePositive(localPaths[uriCount++]);
		parseNegative(localPaths[uriCount]);
		
		initializeContextHashsets();
		initializeAdjectives();
	}
	
	private void initializeContextHashsets(){
        trumpContext.add("trump");
        trumpContext.add("trumps");
        trumpContext.add("don");
        trumpContext.add("donald");
        trumpContext.add("donny");
        trumpContext.add("dons");
        trumpContext.add("donalds");
        trumpContext.add("donnys");
        trumpContext.add("he");
        trumpContext.add("his");
        trumpContext.add("hed");
        trumpContext.add("hell");   //he'll without the apostrophe (removed by preprocessing). Not a joke! :)
        trumpContext.add("him");
        trumpContext.add("man");
        trumpContext.add("guy");
        trumpContext.add("boy");
        trumpContext.add("drumpf");
        
        hillaryContext.add("hillary");
        hillaryContext.add("hilary");
        hillaryContext.add("hillarys");
        hillaryContext.add("hilarys");
        hillaryContext.add("clinton");
        hillaryContext.add("clintons");
        hillaryContext.add("shed");
        hillaryContext.add("she");
        hillaryContext.add("hers");
        hillaryContext.add("her");
        hillaryContext.add("shell");
        hillaryContext.add("woman");
        hillaryContext.add("lady");
        hillaryContext.add("girl");
    }
    
    private void initializeAdjectives(){
		 adjectives.add("as");
		 adjectives.add("quite");
		 adjectives.add("like");
		 adjectives.add("much");
		 adjectives.add("for");
	}
	
	// Parse the positive words 
	private void parsePositive(URI posWordsUri) {
		try {
			BufferedReader fis = new BufferedReader(new FileReader(
					new File(posWordsUri.getPath()).getName()));
			String posWord;
			while ((posWord = fis.readLine()) != null) {
				posWords.add(posWord);
			}
		} catch (IOException ioe) {
			System.err.println("Caught exception parsing cached file '"
					+ posWords + "' : " + StringUtils.stringifyException(ioe));
		}
	}
  
  // Parse the negative words 
	private void parseNegative(URI negWordsUri) {
		try {
			BufferedReader fis = new BufferedReader(new FileReader(
					new File(negWordsUri.getPath()).getName()));
			String negWord;
			while ((negWord = fis.readLine()) != null) {
				negWords.add(negWord);
			}
		} catch (IOException ioe) {
			System.err.println("Caught exception while parsing cached file '"
					+ negWords + "' : " + StringUtils.stringifyException(ioe));
		}
	}
	
	// Parse the negation words 
	private void parseNegation(URI negationWordsUri) {
		try {
			BufferedReader fis = new BufferedReader(new FileReader(
					new File(negationWordsUri.getPath()).getName()));
			String negationWord;
			while ((negationWord = fis.readLine()) != null) {
				negationWords.add(negationWord);
			}
		} catch (IOException ioe) {
			System.err.println("Caught exception while parsing cached file '"
					+ negationWords + "' : " + StringUtils.stringifyException(ioe));
		}
	}
  	
  	
  	// input to mapper is in form: docID<tab>hillary|trump|both<tab>comment<newline>
	public void map(LongWritable offset, Text lineData, Context context)
        throws IOException, InterruptedException {
        
		StringTokenizer itr = new StringTokenizer(lineData.toString(), "\n");
		
		// Setup for sentiment analysis
        String line;
        String docId;
        String writtenAbout;
        String uTC;
        int sentimentValue;
        int index;
        boolean negation;
        
        
        // Parse comment by comment
        while(itr.hasMoreTokens()){
            sentimentValue = 0;
            negation = false;
            line = itr.nextToken();
            if (line.isEmpty()) {
                continue;
            }
            
            //get docID
            index = line.indexOf('\t');
            
            //if there is no docId/writtenAbout/UTC time code for some reason, don't error, just skip comment
            if(index == -1){    
                index = 0;
            }
            docId = line.substring(0, index);
            line = line.substring(++index);
            
            //get who the comment was written about (trump, hillary, both)
            index = line.indexOf('\t');
            
            if(index == -1){
                index = 0;
            }
            writtenAbout = line.substring(0, index);
            line = line.substring(++index);
            
            //get UTC (time code)
            index = line.indexOf('\t');
            
            if(index == -1){
				index = 0;
			}
            uTC = line.substring(0, index);
            
            
            //remaining line = comment body
            line = line.substring(++index);
            line = line.replaceAll("[^a-z ]", "");
            
            //increment comment counter for appropriate party (TRUMP, HILLARY, or BOTH)
            if(writtenAbout.equals("hillary")){
				context.getCounter(CTR.HILLARY).increment(1);
			}
			else if(writtenAbout.equals("trump")){
				context.getCounter(CTR.TRUMP).increment(1);
			}
			else{
				context.getCounter(CTR.BOTH).increment(1);
			}
            
            //if we know the comment was written entirely about either trump or hillary explicitly:
            if(!writtenAbout.equals("both")){
            
                //parse comment word by word
                StringTokenizer itr_2 = new StringTokenizer(line);
                while(itr_2.hasMoreTokens()){
                    String word = itr_2.nextToken();
            
                    // Classify each word as pos or neg, or a negation word
                    if (posWords.contains(word)) {
                        sentimentValue += 1; 
                    }
                    if (negWords.contains(word)) {
                        sentimentValue -= 1;
                    }
                    if (negationWords.contains(word)) {
                        if(negation == true){
                            negation = false;
                        }
                        else{
                            negation = true;
                        }
                    }
                }
                
                // output to context and move to next comment
                outKey = new Text(uTC.concat("\t").concat(writtenAbout));
                if(negation == true){
                    sentimentValue *= -1; 
                }
                value.set(sentimentValue);
                context.write(outKey, value);
            }
            
            // else, comment contains mentions of both trump and hillary:
            else{
                boolean unknown = true;
                boolean trump = false;
                int trumpSentiment = 0;
                int hillarySentiment = 0;
                
                //parse comment word by word
                StringTokenizer itr_2 = new StringTokenizer(line);
                while(itr_2.hasMoreTokens()){
                    String word = itr_2.nextToken();
                    
                    //check for hillary context switch:
                    if(hillaryContext.contains(word)){
                        //if we didn't know who the previous words (if any) were concerning:
                        if(unknown == true){
                            trump = false;
                            unknown = false;
                        }
                        //else if we know the previous words all applied to trump, but context switches to hillary:
                        else if(unknown == false && trump == true){
                            trump = false;
                            if(negation == true){
                                sentimentValue *= -1;
                            }
                            trumpSentiment += sentimentValue;
                            sentimentValue = 0;
                            negation = false;
                        }
                    }
                    
                    //check for trump context switch:
                    if(trumpContext.contains(word)){
                        //if we didn't know who the previous words (if any) were concerning:
                        if(unknown == true){
                            trump = true;
                            unknown = false;
                        }
                        //else if we know the previous words all applied to hillary, but context switches to trump:
                        else if(unknown == false && trump == false){
                            trump = true;
                            if(negation == true){
                                sentimentValue *= -1;
                            }
                            hillarySentiment += sentimentValue;
                            sentimentValue = 0;
                            negation = false;
                        }
                    }
                    
                    // Classify each word as pos or neg, or a negation word
                    if (posWords.contains(word)) {
                        
                        //checking for edge cases, such as "hillary is good, but screw trump" 
                        // or "hillary is ok, but no one will ever be as good as trump" ...
                        if(unknown == false){
							while(itr_2.hasMoreTokens()){
								String nextWord = itr_2.nextToken();
								if(trump == true){
									if(hillaryContext.contains(nextWord)){
										if(negation == true){
											sentimentValue *= -1;
											negation = false;
										}
										trumpSentiment += sentimentValue;
										sentimentValue = 0;
										trump = false;
										break;
									}
									else if(adjectives.contains(nextWord)){
										continue;
									}
									else{
										break;
									}
								}
								else{
									if(trumpContext.contains(nextWord)){
										if(negation == true){
											sentimentValue *= -1;
											negation = false;
										}
										hillarySentiment += sentimentValue;
										sentimentValue = 0;
										trump = true;
										break;
									}
									else if(adjectives.contains(nextWord)){
										continue;
									}
									else{
										break;
									}
								}
							}
						}
                        sentimentValue += 1; 
                    }
                    if (negWords.contains(word)) {
                        if(unknown == false){
							while(itr_2.hasMoreTokens()){
								String nextWord = itr_2.nextToken();
								if(trump == true){
									if(hillaryContext.contains(nextWord)){
										if(negation == true){
											sentimentValue *= -1;
											negation = false;
										}
										trumpSentiment += sentimentValue;
										sentimentValue = 0;
										trump = false;
										break;
									}
									else if(adjectives.contains(nextWord) || negWords.contains(nextWord)){
										continue;
									}
									else{
										break;
									}
								}
								else{
									if(trumpContext.contains(nextWord)){
										if(negation == true){
											sentimentValue *= -1;
											negation = false;
										}
										hillarySentiment += sentimentValue;
										sentimentValue = 0;
										trump = true;
										break;
									}
									else if(adjectives.contains(nextWord) || negWords.contains(nextWord)){
										continue;
									}
									else{
										break;
									}
								}
							}
						}
                        
                        sentimentValue -= 1;
                    }
                    if (negationWords.contains(word)) {
                        if(negation == true){
                            negation = false;
                        }
                        else{
                            negation = true;
                        }
                    }
                }
                //add last portion of sentiment analysis (remaining since comment's last context switch)
                if(negation == true){
                        sentimentValue *= -1;
                }
                if(trump == true){
                    trumpSentiment += sentimentValue;
                }
                else{
                    hillarySentiment += sentimentValue;
                }
                
                // output hillary's score for "both" comment to context:
                outKey = new Text(uTC.concat("\t").concat("hillary"));
                value.set(hillarySentiment);
                context.write(outKey, value);
                
                // output trump's score for "both" comment to context:
                outKey = new Text(uTC.concat("\t").concat("trump"));
                value.set(trumpSentiment);
                context.write(outKey, value);
            }
                
        }
    }
}








