package com.rbz.sparkconsumer1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbz.sparkconsumer1.config.KafkaConsumerConfig;
import com.rbz.sparkconsumer1.util.SteamType;
import net.dean.jraw.models.Submission;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
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

    //in seconds
    private Integer batchInterval = 3;

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

        // Create context with a 1 second batch interval
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(batchInterval));

        // Create direct kafka stream with brokers and topics
        JavaInputDStream<ConsumerRecord<String, Submission>> submissions = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaConsumerConfig.consumerConfigs()));

        //Count the posts and print
        JavaDStream<Long> count = submissions
                .count();
        count.foreachRDD( rdd -> {
            System.out.println("Stream Speed: " + rdd.collect().get(0));

            Map<String, Object> obj = new HashMap<>();
            obj.put("count", rdd.collect().get(0));
            obj.put("duration", batchInterval);

            Map<String, Object> results = new HashMap<>();
            results.put("type", SteamType.COUNT);
            results.put("data", obj);


            ObjectMapper objectMapper = new ObjectMapper();

            String json = null;

            json = objectMapper.writeValueAsString(results);

            Map<String, Object> props = new HashMap<>();

            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            KafkaProducer producer = new KafkaProducer<String, String>(props);

            ProducerRecord<String, String> message = new ProducerRecord<>(outputTopic, null, json);
            producer.send(message);
        });

                //.map(cnt -> "Stream Speed (" + cnt + " posts per minute):")
                //.print();

        // Start the computation
        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
