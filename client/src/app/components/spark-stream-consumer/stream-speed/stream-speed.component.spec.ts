import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StreamSpeedComponent } from './stream-speed.component';

describe('StreamSpeedComponent', () => {
  let component: StreamSpeedComponent;
  let fixture: ComponentFixture<StreamSpeedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StreamSpeedComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StreamSpeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
