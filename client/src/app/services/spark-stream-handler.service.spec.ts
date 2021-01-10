import { TestBed } from '@angular/core/testing';

import { SparkStreamHandlerService } from './spark-stream-handler.service';

describe('SparkStreamHandlerService', () => {
  let service: SparkStreamHandlerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SparkStreamHandlerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
