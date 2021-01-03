package com.rbz.sparkconsumer.service;

import com.rbz.sparkconsumer.config.KafkaConsumerConfig;
import net.dean.jraw.models.Submission;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class SparkConsumerService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SparkConf sparkConf;
    private final KafkaConsumerConfig kafkaConsumerConfig;
    private final Collection<String> topics;

    @Autowired
    public SparkConsumerService(SparkConf sparkConf,
                                KafkaConsumerConfig kafkaConsumerConfig,
                                @Value("${spring.kafka.template.default-topic}") String[] topics) {
        this.sparkConf = sparkConf;
        this.kafkaConsumerConfig = kafkaConsumerConfig;
        this.topics = Arrays.asList(topics);
    }
    public void run(){
        log.debug("Running Spark Consumer Service..");

        // Create context with a 10 seconds batch interval
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(10));

        // Create direct kafka stream with brokers and topics
        JavaInputDStream<ConsumerRecord<String, Submission>> submissions = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaConsumerConfig.consumerConfigs()));

        //Count the posts and print
        submissions
                .count()
                .map(cnt -> "Popular subreddits in last 10 seconds (" + cnt + " total posts):")
                .print();

        JavaDStream<ConsumerRecord<String, Submission>> clean_submissions = submissions.filter((submission) ->{
            return !submission.value().isNsfw();
        });

        JavaDStream<String> posts_subreddit = clean_submissions.map((submission) -> {;
            return submission.value().getSubreddit();
        });
        posts_subreddit.foreachRDD( x-> {
            x.collect().stream().forEach(subreddit -> System.out.println("subreddit: "+subreddit));
        });

        posts_subreddit
            .mapToPair(subreddit -> new Tuple2<>(subreddit, 1))
            .reduceByKey((a, b) -> Integer.sum(a, b))

            .mapToPair(stringIntegerTuple2 -> stringIntegerTuple2.swap())
            .foreachRDD(rrdd -> {
                System.out.println("---------------------------------------------------------------");
                List<Tuple2<Integer, String>> sorted;
                JavaPairRDD<Integer, String> counts = rrdd.sortByKey(false);
                sorted = counts.collect();
                sorted.forEach( record -> {
                    System.out.println(String.format("subreddit -> %s (%d posts)", record._2, record._1));
                });
            });
        // Start the computation
        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
