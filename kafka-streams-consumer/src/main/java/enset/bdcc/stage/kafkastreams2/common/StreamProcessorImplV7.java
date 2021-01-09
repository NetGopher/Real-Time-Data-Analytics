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
import java.util.concurrent.TimeUnit;

//@Component
//TODO:  fix ClassCast Excpetion Error
@Data
@NoArgsConstructor
@AllArgsConstructor

@Component
// PROBLEM: Heavy Data
public class StreamProcessorImplV7 implements StreamProcessor {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public KStream<String, String> getSubredditMensionsStream(KStream<String, Submission> initialStream) {

        KStream<String,Long> count_stream=
                initialStream.map((KeyValueMapper<String, Submission, KeyValue<String, Long>>) (k, v) -> KeyValue.pair(v.getSubreddit(),1L)
                )
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)))
                .count()
                        .toStream()
                .map((windowed, aLong) -> KeyValue.pair(windowed.key(),aLong));
               KStream<String,Long> count_bigger_than_one = count_stream
                       .filter((key, countValue) -> countValue > 1)
//                       .peek((key, countValue) -> System.out.println("count_bigger_than_one: ->" + key + ", count ->" + countValue))
                       ;
               KStream<String,Long> count_equals_one = count_stream
                       .filter((key, countValue) -> countValue == 1)
                       .map((windowed, aLong) -> KeyValue.pair("__Others__",1L))
                       .groupByKey(Grouped.with(Serdes.String(),Serdes.Long()))
                       .count()
                       .toStream()
//                       .peek((key, countValue) -> System.out.println("count_equals_one ->" + key + ", count ->" + countValue))
                       ;

              return count_bigger_than_one.merge(count_equals_one)
                    .map(new KeyValueMapper<String, Long, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(String key, Long value) {
                        SubredditData subredditData = new SubredditData();
                        subredditData.setSubreddit(key);
                        subredditData.setCount(value);
                        return KeyValue.pair("group", objectMapper.writeValueAsString(subredditData));
                    }
                }).groupByKey(Grouped.with(Serdes.String(),Serdes.String()))
                .aggregate(new Initializer<String>() {
                    @SneakyThrows
                    @Override
                    public String apply() {
                        List<SubredditData> subredditDataList = new ArrayList<>();
                        JsonMapper jsonMapper = new JsonMapper();
                        String jsonResultString = jsonMapper.writeValueAsString(subredditDataList);
//                        System.out.println("RESULT------>" + jsonResultString);
                        return jsonResultString;
                    }
                }, new Aggregator<String, String, String>() {
                    @SneakyThrows
                    @Override
                    public String apply(String s, String newSubredditDataString, String reduceList) {
                        JsonMapper jsonMapper = new JsonMapper();
                        SubredditData newSubredditData = objectMapper.readValue(newSubredditDataString, SubredditData.class);
//                        System.out.println("REDUCE_RESULT---->" + reduceList);
                        String valuesString = null;
                        try {
                            SubredditData[] values = jsonMapper.readValue(reduceList, SubredditData[].class);
                            List<SubredditData> dataList = new ArrayList<>(Arrays.asList(values));
                            dataList.add(newSubredditData);
//                            for (SubredditData v : dataList)
//                                System.out.println(v);
//                            System.out.println("LENGTH of THAT SHIT--->" + dataList.size());
                            valuesString = jsonMapper.writeValueAsString(dataList.toArray());
//                            System.out.println("VALUES STRING ----->" + valuesString);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();

                        }
                        return valuesString;
                    }
                }, Materialized.with(Serdes.String(), Serdes.String()))
                .toStream()
                      .peek((s, s2) -> {

                          System.out.println("------------------------------------------");
                          System.out.println("------------------------------------------");
                          System.out.println(s + " --> " + s2);
                      });
//                .map(new KeyValueMapper<String, String, KeyValue<String, String>>() {
//                    @SneakyThrows
//                    @Override
//                    public KeyValue<String, String> apply(String key, String value) {
//
//                        List<SubredditData> subredditDataList = Arrays.asList(objectMapper.readValue(value, SubredditData[].class));
//                        SubredditDataHolder subredditDataHolder = new SubredditDataHolder(subredditDataList, Double.valueOf(String.valueOf(Common.WINDOW_SIZE)));
//                        Map<String, Object> resultMap = Common.addDataToStreamMap(StreamType.REDDIT_MENTIONS_BATCH, subredditDataHolder);
//                        String jsonString = Common.maptoJsonString(resultMap);
//                        return KeyValue.pair("Result_Batch", jsonString);
//                    }
//                });


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
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)))
                .reduce(new Reducer<Long>() {
                    @Override
                    public Long apply(Long aLong, Long v1) {
                        return aLong + v1;
                    }
                }).toStream()
//                .peek((stringWindowed, aLong) -> System.out.println("COUNT-> key: " + stringWindowed.key() + "value: " + aLong))
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
