package com.rbz.redditproducer.service;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Account;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.Stream;
import net.dean.jraw.references.SubredditReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SubmissionStreamEventService {

    @Value("${spring.reddit.username}") private String username;
    @Value("${spring.reddit.password}") private  String password;
    @Value("${spring.reddit.client-id}") private String clientID;
    @Value("${spring.reddit.client-secret}") private String clientSecret;

    @Value("${spring.reddit.platform}") private String platform;
    @Value("${spring.reddit.appid}") private String appid;
    @Value("${spring.reddit.version}") private String version;

    @Value("${spring.reddit.subreddit}") private String subredditName;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String kafkaTopic;
    private final KafkaProducer kafkaProducer;

    public SubmissionStreamEventService(KafkaProducer kafkaProducer,
                                        @Value(value = "${spring.kafka.template.default-topic}") String kafkaTopic) {
        this.kafkaProducer = kafkaProducer;
        this.kafkaTopic = kafkaTopic;
    }

    public void run(){
        // Credentials
        Credentials oauthCreds = Credentials.script(username, password, clientID, clientSecret);

        // Create a unique User-Agent for our producer
        UserAgent userAgent = new UserAgent(platform, appid, version, username);

        // Authenticate our client
        RedditClient redditClient = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent), oauthCreds);

        // Get info about the user
        Account me = redditClient.me().about();
        System.out.println(me.getName());
        log.info(me.getName());

        // "Navigate" to the subreddit
        SubredditReference subreddit = redditClient.subreddit(subredditName);

        Stream<Submission> postsStream= subreddit.posts().sorting(SubredditSort.NEW).limit(1).build().stream();
        for (Stream<Submission> it = postsStream; it.hasNext(); ) {
            Submission submission = it.next();
            System.out.println(submission.getTitle()+ " by " + submission.getAuthor());
            //Send tweet to Kafka topic
            log.info("User '{}', Posted : {}", submission.getAuthor() , submission.getTitle());
            kafkaProducer.send(kafkaTopic, submission);
        }
    }
}
