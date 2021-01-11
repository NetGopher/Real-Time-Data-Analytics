import { Injectable } from '@angular/core';
import {PostsSpeed, SubredditMention, SubredditMentionBatch} from "../other/Entities";
import {BehaviorSubject, Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SparkStreamHandlerService {

  public counter: number = 0;
  public i = 0;
  public popularCommunitiesObserver: BehaviorSubject<SubredditMention[]> = new BehaviorSubject<SubredditMention[]>(null);
  public results: SubredditMention[] = []
  public postsSpeedList: PostsSpeed[] = [];
  public postsSpeedObserver: BehaviorSubject<number> = new BehaviorSubject<number>(1);


  constructor() { }

  handleRedditMentions(subredditMentionBatch: SubredditMentionBatch) {
    //console.log("New Data: " + JSON.stringify(subredditMentionBatch.subredditMentions))
    // @ts-ignore
    this.popularCommunitiesObserver.next(subredditMentionBatch.subredditMentions);
  }
  handlePostsSpeed(value: PostsSpeed) {
    this.postsSpeedObserver.next(value.count);
    console.log("New Speed: " + value.count)
    this.postsSpeedList.push(value);
  }
}
