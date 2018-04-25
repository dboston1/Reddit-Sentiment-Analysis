package org.Summary;

// Java file IO 
import java.io.IOException;

// Reducer and configuration class
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Reducer;


// Hadoop wrapper classes
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.StringUtils;


public class Reduce extends 
	Reducer<Text, IntWritable, Text, IntWritable> {
	private final static IntWritable value = new IntWritable();
	@Override
	public void reduce(Text word, Iterable<IntWritable> instances, Context context)
			throws IOException, InterruptedException
	{
		int sum = 0;

		// Sum up the instances (sentiment values) of the current word. Note that unless docId's match, there should only be one value in instances.
		for (IntWritable instance : instances) {
			sum += instance.get();
			//context.write(word, instance);
		}
		
		// Write the word and count to output.
		value.set(sum);
		context.write(word, value);
		
	}
}
