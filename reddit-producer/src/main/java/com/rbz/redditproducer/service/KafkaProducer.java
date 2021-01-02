package com.rbz.redditproducer.service;

import net.dean.jraw.models.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaProducer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private KafkaTemplate<String, Submission> kafkaTemplate;

    public void send(String topic, Submission payload) {
        log.info("Sending payload='{}' to topic='{}'", payload, topic);
        kafkaTemplate.send(topic, payload);
    }
}
