package enset.bdcc.stage.kafkastreams2.common;

import net.dean.jraw.models.Submission;
import org.apache.kafka.streams.kstream.KStream;

public interface StreamProcessor {
    public KStream<String, String> getSubredditMensionsStream(KStream<String, Submission> initialStream);
    public KStream<String, String> calculateStreamCount(KStream<String, Submission> intialStream);
    public KStream<String, String> getSubredditPostsProportion(KStream<String, Submission> intialStream);
    public KStream<String, String> getWordCount(KStream<String, Submission> intialStream);
    public KStream<String, String> getPostsPerDuration(KStream<String, Submission> intialStream);
    public KStream<String, String> getNsfwProportion(KStream<String, Submission> intialStream);
    public KStream<String, String> getActiveUsersInActiveCommunitiesByPosts(KStream<String, Submission> intialStream);

}
