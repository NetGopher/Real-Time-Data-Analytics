package com.rbz.sparkconsumer1;

import com.rbz.sparkconsumer1.service.SparkConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Sparkconsumer1Application implements CommandLineRunner {

    @Autowired
    private SparkConsumerService sparkConsumerService;

    public static void main(String[] args) {
        SpringApplication.run(Sparkconsumer1Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        sparkConsumerService.run();
    }
}
