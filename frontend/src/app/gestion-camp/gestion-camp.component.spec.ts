import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionCampComponent } from './gestion-camp.component';

describe('GestionCampComponent', () => {
  let component: GestionCampComponent;
  let fixture: ComponentFixture<GestionCampComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GestionCampComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionCampComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
