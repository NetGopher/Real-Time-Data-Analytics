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
import am4themes_animated from "@amcharts/amcharts4/themes/animated";
import {isPlatformBrowser} from "@angular/common";
import {DataService} from "../../../services/data.service";
import {KafkaStreamHandlerService} from "../../../services/kafka-stream-handler.service";
import {Observable} from "rxjs";
import * as am4plugins_wordCloud from "@amcharts/amcharts4/plugins/wordCloud";

import {WordCloudSeries} from "@amcharts/amcharts4/plugins/wordCloud";
import {KeyValuePair} from "../../../other/Entities";


@Component({
  selector: 'app-circle',
  templateUrl: './circle.component.html',
  styleUrls: ['./circle.component.css']
})
export class CircleComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input('dataObservable')
  public dataObservable: Observable<any[]>;
  @Input('categoryProperty')
  public categoryProperty: string = "key"
  @Input('valueProperty')
  public valueProperty: string = "value"
  @Input('refreshInterval')
  public refreshInterval: number = 5000; //MILLISECOND | OPTIONAL
  @Input("capacityType")
  public capacityType: string = "Object"
  @Input("targetCategory")
  public targetCategory: string = "NSFW"
  // OPTIONAL | type of the capacity (Posts, Litres, ...)
  @Input('transitionDuration')
  public transitionDuration: number = 2500; //MILLISECOND | OPTIONAL
  @Input('initialData')
  public newData: any[] = [];
  @Input('title')
  public Title: string = "Circle";
  @Input('forceRefresh')
  public forceRefresh: boolean = false;
  private chart: am4plugins_wordCloud.WordCloud;
  private series: WordCloudSeries;
  public static counterId: number = 0;

  private capacity: number;
  private value: number;
  private circleSize: number;
  private component: am4core.Container;
  private chartContainer: am4core.Container;
  private circle: am4core.Circle;
  private circleMask: am4core.Circle;
  private waves: am4core.WavedRectangle;
  private smallerSize: number;
  private radius: number;
  private labelRadius: number;
  private capacityLabel: any;
  private label: am4core.Label;
  private formattedValue: string;
  private formattedCapacity: string;
  public triggerChange: boolean = true;

  constructor(@Inject(PLATFORM_ID) private platformId, private zone: NgZone) {
  }

  randomIdValueString: string;

  browserOnly(f: (myComponent: CircleComponent) => void) {
    if (isPlatformBrowser(this.platformId)) {
      this.zone.runOutsideAngular(() => {
        f(this);
      });
    }
  }

  ngAfterViewInit() {
    // Chart code goes in here
    this.browserOnly((ctx) => {
      am4core.useTheme(am4themes_animated);
// Themes end

      ctx.capacity = 100;
      ctx.value = 0;
      ctx.circleSize = 0.8;

      ctx.component = am4core.create("circle_chart_" + ctx.randomIdValueString, am4core.Container)
      ctx.component.width = am4core.percent(100);
      ctx.component.height = am4core.percent(100);

      ctx.chartContainer = this.component.createChild(am4core.Container);
      ctx.chartContainer.x = am4core.percent(50)
      ctx.chartContainer.y = am4core.percent(50)

      ctx.circle = ctx.chartContainer.createChild(am4core.Circle);
      ctx.circle.fill = am4core.color("#dadada");

      ctx.circleMask = ctx.chartContainer.createChild(am4core.Circle);
      let gradient = new am4core.LinearGradient();
      // let gradient = new am4core.RadialGradient();
      gradient.addColor(am4core.color("cyan"));
      gradient.addColor(am4core.color("cyan"));
      gradient.addColor(am4core.color("cyan"));
      gradient.addColor(am4core.color("cyan"));
      gradient.addColor(am4core.color("cyan"));
      gradient.addColor(am4core.color("green"));
      gradient.addColor(am4core.color("orange"));
      gradient.addColor(am4core.color("red"));
      gradient.rotation= -90;
      ctx.waves = ctx.chartContainer.createChild(am4core.WavedRectangle);
      ctx.waves.fill = gradient
      ctx.waves.mask = ctx.circleMask;
      ctx.waves.horizontalCenter = "middle";
      ctx.waves.waveHeight = 10;
      ctx.waves.waveLength = 30;
      ctx.waves.y = 500;
      ctx.circleMask.y = -500;

      ctx.component.events.on("maxsizechanged", function () {
        ctx.smallerSize = Math.min(ctx.component.pixelWidth, ctx.component.pixelHeight);
        ctx.radius = ctx.smallerSize * ctx.circleSize / 2;

        ctx.circle.radius = ctx.radius;
        ctx.circleMask.radius = ctx.radius;
        ctx.waves.height = ctx.smallerSize;
        ctx.waves.width = Math.max(ctx.component.pixelWidth, ctx.component.pixelHeight);

        //capacityLabel.y = radius;

        ctx.labelRadius = ctx.radius + 20

        ctx.capacityLabel.path = am4core.path.moveTo({x: -ctx.labelRadius, y: 0}) + am4core.path.arcToPoint({
          x: ctx.labelRadius,
          y: 0
        }, ctx.labelRadius, ctx.labelRadius);
        ctx.capacityLabel.locationOnPath = 0.5;

        setValue(ctx.value);
      })


      function setValue(value) {
        let y = -ctx.circle.radius - ctx.waves.waveHeight + (1 - value / ctx.capacity) * ctx.circle.pixelRadius * 2;
        ctx.waves.animate([{property: "y", to: y}, {property: "waveHeight", to: 10, from: 15}, {
          property: "x",
          from: -50,
          to: 0
        }], ctx.transitionDuration, am4core.ease.elasticOut);
        ctx.circleMask.animate([{property: "y", to: -y}, {
          property: "x",
          from: 50,
          to: 0
        }], ctx.transitionDuration, am4core.ease.elasticOut);
      }


      ctx.label = ctx.chartContainer.createChild(am4core.Label)
      ctx.formattedValue = ctx.component.numberFormatter.format(ctx.value, "#.#a");
      ctx.formattedValue = ctx.formattedValue.toUpperCase();

      ctx.label.text = ctx.formattedValue + " " + ctx.targetCategory + " " + ctx.capacityType + "s";
      ctx.label.fill = am4core.color("#fff");
      ctx.label.fontSize = 25;
      ctx.label.horizontalCenter = "middle";


      ctx.capacityLabel = ctx.chartContainer.createChild(am4core.Label)

      ctx.formattedCapacity = ctx.component.numberFormatter.format(ctx.capacity, "#.#a").toUpperCase();

      ctx.capacityLabel.text = "Capacity " + ctx.formattedCapacity + " " + ctx.capacityType + "s";
      ctx.capacityLabel.fill = am4core.color("#34a4eb");
      ctx.capacityLabel.fontSize = 20;
      ctx.capacityLabel.textAlign = "middle";
      ctx.capacityLabel.padding(0, 0, 0, 0);
      ctx.component.setTimeout(refresh, ctx.refreshInterval);
      //Subscribing to stream
      ctx.dataObservable.subscribe(values => {
          if (!values) {
            console.log("nope")
            return;
          }
          if (JSON.stringify(values) != JSON.stringify(this.newData)) {
            ctx.triggerChange = true;
            ctx.newData = values;
            let data: any[] = values
            let total: number = 0
            for (let value of data) {
              total += Number(value[ctx.valueProperty])
              if (value.key.toLowerCase() == ctx.targetCategory.toLowerCase()) {
                ctx.value = Number(value[ctx.valueProperty]);
              }
              ctx.capacity = total;
              ctx.value = ctx.capacity - 10
            }
          }
        }
      )

      function refresh() {
        if (ctx.triggerChange) {
          ctx.triggerChange = false;
          ctx.formattedCapacity = ctx.component.numberFormatter.format(ctx.capacity, "#.#a").toUpperCase();
          ctx.capacityLabel.text = "Capacity " + ctx.formattedCapacity + " " + ctx.capacityType + "(s)";
          ctx.formattedValue = ctx.component.numberFormatter.format(ctx.value, "#.#a");
          ctx.formattedValue = ctx.formattedValue.toUpperCase();
          ctx.label.text = ctx.formattedValue + " " + ctx.targetCategory + " " +  ctx.capacityType + "(s)";
          ctx.component.events.dispatchImmediately("maxsizechanged", null);
          ctx.component.setTimeout(refresh, ctx.refreshInterval);
        } else ctx.component.setTimeout(refresh, 1000);
      }
    });

  }

  titleCaseWord(word: string) {
    if (!word) return word;
    return word[0].toUpperCase() + word.substr(1).toLowerCase();
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
    this.randomIdValueString = `` + CircleComponent.counterId;
    CircleComponent.counterId++;
    this.categoryProperty = this.titleCaseWord(this.categoryProperty);

  }

  onClick() {
    this.series.data = this.newData;
  }

}
