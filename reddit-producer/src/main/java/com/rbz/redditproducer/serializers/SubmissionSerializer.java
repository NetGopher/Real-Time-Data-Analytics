package com.rbz.redditproducer.serializers;

import com.squareup.moshi.JsonAdapter;
import net.dean.jraw.JrawUtils;
import net.dean.jraw.models.Submission;
import org.apache.kafka.common.serialization.Serializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SubmissionSerializer implements Serializer {
    @Override
    public void configure(Map configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String arg0, Object arg1){
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        /*Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Submission> jsonAdapter = moshi.adapter(Submission.class);*/
        JsonAdapter<Submission> jsonAdapter = JrawUtils.moshi.adapter(Submission.class).serializeNulls();
        //JsonAdapter<Submission> jsonAdapter = Submission.jsonAdapter(moshi);
        String json = jsonAdapter.toJson((Submission) arg1);
        try{
            retVal = json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e){
            e.printStackTrace();
        }

        return retVal;
    }

    @Override
    public void close() {

    }
}
