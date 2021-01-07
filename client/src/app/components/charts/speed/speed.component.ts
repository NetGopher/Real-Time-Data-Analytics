import {AfterViewInit, Component, Inject, NgZone, OnDestroy, OnInit, PLATFORM_ID} from '@angular/core';
import * as am4core from '@amcharts/amcharts4/core';
import * as am4charts from '@amcharts/amcharts4/charts';
import am4themes_animated from '@amcharts/amcharts4/themes/animated';
import {isPlatformBrowser} from "@angular/common";
import {Axis} from "@amcharts/amcharts4/charts";
import {DataService} from "../../../services/data.service";

@Component({
  selector: 'app-speed',
  templateUrl: './speed.component.html',
  styleUrls: ['./speed.component.css']
})
export class SpeedComponent implements OnInit, AfterViewInit,OnDestroy {

  private chart: am4charts.XYChart;
  private refreshInterval: number = 2000; //MILLISECOND
  private transitionDuration: number = 1000; //MILLISECOND
  private hand: any;
  constructor(@Inject(PLATFORM_ID) private platformId, private zone: NgZone, private dataService: DataService) {
  }

  // Run the function only in the browser
  browserOnly(f: () => void) {
    if (isPlatformBrowser(this.platformId)) {
      this.zone.runOutsideAngular(() => {
        f();
      });
    }
  }

  ngAfterViewInit() {

    // Chart code goes in here
    this.browserOnly(() => {
      am4core.useTheme(am4themes_animated);
// Themes end

// create chart
      this.chart = am4core.create("chartdiv", am4charts.GaugeChart);
      this.chart.hiddenState.properties.opacity = 0; // this makes initial fade in effect

      // @ts-ignore
      this.chart.innerRadius = -25;

      let axis: any = this.chart.xAxes.push(new am4charts.ValueAxis());
      axis.min = 0;
      axis.max = 100;
      axis.strictMinMax = true;
      axis.renderer.grid.template.stroke = new am4core.InterfaceColorSet().getFor("background");
      axis.renderer.grid.template.strokeOpacity = 0.3;

      let colorSet = new am4core.ColorSet();

      let range0: any = axis.axisRanges.create();
      range0.value = 0;
      range0.endValue = 50;
      range0.axisFill.fillOpacity = 1;
      range0.axisFill.fill = colorSet.getIndex(0);
      range0.axisFill.zIndex = -1;

      let range1: any = axis.axisRanges.create();
      range1.value = 50;
      range1.endValue = 80;
      range1.axisFill.fillOpacity = 1;
      range1.axisFill.fill = colorSet.getIndex(2);
      range1.axisFill.zIndex = -1;

      let range2: any = axis.axisRanges.create();
      range2.value = 80;
      range2.endValue = 100;
      range2.axisFill.fillOpacity = 1;
      range2.axisFill.fill = colorSet.getIndex(4);
      range2.axisFill.zIndex = -1;

      // @ts-ignore
      this.hand = this.chart.hands.push(new am4charts.ClockHand());

// using this.chart.setTimeout method as the timeout will be disposed together with a chart
      this.chart.setTimeout(this.refresh, this.refreshInterval);

    });
  }

  refresh() {
    this.hand.showValue(this.dataService.numberOfPosts, this.transitionDuration, am4core.ease.cubicOut);
    this.chart.setTimeout(this.refresh, this.refreshInterval);
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
  }
}
