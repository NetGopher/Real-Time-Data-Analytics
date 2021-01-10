package enset.bdcc.stage.kafkastreams2.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import enset.bdcc.stage.kafkastreams2.config.StreamType;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Stream;

public class Common {
    private static JsonMapper jsonMapper = new JsonMapper();
    public static long WINDOW_SIZE = 3L;
    public static String maptoJsonString(Object object) {
        String jsonString = null;
        try {
            jsonString = jsonMapper.writeValueAsString(object);
            return jsonString;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> resultMap = new HashMap<>();

            resultMap.put("error", e.getMessage());
            return maptoJsonString(addDataToStreamMap(StreamType.ERROR, resultMap));
        }
    }

    public static Map<String, Object> addDataToStreamMap(String type, Object data) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", type);
        dataMap.put("data", data);
        return dataMap;


    }
//    public static Stream<String,String> convertStreamToJson(KStream<String,String> kStream, Class tClass){
//            return kStream.map((s, aLong) -> {
//                       return KeyValue.pair("__data__", aLong);
//                    })
//                .groupByKey(Grouped.with(Serdes.String(),Serdes.String()))
//                .aggregate(new Initializer<String>() {
//                    @SneakyThrows
//                    @Override
//                    public String apply() {
//                        List<SubredditData> subredditDataList = new ArrayList<>();
//                        String jsonResultString = jsonMapper.writeValueAsString(subredditDataList);
//                        return jsonResultString;
//                    }
//                }, new Aggregator<String, String, String>() {
//                    @SneakyThrows
//                    @Override
//                    public String apply(String s, String newSubredditDataString, String reduceList) {
//                        Object newSubredditData = jsonMapper.readValue(newSubredditDataString, tClass);
//
//                        String valuesString = null;
//                        try {
//                            SubredditData[] values = jsonMapper.readValue(reduceList, tClass);
//                            List<SubredditData> dataList = new ArrayList<>(Arrays.asList(values));
//                            dataList.add(newSubredditData);
//                            valuesString = jsonMapper.writeValueAsString(dataList.toArray());
//                        } catch (JsonProcessingException e) {
//                            e.printStackTrace();
//
//                        }
//                        return valuesString;
//                    }
//                }, Materialized.with(Serdes.String(), Serdes.String()))
//                .toStream
//                .map(new KeyValueMapper<String, String, KeyValue<String, String>>() {
//                    @SneakyThrows
//                    @Override
//                    public KeyValue<String, String> apply(String key, String value) {
//
//                        List<SubredditData> subredditDataList = Arrays.asList(jsonMapper.readValue(value, SubredditData[].class));
//                        SubredditDataHolder subredditDataHolder = new SubredditDataHolder(subredditDataList, Double.valueOf(String.valueOf(Common.WINDOW_SIZE)));
//                        Map<String, Object> resultMap = Common.addDataToStreamMap(StreamType.REDDIT_MENTIONS_BATCH, subredditDataHolder);
//                        String jsonString = Common.maptoJsonString(resultMap);
//                        System.out.println(jsonString);
//                        return KeyValue.pair("Result_Batch", jsonString);
//                    }
//                });
//
//    }
}
