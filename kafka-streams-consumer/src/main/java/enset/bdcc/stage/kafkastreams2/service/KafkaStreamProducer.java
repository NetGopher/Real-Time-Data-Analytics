package enset.bdcc.stage.kafkastreams2.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KafkaStreamProducer {

    public void start() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "stream-prod-1");
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);
        List<Character> characters = new ArrayList<>();
        for (char c = 'A'; c < 'Z'; c++) {
            characters.add(c);
        }
        Random random = new Random();
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            String message = "";
            for (int i = 0; i < 10; i++) {
                message += " " + characters.get(random.nextInt(characters.size()));
            }
            String finalMessage = message;
            kafkaProducer.send(new ProducerRecord<String, String>("numbers", null, message), (recordMetadata, e) -> {
                System.out.println("Message sent?" + finalMessage + ": partition: " + recordMetadata.partition());

            });
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

}
