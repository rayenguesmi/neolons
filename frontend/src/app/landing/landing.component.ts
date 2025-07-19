import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { ContextService } from '../services/context.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css'],
})
export class LandingComponent implements OnInit {
  isCollapsed = false; // Gère l'état du hamburger
  currentRoute: string = ''; // Suivi de la route active
  data1: string = 'Example Data'; // Données à afficher
  data2: string = new Date().toLocaleString(); // Heure actuelle
  error: string = ''; // Message d'erreur
  isNavbarVisible: boolean = true;

  // Dropdowns for detailed sidebar
  dropdowns: { [key: string]: boolean } = {
    admin: false,
    docs: false,
    campagnes: false,
    executions: false,
    dashboard: false,
    assistant: false,
    connecteurs: false
  };

  subMenus: { [key: string]: boolean } = {
    administration: false, // Sous-menu pour Administration
  };

  constructor(
    private keycloakService: KeycloakService,
    private contextService: ContextService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Track current route
    this.currentRoute = this.router.url;
    
    // Subscribe to router events to update current route
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.currentRoute = event.url;
      }
    });
  }

  logout() {
    this.keycloakService.logout();
    window.location.reload();
  }

  // Méthode pour basculer l'état du Navbar
  toggleNavbar() {
    this.isCollapsed = !this.isCollapsed;
  }

  // Navigation method for detailed sidebar
  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  // Toggle dropdown for detailed sidebar
  toggleDropdown(name: string): void {
    this.dropdowns[name] = !this.dropdowns[name];
  }

  // Méthode pour afficher le bouton hamburger
  shouldShowHamburger(): boolean {
    const routesWithNavbar = [
      '/home',
      '/g-de-campagne',
      '/g-dexecution',
      '/gestionnaire-de-doc',
      '/administration',
      '/gestion-utilisateur',
      '/gestion-licences',
      '/audit-suivi'
    ];
    return routesWithNavbar.includes(this.router.url);
  }

  // Vérifie si la Navbar doit être affichée
  shouldShowNavbar(): boolean {
    const navbarRoutes = [
      '/home',
      '/g-de-campagne',
      '/g-dexecution',
      '/gestionnaire-de-doc',
      '/administration',
      '/gestion-licences',
      '/gestion-utilisateur',
      '/audit-suivi',
    ];
    return navbarRoutes.includes(this.router.url) && !this.isCollapsed;
  }

  // Basculer l'affichage du sous-menu (Administration)
  toggleSubMenu(menu: string) {
    this.subMenus[menu] = !this.subMenus[menu];
  }
}