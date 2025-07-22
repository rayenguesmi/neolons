import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuditLogExtended, AuditChange, AuditAnalytics } from './models/audit-change.model';

interface DashboardStats {
  totalLogs: number;
  logsLast24h: number;
  failedLogsLast24h: number;
  activeUsersLast24h: number;
  mostFrequentActions: { [key: string]: number };
  mostAccessedResourceTypes: { [key: string]: number };
}

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.css']
})
export class AuditComponent implements OnInit {
  auditLogs: AuditLogExtended[] = [];
  dashboardStats: DashboardStats | null = null;
  loading = false;
  error = '';
  
  // New features
  selectedLog: AuditLogExtended | null = null;
  selectedLogChanges: AuditChange[] = [];
  showAnalytics = false;
  showDiffViewer = false;
  
  // Enhanced filters
  selectedAction = '';
  selectedResourceType = '';
  selectedUser = '';
  selectedRiskLevel = '';
  dateFrom = '';
  dateTo = '';
  searchText = '';
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  
  // View options
  viewMode: 'table' | 'cards' | 'timeline' = 'table';
  showSuccessOnly = false;
  showFailuresOnly = false;
  
  // Options pour les filtres
  actions = ['LOGIN', 'LOGOUT', 'CREATE', 'UPDATE', 'DELETE', 'VIEW'];
  resourceTypes = ['USER', 'DOCUMENT', 'LICENCE', 'ROLE', 'PROJECT', 'CAMPAGNE'];
  riskLevels = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  private apiUrl = 'http://localhost:8090/api/audit-logs';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadDashboardStats();
    this.loadAuditLogs();
    this.initializeTestData();
  }

  loadDashboardStats(): void {
    this.loading = true;
    this.http.get<DashboardStats>(`${this.apiUrl}/dashboard-stats`).subscribe({
      next: (stats) => {
        this.dashboardStats = stats;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des statistiques:', error);
        this.error = 'Erreur lors du chargement des statistiques';
        this.loading = false;
      }
    });
  }

  loadAuditLogs(): void {
    this.loading = true;
    this.http.get<AuditLogExtended[]>(this.apiUrl).subscribe({
      next: (logs) => {
        this.auditLogs = logs;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des logs:', error);
        this.error = 'Erreur lors du chargement des logs d\'audit';
        this.loading = false;
      }
    });
  }

  initializeTestData(): void {
    this.http.post(`${this.apiUrl}/initialize-test-data`, {}).subscribe({
      next: (response) => {
        console.log('Données de test initialisées');
        this.loadAuditLogs();
        this.loadDashboardStats();
      },
      error: (error) => {
        console.error('Erreur lors de l\'initialisation des données de test:', error);
      }
    });
  }

  applyFilters(): AuditLogExtended[] {
    let filteredLogs = [...this.auditLogs];

    // Existing filters
    if (this.selectedAction) {
      filteredLogs = filteredLogs.filter(log => log.action === this.selectedAction);
    }

    if (this.selectedResourceType) {
      filteredLogs = filteredLogs.filter(log => log.resourceType === this.selectedResourceType);
    }

    if (this.selectedUser) {
      filteredLogs = filteredLogs.filter(log => 
        log.username.toLowerCase().includes(this.selectedUser.toLowerCase()) ||
        log.userId.toLowerCase().includes(this.selectedUser.toLowerCase())
      );
    }

    if (this.dateFrom) {
      const fromDate = new Date(this.dateFrom);
      filteredLogs = filteredLogs.filter(log => new Date(log.timestamp) >= fromDate);
    }

    if (this.dateTo) {
      const toDate = new Date(this.dateTo);
      filteredLogs = filteredLogs.filter(log => new Date(log.timestamp) <= toDate);
    }

    // New filters
    if (this.selectedRiskLevel) {
      filteredLogs = filteredLogs.filter(log => log.riskLevel === this.selectedRiskLevel);
    }

    if (this.searchText) {
      const searchLower = this.searchText.toLowerCase();
      filteredLogs = filteredLogs.filter(log => 
        log.details.toLowerCase().includes(searchLower) ||
        log.username.toLowerCase().includes(searchLower) ||
        log.resourceId.toLowerCase().includes(searchLower)
      );
    }

    if (this.showSuccessOnly) {
      filteredLogs = filteredLogs.filter(log => log.success);
    }

    if (this.showFailuresOnly) {
      filteredLogs = filteredLogs.filter(log => !log.success);
    }

    return filteredLogs;
  }

  get paginatedLogs(): AuditLogExtended[] {
    const filtered = this.applyFilters();
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return filtered.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages(): number {
    return Math.ceil(this.applyFilters().length / this.itemsPerPage);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  clearFilters(): void {
    this.selectedAction = '';
    this.selectedResourceType = '';
    this.selectedUser = '';
    this.selectedRiskLevel = '';
    this.dateFrom = '';
    this.dateTo = '';
    this.searchText = '';
    this.showSuccessOnly = false;
    this.showFailuresOnly = false;
    this.currentPage = 1;
  }

  exportLogs(): void {
    const filtered = this.applyFilters();
    const csvContent = this.convertToCSV(filtered);
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audit-logs-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  private convertToCSV(logs: AuditLogExtended[]): string {
    const headers = ['Date/Heure', 'Utilisateur', 'Action', 'Type de Ressource', 'ID Ressource', 'Détails', 'IP', 'Statut', 'Niveau de Risque'];
    const rows = logs.map(log => [
      new Date(log.timestamp).toLocaleString('fr-FR'),
      log.username,
      log.action,
      log.resourceType,
      log.resourceId,
      log.details,
      log.ipAddress,
      log.success ? 'Succès' : 'Échec',
      log.riskLevel || 'N/A'
    ]);

    return [headers, ...rows].map(row => 
      row.map(field => `"${field}"`).join(',')
    ).join('\n');
  }

  // New methods
  selectLog(log: AuditLogExtended): void {
    this.selectedLog = log;
    this.loadLogChanges(log.id);
    this.showDiffViewer = true;
  }

  loadLogChanges(logId: string): void {
    this.http.get<AuditChange[]>(`${this.apiUrl}/${logId}/changes`).subscribe({
      next: (changes) => {
        this.selectedLogChanges = changes;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des modifications:', error);
        this.selectedLogChanges = [];
      }
    });
  }

  toggleAnalytics(): void {
    this.showAnalytics = !this.showAnalytics;
  }

  closeDiffViewer(): void {
    this.showDiffViewer = false;
    this.selectedLog = null;
    this.selectedLogChanges = [];
  }

  changeViewMode(mode: 'table' | 'cards' | 'timeline'): void {
    this.viewMode = mode;
  }

  getStatusClass(success: boolean): string {
    return success ? 'status-success' : 'status-error';
  }

  getActionClass(action: string): string {
    const actionClasses: { [key: string]: string } = {
      'LOGIN': 'action-login',
      'LOGOUT': 'action-logout',
      'CREATE': 'action-create',
      'UPDATE': 'action-update',
      'DELETE': 'action-delete',
      'VIEW': 'action-view'
    };
    return actionClasses[action] || 'action-default';
  }

  getRiskLevelClass(riskLevel?: string): string {
    if (!riskLevel) return 'risk-unknown';
    const riskClasses: { [key: string]: string } = {
      'LOW': 'risk-low',
      'MEDIUM': 'risk-medium',
      'HIGH': 'risk-high',
      'CRITICAL': 'risk-critical'
    };
    return riskClasses[riskLevel] || 'risk-unknown';
  }

  hasChanges(log: AuditLogExtended): boolean {
    return log.action === 'UPDATE' || log.action === 'CREATE' || log.action === 'DELETE';
  }

  formatRelativeTime(timestamp: Date): string {
    const now = new Date();
    const diff = now.getTime() - new Date(timestamp).getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) return `il y a ${days} jour${days > 1 ? 's' : ''}`;
    if (hours > 0) return `il y a ${hours} heure${hours > 1 ? 's' : ''}`;
    if (minutes > 0) return `il y a ${minutes} minute${minutes > 1 ? 's' : ''}`;
    return 'À l\'instant';
  }

  getResourceIcon(resourceType: string): string {
    const icons: { [key: string]: string } = {
      'USER': 'fa-user',
      'DOCUMENT': 'fa-file',
      'LICENCE': 'fa-key',
      'ROLE': 'fa-shield-alt',
      'PROJECT': 'fa-project-diagram',
      'CAMPAGNE': 'fa-bullhorn'
    };
    return icons[resourceType] || 'fa-cube';
  }
}

