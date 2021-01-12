import { SparkStreamConsumerComponent } from './spark-stream-consumer/spark-stream-consumer.component';
import { KafkaStreamConsumerComponent } from './kafka-stream-consumer/kafka-stream-consumer.component';

import { HBarComponent } from './charts/h-bar/h-bar.component';
import { SpeedComponent } from './charts/speed/speed.component';
import { DonutComponent } from './charts/donut/donut.component';

const components = [
  SparkStreamConsumerComponent,
  KafkaStreamConsumerComponent,
  HBarComponent,
  SpeedComponent,
  DonutComponent,
]

export default {
  components
}
