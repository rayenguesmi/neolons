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

  // Statistiques générales - initialisées à 0
  totalUsers = 0;
  activeUsers = 0;
  totalDocuments = 0;
  totalCampaigns = 0;

  userStats = {
    administrators: 0,
    managers: 0,
    standardUsers: 0,
    newUsersThisMonth: 0,
    activeSessionsToday: 0
  };

  auditStats = {
    totalActionsToday: 0,
    creations: 0,
    modifications: 0,
    deletions: 0,
    consultations: 0,
    securityAlerts: 0,
    failedLogins: 0
  };

  licenseStats = {
    totalLicenses: 0,
    activeLicenses: 0,
    expiringSoon: 0,
    expired: 0,
    utilizationRate: 0
  };

  roleStats = {
    totalRoles: 0,
    activeRoles: 0,
    customRoles: 0
  };

  userActivityData = [
    { name: 'Lun', value: 0 },
    { name: 'Mar', value: 0 },
    { name: 'Mer', value: 0 },
    { name: 'Jeu', value: 0 },
    { name: 'Ven', value: 0 },
    { name: 'Sam', value: 0 },
    { name: 'Dim', value: 0 }
  ];

  documentTypeData = [
    { name: 'Contrats', value: 0, color: '#3B82F6' },
    { name: 'Rapports', value: 0, color: '#10B981' },
    { name: 'Factures', value: 0, color: '#F59E0B' },
    { name: 'Autres', value: 0, color: '#EF4444' }
  ];

  recentActivities: any[] = [];

  systemHealth = {
    cpu: 0,
    memory: 0,
    storage: 0,
    network: 0
  };

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    // S'abonner aux statistiques du tableau de bord
    this.dashboardSubscription = this.dashboardService.dashboardStats$.subscribe(
      (stats: DashboardStats) => {
        this.updateDashboardData(stats);
      }
    );

    // Charger les activités récentes
    this.loadRecentActivities();

    // Charger les métriques système
    this.loadSystemHealth();

    // Mettre à jour les données en temps réel
    setInterval(() => {
      this.updateRealTimeData();
    }, 30000);

    // Mettre à jour les utilisateurs actifs
    setInterval(() => {
      this.dashboardService.updateActiveUsers();
    }, 10000);
  }

  ngOnDestroy(): void {
    if (this.dashboardSubscription) {
      this.dashboardSubscription.unsubscribe();
    }
  }

  private updateDashboardData(stats: DashboardStats): void {
    this.totalUsers = stats.totalUsers ?? 0;
    this.activeUsers = stats.activeUsers ?? 0;
    this.totalDocuments = stats.totalDocuments ?? 0;
    this.totalCampaigns = stats.totalCampaigns ?? 0;
    this.userStats = { ...stats.userStats };
    this.auditStats = { ...stats.auditStats };
    this.licenseStats = { ...stats.licenseStats };
    this.roleStats = { ...stats.roleStats };
  }

  private loadRecentActivities(): void {
    this.dashboardService.loadRecentActivities().subscribe(
      activities => {
        this.recentActivities = activities.map(activity => ({
          user: activity.user || 'Système',
          action: activity.action || 'Action inconnue',
          resource: activity.resource || 'Ressource',
          time: this.formatTime(activity.time),
          status: activity.status || 'success'
        }));
      },
      error => {
        console.error('Erreur lors du chargement des activités récentes:', error);
        // Garder les activités par défaut en cas d'erreur
        this.recentActivities = [];
      }
    );
  }

  private loadSystemHealth(): void {
    this.dashboardService.loadSystemHealth().subscribe(
      health => {
        this.systemHealth = {
          cpu: health.cpu || 0,
          memory: health.memory || 0,
          storage: health.storage || 0,
          network: health.network || 0
        };
      },
      error => {
        console.error('Erreur lors du chargement des métriques système:', error);
        // Garder les métriques par défaut en cas d'erreur
      }
    );
  }

  private formatTime(time: any): string {
    if (!time) return new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    
    if (typeof time === 'string') {
      return new Date(time).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    }
    
    if (time instanceof Date) {
      return time.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    }
    
    return time.toString();
  }

  updateRealTimeData(): void {
    this.currentDate = new Date();
    this.updateUserActivityChart();
    
    // Recharger les activités récentes
    this.loadRecentActivities();
    
    // Recharger les métriques système
    this.loadSystemHealth();
  }

  private updateUserActivityChart(): void {
    this.userActivityData = this.userActivityData.map(day => ({
      ...day,
      value: Math.max(10, Math.floor(Math.random() * 50) + 20)
    }));
  }

  onQuickActionUsed(action: string): void {
    const currentStats = this.dashboardService.getCurrentStats();
    currentStats.auditStats.consultations++;
    currentStats.auditStats.totalActionsToday++;

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
