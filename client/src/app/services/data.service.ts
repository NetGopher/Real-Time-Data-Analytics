import {Injectable} from '@angular/core';
import {ConnectWebSocket} from "@ngxs/websocket-plugin";
import {Observable} from "rxjs";
import {KafkaState} from "../state/kafka.state";
import {MyResult} from "../other/Entities";
import {Select, Store} from "@ngxs/store";

@Injectable({
  providedIn: 'root'
})
export class DataService {
  public counter: number = 0;
  public i = 0;
  public numberOfPosts = 0;
  @Select(KafkaState.messages)
  public kafkaMessages$: Observable<string[]>
  public results: MyResult[] = []

  constructor(private store: Store) {
    this.store.dispatch(new ConnectWebSocket())
    this.kafkaMessages$.subscribe(values => {
      console.log("--------------RECEIVE---------")
      console.log(values)
      console.log("LENGTH:" + values.length)
    }, error => {
      console.log(error)
    })
  }

  init() {

  }
}
