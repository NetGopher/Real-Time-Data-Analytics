package enset.bdcc.stage.kafkastreams2.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WordData {
    private String word;
    private Long count;
}
