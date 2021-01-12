import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HBarComponent } from './h-bar.component';

describe('HBarComponent', () => {
  let component: HBarComponent;
  let fixture: ComponentFixture<HBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HBarComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
