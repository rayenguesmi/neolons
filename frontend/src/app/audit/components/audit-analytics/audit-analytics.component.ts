import { Component, OnInit, OnDestroy } from '@angular/core';
import { AuditAnalyticsService } from '../../services/audit-analytics.service';
import { AuditAnalytics, TimeSeriesPoint, UserActivity, AuditAnomaly } from '../../models/audit-change.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-audit-analytics',
  templateUrl: './audit-analytics.component.html',
  styleUrls: ['./audit-analytics.component.css']
})
export class AuditAnalyticsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  analytics: AuditAnalytics | null = null;
  timeSeriesData: TimeSeriesPoint[] = [];
  topUsers: UserActivity[] = [];
  anomalies: AuditAnomaly[] = [];
  
  loading = false;
  error = '';
  
  // Date range controls
  startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000); // 30 days ago
  endDate = new Date();
  
  // Chart configurations
  timeSeriesOptions: any;
  actionDistributionOptions: any;
  riskLevelOptions: any;
  
  // View controls
  selectedTimeRange = '30d';
  selectedGranularity = 'DAY';
  showAnomalies = true;
  
  constructor(private analyticsService: AuditAnalyticsService) {
    this.initializeChartOptions();
  }

  ngOnInit(): void {
    this.loadAnalytics();
    this.loadTimeSeriesData();
    this.loadTopUsers();
    this.loadAnomalies();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAnalytics(): void {
    this.loading = true;
    this.analyticsService.getAnalytics(this.startDate, this.endDate)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (analytics) => {
          this.analytics = analytics;
          this.updateCharts();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading analytics:', error);
          this.error = 'Erreur lors du chargement des analytics';
          this.loading = false;
        }
      });
  }

  loadTimeSeriesData(): void {
    this.analyticsService.getTimeSeriesData(this.startDate, this.endDate, this.selectedGranularity as any)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.timeSeriesData = data;
          this.updateTimeSeriesChart();
        },
        error: (error) => {
          console.error('Error loading time series data:', error);
        }
      });
  }

  loadTopUsers(): void {
    this.analyticsService.getTopUsers(10, this.startDate, this.endDate)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (users) => {
          this.topUsers = users;
        },
        error: (error) => {
          console.error('Error loading top users:', error);
        }
      });
  }

  loadAnomalies(): void {
    if (!this.showAnomalies) return;
    
    this.analyticsService.getAnomalies(this.startDate, this.endDate)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (anomalies) => {
          this.anomalies = anomalies;
        },
        error: (error) => {
          console.error('Error loading anomalies:', error);
        }
      });
  }

  onTimeRangeChange(): void {
    const now = new Date();
    switch (this.selectedTimeRange) {
      case '24h':
        this.startDate = new Date(now.getTime() - 24 * 60 * 60 * 1000);
        this.selectedGranularity = 'HOUR';
        break;
      case '7d':
        this.startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        this.selectedGranularity = 'DAY';
        break;
      case '30d':
        this.startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
        this.selectedGranularity = 'DAY';
        break;
      case '90d':
        this.startDate = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000);
        this.selectedGranularity = 'WEEK';
        break;
    }
    this.endDate = now;
    this.refreshData();
  }

  onCustomDateRange(): void {
    this.refreshData();
  }

  refreshData(): void {
    this.loadAnalytics();
    this.loadTimeSeriesData();
    this.loadTopUsers();
    this.loadAnomalies();
  }

  exportReport(format: 'PDF' | 'EXCEL' | 'CSV'): void {
    this.analyticsService.exportAnalyticsReport(format, this.startDate, this.endDate)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `audit-analytics-${this.startDate.toISOString().split('T')[0]}-${this.endDate.toISOString().split('T')[0]}.${format.toLowerCase()}`;
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: (error) => {
          console.error('Error exporting report:', error);
        }
      });
  }

  private initializeChartOptions(): void {
    this.timeSeriesOptions = {
      responsive: true,
      plugins: {
        title: {
          display: true,
          text: 'Activité dans le temps'
        },
        legend: {
          display: true
        }
      },
      scales: {
        x: {
          type: 'time',
          time: {
            displayFormats: {
              hour: 'HH:mm',
              day: 'DD/MM',
              week: 'DD/MM'
            }
          }
        },
        y: {
          beginAtZero: true
        }
      }
    };

    this.actionDistributionOptions = {
      responsive: true,
      plugins: {
        title: {
          display: true,
          text: 'Distribution des actions'
        },
        legend: {
          position: 'right'
        }
      }
    };

    this.riskLevelOptions = {
      responsive: true,
      plugins: {
        title: {
          display: true,
          text: 'Niveaux de risque'
        }
      },
      scales: {
        y: {
          beginAtZero: true
        }
      }
    };
  }

  private updateCharts(): void {
    this.updateTimeSeriesChart();
    // Update other charts based on analytics data
  }

  private updateTimeSeriesChart(): void {
    if (!this.timeSeriesData.length) return;
    
    // Chart.js data update logic would go here
    // This is a placeholder for the actual chart update
  }

  getSeverityClass(severity: string): string {
    const classes: { [key: string]: string } = {
      'LOW': 'severity-low',
      'MEDIUM': 'severity-medium',
      'HIGH': 'severity-high',
      'CRITICAL': 'severity-critical'
    };
    return classes[severity] || 'severity-low';
  }

  getRiskScoreClass(score: number): string {
    if (score >= 80) return 'risk-critical';
    if (score >= 60) return 'risk-high';
    if (score >= 40) return 'risk-medium';
    return 'risk-low';
  }

  formatRiskScore(score: number): string {
    return `${score.toFixed(1)}%`;
  }

  formatNumber(num: number): string {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }
}

