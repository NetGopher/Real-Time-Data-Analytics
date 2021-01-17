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
import * as am4plugins_ForceDirected from "@amcharts/amcharts4/plugins/forceDirected";
import {isPlatformBrowser} from "@angular/common";
import * as am4charts from "@amcharts/amcharts4/charts";
import {DataService} from "../../../services/data.service";
import {KafkaStreamHandlerService} from "../../../services/kafka-stream-handler.service";
import {Observable} from "rxjs";
import * as am4plugins_wordCloud from "@amcharts/amcharts4/plugins/wordCloud";

import {WordCloudSeries} from "@amcharts/amcharts4/plugins/wordCloud";
import {KeyValuePair} from "../../../other/Entities";
import {SpriteState} from "@amcharts/amcharts4/core";

@Component({
  selector: 'app-force-directed-network',
  templateUrl: './force-directed-network.component.html',
  styleUrls: ['./force-directed-network.component.css']
})
export class ForceDirectedNetworkComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input('dataObservable')
  public dataObservable: Observable<any[]>;
  @Input('categoryProperty')
  public categoryProperty: string = "key"
  @Input('valueProperty')
  public valueProperty: string = "value"
  @Input('refreshInterval')
  public refreshInterval: number = 5000; //MILLISECOND | OPTIONAL
  @Input('transitionDuration')
  public transitionDuration: number = 2500; //MILLISECOND | OPTIONAL
  @Input('initialData')
  public newData: any[] = [];
  @Input('title')
  public Title: string = "Circle";
  @Input('forceRefresh')
  public forceRefresh: boolean = false;
  public static counterId: number = 0;

  public triggerChange: boolean = true;
  public chart: am4plugins_ForceDirected.ForceDirectedTree;
  public networkSeries: am4plugins_ForceDirected.ForceDirectedSeries;
  private nodeTemplate: am4plugins_ForceDirected.ForceDirectedSeries["_node"];
  private linkTemplate: am4plugins_ForceDirected.ForceDirectedSeries["_link"];
  private linkHoverState: SpriteState<am4plugins_ForceDirected.ForceDirectedLink["_properties"], am4plugins_ForceDirected.ForceDirectedLink["_adapter"]>;
  private dataItem: any;

  constructor(@Inject(PLATFORM_ID) private platformId, private zone: NgZone, private dataService: DataService, private kafkaStreamHander: KafkaStreamHandlerService) {
  }

  randomIdValueString: string;

  browserOnly(f: (myComponent: ForceDirectedNetworkComponent) => void) {
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

      ctx.chart = am4core.create("force_directed_network_" + ctx.randomIdValueString, am4plugins_ForceDirected.ForceDirectedTree);

      ctx.networkSeries = ctx.chart.series.push(new am4plugins_ForceDirected.ForceDirectedSeries())
      ctx.networkSeries.dataFields.linkWith = "linkWith";
      ctx.networkSeries.dataFields.name = "key";
      ctx.networkSeries.dataFields.id = "key";
      ctx.networkSeries.dataFields.value = "value";
      ctx.networkSeries.dataFields.children = "children";

      ctx.networkSeries.nodes.template.label.text = "{name}"
      ctx.networkSeries.fontSize = 8;
      ctx.networkSeries.linkWithStrength = 0;

      ctx.nodeTemplate = ctx.networkSeries.nodes.template;
      ctx.nodeTemplate.tooltipText = "{name}";
      ctx.nodeTemplate.fillOpacity = 1;
      ctx.nodeTemplate.label.hideOversized = true;
      ctx.nodeTemplate.label.truncate = true;

      ctx.linkTemplate = ctx.networkSeries.links.template;
      ctx.linkTemplate.strokeWidth = 1;
      ctx.linkHoverState = ctx.linkTemplate.states.create("hover");
      ctx.linkHoverState.properties.strokeOpacity = 1;
      ctx.linkHoverState.properties.strokeWidth = 2;

      ctx.nodeTemplate.events.on("over", function (event) {
        ctx.dataItem = event.target.dataItem;
        ctx.dataItem.childLinks.each(function (link) {
          link.isHover = true;
        })
      })

      ctx.nodeTemplate.events.on("out", function (event) {
        ctx.dataItem = event.target.dataItem;
        ctx.dataItem.childLinks.each(function (link) {
          link.isHover = false;
        })
      })

      ctx.dataObservable.subscribe(value => {
        if (!value) return;
        ctx.newData = value;
      })

      ctx.networkSeries.data = ctx.newData;

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
    this.randomIdValueString = `` + ForceDirectedNetworkComponent.counterId;
    ForceDirectedNetworkComponent.counterId++;
    this.categoryProperty = this.titleCaseWord(this.categoryProperty);

  }


  refreshData() {
    console.log("data update")
    this.networkSeries.data = [this.newData]

  }
}
