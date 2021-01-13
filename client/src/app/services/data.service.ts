import {Injectable} from '@angular/core';
import {ConnectWebSocket} from "@ngxs/websocket-plugin";
import {Observable, Subject} from "rxjs";
import {KafkaState} from "../state/kafka.state";
import {
  KeyValuePairBatch,
  PostsSpeed,
  StreamData,
  StreamTypes,
  SubredditMention,
  SubredditMentionBatch,
  WordCountBatch
} from "../other/Entities";
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
        case StreamTypes.REDDIT_POSTS_PROPORTION: // "type == 'REDDIT_MENTIONS'"
          this.kafkaStreamHandlerService.handleRedditPostsProportion(value.data as SubredditMentionBatch);
          break;
        case StreamTypes.COUNT_STREAM: // "type == 'COUNT_STREAM'"
          this.kafkaStreamHandlerService.handlePostsSpeed(value.data as PostsSpeed);
          break;
        case StreamTypes.NSFW_COUNT_BATCH:
          this.kafkaStreamHandlerService.handleNSFWMeter(value.data as KeyValuePairBatch);
          break;
        case StreamTypes.WORD_COUNT_BATCH: // "type == 'WORD_COUNT_BATCH'"
          this.kafkaStreamHandlerService.handleWordCountBatch(value.data as WordCountBatch);
          break;
        default:
          console.log(value.type, " is undefined StreamType!")
          console.log(value.type)
      }

    }, error => {
      console.log(error)
    })
  }

  init() {

  }
}
