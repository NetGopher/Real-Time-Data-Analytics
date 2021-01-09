import { Component, OnInit } from '@angular/core';
import {SparkDataService} from "../../services/spark.data.service";
import {SparkStreamHandlerService} from "../../services/spark-stream-handler.service";

@Component({
  selector: 'app-spark-stream-consumer',
  templateUrl: './spark-stream-consumer.component.html',
  styleUrls: ['./spark-stream-consumer.component.css']
})
export class SparkStreamConsumerComponent implements OnInit {

  constructor(public dataService:SparkDataService, public sparkStreamHandler: SparkStreamHandlerService) { }

  ngOnInit(): void {
  }

}
