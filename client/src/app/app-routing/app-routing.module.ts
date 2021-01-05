import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from '../components/home/home.component';
import {KafkaStreamConsumerComponent} from "../components/kafka-stream-consumer/kafka-stream-consumer.component";
const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent,
  },
  {
    path: 'test',
    component: KafkaStreamConsumerComponent,
  },
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
