package com.rbz.redditproducer.service;

import com.fasterxml.jackson.databind.JsonNode;
import net.dean.jraw.models.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;


public class KafkaProducer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private KafkaTemplate<String, Submission> kafkaTemplate;

    public void send(String topic, Submission submission) {
        log.info(submission.getTitle() + " by " + submission.getAuthor() + " body: " +  submission.getBody());
        log.info("Sending submission='{}' to topic='{}'", submission.toString() , topic);
        kafkaTemplate.send(topic, submission);
    }
}
