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
import org.apache.kafka.streams.kstream.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

//@Component
//TODO:  fix ClassCast Excpetion Error
@Data
@NoArgsConstructor
@AllArgsConstructor
class SubredditMention {
    private String subreddit;
    private long count = 0;
}
class SubredditMentionData{
    List<SubredditMention> subredditMentions = new ArrayList<>();
}
//@Component
public class StreamProcessorImplV3 implements StreamProcessor {
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

        KStream<String, String> resultStream = subreddit_mentions.map(new KeyValueMapper<Windowed<String>, Long, KeyValue<String, SubredditData>>() {
            @Override
            public KeyValue<String, SubredditData> apply(Windowed<String> key, Long value) {
//                Map<String, Object> resultMap = new HashMap<>();
//                resultMap.put("subreddit", key.key());
//                resultMap.put("count", value);
                SubredditData subredditData = new SubredditData();
                subredditData.setSubreddit(key.key());
                subredditData.setCount(value);
                return new KeyValue<>("group", subredditData);
            }
        }).
                groupBy(new KeyValueMapper<String, SubredditData, String>() {

            @Override
            public String apply(String s, SubredditData stringObjectMap) {
                return "MYDATA";
            }
        })
                .aggregate(new Initializer<String>() {
                    @SneakyThrows
                    @Override
                    public String apply() {
                        SubredditData subredditData = new SubredditData();
                        subredditData.setSubreddit("test");
                        subredditData.setCount(5L);
                        List<SubredditData> subredditDataList = new ArrayList<>();
                        subredditDataList.add(subredditData);
                        JsonMapper jsonMapper = new JsonMapper();
                        String jsonResultString =  jsonMapper.writeValueAsString(subredditDataList);
                        System.out.println("RESULT------>" + jsonResultString);
                        System.out.println("RESULT------>" + jsonResultString);
                        System.out.println("RESULT------>" + jsonResultString);
                        System.out.println("RESULT------>" + jsonResultString);
                        System.out.println("RESULT------>" + jsonResultString);
                        System.out.println("RESULT------>" + jsonResultString);
                        System.out.println("RESULT------>" + jsonResultString);
                        System.out.println("RESULT------>" + jsonResultString);
                        System.out.println("RESULT------>" + jsonResultString);
                        return  jsonResultString;
                    }
                }, new Aggregator<String, SubredditData, String>() {
                    @SneakyThrows
                    @Override
                    public String apply(String s, SubredditData newSubredditData, String reduceList) {
                        JsonMapper jsonMapper = new JsonMapper();
                        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                        System.out.println("REDUCE_RESULT---->" + reduceList);
                        System.out.println("REDUCE_RESULT---->" + reduceList);
                        System.out.println("REDUCE_RESULT---->" + reduceList);
                        System.out.println("REDUCE_RESULT---->" + reduceList);
                        System.out.println("REDUCE_RESULT---->" + reduceList);
                        String valuesString = null;
                        try {
                            SubredditData[] values = jsonMapper.readValue(reduceList, SubredditData[].class);
                            List<SubredditData> dataList = new ArrayList<>(Arrays.asList(values));
                            dataList.add(newSubredditData);
                             for (SubredditData v: dataList)
                                  {
                                      System.out.println(v);
                            }
                            valuesString = jsonMapper.writeValueAsString(dataList.toArray());
                            System.out.printf("VALUES STRING ----->" + valuesString);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();

                        }
                        return valuesString;
                    }
                }, Materialized.with(Serdes.String(),Serdes.String()))
                .toStream()
//                .map(new KeyValueMapper<String, String, KeyValue<String, String>>() {
//                    @Override
//                    public KeyValue<String, String> apply(String stringWindowed, String dataMapList) {
//
//                        String jsonString = Common.MaptoJsonString(Common.addDataToStreamMap(StreamType.REDDIT_MENTIONS, dataMapList.getMapList()));
//
//
//                        return new KeyValue<>("", jsonString);
//                    }
//                })
//                                .map(new KeyValueMapper<Windowed<String>, String, KeyValue<String, String>>() {
//                    @Override
//                    public KeyValue<String, String> apply(Windowed<String> stringWindowed, String dataMapList) {
//
//                        String jsonString = Common.MaptoJsonString(Common.addDataToStreamMap(StreamType.REDDIT_MENTIONS, dataMapList.getMapList()));
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

    @Override
    public KStream<String, String> getSubredditPostsProportion(KStream<String, Submission> intialStream) {
        return null;
    }
}
