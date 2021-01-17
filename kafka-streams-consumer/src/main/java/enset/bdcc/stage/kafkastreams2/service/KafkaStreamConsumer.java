package enset.bdcc.stage.kafkastreams2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import enset.bdcc.stage.kafkastreams2.common.Common;
import enset.bdcc.stage.kafkastreams2.common.StreamProcessor;
import enset.bdcc.stage.kafkastreams2.config.KafkaStreamConfig;
import enset.bdcc.stage.kafkastreams2.config.StreamType;
import enset.bdcc.stage.kafkastreams2.serializers.CustomSerdes;
import lombok.NoArgsConstructor;
import net.dean.jraw.models.Submission;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@NoArgsConstructor
public class KafkaStreamConsumer {
    @Autowired
    private KafkaStreamConfig kafkaStreamConfig;
    @Autowired
    private StreamProcessor streamProcessor;

    public void start() {

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, Submission> initialStream = streamsBuilder.stream("submissions", Consumed.with(Serdes.String(), CustomSerdes.SubmissionSerde()));
        streamProcessor.getActiveUsersInActiveCommunitiesByPosts(initialStream)
                .merge(streamProcessor.getWordCount(initialStream))
                .merge(streamProcessor.getSubredditMensionsStream(initialStream))
                .merge(streamProcessor.calculateStreamCount(initialStream))
                .merge(streamProcessor.getSubredditPostsProportion(initialStream))
                .merge(streamProcessor.getNsfwProportion(initialStream))
                .to("reddit-json", Produced.with(Serdes.String(), Serdes.String()));

        Topology topology = streamsBuilder.build();
        KafkaStreams kafkaStreams = new KafkaStreams(topology, kafkaStreamConfig.getKafkaStreamProperties());
        kafkaStreams.start();
    }

}
