# Reddit-Sentiment-Analysis
Program that performs textual analysis of Reddit data (approx. 300 GB) preprocessed by another team member. Uses Hadoop's Mapreduce to classify comments as either positive or negative based on certain keywords, negation, etc.


<h3>TO COMPILE PROGRAM:</h3>

$ mkdir build

$ HADOOP_HOME/bin/hadoop com.sun.tools.javac.Main *.java -d build -Xlint

$ jar -cvf SentimentAnalysis.jar -C build/ .

$ rm -r build

<h3>TO RUN:</h3>

*This assumes you have all text files (test.txt, negate-words.txt, pos-words.txt, and neg-words.txt) in /termProject directory in hdfs. Modify the paths to reflect any differences.*

$HADOOP_HOME/bin/hadoop jar SentimentAnalysis.jar org.SentimentAnalysis.Driver /termProject/test.txt /test-output -negation /termProject/negate-words.txt -pos /termProject/pos-words.txt -neg /termProject/neg-words.txt

As-is, it will take /termProject/test.txt, run the program, and store the results in /test-output. This can be modified to a directory of input files by replacing *termProject/test.txt* with */your-HDFS-Directory/* 
