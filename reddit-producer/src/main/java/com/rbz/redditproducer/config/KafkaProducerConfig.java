package com.rbz.redditproducer.config;

import com.rbz.redditproducer.serializers.SubmissionSerializer;
import com.rbz.redditproducer.service.KafkaProducer;
import net.dean.jraw.models.Submission;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    // get bootstrap servers host:port pairs from application.properties
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    //Producer Configuration
    @Bean
    public Map<String, Object> producerConfigs(){
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SubmissionSerializer.class);

        return props;
    }

    @Bean
    public ProducerFactory<String, Submission> producerFactory(){
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, Submission> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaProducer kafkaProducer(){
        return new KafkaProducer();
    }
}
