import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionExecComponent } from './gestion-exec.component';

describe('GestionExecComponent', () => {
  let component: GestionExecComponent;
  let fixture: ComponentFixture<GestionExecComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GestionExecComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionExecComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
