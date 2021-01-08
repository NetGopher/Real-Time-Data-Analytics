package com.rbz.sparkconsumer.config;

import com.rbz.sparkconsumer.serializers.SubmissionDeserializer;
import net.dean.jraw.models.Submission;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    // get bootstrap servers host:port pairs from application.properties
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value(value = "${spring.kafka.consumer.group-id}")
    private String groupID;

    @Value(value = "${spring.kafka.consumer.client-id}")
    private String clientID;

    //Consumer Configuration
    @Bean
    public Map<String, Object> consumerConfigs(){
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SubmissionDeserializer.class);

        // automatically reset the offset to the earliest offset
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientID);

        return props;
    }

    @Bean
    public ConsumerFactory<String, Submission> consumerFactory(){
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Submission> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, Submission> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
