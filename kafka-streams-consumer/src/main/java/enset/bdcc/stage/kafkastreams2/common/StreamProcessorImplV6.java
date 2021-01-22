package enset.bdcc.stage.kafkastreams2.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.std.ArraySerializerBase;
import enset.bdcc.stage.kafkastreams2.config.StreamType;
import enset.bdcc.stage.kafkastreams2.entities.WordData;
import enset.bdcc.stage.kafkastreams2.filters.words.english.Stopwords;
import enset.bdcc.stage.kafkastreams2.serializers.CustomSerdes;
import lombok.*;
import net.dean.jraw.models.Submission;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@Component
//TODO:  fix ClassCast Excpetion Error
@Data
@NoArgsConstructor
@AllArgsConstructor

// PROBLEM: Heavy Data
@Component
public class StreamProcessorImplV6 implements StreamProcessor {

    private JsonMapper objectMapper = new JsonMapper();
    private JsonMapper jsonMapper = new JsonMapper();
    private int minWordLength = 5;//words with length lower(or equals) than this will be filtered out
    private int maxWordLength = 31; //words with length greater than this will be filtered out
    private Long minWordCount = 2L; //words found minWordCount times will be filtered out

    @Override
    public KStream<String, String> getSubredditMensionsStream(KStream<String, Submission> initialStream) {

        return initialStream
//                .filter((s, submission) -> !submission.isNsfw())
                .map((KeyValueMapper<String, Submission, KeyValue<String, Long>>) (k, v) -> KeyValue.pair(v.getSubreddit(), 1L))
                //;
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)).advanceBy(Duration.ofSeconds(Common.WINDOW_SIZE)))
                .count()
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.WINDOW_SIZE), Suppressed.BufferConfig.unbounded()))
                .toStream()
//                 .peek((key, value) -> System.out.println("incoming message: {"+key.key()+"} {"+value+"}"))
                .map((key, value) -> {
                    if (value == 1L) return KeyValue.pair("__Others__", 1L);
                    return KeyValue.pair(key.key(), value);
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)).advanceBy(Duration.ofSeconds(Common.WINDOW_SIZE)))
                .reduce(Long::sum)
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.WINDOW_SIZE), Suppressed.BufferConfig.unbounded()))
                .toStream()
//                .peek((s, aLong) -> {
//                            System.out.println("key -> " + s.key() + ", value: " + aLong);
//                        }
//                )
                .map(new KeyValueMapper<Windowed<String>, Long, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> windowed, Long aLong) {
                        return new KeyValue<String, String>("__data__", objectMapper.writeValueAsString(new SubredditData(windowed.key(), aLong)));
                    }
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)).advanceBy(Duration.ofSeconds(Common.WINDOW_SIZE)))
                .aggregate(new Initializer<String>() {

                    @SneakyThrows
                    @Override
                    public String apply() {
                        List<SubredditData> subredditDataList = new ArrayList<>();
                        String jsonResultString = jsonMapper.writeValueAsString(subredditDataList);
                        return jsonResultString;
                    }
                }, new Aggregator<String, String, String>() {
                    @SneakyThrows
                    @Override
                    public String apply(String key, String value, String aggregateValue) {
                        SubredditData newSubredditData = objectMapper.readValue(value, SubredditData.class);
                        String valuesString = null;
                        try {
                            SubredditData[] values = jsonMapper.readValue(aggregateValue, SubredditData[].class);
                            List<SubredditData> dataList = new ArrayList<>(Arrays.asList(values));
                            dataList.add(newSubredditData);
                            valuesString = jsonMapper.writeValueAsString(dataList.toArray());
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            List<SubredditData> subredditDataList = new ArrayList<>();
                            return objectMapper.writeValueAsString(subredditDataList);

                        }
                        return valuesString;
                    }
                }, Materialized.with(Serdes.String(), Serdes.String()))
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.WINDOW_SIZE), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map(new KeyValueMapper<Windowed<String>, String, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> windowed, String s) {
                        SubredditData[] values = jsonMapper.readValue(s, SubredditData[].class);
                        List<SubredditData> dataList = new ArrayList<>(Arrays.asList(values));
                        Map<String, Object> map = new HashMap<>();
                        map.put("data", dataList);
                        map.put("duration", Common.WINDOW_SIZE);

                        return new KeyValue<>(windowed.key(), Common.maptoJsonString(Common.addDataToStreamMap(StreamType.REDDIT_MENTIONS_BATCH, map)));
                    }
                });
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
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.SPEED_METER_WINDOW)))
                .reduce(new Reducer<Long>() {
                    @Override
                    public Long apply(Long aLong, Long v1) {
                        return aLong + v1;
                    }
                })
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.SPEED_METER_WINDOW), Suppressed.BufferConfig.unbounded()))

                .toStream()
//                .peek((stringWindowed, aLong) -> System.out.println("COUNT-> key: " + stringWindowed.key() + "value: " + aLong))
                .map(new KeyValueMapper<Windowed<String>, Long, KeyValue<String, String>>() {
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> key, Long value) {
                        Map<String, String> resultMap = new HashMap<>();
                        resultMap.put("duration", String.valueOf(Common.SPEED_METER_WINDOW));
                        resultMap.put("count", String.valueOf(value));
                        String jsonString = Common.maptoJsonString(Common.addDataToStreamMap(StreamType.COUNT, resultMap));
                        return KeyValue.pair("stream", jsonString);
                    }
                });

    }

    @Override
    public KStream<String, String> getSubredditPostsProportion(KStream<String, Submission> initialStream) {

        return initialStream
                .map((KeyValueMapper<String, Submission, KeyValue<String, Long>>) (k, v) -> {
                    return KeyValue.pair(v.getSubreddit(), 1L);
                })
                //;
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)).advanceBy(Duration.ofSeconds(Common.WINDOW_SIZE)))
                .count()
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.WINDOW_SIZE), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map((key, value) -> {
                    return KeyValue.pair(String.valueOf("R's with " + value + (value == 1 ? " Post" : "Posts")), 1L);
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)).advanceBy(Duration.ofSeconds(Common.WINDOW_SIZE)))
                .reduce(Long::sum)
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.WINDOW_SIZE), Suppressed.BufferConfig.unbounded()))
                .toStream()

                .map(new KeyValueMapper<Windowed<String>, Long, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> windowed, Long aLong) {
                        return new KeyValue<String, String>("__data__", objectMapper.writeValueAsString(new SubredditData(windowed.key(), aLong)));
                    }
                })
//
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WINDOW_SIZE)).advanceBy(Duration.ofSeconds(Common.WINDOW_SIZE)))

                .aggregate(new Initializer<String>() {

                    @SneakyThrows
                    @Override
                    public String apply() {
                        List<SubredditData> subredditDataList = new ArrayList<>();
                        String jsonResultString = jsonMapper.writeValueAsString(subredditDataList);
                        return jsonResultString;
                    }
                }, new Aggregator<String, String, String>() {
                    @SneakyThrows
                    @Override
                    public String apply(String key, String value, String aggregateValue) {
                        SubredditData newSubredditData = objectMapper.readValue(value, SubredditData.class);
                        String valuesString = null;
                        try {
                            SubredditData[] values = jsonMapper.readValue(aggregateValue, SubredditData[].class);
                            List<SubredditData> dataList = new ArrayList<>(Arrays.asList(values));
                            dataList.add(newSubredditData);
                            valuesString = jsonMapper.writeValueAsString(dataList.toArray());
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            List<SubredditData> subredditDataList = new ArrayList<>();
                            return objectMapper.writeValueAsString(subredditDataList);

                        }
                        return valuesString;


                    }
                }, Materialized.with(Serdes.String(), Serdes.String()))
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.WINDOW_SIZE), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map(new KeyValueMapper<Windowed<String>, String, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> windowed, String s) {
                        SubredditData[] values = jsonMapper.readValue(s, SubredditData[].class);
                        List<SubredditData> dataList = new ArrayList<>(Arrays.asList(values));
                        Map<String, Object> map = new HashMap<>();
                        map.put("data", dataList);
                        map.put("duration", Common.WINDOW_SIZE);

                        return new KeyValue<>(windowed.key(), Common.maptoJsonString(Common.addDataToStreamMap(StreamType.REDDIT_POSTS_PROPORTION, map)));
                    }
                });
    }

    @Override
    public KStream<String, String> getWordCount(KStream<String, Submission> intialStream) {
        return intialStream
                .flatMap(new KeyValueMapper<String, Submission, Iterable<KeyValue<String, Long>>>() {
                    @Override
                    public Iterable<KeyValue<String, Long>> apply(String s, Submission submission) {
//                        List<String> values = Arrays.asList(Stopwords.removeAllStopwords(submission.getSelfText().toLowerCase()).trim().split("[ :\\t)@.,]"));
                        List<String> values = Arrays.asList(submission.getSelfText().toLowerCase().trim().split("[ :\\t)@.,]"));
                        return values.stream().map(value -> new KeyValue<String, Long>(value.trim(), 1L)).collect(Collectors.toList());
                    }
                })
                .filterNot((s, aLong) -> s.length() <= minWordLength || s.length() >= maxWordLength)
//                .filterNot((s, aLong) -> s.toLowerCase().matches("(this|pretty|cool|things|mainly|although|always|another|mostly|various|using|about|again)"))
                .filterNot((s, aLong) -> Stopwords.isStopword(s.toLowerCase()))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WORD_MAP_WINDOW)).advanceBy(Duration.ofSeconds(Common.WORD_MAP_WINDOW)))
                .count()
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.WORD_MAP_WINDOW), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .filterNot((windowed, aLong) -> aLong <= minWordCount)
                .map(new KeyValueMapper<Windowed<String>, Long, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> windowed, Long aLong) {
                        return new KeyValue<String, String>("__data__", objectMapper.writeValueAsString(new WordData(windowed.key(), aLong)));
                    }
                })
//
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.WORD_MAP_WINDOW)).advanceBy(Duration.ofSeconds(Common.WORD_MAP_WINDOW)))

                .aggregate(new Initializer<String>() {

                    @SneakyThrows
                    @Override
                    public String apply() {
                        List<WordData> subredditDataList = new ArrayList<>();
                        String jsonResultString = jsonMapper.writeValueAsString(subredditDataList);
                        return jsonResultString;
                    }
                }, new Aggregator<String, String, String>() {
                    @SneakyThrows
                    @Override
                    public String apply(String key, String value, String aggregateValue) {
                        WordData newWordData = objectMapper.readValue(value, WordData.class);
                        String valuesString = null;
                        try {
                            WordData[] values = jsonMapper.readValue(aggregateValue, WordData[].class);
                            List<WordData> dataList = new ArrayList<>(Arrays.asList(values));
                            dataList.add(newWordData);
                            valuesString = jsonMapper.writeValueAsString(dataList.toArray());
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            List<WordData> subredditDataList = new ArrayList<>();
                            return objectMapper.writeValueAsString(subredditDataList);
                        }
                        return valuesString;
                    }
                }, Materialized.with(Serdes.String(), Serdes.String()))
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.WORD_MAP_WINDOW), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map(new KeyValueMapper<Windowed<String>, String, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> windowed, String s) {
                        WordData[] values = jsonMapper.readValue(s, WordData[].class);
                        List<WordData> dataList = new ArrayList<>(Arrays.asList(values));
                        Map<String, Object> map = new HashMap<>();
                        map.put("data", dataList);
                        map.put("duration", Common.WORD_MAP_WINDOW);
                        return new KeyValue<>(windowed.key(), Common.maptoJsonString(Common.addDataToStreamMap(StreamType.WORD_COUNT_BATCH, map)));
                    }
                });


    }

    @Override
    public KStream<String, String> getNsfwProportion(KStream<String, Submission> intialStream) {
        return intialStream.map((s, submission) -> new KeyValue<>(submission.isNsfw() ? "NSFW" : "NOT_NSFW", 1L))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.NSFW_METER_WINDOW)).advanceBy(Duration.ofSeconds(Common.NSFW_METER_WINDOW)))
                .count()
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.NSFW_METER_WINDOW), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map(new KeyValueMapper<Windowed<String>, Long, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> windowed, Long aLong) {
                        return new KeyValue<String, String>("__data__", objectMapper.writeValueAsString(new KeyValuePair(windowed.key(), aLong)));
                    }
                })
//
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.NSFW_METER_WINDOW)).advanceBy(Duration.ofSeconds(Common.NSFW_METER_WINDOW)))

                .aggregate(new Initializer<String>() {

                    @SneakyThrows
                    @Override
                    public String apply() {
                        List<KeyValuePair> subredditDataList = new ArrayList<>();
                        String jsonResultString = jsonMapper.writeValueAsString(subredditDataList);
                        return jsonResultString;
                    }
                }, new Aggregator<String, String, String>() {
                    @SneakyThrows
                    @Override
                    public String apply(String key, String value, String aggregateValue) {
                        KeyValuePair newKeyValuePair = objectMapper.readValue(value, KeyValuePair.class);
                        String valuesString = null;
                        try {
                            KeyValuePair[] values = jsonMapper.readValue(aggregateValue, KeyValuePair[].class);
                            List<KeyValuePair> dataList = new ArrayList<>(Arrays.asList(values));
                            dataList.add(newKeyValuePair);
                            valuesString = jsonMapper.writeValueAsString(dataList.toArray());
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            List<KeyValuePair> subredditDataList = new ArrayList<>();
                            return objectMapper.writeValueAsString(subredditDataList);

                        }
                        return valuesString;


                    }
                }, Materialized.with(Serdes.String(), Serdes.String()))
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.NSFW_METER_WINDOW), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map(new KeyValueMapper<Windowed<String>, String, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> windowed, String s) {
                        KeyValuePair[] values = jsonMapper.readValue(s, KeyValuePair[].class);
                        List<KeyValuePair> dataList = new ArrayList<>(Arrays.asList(values));
                        Map<String, Object> map = new HashMap<>();
                        map.put("data", dataList);
                        map.put("duration", Common.NSFW_METER_WINDOW);
                        return new KeyValue<>(windowed.key(), Common.maptoJsonString(Common.addDataToStreamMap(StreamType.NSFW_COUNT_BATCH, map)));
                    }
                });

    }

    @Override
    public KStream<String, String> getActiveUsersInActiveCommunitiesByPosts(KStream<String, Submission> initialStream) {
        return initialStream.map(new KeyValueMapper<String, Submission, KeyValue<String, String>>() {
            @Override
            public KeyValue<String, String> apply(String s, Submission submission) {

                List<String> list = new ArrayList<>();
                return new KeyValue<>(submission.getSubreddit() + " " + submission.getAuthor(), submission.getAuthor());
            }
        }).groupBy(new KeyValueMapper<String, String, String>() {
            @Override
            public String apply(String key, String value) {
                return key;
            }
        }, Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.ACTIVE_USERS_PER_ACTIVE_R_WINDOW)).advanceBy(Duration.ofSeconds(Common.ACTIVE_USERS_PER_ACTIVE_R_WINDOW)))
                .count()
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.ACTIVE_USERS_PER_ACTIVE_R_WINDOW), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map((key, aLong) -> {
                    String s = key.key();
                    KeyValuePair keyValuePair = new KeyValuePair(s.substring(s.indexOf(" ")), aLong); //getting -->(subreddit,user)
                    try {
                        return new KeyValue<String, String>(s.substring(0, s.indexOf(" ")), jsonMapper.writeValueAsString(keyValuePair));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return new KeyValue<>("__error__", "");
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.ACTIVE_USERS_PER_ACTIVE_R_WINDOW)).advanceBy(Duration.ofSeconds(Common.ACTIVE_USERS_PER_ACTIVE_R_WINDOW)))
                .aggregate(new Initializer<String>() {
                    @SneakyThrows
                    @Override
                    public String apply() {
                        SubredditPostAggregate subredditPostAggregate = new SubredditPostAggregate();
                        subredditPostAggregate.setValue(0L);
                        return jsonMapper.writeValueAsString(new SubredditPostAggregate());
                    }
                }, new Aggregator<String, String, String>() {

                    @SneakyThrows
                    @Override
                    public String apply(String key, String value, String aggregateString) {
                        SubredditPostAggregate subredditPostAggregate = jsonMapper.readValue(aggregateString, SubredditPostAggregate.class);
                        subredditPostAggregate.setKey(key);
                        KeyValuePair keyValuePair = jsonMapper.readValue(value, KeyValuePair.class);
                        subredditPostAggregate.setValue(subredditPostAggregate.getValue() + keyValuePair.getValue());
                        subredditPostAggregate.getChildren().add(keyValuePair);
                        return jsonMapper.writeValueAsString(subredditPostAggregate);
                    }
                }, Materialized.with(Serdes.String(), Serdes.String()))
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.ACTIVE_USERS_PER_ACTIVE_R_WINDOW), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map(new KeyValueMapper<Windowed<String>, String, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> windowed, String value) {
                        return new KeyValue<String, String>("__data__", value);
                    }
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.ACTIVE_USERS_PER_ACTIVE_R_WINDOW)).advanceBy(Duration.ofSeconds(Common.ACTIVE_USERS_PER_ACTIVE_R_WINDOW)))
                .aggregate(new Initializer<String>() {

                    @SneakyThrows
                    @Override
                    public String apply() {
                        List<SubredditPostAggregate> subredditDataList = new ArrayList<>();
                        String jsonResultString = jsonMapper.writeValueAsString(subredditDataList);
                        return jsonResultString;
                    }
                }, new Aggregator<String, String, String>() {
                    @SneakyThrows
                    @Override
                    public String apply(String key, String value, String aggregateValue) {
                        SubredditPostAggregate newSubredditPostAggregate = objectMapper.readValue(value, SubredditPostAggregate.class);
                        String valuesString = null;
                        try {
                            SubredditPostAggregate[] values = jsonMapper.readValue(aggregateValue, SubredditPostAggregate[].class);
                            List<SubredditPostAggregate> dataList = new ArrayList<>(Arrays.asList(values));
                            dataList.add(newSubredditPostAggregate);
                            valuesString = jsonMapper.writeValueAsString(dataList.toArray());
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            List<SubredditPostAggregate> subredditDataList = new ArrayList<>();
                            return objectMapper.writeValueAsString(subredditDataList);
                        }
                        return valuesString;
                    }
                }, Materialized.with(Serdes.String(), Serdes.String()))
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.ACTIVE_USERS_PER_ACTIVE_R_WINDOW), Suppressed.BufferConfig.unbounded()))
                .toStream()
                .map(new KeyValueMapper<Windowed<String>, String, KeyValue<String, String>>() {
                    @SneakyThrows
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> windowed, String s) {
                        SubredditPostAggregate[] values = jsonMapper.readValue(s, SubredditPostAggregate[].class);
                        List<SubredditPostAggregate> dataList = new ArrayList<>(Arrays.asList(values));
                        Map<String, Object> resMap = new HashMap<>();
                        resMap.put("key", "r/all");
                        long count = 0L;
                        for (SubredditPostAggregate subredditPostAggregate : dataList) {
                            count += subredditPostAggregate.getValue();
                        }
                        resMap.put("value", count);
                        resMap.put("children", dataList);
                        Map<String, Object> map = new HashMap<>();
                        map.put("data", resMap);
                        map.put("duration", Common.ACTIVE_USERS_PER_ACTIVE_R_WINDOW);
                        return new KeyValue<>(windowed.key(), Common.maptoJsonString(Common.addDataToStreamMap(StreamType.ACTIVE_USERS_PER_ACTIVE_SUBREDDITS, map)));
                    }
                });


    }

    @Override
    public KStream<String, String> getPostsPerDuration(KStream<String, Submission> initialStream) {
        return initialStream.map(new KeyValueMapper<String, Submission, KeyValue<String, Long>>() {
                                     @Override
                                     public KeyValue<String, Long> apply(String k, Submission v) {
                                         return KeyValue.pair("count", 1L);
                                     }
                                 }
        ).groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofSeconds(Common.POSTS_PER_DURATION_WINDOW)).advanceBy(Duration.ofSeconds(Common.POSTS_PER_DURATION_WINDOW)))
                .reduce(new Reducer<Long>() {
                    @Override
                    public Long apply(Long aLong, Long v1) {
                        return aLong + v1;
                    }
                })
                .suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(Common.POSTS_PER_DURATION_WINDOW), Suppressed.BufferConfig.unbounded()))

                .toStream()
//                .peek((stringWindowed, aLong) -> System.out.println("COUNT-> key: " + stringWindowed.key() + "value: " + aLong))
                .map(new KeyValueMapper<Windowed<String>, Long, KeyValue<String, String>>() {
                    @Override
                    public KeyValue<String, String> apply(Windowed<String> key, Long value) {
                        Map<String, String> resultMap = new HashMap<>();
                        DateFormat format = new SimpleDateFormat("HH:mm:ss");
                        resultMap.put("time", format.format(new Date(key.window().end())));
                        resultMap.put("duration",String.valueOf(Common.POSTS_PER_DURATION_WINDOW));
                        resultMap.put("time_epoch_start", String.valueOf(key.window().start()));
                        resultMap.put("time_epoch_end", String.valueOf(key.window().end()));
                        resultMap.put("count", String.valueOf(value));
                        String jsonString = Common.maptoJsonString(Common.addDataToStreamMap(StreamType.POSTS_PER_DURATION, resultMap));
                        return KeyValue.pair("stream", jsonString);
                    }
                });

    }
}

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
class KeyValuePair {
    private String key;
    private Long value;
}

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
class SubredditPostAggregate {
    protected long value;
    protected List<KeyValuePair> children = new ArrayList<>();
    private String key;
}

