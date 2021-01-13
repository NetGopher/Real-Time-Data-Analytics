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
import {PostsPerMinuteItem} from "../../../other/Entities";
@Component({
  selector: 'app-column',
  templateUrl: './column.component.html',
  styleUrls: ['./column.component.css']
})
export class ColumnComponent implements OnInit {
  @Input('dataObservable')
  public dataObservable: Observable<any>;
  @Input('refreshInterval')
  public refreshInterval: number = 2000; //MILLISECOND
  @Input('transitionDuration')
  public transitionDuration: number = 1000; //MILLISECOND

  @Input('initialData')
  public newData: any[] = [];
  private chart: any;
  public static counterId: number = 0;

  private triggerChange: boolean = false;
  columnComponent: PostsPerMinuteItem;

  constructor(@Inject(PLATFORM_ID) private platformId, private zone: NgZone) { }

  randomIdValueString: string;

  browserOnly(f: (columnComponent: ColumnComponent) => void) {
    if (isPlatformBrowser(this.platformId)) {
      this.zone.runOutsideAngular(() => {
        f(this);
      });
    }
  }

  ngAfterViewInit() {
    this.browserOnly((columnComponent) => {
      /* Chart code */
      // Themes begin
      am4core.useTheme(am4themes_animated);
      // Themes end

      // Create chart instance
      columnComponent.chart = am4core.create("column_chart_" + columnComponent.randomIdValueString, am4charts.XYChart);

      // Add data
      columnComponent.chart.data = columnComponent.newData;

      // Create axes

      let categoryAxis = columnComponent.chart.xAxes.push(new am4charts.CategoryAxis());
      categoryAxis.dataFields.category = "time";
      categoryAxis.renderer.grid.template.location = 0;
      categoryAxis.renderer.minGridDistance = 30;

      categoryAxis.renderer.labels.template.adapter.add("dy", function(dy, target) {
        // @ts-ignore
        if (target.dataItem && target.dataItem.index & 2 == 2) {
          return dy + 25;
        }
        return dy;
      });

      let valueAxis = columnComponent.chart.yAxes.push(new am4charts.ValueAxis());

      // Create series
      let series = columnComponent.chart.series.push(new am4charts.ColumnSeries());
      series.dataFields.valueY = "count";
      series.dataFields.categoryX = "time";
      series.name = "Count";
      series.columns.template.tooltipText = "{categoryX}: [bold]{valueY}[/]";
      series.columns.template.fillOpacity = .8;

      let columnTemplate = series.columns.template;
      columnTemplate.strokeWidth = 2;
      columnTemplate.strokeOpacity = 1;

      columnComponent.dataObservable.subscribe((newValue: PostsPerMinuteItem) => {
        if(newValue){
          console.log('New PostsPerMinute Value: ' + JSON.stringify(newValue));
          //columnComponent.newData = columnComponent.newData.shift()
          if(columnComponent.newData.length === 3)
            columnComponent.newData.shift()
          columnComponent.newData.push(newValue)
          columnComponent.chart.data = columnComponent.newData;
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

  }

}
