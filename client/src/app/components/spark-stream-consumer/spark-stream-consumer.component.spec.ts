import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SparkStreamConsumerComponent } from './spark-stream-consumer.component';

describe('SparkStreamConsumerComponent', () => {
  let component: SparkStreamConsumerComponent;
  let fixture: ComponentFixture<SparkStreamConsumerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SparkStreamConsumerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SparkStreamConsumerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
