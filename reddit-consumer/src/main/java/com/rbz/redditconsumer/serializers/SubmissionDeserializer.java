package com.rbz.redditconsumer.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dean.jraw.models.Submission;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class SubmissionDeserializer implements Deserializer {
    @Override
    public void configure(Map configs, boolean isKey) {

    }

    @Override
    public Submission deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        Submission submission = null;
        try{
            submission = mapper.readValue(bytes, Submission.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return submission;
    }

    @Override
    public void close() {

    }
}
