import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-audit-suivi',
  templateUrl: './audit-suivi.component.html',
  styleUrls: ['./audit-suivi.component.css'],
})
export class AuditSuiviComponent implements OnInit {
  activeMenuId: number | null = null;
  menuVisible: boolean = false;
  constructor() {}

  ngOnInit(): void {}
  toggleMenu(event: Event, id: number): void {
    event.stopPropagation();
    this.menuVisible = !this.menuVisible || this.activeMenuId !== id;
    this.activeMenuId = this.menuVisible ? id : null;
  }
}
