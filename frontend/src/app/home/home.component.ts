import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  currentDate: Date = new Date();

  // Statistiques générales
  totalUsers = 156;
  activeUsers = 89;
  totalDocuments = 2847;
  totalCampaigns = 45;
  
  // Données de gestion des utilisateurs
  userStats = {
    administrators: 8,
    managers: 23,
    standardUsers: 125,
    newUsersThisMonth: 12,
    activeSessionsToday: 67
  };

  // Données de journalisation
  auditStats = {
    totalActionsToday: 247,
    creations: 45,
    modifications: 128,
    deletions: 12,
    consultations: 62,
    securityAlerts: 3,
    failedLogins: 8
  };

  // Données de licences
  licenseStats = {
    totalLicenses: 200,
    activeLicenses: 156,
    expiringSoon: 15,
    expired: 8,
    utilizationRate: 78
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
      user: 'user@neolons.com',
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

  constructor() { }

  ngOnInit(): void {
    // Simulation de mise à jour des données en temps réel
    setInterval(() => {
      this.updateRealTimeData();
    }, 30000); // Mise à jour toutes les 30 secondes
  }

  updateRealTimeData(): void {
    // Simulation de nouvelles données
    this.currentDate = new Date();
    this.activeUsers = Math.floor(Math.random() * 20) + 80;
    this.auditStats.totalActionsToday += Math.floor(Math.random() * 5);
    this.systemHealth.cpu = Math.floor(Math.random() * 30) + 30;
    this.systemHealth.memory = Math.floor(Math.random() * 40) + 50;
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
