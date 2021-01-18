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
import * as am4core from "@amcharts/amcharts4/core";
import * as am4charts from "@amcharts/amcharts4/charts";
import am4themes_animated from "@amcharts/amcharts4/themes/animated";
import {Observable} from "rxjs";
import {isPlatformBrowser} from "@angular/common";
import {PostsPerDuration, PostsPerMinuteItem} from "../../../other/Entities";
import {CategoryAxis, ColumnSeries, ValueAxis, XYChart} from "@amcharts/amcharts4/charts";

@Component({
  selector: 'app-column',
  templateUrl: './column.component.html',
  styleUrls: ['./column.component.css']
})
export class ColumnComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input('dataObservable')
  public dataObservable: Observable<any>;
  @Input('refreshInterval')
  public refreshInterval: number = 2000; //MILLISECOND
  @Input('transitionDuration')
  public transitionDuration: number = 1000; //MILLISECOND

  @Input('initialData')
  public newData: any[] = [];
  public chart: XYChart;
  public static counterId: number = 0;

  private triggerChange: boolean = false;
  columnComponent: PostsPerDuration;
  private categoryAxis: CategoryAxis;
  private valueAxis: ValueAxis;
  private series: ColumnSeries;
  private columnTemplate: ColumnSeries["_column"];

  constructor(@Inject(PLATFORM_ID) private platformId, private zone: NgZone) {
  }

  randomIdValueString: string;

  browserOnly(f: (ctx: ColumnComponent) => void) {
    if (isPlatformBrowser(this.platformId)) {
      this.zone.runOutsideAngular(() => {
        f(this);
      });
    }
  }

  ngAfterViewInit() {
    this.browserOnly((ctx) => {
      /* Chart code */
      // Themes begin
      am4core.useTheme(am4themes_animated);
      // Themes end

      // Create chart instance
      ctx.chart = am4core.create("column_chart_" + ctx.randomIdValueString, am4charts.XYChart);

      // Add data
      if (ctx.newData.length > 0 && ctx.newData[0] != null)
        ctx.chart.data = ctx.newData;
      else ctx.chart.data = []

      // Create axes

      ctx.categoryAxis = ctx.chart.xAxes.push(new am4charts.CategoryAxis());
      ctx.categoryAxis.dataFields.category = "time";
      ctx.categoryAxis.renderer.grid.template.location = 0;
      ctx.categoryAxis.renderer.minGridDistance = 30;

      ctx.categoryAxis.renderer.labels.template.adapter.add("dy", function (dy, target) {
        // @ts-ignore
        if (target.dataItem && target.dataItem.index & 2 == 2) {
          return dy + 25;
        }
        return dy;
      });

      ctx.valueAxis = ctx.chart.yAxes.push(new am4charts.ValueAxis());

      // Create series
      ctx.series = ctx.chart.series.push(new am4charts.ColumnSeries());
      ctx.series.dataFields.valueY = "count";
      ctx.series.dataFields.categoryX = "time";
      ctx.series.name = "Count";
      ctx.series.columns.template.tooltipText = "{categoryX}: [bold]{valueY}[/]";
      ctx.series.columns.template.fillOpacity = .8;

      ctx.columnTemplate = ctx.series.columns.template;
      ctx.columnTemplate.strokeWidth = 2;
      ctx.columnTemplate.strokeOpacity = 1;

      ctx.dataObservable.subscribe((newValue: PostsPerDuration) => {
        if (newValue) {
          console.log('New PostsPerMinute Value: ' + newValue);
          //ctx.newData = ctx.newData.shift()
          if (ctx.newData.length >= 4){

            ctx.newData.shift()
          }
          ctx.newData.push(newValue)
          ctx.chart.data = ctx.newData;

        }

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
    this.randomIdValueString = `` + ColumnComponent.counterId;
    ColumnComponent.counterId++;
    if (this.newData.length == 1 && !this.newData[0]) {
      this.newData = [];
    }

  }

}
