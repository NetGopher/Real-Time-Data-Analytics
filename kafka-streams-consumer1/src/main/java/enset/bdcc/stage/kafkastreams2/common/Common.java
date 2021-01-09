package enset.bdcc.stage.kafkastreams2.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import enset.bdcc.stage.kafkastreams2.config.StreamType;

import java.util.HashMap;
import java.util.Map;

public class Common {
    private static JsonMapper jsonMapper = new JsonMapper();
    public static long WINDOW_SIZE = 6;
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
}
