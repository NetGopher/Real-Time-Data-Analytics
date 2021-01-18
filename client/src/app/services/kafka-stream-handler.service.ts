import {Injectable} from '@angular/core';
import {
  KeyValuePairBatch,
  PostsSpeed,
  SubredditMention,
  SubredditMentionBatch,
  WordCountBatch,
  ActiveUsersPerActiveSubredditsBatch,
  WordData, KeyValuePair, SubredditUsers,
  PostsPerDuration
} from "../other/Entities";
import {BehaviorSubject, Observable, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class KafkaStreamHandlerService {
  public counter: number = 0;
  public i = 0;
  public subredditMentions: SubredditMention[] = []
  public subredditPostsProportion: SubredditMention[] = []
  public wordData: WordData[] = []
  public subredditMentionsObserver: BehaviorSubject<SubredditMention[]> = new BehaviorSubject<SubredditMention[]>(null);
  public wordDataObserver: BehaviorSubject<WordData[]> = new BehaviorSubject<WordData[]>(null);
  public subredditPostsProportionObserver: BehaviorSubject<SubredditMention[]> = new BehaviorSubject<SubredditMention[]>(null);
  public postsSpeedList: PostsSpeed[] = [];
  public postsSpeedObserver: BehaviorSubject<number> = new BehaviorSubject<number>(1);
  public postsPerDurationObserver: BehaviorSubject<PostsPerDuration> = new BehaviorSubject<PostsPerDuration>(new PostsPerDuration());
  public currentPostPerDuration:PostsPerDuration = null;
  public nsfwData: KeyValuePair[];
  public nsfwDataObserver: BehaviorSubject<KeyValuePair[]> = new BehaviorSubject<KeyValuePair[]>(null);
  public activeUsersPerActiveSubredditsData: SubredditUsers[];
  public activeUsersPerActiveSubredditsDataObserver: BehaviorSubject<SubredditUsers[]> = new BehaviorSubject<SubredditUsers[]>(null);

  constructor() {
  }

  handleRedditMentions(value: SubredditMention) {
    const shit: Observable<number> = new Observable((observer) => {
      observer.next(5)
    });
    shit.pipe()
  }

  handlePostsSpeed(value: PostsSpeed) {
    this.postsSpeedObserver.next(value.count);
    this.postsSpeedList.push(value);
  }

  handleRedditMentionsBatch(data: SubredditMentionBatch) {
    if (!data.data) return;
    this.subredditMentions = data.data;
    this.subredditMentionsObserver.next(data.data);
  }

  handleRedditPostsProportion(data: SubredditMentionBatch) {
    if (!data.data) return;
    this.subredditPostsProportion = data.data;
    this.subredditPostsProportionObserver.next(data.data);
  }

  handleWordCountBatch(data: WordCountBatch) {
    if (!data.data) return;
    this.wordData = data.data;
    this.wordDataObserver.next(data.data);
  }

  handleNSFWMeter(data: KeyValuePairBatch) {
    if (!data.data) return;
    this.nsfwData = data.data;
    this.nsfwDataObserver.next(data.data);
  }

  handleActiveUsersPerActiveSubreddits(data: ActiveUsersPerActiveSubredditsBatch) {
    if (!data.data) return;
    // let convertedData: any = []
    // for (const value of data.data) {
    //   let obj: any = {}
    //   obj.count = value.value
    //   obj.key = value.key
    //   obj.children = value.children
    //   convertedData.push(obj);
    // }

    // this.activeUsersPerActiveSubredditsData = convertedData
    // this.activeUsersPerActiveSubredditsDataObserver.next(convertedData);

    // @ts-ignore
    if (data.data.length == 1 && (data.data[0]['key']) == 'r/all') {
      this.activeUsersPerActiveSubredditsData = data.data[0].children;
      this.activeUsersPerActiveSubredditsDataObserver.next(data.data[0].children);

    } else {
      this.activeUsersPerActiveSubredditsData = data.data
      this.activeUsersPerActiveSubredditsDataObserver.next(data.data);
    }
    console.log("this")
    console.log(data.data)
    console.log("typeof array"  + typeof [1,2])
    console.log("typeof obj"  + typeof {})
  }

  handlePostsPerDuration(data: PostsPerDuration) {
          this.currentPostPerDuration = data
    this.postsPerDurationObserver.next(data)
  }
}
