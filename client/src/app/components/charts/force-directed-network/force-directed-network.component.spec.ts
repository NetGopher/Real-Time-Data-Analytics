import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ForceDirectedNetworkComponent } from './force-directed-network.component';

describe('ForceDirectedNetworkComponent', () => {
  let component: ForceDirectedNetworkComponent;
  let fixture: ComponentFixture<ForceDirectedNetworkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ForceDirectedNetworkComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ForceDirectedNetworkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
