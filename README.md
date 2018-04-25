# Reddit-Sentiment-Analysis
Program that performs textual analysis of Reddit data (approx. 300 GB of JSON data) preprocessed by another team member. Uses Hadoop's Mapreduce to classify comments as either positive or negative based on certain keywords, negation, etc.


<h2>PART 1:</h2>

<h3>TO COMPILE PROGRAM:</h3>

$ mkdir build

$ $HADOOP_HOME/bin/hadoop com.sun.tools.javac.Main *.java -d build -Xlint

$ jar -cvf SentimentAnalysis.jar -C build/ .

$ rm -r build

<h3>TO RUN:</h3>

*This assumes you have all text files (ExampleInput.txt, negate-words.txt, pos-words.txt, and neg-words.txt) in /sentimentAnalysis directory in hdfs. Modify the paths to reflect any differences.*

$HADOOP_HOME/bin/hadoop jar SentimentAnalysis.jar org.SentimentAnalysis.Driver /sentimentAnalysis/ExampleInput.txt /sentimentAnalysis/out -negation /sentimentAnalysis/negate-words.txt -pos /sentimentAnalysis/pos-words.txt -neg /sentimentAnalysis/neg-words.txt

As-is, it will take /termProject/ExampleInput.txt, run the program, and store the results in /sentimentAnalysis/out. This can be modified to a directory of input files by replacing *sentimentAnalysis/ExampleInput.txt* with */your-HDFS-Directory/* 

<h2>Part 2:</h2>

<h5> Part 2 takes the output from part 1, and summarizes the data. It is hardcoded to utilize the partitions defined in details.md, but could be altered easily to read partition data from a file, etc. </h5>

<h3> TO COMPILE PROGRAM:</h3>

$ mkdir build

$ $HADOOP_HOME/bin/hadoop com.sun.tools.javac.Main *.java -d build -Xlint

$ jar -cvf SentimentAnalysis.jar -C build/ .

$ rm -r build

<h3>TO RUN:</h3>

$ $HADOOP_HOME/bin/hadoop jar Summary.jar org.Summary.Driver /SentimentAnalysis/out /SentimentAnalysis/summary

