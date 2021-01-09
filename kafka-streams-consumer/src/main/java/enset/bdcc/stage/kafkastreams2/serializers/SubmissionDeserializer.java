package enset.bdcc.stage.kafkastreams2.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.moshi.JsonAdapter;
import net.dean.jraw.JrawUtils;
import net.dean.jraw.models.Submission;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
@Component
public class SubmissionDeserializer implements Deserializer<Submission> {
    @Override
    public void configure(Map configs, boolean isKey) {

    }

    @Override
    public Submission deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = null;
        Submission data = null;
        JsonAdapter<Submission> submissionAdapter = JrawUtils.moshi.adapter(Submission.class).serializeNulls();
        try{
            JSONObject jsonObject = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
            jsonString = jsonObject.toString();
            data = submissionAdapter.fromJson(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public void close() {

    }
}
