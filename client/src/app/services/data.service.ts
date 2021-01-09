import {Injectable} from '@angular/core';
import {ConnectWebSocket} from "@ngxs/websocket-plugin";
import {Observable, Subject} from "rxjs";
import {KafkaState} from "../state/kafka.state";
import {PostsSpeed, StreamData, StreamTypes, SubredditMention, SubredditMentionBatch} from "../other/Entities";
import {Select, Store} from "@ngxs/store";
import {KafkaStreamHandlerService} from "./kafka-stream-handler.service";

@Injectable({
  providedIn: 'root'
})
export class DataService {
  @Select(KafkaState.messages)
  public kafkaMessages$: Observable<string[]>

  constructor(private store: Store, private kafkaStreamHandlerService: KafkaStreamHandlerService) {
    this.store.dispatch(new ConnectWebSocket())
    this.kafkaMessages$.subscribe(values => {
      if (typeof values[0] != "string") return;
      let value: StreamData = JSON.parse(values[0]);
      console.log(value);
      switch (value.type) {
        case StreamTypes.REDDIT_MENTIONS: // "type == 'REDDIT_MENTIONS'"
          this.kafkaStreamHandlerService.handleRedditMentions(value.data as SubredditMention);
          break;
        case StreamTypes.REDDIT_MENTIONS_BATCH: // "type == 'REDDIT_MENTIONS'"
          this.kafkaStreamHandlerService.handleRedditMentionsBatch(value.data as SubredditMentionBatch);
          break;
        case StreamTypes.COUNT_STREAM: // "type == 'COUNT_STREAM'"
          this.kafkaStreamHandlerService.handlePostsSpeed(value.data as PostsSpeed);
          break;
        default:
          console.log(value.data)
      }

    }, error => {
      console.log(error)
    })
  }

  init() {

  }
}
