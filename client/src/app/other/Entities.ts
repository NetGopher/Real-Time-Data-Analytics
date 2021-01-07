export class SubredditMention{
  public subreddit:string
  public count:number
}
export class StreamData{
  public data: any;
  public type:string;
}
export class StreamTypes{
  public static ERROR = "ERROR";
  public static COUNT_STREAM = "COUNT_STREAM";
  public static REDDIT_MENTIONS = "REDDIT_MENTIONS";
}
export class PostsSpeed{
  public count:number = 1
  public duration:number = 0
}
