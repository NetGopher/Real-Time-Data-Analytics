export class MyResult{
  public subreddit:string
  public mentions:number
}
export class StreamData{
  public data: any;
  public type:string;
}
export class StreamTypes{
  public static ERROR = "ERROR";
  public static COUNT = "COUNT";
  public static REDDIT_MENTIONS = "REDDIT_MENTIONS";
}
