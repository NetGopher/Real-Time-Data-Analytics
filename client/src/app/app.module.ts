import { BrowserModule } from "@angular/platform-browser";
import { AppRoutingModule } from './app-routing/app-routing.module';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { HomeComponent } from './components/home/home.component';
import { KafkaStreamConsumerComponent } from './components/kafka-stream-consumer/kafka-stream-consumer.component';
import {NgxsWebsocketPluginModule} from "@ngxs/websocket-plugin";
import {KafkaState} from "./state/kafka.state";
import {NgxsModule} from "@ngxs/store";
import { SpeedComponent } from './components/charts/speed/speed.component';
import {FormsModule} from "@angular/forms";
import { StreamSpeedComponent } from './components/spark-stream-consumer/stream-speed/stream-speed.component';
import { SparkStreamConsumerComponent } from './components/spark-stream-consumer/spark-stream-consumer.component';
import { PopularCommunitiesComponent } from './components/spark-stream-consumer/popular-communities/popular-communities.component';
import { DonutChartComponent } from './components/spark-stream-consumer/donut-chart/donut-chart.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    KafkaStreamConsumerComponent,
    SpeedComponent,
    StreamSpeedComponent,
    SparkStreamConsumerComponent,
    PopularCommunitiesComponent,
    DonutChartComponent
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        NgxsModule.forRoot([
            KafkaState
        ]),
        NgxsWebsocketPluginModule.forRoot({
            url: 'ws://localhost:8080/websocket'
        }),
        FormsModule
    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
