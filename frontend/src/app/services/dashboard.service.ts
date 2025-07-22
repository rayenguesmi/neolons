import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

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
  private dashboardStatsSubject = new BehaviorSubject<DashboardStats>(this.getInitialStats());
  public dashboardStats$ = this.dashboardStatsSubject.asObservable();

  constructor() {}

  private getInitialStats(): DashboardStats {
    return {
      totalUsers: 156,
      activeUsers: 89,
      totalDocuments: 2847,
      totalCampaigns: 45,
      userStats: {
        administrators: 8,
        managers: 23,
        standardUsers: 125,
        newUsersThisMonth: 12,
        activeSessionsToday: 67
      },
      auditStats: {
        totalActionsToday: 247,
        creations: 45,
        modifications: 128,
        deletions: 12,
        consultations: 62,
        securityAlerts: 3,
        failedLogins: 8
      },
      licenseStats: {
        totalLicenses: 200,
        activeLicenses: 156,
        expiringSoon: 15,
        expired: 8,
        utilizationRate: 78
      },
      roleStats: {
        totalRoles: 12,
        activeRoles: 10,
        customRoles: 5
      }
    };
  }

  getCurrentStats(): DashboardStats {
    return this.dashboardStatsSubject.value;
  }

  // Méthodes pour mettre à jour les statistiques utilisateurs
  incrementUserCount(role: 'administrators' | 'managers' | 'standardUsers' = 'standardUsers'): void {
    const currentStats = this.getCurrentStats();
    currentStats.totalUsers++;
    currentStats.userStats[role]++;
    currentStats.userStats.newUsersThisMonth++;
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('creation');
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
  }

  // Méthodes pour mettre à jour les statistiques de licences
  incrementLicenseCount(): void {
    const currentStats = this.getCurrentStats();
    currentStats.licenseStats.totalLicenses++;
    currentStats.licenseStats.activeLicenses++;
    this.updateLicenseUtilization(currentStats);
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('creation');
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
  }

  // Méthodes pour mettre à jour les statistiques de documents
  incrementDocumentCount(): void {
    const currentStats = this.getCurrentStats();
    currentStats.totalDocuments++;
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('creation');
  }

  decrementDocumentCount(): void {
    const currentStats = this.getCurrentStats();
    if (currentStats.totalDocuments > 0) {
      currentStats.totalDocuments--;
    }
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('deletion');
  }

  // Méthodes pour mettre à jour les statistiques de campagnes
  incrementCampaignCount(): void {
    const currentStats = this.getCurrentStats();
    currentStats.totalCampaigns++;
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('creation');
  }

  decrementCampaignCount(): void {
    const currentStats = this.getCurrentStats();
    if (currentStats.totalCampaigns > 0) {
      currentStats.totalCampaigns--;
    }
    this.dashboardStatsSubject.next(currentStats);
    this.logAuditAction('deletion');
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
  }

  // Méthode pour charger les statistiques depuis une source externe (API)
  loadStatsFromAPI(): Observable<DashboardStats> {
    // Cette méthode pourrait être utilisée pour charger les vraies données depuis l'API
    // Pour l'instant, on retourne les données actuelles
    return this.dashboardStats$;
  }
}

