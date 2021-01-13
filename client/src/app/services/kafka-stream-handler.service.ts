import {Injectable} from '@angular/core';
import {
  KeyValuePairBatch,
  PostsSpeed,
  SubredditMention,
  SubredditMentionBatch,
  WordCountBatch,
  WordData,KeyValuePair
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
  public nsfwData: KeyValuePair[];
  public nsfwDataObserver: BehaviorSubject<KeyValuePair[]> = new BehaviorSubject<KeyValuePair[]>(null);

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
    if(!data.data) return;
    this.subredditMentions = data.data;
    this.subredditMentionsObserver.next(data.data);
  }

  handleRedditPostsProportion(data: SubredditMentionBatch) {
    if(!data.data) return;
    this.subredditPostsProportion = data.data;
    this.subredditPostsProportionObserver.next(data.data);
  }

  handleWordCountBatch(data: WordCountBatch) {
    if(!data.data) return;
    this.wordData = data.data;
    this.wordDataObserver.next(data.data);
  }

  handleNSFWMeter(data: KeyValuePairBatch) {
    if(!data.data) return;
    this.nsfwData = data.data;
    this.nsfwDataObserver.next(data.data);
  }
}
