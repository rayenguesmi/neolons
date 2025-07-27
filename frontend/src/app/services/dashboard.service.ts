import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, interval, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  totalDocuments: number;
  totalCampaigns: number;
  userStats: {
    administrators: number;
    managers: number;
    standardUsers: number;
    newUsersThisMonth: number;
    activeSessionsToday: number;
  };
  auditStats: {
    totalActionsToday: number;
    creations: number;
    modifications: number;
    deletions: number;
    consultations: number;
    securityAlerts: number;
    failedLogins: number;
  };
  licenseStats: {
    totalLicenses: number;
    activeLicenses: number;
    expiringSoon: number;
    expired: number;
    utilizationRate: number;
  };
  roleStats: {
    totalRoles: number;
    activeRoles: number;
    customRoles: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = 'http://localhost:8090/api/dashboard';
  private dashboardStatsSubject = new BehaviorSubject<DashboardStats>(this.getInitialStats());
  public dashboardStats$ = this.dashboardStatsSubject.asObservable();

  constructor(private http: HttpClient) {
    // Charger les données initiales
    this.loadStatsFromAPI();
    
    // Actualiser les données toutes les 30 secondes
    interval(30000).subscribe(() => {
      this.loadStatsFromAPI();
    });
  }

  private getInitialStats(): DashboardStats {
    return {
      totalUsers: 0,
      activeUsers: 0,
      totalDocuments: 0,
      totalCampaigns: 0,
      userStats: {
        administrators: 0,
        managers: 0,
        standardUsers: 0,
        newUsersThisMonth: 0,
        activeSessionsToday: 0
      },
      auditStats: {
        totalActionsToday: 0,
        creations: 0,
        modifications: 0,
        deletions: 0,
        consultations: 0,
        securityAlerts: 0,
        failedLogins: 0
      },
      licenseStats: {
        totalLicenses: 0,
        activeLicenses: 0,
        expiringSoon: 0,
        expired: 0,
        utilizationRate: 0
      },
      roleStats: {
        totalRoles: 0,
        activeRoles: 0,
        customRoles: 0
      }
    };
  }

  getCurrentStats(): DashboardStats {
    return this.dashboardStatsSubject.value;
  }

  /**
   * Charge les statistiques depuis l'API
   */
  loadStatsFromAPI(): void {
    this.http.get<any>(`${this.apiUrl}/stats`).pipe(
      catchError(error => {
        console.error('Erreur lors du chargement des statistiques:', error);
        // En cas d'erreur, garder les données actuelles
        return of(null);
      })
    ).subscribe(response => {
      if (response && response.userStats) {
        const stats: DashboardStats = {
          totalUsers: response.userStats.totalUsers || 0,
          activeUsers: response.userStats.activeUsers || 0,
          totalDocuments: response.documentStats?.totalDocuments || 0,
          totalCampaigns: response.campaignStats?.totalCampaigns || 0,
          userStats: {
            administrators: response.userStats.administrators || 0,
            managers: response.userStats.managers || 0,
            standardUsers: response.userStats.standardUsers || 0,
            newUsersThisMonth: response.userStats.newUsersThisMonth || 0,
            activeSessionsToday: response.userStats.activeSessionsToday || 0
          },
          auditStats: {
            totalActionsToday: response.auditStats?.totalActionsToday || 0,
            creations: response.auditStats?.creations || 0,
            modifications: response.auditStats?.modifications || 0,
            deletions: response.auditStats?.deletions || 0,
            consultations: response.auditStats?.consultations || 0,
            securityAlerts: response.auditStats?.securityAlerts || 0,
            failedLogins: response.auditStats?.failedLogins || 0
          },
          licenseStats: {
            totalLicenses: response.licenseStats?.totalLicenses || 0,
            activeLicenses: response.licenseStats?.activeLicenses || 0,
            expiringSoon: response.licenseStats?.expiringSoon || 0,
            expired: response.licenseStats?.expired || 0,
            utilizationRate: response.licenseStats?.utilizationRate || 0
          },
          roleStats: {
            totalRoles: response.roleStats?.totalRoles || 0,
            activeRoles: response.roleStats?.activeRoles || 0,
            customRoles: response.roleStats?.customRoles || 0
          }
        };
        
        this.dashboardStatsSubject.next(stats);
      }
    });
  }

  /**
   * Charge les activités récentes depuis l'API
   */
  loadRecentActivities(): Observable<any[]> {
    return this.http.get<any>(`${this.apiUrl}/activities/recent`).pipe(
      map(response => response.activities || []),
      catchError(error => {
        console.error('Erreur lors du chargement des activités récentes:', error);
        return of([]);
      })
    );
  }

  /**
   * Charge les métriques système depuis l'API
   */
  loadSystemHealth(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/system/health`).pipe(
      catchError(error => {
        console.error('Erreur lors du chargement des métriques système:', error);
        return of({
          cpu: 0,
          memory: 0,
          storage: 0,
          network: 0
        });
      })
    );
  }

  // Méthodes pour mettre à jour les statistiques utilisateurs
  incrementUserCount(role: 'administrators' | 'managers' | 'standardUsers' = 'standardUsers'): void {
    const currentStats = this.getCurrentStats();
    currentStats.totalUsers++;
    currentStats.userStats[role]++;
    currentStats.userStats.newUsersThisMonth++;
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('creation');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  decrementUserCount(role: 'administrators' | 'managers' | 'standardUsers' = 'standardUsers'): void {
    const currentStats = this.getCurrentStats();
    if (currentStats.totalUsers > 0) {
      currentStats.totalUsers--;
      if (currentStats.userStats[role] > 0) {
        currentStats.userStats[role]--;
      }
    }
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('deletion');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  updateUserRole(oldRole: 'administrators' | 'managers' | 'standardUsers', 
                 newRole: 'administrators' | 'managers' | 'standardUsers'): void {
    if (oldRole === newRole) return;
    
    const currentStats = this.getCurrentStats();
    if (currentStats.userStats[oldRole] > 0) {
      currentStats.userStats[oldRole]--;
    }
    currentStats.userStats[newRole]++;
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('modification');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  // Méthodes pour mettre à jour les statistiques de licences
  incrementLicenseCount(): void {
    const currentStats = this.getCurrentStats();
    currentStats.licenseStats.totalLicenses++;
    currentStats.licenseStats.activeLicenses++;
    this.updateLicenseUtilization(currentStats);
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('creation');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  decrementLicenseCount(): void {
    const currentStats = this.getCurrentStats();
    if (currentStats.licenseStats.totalLicenses > 0) {
      currentStats.licenseStats.totalLicenses--;
      if (currentStats.licenseStats.activeLicenses > 0) {
        currentStats.licenseStats.activeLicenses--;
      }
    }
    this.updateLicenseUtilization(currentStats);
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('deletion');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  updateLicenseStatus(isActive: boolean): void {
    const currentStats = this.getCurrentStats();
    if (isActive) {
      currentStats.licenseStats.activeLicenses++;
    } else {
      if (currentStats.licenseStats.activeLicenses > 0) {
        currentStats.licenseStats.activeLicenses--;
      }
    }
    this.updateLicenseUtilization(currentStats);
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('modification');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  private updateLicenseUtilization(stats: DashboardStats): void {
    if (stats.licenseStats.totalLicenses > 0) {
      stats.licenseStats.utilizationRate = Math.round(
        (stats.licenseStats.activeLicenses / stats.licenseStats.totalLicenses) * 100
      );
    }
  }

  // Méthodes pour mettre à jour les statistiques de rôles
  incrementRoleCount(): void {
    const currentStats = this.getCurrentStats();
    currentStats.roleStats.totalRoles++;
    currentStats.roleStats.activeRoles++;
    currentStats.roleStats.customRoles++;
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('creation');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  decrementRoleCount(): void {
    const currentStats = this.getCurrentStats();
    if (currentStats.roleStats.totalRoles > 0) {
      currentStats.roleStats.totalRoles--;
      if (currentStats.roleStats.activeRoles > 0) {
        currentStats.roleStats.activeRoles--;
      }
      if (currentStats.roleStats.customRoles > 0) {
        currentStats.roleStats.customRoles--;
      }
    }
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('deletion');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  // Méthodes pour mettre à jour les statistiques de documents
  incrementDocumentCount(): void {
    const currentStats = this.getCurrentStats();
    currentStats.totalDocuments++;
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('creation');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  decrementDocumentCount(): void {
    const currentStats = this.getCurrentStats();
    if (currentStats.totalDocuments > 0) {
      currentStats.totalDocuments--;
    }
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('deletion');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  // Méthodes pour mettre à jour les statistiques de campagnes
  incrementCampaignCount(): void {
    const currentStats = this.getCurrentStats();
    currentStats.totalCampaigns++;
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('creation');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  decrementCampaignCount(): void {
    const currentStats = this.getCurrentStats();
    if (currentStats.totalCampaigns > 0) {
      currentStats.totalCampaigns--;
    }
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('deletion');
    
    // Recharger les données depuis l'API après un délai
    setTimeout(() => this.loadStatsFromAPI(), 1000);
  }

  // Méthode pour enregistrer les actions d'audit
  private logAuditAction(action: 'creation' | 'modification' | 'deletion' | 'consultation'): void {
    const currentStats = this.getCurrentStats();
    currentStats.auditStats.totalActionsToday++;
    currentStats.auditStats[action === 'creation' ? 'creations' : 
                           action === 'modification' ? 'modifications' :
                           action === 'deletion' ? 'deletions' : 'consultations']++;
    // Note: On ne met pas à jour ici pour éviter la récursion
  }

  // Méthode pour simuler l'activité utilisateur
  updateActiveUsers(): void {
    const currentStats = this.getCurrentStats();
    currentStats.activeUsers = Math.min(
      currentStats.totalUsers,
      Math.floor(Math.random() * 20) + Math.max(1, currentStats.totalUsers - 30)
    );
    currentStats.userStats.activeSessionsToday = Math.floor(Math.random() * 30) + currentStats.activeUsers;
    this.dashboardStatsSubject.next(currentStats);
  }

  // Méthode pour réinitialiser les statistiques
  resetStats(): void {
    this.dashboardStatsSubject.next(this.getInitialStats());
    this.loadStatsFromAPI();
  }

  // Méthode pour charger les statistiques depuis une source externe (API)
  loadStatsFromAPIObservable(): Observable<DashboardStats> {
    return this.dashboardStats$;
  }
}