import {Component, OnInit} from '@angular/core';
import {KafkaState} from "../../state/kafka.state";
import {Select, Store} from "@ngxs/store";
import {Observable} from "rxjs";
import {ConnectWebSocket} from "@ngxs/websocket-plugin";


@Component({
  selector: 'app-kafka-stream-consumer',
  templateUrl: './kafka-stream-consumer.component.html',
  styleUrls: ['./kafka-stream-consumer.component.css']
})

export class KafkaStreamConsumerComponent implements OnInit {
  @Select(KafkaState.messages)
  kafkaMessages$: Observable<string[]>

  constructor(private store: Store) {
  }

  ngOnInit(): void {
    this.store.dispatch(new ConnectWebSocket())
  }


}
