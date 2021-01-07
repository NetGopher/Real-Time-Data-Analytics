import {Component, OnInit} from '@angular/core';
import {KafkaState} from "../../state/kafka.state";
import {Select, Store} from "@ngxs/store";
import {Observable} from "rxjs";
import {ConnectWebSocket} from "@ngxs/websocket-plugin";
import {MyResult} from "../../other/Entities";
import {DataService} from "../../services/data.service";
@Component({
  selector: 'app-kafka-stream-consumer',
  templateUrl: './kafka-stream-consumer.component.html',
  styleUrls: ['./kafka-stream-consumer.component.css']
})
export class KafkaStreamConsumerComponent implements OnInit {



  constructor(public dataService:DataService) {
  }

  ngOnInit(): void {

  }


}
