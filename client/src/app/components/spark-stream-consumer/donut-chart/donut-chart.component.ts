import {
  AfterViewInit,
  Component,
  Directive,
  Inject,
  Input,
  NgZone,
  OnDestroy,
  OnInit,
  PLATFORM_ID
} from '@angular/core';
import * as am4core from '@amcharts/amcharts4/core';
import * as am4charts from '@amcharts/amcharts4/charts';
import am4themes_animated from '@amcharts/amcharts4/themes/animated';
import {isPlatformBrowser} from "@angular/common";
import {SparkDataService} from "../../../services/spark.data.service";
import {SparkStreamHandlerService} from "../../../services/spark-stream-handler.service";
import {Observable} from "rxjs";
import {SubredditMention} from "../../../other/Entities";
import {PieChart} from "@amcharts/amcharts4/charts";

@Component({
  selector: 'app-donut-chart',
  templateUrl: './donut-chart.component.html',
  styleUrls: ['./donut-chart.component.css']
})
export class DonutChartComponent implements OnInit {

  @Input('dataObservable')
  public dataObservable:Observable<any>;
  private chart: PieChart;
  @Input('refreshInterval')
  public refreshInterval: number = 2000; //MILLISECOND
  @Input('transitionDuration')
  public transitionDuration: number = 1000; //MILLISECOND
  public static counterId:number = 0;
  public currentData: SubredditMention[]= null;

  constructor(@Inject(PLATFORM_ID) private platformId, private zone: NgZone, private dataService: SparkDataService, private sparkStreamHander: SparkStreamHandlerService) { }

  randomIdValueString: string;
  browserOnly(f: (donutChartComponent: DonutChartComponent) => void) {
    if (isPlatformBrowser(this.platformId)) {
      this.zone.runOutsideAngular(() => {
        f(this);
      });
    }
  }

  ngAfterViewInit() {
    // Chart code goes in here
    this.browserOnly((donutChartComponent) => {
      /* Chart code */
      // Themes begin
      am4core.useTheme(am4themes_animated);
      // Themes end

      // Create chart instance
      donutChartComponent.chart = am4core.create("donutchart_" + donutChartComponent.randomIdValueString, am4charts.PieChart);

      // Add data
      donutChartComponent.chart.data = [];

      // Set inner radius
      donutChartComponent.chart.innerRadius = am4core.percent(50);

      // Add and configure Series
      let pieSeries = donutChartComponent.chart.series.push(new am4charts.PieSeries());
      pieSeries.dataFields.value = "count";
      pieSeries.dataFields.category = "subreddit";
      pieSeries.slices.template.stroke = am4core.color("#fff");
      pieSeries.slices.template.strokeWidth = 2;
      pieSeries.slices.template.strokeOpacity = 1;

      // This creates initial animation
      pieSeries.hiddenState.properties.opacity = 1;
      pieSeries.hiddenState.properties.endAngle = -90;
      pieSeries.hiddenState.properties.startAngle = -90;

       donutChartComponent.chart.setTimeout(setNewData, 2000);

      function setNewData() {
        //console.log("Data: " + JSON.stringify(popularCommunitiesComponent.currentData));
        donutChartComponent.chart.data = donutChartComponent.currentData;
        donutChartComponent.chart.setTimeout(setNewData, donutChartComponent.refreshInterval);
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
    this.randomIdValueString = `` + DonutChartComponent.counterId;
    DonutChartComponent.counterId++;
    this.dataObservable.subscribe(obj => {

      this.currentData = obj;
        //PopularCommunitiesComponent.chart.data = obj;
    });
  }
}
