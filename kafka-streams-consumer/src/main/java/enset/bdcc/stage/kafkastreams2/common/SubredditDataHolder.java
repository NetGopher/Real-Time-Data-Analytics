package enset.bdcc.stage.kafkastreams2.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import enset.bdcc.stage.kafkastreams2.config.StreamType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubredditDataHolder {
    @JsonProperty("data")
    private List<SubredditData> subredditMentions = new ArrayList<>();
    private Double duration = 0d;
}
