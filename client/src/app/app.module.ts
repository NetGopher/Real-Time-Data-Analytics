import { BrowserModule } from "@angular/platform-browser";
import { AppRoutingModule } from './app-routing/app-routing.module';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import {NgxsWebsocketPluginModule} from "@ngxs/websocket-plugin";
import {KafkaState} from "./state/kafka.state";
import {NgxsModule} from "@ngxs/store";

import {FormsModule} from "@angular/forms";


import { SidebarComponent } from './shared/sidebar/sidebar.component';
import { TopbarComponent } from './shared/topbar/topbar.component';
import { FooterComponent } from './shared/footer/footer.component';

import spark_module from "./components/group_1.module";
import kafka_module from "./components/group_2.module";
import { BlankPageComponent } from './pages/blank-page/blank-page.component';
import { HomeComponent } from './pages/home/home.component';

@NgModule({
  declarations: [
    AppComponent,
    SidebarComponent,
    TopbarComponent,
    FooterComponent,
    ...spark_module.components,
    ...kafka_module.components,
    BlankPageComponent,
    HomeComponent,
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
