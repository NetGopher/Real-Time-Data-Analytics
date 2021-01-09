package enset.bdcc.stage.kafkastreams2.common;

import net.dean.jraw.models.Submission;
import org.apache.kafka.streams.kstream.KStream;

public interface StreamProcessor {
    public KStream<String, String> getSubredditMensionsStream(KStream<String, Submission> initialStream);
    public KStream<String, String> calculateStreamCount(KStream<String, Submission> intialStream);

}
