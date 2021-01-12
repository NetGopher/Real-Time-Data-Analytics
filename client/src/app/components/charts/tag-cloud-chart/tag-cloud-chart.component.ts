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
  public refreshInterval: number = 2000; //MILLISECOND
  @Input('transitionDuration')
  public transitionDuration: number = 1000; //MILLISECOND
  @Input('initialData')
  public newData: any[] = [];
  private chart: am4plugins_wordCloud.WordCloud;
  public static counterId: number = 0;

  public triggerChange: boolean = false;
  @Input('title')
  public Title: string = "Word Cloud";
  private series: WordCloudSeries;

  constructor(@Inject(PLATFORM_ID) private platformId, private zone: NgZone, private dataService: DataService, private kafkaStreamHander: KafkaStreamHandlerService) {
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

      myComponent.series.heatRules.push({
        "target": myComponent.series.labels.template,
        "property": "fill",
        "min": am4core.color("#0000CC"),
        "max": am4core.color("#CC00CC"),
        "dataField": "value"
      });

      myComponent.series.labels.template.url = "https://stackoverflow.com/questions/tagged/{word}";
      myComponent.series.labels.template.urlTarget = "_blank";
      myComponent.series.labels.template.tooltipText = "{word}: {value}";

      let hoverState = myComponent.series.labels.template.states.create("hover");
      hoverState.properties.fill = am4core.color("#FF0000");

      // @ts-ignore
      let subtitle = myComponent.chart.titles.create();
      subtitle.text = "(click to open)";

      // @ts-ignore
      let title = myComponent.chart.titles.create();
      title.text = myComponent.Title;
      title.fontSize = 20;
      title.fontWeight = "800";
      myComponent.dataObservable.subscribe((values) => {

        // console.log("Changed --->");
        // console.log(values)
        if (!values) {
          myComponent.triggerChange = false;
          return;
        }
        if (JSON.stringify(values) != JSON.stringify(myComponent.newData)) {
          myComponent.triggerChange = true;
          // myComponent.series.data = values;
          values.sort((a, b) =>  {
            console.log("a" + a);
            return a;
          });
          myComponent.newData = []
          for (let i = 0; i < values.length && i < 30; i++) {
          let value = values[i];
          myComponent.newData.push(value);
        }
          // myComponent.series.data = myComponent.newData;

        }else myComponent.triggerChange = false;

      });
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
