import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionLicencesComponent } from './gestion-licences.component';

describe('GestionLicencesComponent', () => {
  let component: GestionLicencesComponent;
  let fixture: ComponentFixture<GestionLicencesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GestionLicencesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionLicencesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
