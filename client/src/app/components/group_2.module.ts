import { KafkaStreamConsumerComponent } from './kafka-stream-consumer/kafka-stream-consumer.component';
import { SpeedComponent } from './charts/speed/speed.component';
import { DonutComponent } from './charts/donut/donut.component';


const components = [
  SpeedComponent,
  DonutComponent,
  KafkaStreamConsumerComponent
]

export default {
  components
}
