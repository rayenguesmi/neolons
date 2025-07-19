import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionDocComponent } from './gestion-doc.component';

describe('GestionDocComponent', () => {
  let component: GestionDocComponent;
  let fixture: ComponentFixture<GestionDocComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GestionDocComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionDocComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
