package enset.bdcc.stage.kafkastreams2.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.std.ArraySerializerBase;
import enset.bdcc.stage.kafkastreams2.config.StreamType;
import enset.bdcc.stage.kafkastreams2.serializers.CustomSerdes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.dean.jraw.models.Submission;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

//@Component
//TODO:  fix ClassCast Excpetion Error
@Data
@NoArgsConstructor
@AllArgsConstructor

//@Component
// PROBLEM: Heavy Data

public class StreamProcessorImplV8 implements StreamProcessor {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private StreamsBuilder streamsBuilder;
    @Override
    public KStream<String, String> getSubredditMensionsStream(KStream<String, Submission> initialStream) {
        return null;
//
//        KStream<String, Long> count_stream =
//                initialStream.map((KeyValueMapper<String, Submission, KeyValue<String, Long>>) (k, v) -> KeyValue.pair(v.getSubreddit(), 1L)
//                )
//                        .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
//                        .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)).advanceBy(Duration.ofSeconds(Common.WINDOW_SIZE)))
//                        .count()
//                        .toStream()
//                        .map((windowed, aLong) -> KeyValue.pair(windowed.key(), aLong));
//        count_stream.to("reddit-count", Produced.with(Serdes.String(),Serdes.Long()));
//        return streamsBuilder.stream("reddit-count",Consumed.with(Serdes.String(),Serdes.Long()))
//                .map((key, value) -> {
//                    if (value == 1) return KeyValue.pair("__Others__",1L);
//                    return KeyValue.pair(key,value);
//                })
//                .groupByKey(Grouped.with(Serdes.String(),Serdes.Long()))
//                .reduce(Long::sum)
//                .toStream()
//                .map(new KeyValueMapper<String, Long, KeyValue<String, SubredditData>>() {
//                    @Override
//                    public KeyValue<String, SubredditData> apply(String s, Long aLong) {
//                        return new KeyValue<>("__data__", new SubredditData(s, aLong));
//                    }
//                }).groupByKey()
//                .aggregate(new Initializer<SubredditDataHolder>() {
//                    @Override
//                    public SubredditDataHolder apply() {
//                        return new SubredditDataHolder();
//                    }
//
//
//                }, new Aggregator<String, SubredditData, SubredditDataHolder>() {
//                    @Override
//                    public SubredditDataHolder apply(String s, SubredditData subredditData, SubredditDataHolder subredditDataHolder) {
//                        return subredditDataHolder.add(subredditData);
//                    }
//                },TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)),Materialized.with(Serdes.String(),CustomSerdes.SubmissionSerde()))


    }

    @Override
    public KStream<String, String> calculateStreamCount(KStream<String, Submission> intialStream) {
        return intialStream.map(new KeyValueMapper<String, Submission, KeyValue<String, Long>>() {
                                    @Override
                                    public KeyValue<String, Long> apply(String k, Submission v) {
                                        return KeyValue.pair("count", 1L);
                                    }
                                }
        ).groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)).advanceBy(Duration.ofSeconds(Common.WINDOW_SIZE)))
                .reduce(new Reducer<Long>() {
                    @Override
                    public Long apply(Long aLong, Long v1) {
                        return aLong + v1;
                    }
                }).toStream()
                .peek((stringWindowed, aLong) -> System.out.println("COUNT-> key: " + stringWindowed.key() + "value: " + aLong))
                .map(new KeyValueMapper<Windowed<String>, Long, KeyValue<String, String>>() {
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> key, Long value) {
                        Map<String, String> resultMap = new HashMap<>();
                        resultMap.put("duration", String.valueOf(Common.WINDOW_SIZE));
                        resultMap.put("count", String.valueOf(value));
                        String jsonString = Common.maptoJsonString(Common.addDataToStreamMap(StreamType.COUNT, resultMap));
                        return KeyValue.pair("stream", jsonString);
                    }
                });

    }
}
