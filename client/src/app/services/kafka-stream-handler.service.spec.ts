import { TestBed } from '@angular/core/testing';

import { KafkaStreamHandlerService } from './kafka-stream-handler.service';

describe('KafkaStreamHandlerService', () => {
  let service: KafkaStreamHandlerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KafkaStreamHandlerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
