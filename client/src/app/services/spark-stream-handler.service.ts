import { Injectable } from '@angular/core';
import {PostsSpeed, SubredditMention} from "../other/Entities";
import {BehaviorSubject, Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SparkStreamHandlerService {

  public counter: number = 0;
  public i = 0;
  public results: SubredditMention[] = []
  public postsSpeedList: PostsSpeed[] = [];
  public postsSpeedObserver: BehaviorSubject<number> = new BehaviorSubject<number>(1);


  constructor() { }

  handleRedditMentions(value: SubredditMention) {
    const shit: Observable<number> = new Observable((observer) => {
      observer.next(5)
    });
    shit.pipe()
  }

  handlePostsSpeed(value: PostsSpeed) {
    this.postsSpeedObserver.next(value.count);
    console.log("New Speed: " + value.count)
    this.postsSpeedList.push(value);
  }
}
