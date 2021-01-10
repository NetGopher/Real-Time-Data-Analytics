package enset.bdcc.stage.kafkastreams2.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.WindowedSerializer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class WindowedStringSerializer<T> implements WindowedSerializer<T> {
    @Autowired
    private ObjectMapper objectMapper;
    private Serializer<T> serializer;
    public WindowedStringSerializer(Serializer serializer) {
            this.serializer = serializer;
    }
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, Windowed<T> data) {
        if (data == null)
            return null;
        try {
//            return objectMapper.writeValueAsBytes(data);
            return serializer.serialize(topic,data.key());
        } catch (Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }

    }

    @Override
    public byte[] serialize(String topic, Headers headers, Windowed<T> data) {
        return new byte[0];
    }

    @Override
    public void close() {

    }

    @Override
    public byte[] serializeBaseKey(String s, Windowed<T> windowed) {
        return new byte[0];
    }
}
