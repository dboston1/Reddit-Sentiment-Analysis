<h3>Commands:</h3>

* *If running off of preprocessing output (Andy's):*

$HADOOP_HOME/bin/hadoop jar SentimentAnalysis.jar org.SentimentAnalysis.Driver hdfs://austin:30242/user/adolan5/out /test-output -negation /termProject/negate-words.txt -pos /termProject/pos-words.txt -neg /termProject/neg-words.txt

* *If running off of sentiment analysis output (mine):*

*replace bold sections with appropriate args:*

$HADOOP_HOME/bin/hadoop jar **your_jar** **your_driver_class** hdfs://montgomery:40181/termProject/out/ **your_output_file_** **any_additional_args**

<h3>UTC TIME by DATE:</h3>

(2-week intervals starting at Donald Trump's 2016 RNC Nomination through 2016 Election Date)

1. July 19th 12:00am: 1468886400

2. August 2nd, 12:00am: 1470096000

3. August 16th, 12:00am: 1471305600

4. August 30th, 12:00am: 1472515200

5. September 13th, 12:00am: 1473724800

6. September 27th, 12:00am: 1474934400

7. October 11th, 12:00am: 1476144000

8. October 25th, 12:00am: 1477353600

9. November 8th, 11:59pm: 1478649599


<h3>UTC TIME PARTITIONS:</h3>

1. 1468886400 to 1470096000   (July 19th through August 1st)

2. 1470096001 to 1471305600   (August 2nd through August 15th)

3. 1471305601 to 1472515200   (August 16th through August 29th) 

4. 1472515201 to 1473724800   (August 30th through September 12th)

5. 1473724801 to 1474934400   (September 13th through September 26th)

6. 1474934401 to 1476144000   (September 27th through October 10th)

7. 1476144001 to 1477353600   (October 11th through October 24th)

8. 1477353601 to 1478649599   (October 25th through November 8th)

<h3> OUTPUT OF SENTIMENT ANALYSIS </h3>

UTC_TIME_STAMP\<tab\>hillary|trump\<tab\>sentiment_score\<newline\>

Sentiment score can be -1 (negative comment), 0 (neutral), or 1 (positive comment).
