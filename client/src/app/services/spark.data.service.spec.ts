import { TestBed } from '@angular/core/testing';

import { SparkDataService } from './spark.data.service';

describe('Spark.DataService', () => {
  let service: SparkDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SparkDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
