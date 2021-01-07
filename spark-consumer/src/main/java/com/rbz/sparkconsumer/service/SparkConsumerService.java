package com.rbz.sparkconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbz.sparkconsumer.config.KafkaConsumerConfig;
import net.dean.jraw.models.Submission;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
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
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import scala.Tuple2;
import util.SteamType;

import java.util.*;

@Service
public class SparkConsumerService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.spark.output-topic}")
    private String outputTopic;

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

        clean_submissions
                .count()
                .map(cnt -> "Clean subreddits in last 10 seconds (" + cnt + " total posts):")
                .print();

        JavaDStream<String> posts_subreddit = clean_submissions.map((submission) -> {;
            return submission.value().getSubreddit();
        });
        posts_subreddit.foreachRDD( x-> {
            x.collect().stream().forEach(subreddit -> System.out.println("subreddit: "+subreddit));
        });

        ArrayList<Map<String, Object>> recordArray = new ArrayList<Map<String, Object>>();

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
                        Map<String, Object> obj =  new HashMap<>();
                        obj.put("subreddit", record._2);
                        obj.put("count", record._1);

                        recordArray.add(obj);
                        System.out.println(String.format("subreddit -> %s (%d posts)", record._2, record._1));
                    });

                    Map<String, Object> results = new HashMap<>();
                    results.put("type", SteamType.REDDIT_MENTIONS);
                    results.put("data", recordArray);

                    ObjectMapper objectMapper = new ObjectMapper();

                    String json = null;

                    json = objectMapper.writeValueAsString(results);

                    if(json != null){
                        Map<String, Object> props = new HashMap<>();

                        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                        KafkaProducer producer = new KafkaProducer<String, String>(props);

                        ProducerRecord<String, String> message = new ProducerRecord<>(outputTopic, null, json);
                        producer.send(message);
                    }
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
