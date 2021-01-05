import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KafkaStreamConsumerComponent } from './kafka-stream-consumer.component';

describe('KafkaStreamConsumerComponent', () => {
  let component: KafkaStreamConsumerComponent;
  let fixture: ComponentFixture<KafkaStreamConsumerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KafkaStreamConsumerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KafkaStreamConsumerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
