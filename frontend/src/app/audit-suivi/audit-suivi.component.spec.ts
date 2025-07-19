import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditSuiviComponent } from './audit-suivi.component';

describe('AuditSuiviComponent', () => {
  let component: AuditSuiviComponent;
  let fixture: ComponentFixture<AuditSuiviComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AuditSuiviComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuditSuiviComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
