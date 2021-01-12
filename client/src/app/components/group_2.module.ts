import { KafkaStreamConsumerComponent } from './kafka-stream-consumer/kafka-stream-consumer.component';
import { SpeedComponent } from './charts/speed/speed.component';
import { DonutComponent } from './charts/donut/donut.component';
import {TagCloudChartComponent} from "./charts/tag-cloud-chart/tag-cloud-chart.component";


const components = [
  SpeedComponent,
  DonutComponent,
  KafkaStreamConsumerComponent,
  TagCloudChartComponent
]

export default {
  components
}
