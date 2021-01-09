package enset.bdcc.stage.kafkastreams2;

import enset.bdcc.stage.kafkastreams2.service.KafkaStreamConsumer;
import enset.bdcc.stage.kafkastreams2.service.KafkaStreamProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class KafkaStreams2Application {
    @Autowired
    KafkaStreamConsumer kafkaStreamConsumerService;
    public static void main(String[] args) {
        SpringApplication.run(KafkaStreams2Application.class, args);
    }
    @Bean
    CommandLineRunner run(){
        return args -> {
          kafkaStreamConsumerService.start();
        };
    }
}
