import { Component, OnInit, OnDestroy } from '@angular/core';
import { DashboardService, DashboardStats } from '../services/dashboard.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {

  currentDate: Date = new Date();
  private dashboardSubscription: Subscription = new Subscription();

  // Statistiques générales - maintenant dynamiques
  totalUsers = 0;
  activeUsers = 0;
  totalDocuments = 0;
  totalCampaigns = 0;
  
  // Données de gestion des utilisateurs - maintenant dynamiques
  userStats = {
    administrators: 0,
    managers: 0,
    standardUsers: 0,
    newUsersThisMonth: 0,
    activeSessionsToday: 0
  };

  // Données de journalisation - maintenant dynamiques
  auditStats = {
    totalActionsToday: 0,
    creations: 0,
    modifications: 0,
    deletions: 0,
    consultations: 0,
    securityAlerts: 0,
    failedLogins: 0
  };

  // Données de licences - maintenant dynamiques
  licenseStats = {
    totalLicenses: 0,
    activeLicenses: 0,
    expiringSoon: 0,
    expired: 0,
    utilizationRate: 0
  };

  // Nouvelles données de rôles - dynamiques
  roleStats = {
    totalRoles: 0,
    activeRoles: 0,
    customRoles: 0
  };

  // Données pour les graphiques
  userActivityData = [
    { name: 'Lun', value: 45 },
    { name: 'Mar', value: 52 },
    { name: 'Mer', value: 48 },
    { name: 'Jeu', value: 61 },
    { name: 'Ven', value: 55 },
    { name: 'Sam', value: 23 },
    { name: 'Dim', value: 18 }
  ];

  documentTypeData = [
    { name: 'Contrats', value: 35, color: '#3B82F6' },
    { name: 'Rapports', value: 28, color: '#10B981' },
    { name: 'Factures', value: 20, color: '#F59E0B' },
    { name: 'Autres', value: 17, color: '#EF4444' }
  ];

  recentActivities = [
    {
      user: 'admin@neolons.com',
      action: 'Création de document',
      resource: 'Contrat_2024.pdf',
      time: '14:30',
      status: 'success'
    },
    {
      user: 'manager@neolons.com',
      action: 'Modification utilisateur',
      resource: 'Jean Dupont',
      time: '14:25',
      status: 'success'
    },
    {
      user: 'eya@neolons.com',
      action: 'Consultation rapport',
      resource: 'Stats_Q4.pdf',
      time: '14:20',
      status: 'success'
    },
    {
      user: 'guest@neolons.com',
      action: 'Tentative connexion',
      resource: 'Login',
      time: '14:15',
      status: 'failed'
    }
  ];

  systemHealth = {
    cpu: 45,
    memory: 67,
    storage: 23,
    network: 89
  };

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    // S'abonner aux changements des statistiques du dashboard
    this.dashboardSubscription = this.dashboardService.dashboardStats$.subscribe(
      (stats: DashboardStats) => {
        this.updateDashboardData(stats);
      }
    );

    // Simulation de mise à jour des données en temps réel
    setInterval(() => {
      this.updateRealTimeData();
    }, 30000); // Mise à jour toutes les 30 secondes

    // Mise à jour de l'activité utilisateur toutes les 10 secondes
    setInterval(() => {
      this.dashboardService.updateActiveUsers();
    }, 10000);
  }

  ngOnDestroy(): void {
    // Se désabonner pour éviter les fuites mémoire
    if (this.dashboardSubscription) {
      this.dashboardSubscription.unsubscribe();
    }
  }

  private updateDashboardData(stats: DashboardStats): void {
    this.totalUsers = stats.totalUsers;
    this.activeUsers = stats.activeUsers;
    this.totalDocuments = stats.totalDocuments;
    this.totalCampaigns = stats.totalCampaigns;
    this.userStats = { ...stats.userStats };
    this.auditStats = { ...stats.auditStats };
    this.licenseStats = { ...stats.licenseStats };
    this.roleStats = { ...stats.roleStats };
  }

  updateRealTimeData(): void {
    // Simulation de nouvelles données
    this.currentDate = new Date();
    this.systemHealth.cpu = Math.floor(Math.random() * 30) + 30;
    this.systemHealth.memory = Math.floor(Math.random() * 40) + 50;
    this.systemHealth.storage = Math.floor(Math.random() * 20) + 20;
    this.systemHealth.network = Math.floor(Math.random() * 15) + 85;
    
    // Mise à jour des graphiques d'activité utilisateur
    this.updateUserActivityChart();
  }

  private updateUserActivityChart(): void {
    // Simulation de nouvelles données d'activité
    this.userActivityData = this.userActivityData.map(day => ({
      ...day,
      value: Math.max(10, Math.floor(Math.random() * 50) + 20)
    }));
  }

  // Méthodes pour les actions rapides avec mise à jour du dashboard
  onQuickActionUsed(action: string): void {
    // Enregistrer l'action dans les statistiques d'audit
    const currentStats = this.dashboardService.getCurrentStats();
    currentStats.auditStats.consultations++;
    currentStats.auditStats.totalActionsToday++;
    
    // Ajouter une nouvelle activité récente
    const newActivity = {
      user: 'admin@neolons.com',
      action: `Action rapide: ${action}`,
      resource: action,
      time: new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }),
      status: 'success'
    };
    
    this.recentActivities.unshift(newActivity);
    if (this.recentActivities.length > 10) {
      this.recentActivities.pop();
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'success': return 'status-success';
      case 'failed': return 'status-failed';
      case 'warning': return 'status-warning';
      default: return 'status-info';
    }
  }

  getHealthStatus(value: number): string {
    if (value < 50) return 'good';
    if (value < 80) return 'warning';
    return 'critical';
  }
}

