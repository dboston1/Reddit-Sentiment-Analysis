package org.Summary;

// MapReduce utility classes
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.Job; 

// File IO classes
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

// Wrapper classes
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

// Hadoop Counters
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;

// Hadoop File System
import org.apache.hadoop.fs.FileSystem;

// Java file IO 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.StringTokenizer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


//to Compile: $HADOOP_HOME/bin/hadoop com.sun.tools.javac.Main *.java -d build -Xlint (make a "build" directory first
// then: jar -cvf SentimentAnalysis.jar -C build/ .
// to run: $HADOOP_HOME/bin/hadoop jar SentimentAnalysis.jar org.SentimentAnalysis.Driver /termProject/test.txt /test-output -negation /termProject/negate-words.txt -pos /termProject/pos-words.txt -neg /termProject/neg-words.txt

public class Driver extends Configured implements Tool {
	
	public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Driver(), args);
    System.exit(res);
  }

// The run method configures and starts the job.

	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf(), "Driver");

	// Standard job methods
	
		job.setJarByClass(this.getClass());
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setMapperClass(Map.class);
		job.setCombinerClass(Reduce.class);
		job.setReducerClass(Reduce.class);
		job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    int result = job.waitForCompletion(true) ? 0 : 1;
    return result;
  }
}
