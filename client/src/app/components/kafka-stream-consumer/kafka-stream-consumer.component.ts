import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {DataService} from "../../services/data.service";
import {KafkaStreamHandlerService} from "../../services/kafka-stream-handler.service";
import {TagCloudChartComponent} from "../charts/tag-cloud-chart/tag-cloud-chart.component";
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


  onClick() {
    console.log("zabobi")
  }
}
