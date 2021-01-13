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

import chart_components_module from "./components/chart_components.module";
import { BlankPageComponent } from './pages/blank-page/blank-page.component';
import { HomeComponent } from './pages/home/home.component';
import {environment} from "../environments/environment";

@NgModule({
  declarations: [
    AppComponent,
    SidebarComponent,
    TopbarComponent,
    FooterComponent,
    ...chart_components_module.components,
    BlankPageComponent,
    HomeComponent,
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        NgxsModule.forRoot([
            KafkaState
        ],{
          developmentMode:!environment.production
        }),
        NgxsWebsocketPluginModule.forRoot({
            url: 'ws://localhost:8080/websocket'
        }),
        FormsModule
    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
