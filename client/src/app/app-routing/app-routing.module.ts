import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {KafkaStreamConsumerComponent} from "../components/kafka-stream-consumer/kafka-stream-consumer.component";
import {SparkStreamConsumerComponent} from "../components/spark-stream-consumer/spark-stream-consumer.component";
import {BlankPageComponent} from "../pages/blank-page/blank-page.component";
import {HomeComponent} from "../pages/home/home.component";
const routes: Routes = [
  {
    path: '',
    component: HomeComponent
  },
  {
    path: 'kafka',
    component: KafkaStreamConsumerComponent,
  },
  {
    path: 'spark',
    component: SparkStreamConsumerComponent
  },
  {
    path: 'blank',
    component: BlankPageComponent
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [
    RouterModule
  ],
  declarations: []
})
export class AppRoutingModule { }
