package enset.bdcc.stage.kafkastreams2.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import enset.bdcc.stage.kafkastreams2.common.SubredditData;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Arrays;
import java.util.Map;

public class JsonArrayDeserializer implements Deserializer<SubredditData[]> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public SubredditData[] deserialize(String topic, byte[] bytes) {
        if (bytes == null)
            return null;

        SubredditData[] data;
        try {
            data = objectMapper.readValue(bytes,SubredditData[].class);
        } catch (Exception e) {
            throw new SerializationException(e);
        }

            return data;
    }

    @Override
    public SubredditData[] deserialize(String topic, Headers headers, byte[] data) {
        return null;
    }

    @Override
    public void close() {

    }
}
