package enset.bdcc.stage.kafkastreams2.serializers;

import enset.bdcc.stage.kafkastreams2.common.DataMapList;
import enset.bdcc.stage.kafkastreams2.common.SubredditData;
import enset.bdcc.stage.kafkastreams2.common.SubredditDataHolder;
import net.dean.jraw.models.Submission;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public final class CustomSerdes {

    static public final class SubmissionSerde
            extends Serdes.WrapperSerde<Submission> {
        public SubmissionSerde() {
            super(new SubmissionSerializer(),
                    new SubmissionDeserializer());
        }
    }
    public static final class DataMapListSerde extends Serdes.WrapperSerde<DataMapList>{

        public DataMapListSerde() {
            super(new JsonSerializer<DataMapList>(), new JsonDeserializer<>(DataMapList.class));
        }
    }
    public static final class SubredditDataSerde extends Serdes.WrapperSerde<SubredditData>{

        public SubredditDataSerde() {
            super(new JsonSerializer<SubredditData>(), new JsonDeserializer<>(SubredditData.class));
        }
    }
     public static final class SubredditDataHolderSerde extends Serdes.WrapperSerde<SubredditDataHolder>{

        public SubredditDataHolderSerde() {
            super(new JsonSerializer<SubredditDataHolder>(), new JsonDeserializer<>(SubredditDataHolder.class));
        }
    }


    public static Serde<Submission> SubmissionSerde() {
        return new CustomSerdes.SubmissionSerde();
    }
    public static Serde<DataMapList> DataMapListSerde() {
        return new CustomSerdes.DataMapListSerde();
    }
    public static Serde<SubredditData> SubredditDataSerde() {
        return new CustomSerdes.SubredditDataSerde();
    }
    public static Serde<SubredditDataHolder> SubredditDataHolderSerde() {
        return new CustomSerdes.SubredditDataHolderSerde();
    }

}