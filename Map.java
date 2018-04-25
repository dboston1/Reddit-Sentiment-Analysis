package org.Summary;

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
	// Reusable variables
	private Text outKey = new Text();
	private String input;
	private final IntWritable one = new IntWritable(1);
  	
  	
  	// input to mapper is in form: UTC<tab>hillary|trump<tab>sentimentValue<newline>
	public void map(LongWritable offset, Text lineData, Context context)
        throws IOException, InterruptedException {
        
		StringTokenizer itr = new StringTokenizer(lineData.toString(), "\n");
		
		// Setup for sentiment analysis
        String line;
        String result;
        String writtenAbout;
        String uTC_str;
        int uTC;
        int sentimentValue;
        int index;
        
        
        // Parse comment by comment
        while(itr.hasMoreTokens()){
            line = itr.nextToken();
            if (line.isEmpty()) {
                continue;
            }
            
            //get uTC
            index = line.indexOf('\t');
            uTC_str = line.substring(0, index);
            line = line.substring(++index);
            uTC = Integer.parseInt(uTC_str);
            
            //get who the comment was written about (trump or hillary)
            index = line.indexOf('\t');
            writtenAbout = line.substring(0, index);
            
            //get sentimentValue
            sentimentValue = Integer.parseInt(line.substring(++index));
		
			//check which partition each timestamp belongs in:
			if(uTC >= 1468886400 && uTC < 1470096000){
				result = "partition_1";
			}
			else if(uTC >= 1470096000 && uTC < 1471305600){
				result = "partition_2";
			}
			else if(uTC >= 1471305600 && uTC < 1472515200){
				result = "partition_3";
			}
			else if(uTC >= 1472515200 && uTC < 1473724800){
				result = "partition_4";
			}
			else if(uTC >= 1473724800 && uTC < 1474934400){
				result = "partition_5";
			}
			else if(uTC >= 1474934400 && uTC < 1476144000){
				result = "partition_6";
			}
			else if(uTC >= 1476144000 && uTC < 1477353600){
				result = "partition_7";
			}
			else if(uTC >= 1477353600 && uTC <= 1478649599){
				result = "partition_7";
			}
			else{
				result = "ERROR - " + uTC;
			}
			
			//add h or t for written about to key:
			if(writtenAbout.equals("hillary")){
				result += "\thillary";
			}
			else{
				result += "\ttrump";
			}
			
			//determine whether comment was positive (p), neutral (0), or negative (n):
			if(sentimentValue > 0){
				result += "\tpositive";
			}
			else if(sentimentValue == 0){
				result += "\tneutral";
			}
			else{
				result += "\tnegative";
			}
			
			outKey = new Text(result);
			context.write(outKey, one);
        }
    }
}








