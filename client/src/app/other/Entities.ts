export class SubredditMention {
  public subreddit: string
  public count: number
}

export class StreamData {
  public data: any;
  public type: string;
}

export class StreamTypes {
  public static ERROR = "ERROR";
  public static COUNT_STREAM = "COUNT_STREAM";
  public static REDDIT_MENTIONS = "REDDIT_MENTIONS";
  public static REDDIT_MENTIONS_BATCH = "REDDIT_MENTIONS_BATCH";
  public static REDDIT_POSTS_PROPORTION = "REDDIT_POSTS_PROPORTION";
  public static WORD_COUNT_BATCH = "WORD_COUNT_BATCH";
  public static NSFW_COUNT_BATCH =  "NSFW_COUNT_BATCH";

}

export class WordData {
  public count: number
  public word: string
}

export class PostsSpeed {
  public count: number = 1
  public duration: number = 0
}

export class SubredditMentionBatch {
  public data: SubredditMention[] = [];
  public duration: number = 0;
}

export class WordCountBatch {
  public data: WordData[] = []
  public duration: number = 0;
}
export class KeyValuePair {
  public value: string
  public key: string
}
export class KeyValuePairBatch{
  public data: KeyValuePair[] = []
  public duration: number = 0;

}
