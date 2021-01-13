package enset.bdcc.stage.springkafkareactivebackend;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Service
@Component
@AllArgsConstructor
public class KafkaServiceImpl implements KafkaService {
    @Value("${spring.kafka.topic}")
    private String TOPIC;
    @Value("${spring.kafka.consumer.group-id}")
    private String GROUP_ID;
    @Value("${spring.kafka.consumer.client-id}")
    private String CLIENT_ID;
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String AUTO_OFFSET_REST;
    @Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVER;
    private Flux<ReceiverRecord<String, String>> testTopicStream;

    KafkaServiceImpl() throws IOException {
        System.out.println("----------------------------");
        System.out.println("----------------------------");
        System.out.println("----------------------------");
        System.out.println(TOPIC + GROUP_ID + CLIENT_ID);
        System.out.println("----------------------------");
        System.out.println("----------------------------");
        System.out.println("----------------------------");
        System.out.println("----------------------------");
//        Properties kafkaProperties = PropertiesLoaderUtils.loadAllProperties("application.properties");
        Properties kafkaProperties = new Properties();
//        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
//        kafkaProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
//        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
//        kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_REST);
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        kafkaProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "dick1");
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "fat1");
        kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(kafkaProperties);

        testTopicStream = createTopicCache(receiverOptions, "reddit-json");
    }


    public Flux<ReceiverRecord<String, String>> getTestTopicFlux() {

        return testTopicStream;
    }

    private <T, G> Flux<ReceiverRecord<T, G>> createTopicCache(ReceiverOptions<T, G> receiverOptions, String topicName) {
        ReceiverOptions<T, G> options = receiverOptions.subscription(Collections.singleton(topicName)).commitInterval(Duration.ofSeconds(10));

        return KafkaReceiver.create(options).receive().cache(Duration.ofSeconds(5));
    }
}
