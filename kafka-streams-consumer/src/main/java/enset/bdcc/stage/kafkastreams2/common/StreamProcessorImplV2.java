package enset.bdcc.stage.kafkastreams2.common;

import com.fasterxml.jackson.databind.ser.std.ArraySerializerBase;
import enset.bdcc.stage.kafkastreams2.config.StreamType;
import enset.bdcc.stage.kafkastreams2.serializers.CustomSerdes;
import net.dean.jraw.models.Submission;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//@Component
//TODO:  fix ClassCast Excpetion Error

public class StreamProcessorImplV2 implements StreamProcessor {
    @Override
    public KStream<String, String> getSubredditMensionsStream(KStream<String, Submission> initialStream) {
        KStream<String, Long> submissions_stream = initialStream.map(new KeyValueMapper<String, Submission, KeyValue<String, Long>>() {
                                                                         @Override
                                                                         public KeyValue<String, Long> apply(String k, Submission v) {
                                                                             if (v == null) {
                                                                                 return new KeyValue<>(k.toUpperCase(), 1L);
                                                                             }
                                                                             if (k == null) {
                                                                                 return new KeyValue<>(v.getSubreddit(), 1L);
                                                                             }
                                                                             return new KeyValue<>(v.getSubreddit(), 1L);
                                                                         }
                                                                     }
        );

        KStream<Windowed<String>, Long> subreddit_mentions = submissions_stream.groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)))
                .reduce(new Reducer<Long>() {
                    @Override
                    public Long apply(Long aLong, Long v1) {
                        return aLong + v1;
                    }
                }).toStream();

        KStream<String, String> resultStream = subreddit_mentions.map(new KeyValueMapper<Windowed<String>, Long, KeyValue<String, Map<String,Object>>>() {
            @Override
            public KeyValue<String, Map<String,Object>> apply(Windowed<String> key, Long value) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("subreddit", key.key());
                resultMap.put("count", value);
                return new KeyValue<>("group", resultMap);
            }
        }).groupBy(new KeyValueMapper<String, Map<String, Object>, String>() {

            @Override
            public String apply(String s, Map<String, Object> stringObjectMap) {
                return "";
            }
        })
//                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)))
                .aggregate(new Initializer<DataMapList>() {
                    @Override
                    public DataMapList apply() {
                        return new DataMapList();
                    }
                }, new Aggregator<String, Map<String, Object>, DataMapList>() {
                    @Override
                    public DataMapList apply(String s, Map<String, Object> stringObjectMap, DataMapList reduceList) {
                        reduceList.getMapList().add(stringObjectMap);
                        return reduceList;
                    }
                }, Materialized.with(Serdes.String(), CustomSerdes.DataMapListSerde()))
                .toStream()
                .map(new KeyValueMapper<String, DataMapList, KeyValue<String, String>>() {
                    @Override
                    public KeyValue<String, String> apply(String stringWindowed, DataMapList dataMapList) {

                        String jsonString = Common.maptoJsonString(Common.addDataToStreamMap(StreamType.REDDIT_MENTIONS, dataMapList.getMapList()));


                        return new KeyValue<>("",jsonString);
                    }
                })
//                                .map(new KeyValueMapper<Windowed<String>, DataMapList, KeyValue<String, String>>() {
//                    @Override
//                    public KeyValue<String, String> apply(Windowed<String> stringWindowed, DataMapList dataMapList) {
//
//                        String jsonString = Common.maptoJsonString(Common.addDataToStreamMap(StreamType.REDDIT_MENTIONS, dataMapList.getMapList()));
//
//
//                        return new KeyValue<>("",jsonString);
//                    }
//                })

                .peek((key, value) -> {
                    System.out.println("key" + key + ", value: " + value);
                });
        return resultStream;

    }

    @Override
    public KStream<String, String> calculateStreamCount(KStream<String, Submission> intialStream) {
        return intialStream.map(new KeyValueMapper<String, Submission, KeyValue<String, Long>>() {
                                    @Override
                                    public KeyValue<String, Long> apply(String k, Submission v) {
                                        return new KeyValue<>("count", 1L);
                                    }
                                }
        ).groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)))
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
                        return new KeyValue<>("stream", jsonString);
                    }
                });

    }
}
