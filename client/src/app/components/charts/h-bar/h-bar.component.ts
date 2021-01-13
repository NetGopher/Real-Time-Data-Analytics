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
@Component({
  selector: 'app-h-bar',
  templateUrl: './h-bar.component.html',
  styleUrls: ['./h-bar.component.css']
})
export class HBarComponent implements OnInit, OnDestroy, AfterViewInit {

  @Input('dataObservable')
  public dataObservable:Observable<any>;
  private chart: am4charts.XYChart;
  @Input('refreshInterval')
  public refreshInterval: number = 2000; //MILLISECOND
  @Input('transitionDuration')
  public transitionDuration: number = 1000; //MILLISECOND
  public newData: any[] = [];
  public static counterId:number = 0;
  public currentData: SubredditMention[]= null;


  private triggerChange: boolean = false;


  constructor(@Inject(PLATFORM_ID) private platformId, private zone: NgZone) { }

  randomIdValueString: string;
  browserOnly(f: (hBarComponent: HBarComponent) => void) {
    if (isPlatformBrowser(this.platformId)) {
      this.zone.runOutsideAngular(() => {
        f(this);
      });
    }
  }

  ngAfterViewInit() {
    // Chart code goes in here
    this.browserOnly((hBarComponent) => {
      /* Chart code */
      // Themes begin
      am4core.useTheme(am4themes_animated);
      // Themes end

      hBarComponent.chart = am4core.create("hBarChartdiv_" + hBarComponent.randomIdValueString, am4charts.XYChart);
      hBarComponent.chart.padding(40, 40, 40, 40);

      let categoryAxis = hBarComponent.chart.yAxes.push(new am4charts.CategoryAxis());
      categoryAxis.renderer.grid.template.location = 0;
      categoryAxis.dataFields.category = "subreddit";
      categoryAxis.renderer.minGridDistance = 1;
      categoryAxis.renderer.inversed = true;
      categoryAxis.renderer.grid.template.disabled = true;

      let valueAxis = hBarComponent.chart.xAxes.push(new am4charts.ValueAxis());
      valueAxis.min = 0;

      let series = hBarComponent.chart.series.push(new am4charts.ColumnSeries());
      series.dataFields.categoryY = "subreddit";
      series.dataFields.valueX = "count";
      series.tooltipText = "{valueX.value}"
      series.columns.template.strokeOpacity = 0;
      series.columns.template.column.cornerRadiusBottomRight = 5;
      series.columns.template.column.cornerRadiusTopRight = 5;

      let labelBullet = series.bullets.push(new am4charts.LabelBullet())
      labelBullet.label.horizontalCenter = "left";
      labelBullet.label.dx = 10;
      labelBullet.label.text = "{values.valueX.workingValue}";
      labelBullet.locationX = 1;

      // as by default columns of the same series are of the same color, we add adapter which takes colors from chart.colors color set
      series.columns.template.adapter.add("fill", function (fill, target) {
        return hBarComponent.chart.colors.getIndex(target.dataItem.index);
      });

      categoryAxis.sortBySeries = series;
      hBarComponent.chart.data = hBarComponent.newData;

      hBarComponent.chart.setTimeout(setNewData, hBarComponent.refreshInterval);
      hBarComponent.dataObservable.subscribe((values) => {
         if (!values) {
          hBarComponent.triggerChange = false;
          return;
        }
         if (JSON.stringify(values) != JSON.stringify(hBarComponent.newData)) {
          hBarComponent.triggerChange = true;
          hBarComponent.newData = values != null ?  hBarComponent.currentData.slice(1, 6) : null;
        }else hBarComponent.triggerChange = false;
      });

      function setNewData() {
        if(hBarComponent.triggerChange){
          hBarComponent.triggerChange = false;
          series.hide(hBarComponent.transitionDuration / 2);
          setTimeout(() => {
            hBarComponent.chart.data = hBarComponent.newData != null ?  hBarComponent.currentData.slice(1, 6) : null;
            // pieSeries.show(hBarComponent.transitionDuration);
            series.show(hBarComponent.transitionDuration / 2);
            hBarComponent.chart.setTimeout(setNewData, hBarComponent.refreshInterval);
          }, hBarComponent.transitionDuration / 2 + 1000);
        } else
          hBarComponent.chart.setTimeout(setNewData, hBarComponent.refreshInterval);
        //
      }
        //console.log("Data: " + JSON.stringify(hBarComponent.currentData));
        //hBarComponent.chart.data = hBarComponent.currentData != null ?  hBarComponent.currentData.slice(1, 6) : null;
        //hBarComponent.chart.setTimeout(setNewData, hBarComponent.refreshInterval);
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
    this.randomIdValueString = `` + HBarComponent.counterId;
    HBarComponent.counterId++;
    this.dataObservable.subscribe(obj => {

      this.currentData = obj;
        //HBarComponent.chart.data = obj;
    });
  }

}
