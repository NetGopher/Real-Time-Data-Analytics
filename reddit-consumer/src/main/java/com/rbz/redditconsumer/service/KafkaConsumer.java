package com.rbz.redditconsumer.service;

import net.dean.jraw.models.Submission;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @KafkaListener(topics={"${spring.kafka.topic}"}, groupId = "${spring.kafka.group-id}")
    public void onMessage(Submission submission){
        System.out.println("Received: => " + submission.toString());
    }
}
