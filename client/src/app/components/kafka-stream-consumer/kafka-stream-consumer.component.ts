import {Component, OnInit} from '@angular/core';
import {DataService} from "../../services/data.service";
import {KafkaStreamHandlerService} from "../../services/kafka-stream-handler.service";
@Component({
  selector: 'app-kafka-stream-consumer',
  templateUrl: './kafka-stream-consumer.component.html',
  styleUrls: ['./kafka-stream-consumer.component.css']
})
export class KafkaStreamConsumerComponent implements OnInit {



  constructor(public dataService:DataService, public kafkaStreamHandler:KafkaStreamHandlerService) {
  }

  ngOnInit(): void {

  }



}
