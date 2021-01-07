import {Injectable} from '@angular/core';
import {PostsSpeed, SubredditMention} from "../other/Entities";

@Injectable({
  providedIn: 'root'
})
export class KafkaStreamHandlerService {
  public counter: number = 0;
  public i = 0;
  public numberOfPosts = 0;
  public results: SubredditMention[] = []
  public currentPostsSpeed: PostsSpeed = new PostsSpeed()
  public postsSpeedList: PostsSpeed[] = [];

  constructor() {
  }

  handleRedditMentions(value: SubredditMention) {

  }

  handlePostsSpeed(value: PostsSpeed) {
    value.count *= 1;
    this.currentPostsSpeed = value;

    console.log("New Speed: " + value.count)
    this.postsSpeedList.push(value);
  }
}
