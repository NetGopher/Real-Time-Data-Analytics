import {Injectable} from '@angular/core';
import {
  PostsSpeed,
  SubredditMention,
  SubredditMentionBatch,
  WordCountBatch,
  WordData,
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
    this.subredditMentions = data.data;
    this.subredditMentionsObserver.next(data.data);
  }

  handleRedditPostsProportion(data: SubredditMentionBatch) {
    this.subredditPostsProportion = data.data;
    this.subredditPostsProportionObserver.next(data.data);
  }

  handleWordCountBatch(data: WordCountBatch) {
    this.wordData = data.data;
    this.wordDataObserver.next(data.data);
  }
}
