package enset.bdcc.stage.kafkastreams2.common;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DataMapList {
    private List<Map<String,Object>> mapList = new ArrayList<>();
}



