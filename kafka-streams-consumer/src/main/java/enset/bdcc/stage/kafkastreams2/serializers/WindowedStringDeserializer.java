package enset.bdcc.stage.kafkastreams2.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.streams.kstream.TimeWindowedDeserializer;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.WindowedSerializer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class WindowedStringDeserializer<T> extends TimeWindowedDeserializer<T> {
    @Autowired
    private ObjectMapper objectMapper;

    private Class<T> tClass;

    public WindowedStringDeserializer(Class<T> tClass) {
        this.tClass = tClass;
    }

}
