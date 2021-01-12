import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TagCloudChartComponent } from './tag-cloud-chart.component';

describe('TagCloudChartComponent', () => {
  let component: TagCloudChartComponent;
  let fixture: ComponentFixture<TagCloudChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TagCloudChartComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TagCloudChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
