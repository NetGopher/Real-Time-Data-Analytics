import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PopularCommunitiesComponent } from './popular-communities.component';

describe('PopularCommunitiesComponent', () => {
  let component: PopularCommunitiesComponent;
  let fixture: ComponentFixture<PopularCommunitiesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PopularCommunitiesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PopularCommunitiesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
