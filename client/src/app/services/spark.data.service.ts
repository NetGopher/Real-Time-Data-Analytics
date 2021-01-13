import { Injectable } from '@angular/core';
import {Select, Store} from "@ngxs/store";
import {SparkStreamHandlerService} from "./spark-stream-handler.service";
import {ConnectWebSocket} from "@ngxs/websocket-plugin";
import {Observable} from "rxjs";
import {KafkaState} from "../state/kafka.state";
import {PostsSpeed, StreamData, StreamTypes, SubredditMentionBatch} from "../other/Entities";

@Injectable({
  providedIn: 'root'
})
export class SparkDataService {

  @Select(KafkaState.messages)
  public kafkaMessages$: Observable<string[]>

  constructor(private store: Store, private sparkStreamHandlerService: SparkStreamHandlerService) {
     this.store.dispatch(new ConnectWebSocket())
    this.kafkaMessages$.subscribe(values => {
      //console.log("Values: " + values)
      if (typeof values[0] != "string") return;
      let value: StreamData = JSON.parse(values[0]);
      console.log("Value:" + JSON.stringify(value))
      switch (value.type) {
        case StreamTypes.REDDIT_MENTIONS_BATCH: // "type == 'REDDIT_MENTIONS'"
          //console.log("Value:" + JSON.stringify(value))
          this.sparkStreamHandlerService.handleRedditMentionsBatch(value.data as SubredditMentionBatch);
          break;
        case StreamTypes.REDDIT_POSTS_PROPORTION: // "type == 'REDDIT_POSTS_PROPORTION'"
          this.sparkStreamHandlerService.handleRedditPostsProportion(value.data as SubredditMentionBatch)
        case StreamTypes.COUNT_STREAM: // "type == 'COUNT_STREAM'"
          //console.log("Value:" + JSON.stringify(value))
          this.sparkStreamHandlerService.handlePostsSpeed(value.data as PostsSpeed);
          break;
        default:
          console.log(value.data)
      }

    }, error => {
      console.log(error)
    })
  }
}
