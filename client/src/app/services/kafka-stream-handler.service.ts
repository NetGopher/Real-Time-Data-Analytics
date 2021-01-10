import {Injectable} from '@angular/core';
import {PostsSpeed, SubredditMention,SubredditMentionBatch} from "../other/Entities";
import {BehaviorSubject, Observable, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class KafkaStreamHandlerService {
  public counter: number = 0;
  public i = 0;
  public subredditMentions: SubredditMention[] = []
  public subredditPostsProportion: SubredditMention[] = []
  public subredditMentionsObserver: BehaviorSubject<SubredditMention[]> = new BehaviorSubject<SubredditMention[]>(null);
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
    // console.log("New Speed: " + value.count)
    this.postsSpeedList.push(value);
  }

  handleRedditMentionsBatch(data: SubredditMentionBatch) {
    // let array:any[] = [];
    // console.log(data);
    // for (const value of data.data) {
    //       array.push({
    //         "subreddit":value.subreddit,
    //         "count":value.count
    //       })
    // }
    this.subredditMentions = data.data;
    this.subredditMentionsObserver.next(data.data);
  }
    handleRedditPostsProportion(data: SubredditMentionBatch) {
    // let array:any[] = [];
    // console.log(data);
    // for (const value of data.data) {
    //       array.push({
    //         "subreddit":value.subreddit,
    //         "count":value.count
    //       })
    // }
    this.subredditPostsProportion = data.data;
    this.subredditPostsProportionObserver.next(data.data);
  }
}
