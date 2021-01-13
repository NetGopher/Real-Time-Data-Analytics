import {
  AfterViewInit,
  Component,
  Directive,
  Inject, Injectable,
  Input,
  NgZone,
  OnDestroy,
  OnInit,
  PLATFORM_ID
} from '@angular/core';
import * as am4core from "@amcharts/amcharts4/core";
import * as am4charts from "@amcharts/amcharts4/charts";
import am4themes_animated from "@amcharts/amcharts4/themes/animated";
import {isPlatformBrowser} from "@angular/common";
import {Axis} from "@amcharts/amcharts4/charts";
import {DataService} from "../../../services/data.service";
import {KafkaStreamHandlerService} from "../../../services/kafka-stream-handler.service";
import {Observable} from "rxjs";
import * as am4plugins_wordCloud from "@amcharts/amcharts4/plugins/wordCloud";

import {ISpriteEvents} from "@amcharts/amcharts4/.internal/core/Sprite";
import {WordCloudSeries} from "@amcharts/amcharts4/plugins/wordCloud";
import {string} from "@amcharts/amcharts4/core";

@Component({
  selector: 'app-tag-cloud-chart',
  templateUrl: './tag-cloud-chart.component.html',
  styleUrls: ['./tag-cloud-chart.component.css']
})
@Injectable()
export class TagCloudChartComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input('dataObservable')
  public dataObservable: Observable<any[]>;
  @Input('categoryProperty')
  public categoryProperty: string;
  @Input('valueProperty')
  public valueProperty: string;
  @Input('refreshInterval')
  public refreshInterval: number = 5000; //MILLISECOND | OPTIONAL
  @Input('transitionDuration')
  public transitionDuration: number = 500; //MILLISECOND | OPTIONAL
  @Input('initialData')
  public newData: any[] = [];
  @Input('maxCount') //Max Words to show in cloud (0 => Turned off)
  public maxCount: number = 0;
  @Input('title')
  public Title: string = "Word Cloud";
  @Input('forceRefresh')
  public forceRefresh: boolean = false;
  private chart: am4plugins_wordCloud.WordCloud;
  private series: WordCloudSeries;
  public static counterId: number = 0;
  // used when forceRefresh is turned off, checks if cloud is rendered after the specified duration (if refreshInterval ran out)
  public checkTransitionCompleteDuration: number = 1000;

  public triggerChange: boolean = true //true when new data arrives, turned to false when chart received the data
  private isReady: boolean = true; //true when Series (cloud) has finished ("transitioned" event triggered)

  constructor(@Inject(PLATFORM_ID) private platformId, private zone: NgZone) {
  }

  randomIdValueString: string;

  browserOnly(f: (myComponent: TagCloudChartComponent) => void) {
    if (isPlatformBrowser(this.platformId)) {
      this.zone.runOutsideAngular(() => {
        f(this);
      });
    }
  }

  ngAfterViewInit() {
    // Chart code goes in here
    this.browserOnly((myComponent) => {
      /* Chart code */
// Themes begin
      am4core.useTheme(am4themes_animated);
// Themes end

      myComponent.chart = am4core.create("tag_cloud_chart_" + myComponent.randomIdValueString, am4plugins_wordCloud.WordCloud);
      myComponent.chart.fontFamily = "Courier New";
      myComponent.series = myComponent.chart.series.push(new am4plugins_wordCloud.WordCloudSeries());
      myComponent.series.randomness = 0.1;
      myComponent.series.rotationThreshold = 0.5;

      myComponent.series.data = myComponent.newData;

      myComponent.series.dataFields.word = myComponent.categoryProperty;
      myComponent.series.dataFields.value = myComponent.valueProperty;

      // Heat rules generated coors

      // myComponent.series.heatRules.push({
      //   "target": myComponent.series.labels.template,
      //   "property": "fill",
      //   "min": am4core.color("#0000CC"),
      //   "max": am4core.color("#CC00CC"),
      //   logarithmic: true,
      //   "dataField": "value"
      // });


      // Random Colors
      myComponent.series.colors = new am4core.ColorSet();
      myComponent.series.colors.passOptions = {};
      myComponent.series.colors.reuse = true;

      myComponent.series.colors.list = [
        am4core.color("#845EC2"),
        am4core.color("#e300ff"),
        // am4core.color("#ee77c9"),
        am4core.color("#FF6F91"),
        am4core.color("#000000"),
        // am4core.color("#00ffff"),
        // am4core.color("#FF9671"),
        // am4core.color("#FFC75F"),
        am4core.color("#13ee00"),
        am4core.color("#00a3d6"),

      ];
      // myComponent.series.labels.template.url = "https://stackoverflow.com/questions/tagged/{word}";
      // myComponent.series.labels.template.urlTarget = "_blank";
      myComponent.series.labels.template.tooltipText = "{word}: {value}";
      myComponent.series.maxCount = myComponent.maxCount;
      let hoverState = myComponent.series.labels.template.states.create("hover");
      hoverState.properties.fill = am4core.color("#FF0000");
      // @ts-ignore
      // let subtitle = myComponent.chart.titles.create();
      // subtitle.text = "(click to open)";

      // @ts-ignore
      // let title = myComponent.chart.titles.create();
      // title.text = myComponent.Title;
      // title.fontSize = 20;
      // title.fontWeight = "800";
      myComponent.dataObservable.subscribe((values) => {
        if (!values) {
          myComponent.triggerChange = false;
          return;
        }
        if (JSON.stringify(values) != JSON.stringify(myComponent.newData)) {
          myComponent.triggerChange = true;
          // myComponent.series.data = values;

          if (this.maxCount > 0) {
            values.sort((a, b) => {
              return b[myComponent.valueProperty] - a[myComponent.valueProperty];
            });
            myComponent.newData = []
            for (let i = 0; i < values.length && i < myComponent.maxCount; i++) {
              let value = values[i];
              myComponent.newData.push(value);
            }
          } else myComponent.newData = values;

        }
      });
      if (!myComponent.forceRefresh)
        myComponent.chart.setTimeout(easeRefresh, 100);
      else
        myComponent.chart.setTimeout(hardRefresh, 100);

      myComponent.series._eventDispatcher.on("arrangeended", () => {
        myComponent.isReady = true;
      })

      function easeRefresh() { // Refresh isnt forced until the cloud is fully rendered
        if (myComponent.isReady) {
          if (myComponent.triggerChange) {
            myComponent.triggerChange = false;
            myComponent.isReady = false;
            myComponent.chart.hide(myComponent.transitionDuration);

            setTimeout(() => {
              myComponent.series.data = myComponent.newData;
              myComponent.chart.show();
              myComponent.chart.setTimeout(easeRefresh, myComponent.refreshInterval);

            }, 500);

          } else
            myComponent.chart.setTimeout(easeRefresh, myComponent.checkTransitionCompleteDuration);
        } else {
          console.log("Component still rendering... checking in ",)
          myComponent.chart.setTimeout(easeRefresh, myComponent.checkTransitionCompleteDuration);
        }
      }

      function hardRefresh() {
        // Refresh is forced even if the cloud is still being rendered
        myComponent.chart.hide(myComponent.transitionDuration);
        setTimeout(() => {
          myComponent.series.data = myComponent.newData;
          myComponent.chart.show();
            myComponent.chart.setTimeout(hardRefresh, myComponent.refreshInterval);
        }, myComponent.transitionDuration);
      }
    });
  }


  ngOnDestroy() {
    // Clean up chart when the component is removed
    this.browserOnly(() => {
      if (this.chart) {
        this.chart.dispose();
      }
    });
  }

  ngOnInit(): void {
    this.randomIdValueString = `` + TagCloudChartComponent.counterId;
    TagCloudChartComponent.counterId++;

  }

  onClick() {
    this.series.data = this.newData;
  }
}
