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
import org.apache.kafka.common.utils.Java;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
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
import util.SteamType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    private final Integer batchInterval=3;

    @Autowired
    public SparkConsumerService(SparkConf sparkConf,
                                KafkaConsumerConfig kafkaConsumerConfig,
                                @Value("${spring.kafka.template.default-topic}") String[] topics) {
        this.sparkConf = sparkConf;
        this.kafkaConsumerConfig = kafkaConsumerConfig;
        this.topics = Arrays.asList(topics);
    }

    private void startPostsPerMinuteStream(JavaInputDStream<ConsumerRecord<String, Submission>> input){
        int windowTime = 60;
        int slideTime = 60;
        //PostsPerMinute
        JavaDStream<String> windowedSubmissions = input
                .map((submission) -> { return submission.value().getId(); })
                .window(Durations.seconds(windowTime),Durations.seconds(slideTime));
        windowedSubmissions.count()
        .foreachRDD( rdd -> {
            System.out.println("Stream Speed: " + rdd.collect().get(0));

            Map<String, Object> obj = new HashMap<>();
            obj.put("count", rdd.collect().get(0));
            obj.put("duration", windowTime);
            Date date = new Date();
            DateFormat format = new SimpleDateFormat("HH:mm");
            obj.put("time", format.format(date));

            Map<String, Object> results = new HashMap<>();
            results.put("type", SteamType.POSTS_PER_MINUTE);
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
    }

    private void startSpeedStream(JavaInputDStream<ConsumerRecord<String, Submission>> input){
        //Count the posts and print
        JavaDStream<Long> count = input
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
    }

    private void startSubredditMentionsBatchnSubredditProportionStream(JavaInputDStream<ConsumerRecord<String, Submission>> input){
        int windowTime = 30;
        int slideTime = 30;

        // Clean up submissions
        JavaDStream<ConsumerRecord<String, Submission>> clean_submissions = input.filter((submission) ->{
            return !submission.value().isNsfw();
        });

        JavaDStream<String> posts_subreddit = clean_submissions
                .map((submission) -> { return submission.value().getSubreddit(); })
                .window(Durations.seconds(windowTime),Durations.seconds(slideTime));

        posts_subreddit.foreachRDD( x-> {
            x.collect().stream().forEach(subreddit -> System.out.println("subreddit: "+subreddit));
        });

        ArrayList<Map<String, Object>> recordArray = new ArrayList<Map<String, Object>>();

        posts_subreddit
                .mapToPair(subreddit -> new Tuple2<>(subreddit, 1))
                .reduceByKey((a, b) -> Integer.sum(a, b))
                .mapToPair(stringIntegerTuple2 -> stringIntegerTuple2.swap())
                .foreachRDD(rrdd -> {
                    recordArray.clear();
                    System.out.println("---------------------------------------------------------------");
                    List<Tuple2<Integer, String>> sorted;
                    JavaPairRDD<Integer, String> counts = rrdd.sortByKey(false);
                    JavaPairRDD<Integer, String> count_bigger_than_one = counts.filter((Function<Tuple2<Integer, String>, Boolean>) integerStringTuple2 -> integerStringTuple2._1 > 1);
                    JavaPairRDD<Integer, String> count_equals_one = counts.filter((Function<Tuple2<Integer, String>, Boolean>) integerStringTuple2 -> integerStringTuple2._1 == 1);
                    System.out.println("count_bigger_than_one count: "+ count_bigger_than_one.count());
                    System.out.println("count_equals_zero: "+ count_equals_one.count());


                    Map<String, Object> count_equals_one_obj =  new HashMap<>();
                        count_equals_one_obj.put("subreddit", "__OTHERS__");
                        count_equals_one_obj.put("count", count_equals_one.count());
                    recordArray.add(count_equals_one_obj);

                    sorted = count_bigger_than_one.collect();
                    sorted.forEach( record -> {
                        Map<String, Object> obj =  new HashMap<>();
                        obj.put("subreddit", record._2);
                        obj.put("count", record._1);

                        recordArray.add(obj);
                        System.out.println(String.format("subreddit -> %s (%d posts)", record._2, record._1));
                    });

                    Map<String, Object> recordArrayobj = new HashMap<>();
                    recordArrayobj.put("duration", windowTime);
                    recordArrayobj.put("data", recordArray);

                    Map<String, Object> results = new HashMap<>();
                    results.put("type", SteamType.REDDIT_MENTIONS_BATCH);
                    results.put("data", recordArrayobj);
                    System.out.println("results: " + results);
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


        ArrayList<Map<String, Object>> subreddit_proportion = new ArrayList<Map<String, Object>>();

        posts_subreddit
                .mapToPair(subreddit -> new Tuple2<>(subreddit, 1))
                .reduceByKey((a, b) -> Integer.sum(a, b))
                .mapToPair(tuple -> new Tuple2<String, Integer>(String.valueOf("SR's with " + tuple._2 + (tuple._2 == 1 ? " Post" : "Posts")), 1))
                .reduceByKey((a, b) -> Integer.sum(a, b))
                .mapToPair(stringIntegerTuple2 -> stringIntegerTuple2.swap())
                .foreachRDD(rrdd -> {
                    subreddit_proportion.clear();
                    System.out.println("---------------------------------------------------------------");
                    List<Tuple2<Integer, String>> sorted;
                    JavaPairRDD<Integer, String> counts = rrdd.sortByKey(false);

                    sorted = counts.collect();
                    sorted.forEach( record -> {
                        Map<String, Object> obj =  new HashMap<>();
                        obj.put("subreddit", record._2);
                        obj.put("count", record._1);

                        subreddit_proportion.add(obj);
                        System.out.println(String.format("subreddit -> %s (%d posts)", record._2, record._1));
                    });

                    Map<String, Object> recordArrayobj = new HashMap<>();
                    recordArrayobj.put("duration", windowTime);
                    recordArrayobj.put("data", subreddit_proportion);

                    Map<String, Object> results = new HashMap<>();
                    results.put("type", SteamType.REDDIT_POSTS_PROPORTION);
                    results.put("data", recordArrayobj);
                    System.out.println("results: " + results);
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
    }

    private void startWordCountStream(JavaInputDStream<ConsumerRecord<String, Submission>> input){
        int windowTime = 30;
        int slideTime = 30;
        int minWordLength = 4;
        int maxWordLength = 31;
        Long minWordCount = 1L;

        // Clean up submissions
        JavaDStream<ConsumerRecord<String, Submission>> clean_submissions = input.filter((submission) ->{
            return !submission.value().isNsfw();
        });

        JavaDStream<String> posts_selfText = clean_submissions
                .map((submission) -> { return submission.value().getSelfText(); })
                .window(Durations.seconds(windowTime),Durations.seconds(slideTime));

        ArrayList<Map<String, Object>> words_count = new ArrayList<Map<String, Object>>();

        posts_selfText
                .flatMap(body -> Arrays.asList(body.toLowerCase().trim().split("[ :\\t)@.,]")).iterator())
                .mapToPair(s -> new Tuple2<>(s, 1))
                .filter(tuple -> tuple._1.length() > minWordLength && tuple._1.length() < maxWordLength)
                .reduceByKey((i1, i2) -> i1 + i2)
                .filter(tuple -> tuple._2 > 1)
                .mapToPair(stringIntegerTuple2 -> stringIntegerTuple2.swap())
                .foreachRDD(rrdd -> {
                    words_count.clear();
                    System.out.println("---------------------------------------------------------------");
                    List<Tuple2<Integer, String>> sorted;
                    JavaPairRDD<Integer, String> counts = rrdd.sortByKey(false);

                    sorted = counts.collect();
                    sorted.forEach( record -> {
                        Map<String, Object> obj =  new HashMap<>();
                        obj.put("word", record._2);
                        obj.put("count", record._1);

                        words_count.add(obj);
                        System.out.println(String.format("word -> %s (%d)", record._2, record._1));
                    });

                    Map<String, Object> recordArrayobj = new HashMap<>();
                    recordArrayobj.put("duration", windowTime);
                    recordArrayobj.put("data", words_count);

                    Map<String, Object> results = new HashMap<>();
                    results.put("type", SteamType.WORD_COUNT_BATCH);
                    results.put("data", recordArrayobj);
                    System.out.println("results: " + results);
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
                /*.flatMap(body -> {
                    List<String> values = Arrays.asList(body.toLowerCase().trim().split("[ :\\t)@.,]"));
                    return values.stream().map(value -> new Tuple2(value.trim(), 1L)).collect(Collectors.toList());
                })*/

    }

    public void run(){
        log.debug("Running Spark Consumer Service..");

        // Create context with a 10 seconds batch interval
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(batchInterval));

        // Create direct kafka stream with brokers and topics
        JavaInputDStream<ConsumerRecord<String, Submission>> submissions = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaConsumerConfig.consumerConfigs()));

        this.startPostsPerMinuteStream(submissions);

        this.startSpeedStream(submissions);

        this.startSubredditMentionsBatchnSubredditProportionStream(submissions);

        this.startWordCountStream(submissions);

        // Start the computation
        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
