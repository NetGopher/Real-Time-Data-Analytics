package enset.bdcc.stage.kafkastreams2.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.moshi.JsonAdapter;
import net.dean.jraw.JrawUtils;
import net.dean.jraw.models.Submission;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
@Component
public class SubmissionSerializer implements Serializer<Submission> {
    @Override
    public void configure(Map configs, boolean isKey) {

    }

//    @Override
//    public byte[] serialize(String arg0, Object arg1){
//        byte[] retVal = null;
//        ObjectMapper objectMapper = new ObjectMapper();
//        /*Moshi moshi = new Moshi.Builder().build();
//        JsonAdapter<Submission> jsonAdapter = moshi.adapter(Submission.class);*/
//        JsonAdapter<Submission> jsonAdapter = JrawUtils.moshi.adapter(Submission.class).serializeNulls();
//        //JsonAdapter<Submission> jsonAdapter = Submission.jsonAdapter(moshi);
//        String json = jsonAdapter.toJson((Submission) arg1);
//        try{
//            retVal = json.getBytes(StandardCharsets.UTF_8);
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//        return retVal;
//    }

    @Override
    public byte[] serialize(String topic, Submission data) {
        return new byte[0];
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Submission data) {
        return new byte[0];
    }

    @Override
    public void close() {

    }
}
