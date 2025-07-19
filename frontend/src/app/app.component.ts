import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'frontend';
  currentView = 'home'; // Vue par défaut

  // Ajoute ceci pour gérer l'état des dropdowns
  dropdowns: { [key: string]: boolean } = {
    admin: false,
    docs: false,
    campagnes: false,
    executions: false,
    dashboard: false,
    assistant: false,
    connecteurs: false
  };

  constructor(private router: Router) {}

  setCurrentView(view: string): void {
    this.currentView = view;
    this.router.navigate([view]);
  }

  // Méthode pour ouvrir/fermer le menu déroulant
  toggleDropdown(name: string): void {
    this.dropdowns[name] = !this.dropdowns[name];
  }
}