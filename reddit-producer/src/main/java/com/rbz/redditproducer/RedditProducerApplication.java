package com.rbz.redditproducer;

import com.rbz.redditproducer.service.SubmissionStreamEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedditProducerApplication implements CommandLineRunner {

    @Autowired
    SubmissionStreamEventService submissionStreamEventService;

    public static void main(String[] args) {
        SpringApplication.run(RedditProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        submissionStreamEventService.run();
    }
}
