package com.rbz.redditproducer.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.dean.jraw.models.Submission;
import org.apache.kafka.common.serialization.Serializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class SubmissionSerializer implements Serializer {
    @Override
    public void configure(Map configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String arg0, Object arg1){
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            retVal = objectMapper.writeValueAsString(arg1).getBytes();
        } catch (Exception e){
            e.printStackTrace();
        }

        return retVal;
    }

    @Override
    public void close() {

    }
}
