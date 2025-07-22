import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditAnalytics, AuditAnomaly, TimeSeriesPoint, UserActivity } from '../models/audit-change.model';

@Injectable({
  providedIn: 'root'
})
export class AuditAnalyticsService {
  private apiUrl = 'http://localhost:8080/api/audit-analytics';

  constructor(private http: HttpClient) {}

  getAnalytics(startDate?: Date, endDate?: Date): Observable<AuditAnalytics> {
    let params = new HttpParams();
    if (startDate) {
      params = params.set('startDate', startDate.toISOString());
    }
    if (endDate) {
      params = params.set('endDate', endDate.toISOString());
    }
    return this.http.get<AuditAnalytics>(`${this.apiUrl}/overview`, { params });
  }

  getTimeSeriesData(startDate: Date, endDate: Date, granularity: 'HOUR' | 'DAY' | 'WEEK' = 'DAY'): Observable<TimeSeriesPoint[]> {
    const params = new HttpParams()
      .set('startDate', startDate.toISOString())
      .set('endDate', endDate.toISOString())
      .set('granularity', granularity);
    return this.http.get<TimeSeriesPoint[]>(`${this.apiUrl}/time-series`, { params });
  }

  getTopUsers(limit: number = 10, startDate?: Date, endDate?: Date): Observable<UserActivity[]> {
    let params = new HttpParams().set('limit', limit.toString());
    if (startDate) {
      params = params.set('startDate', startDate.toISOString());
    }
    if (endDate) {
      params = params.set('endDate', endDate.toISOString());
    }
    return this.http.get<UserActivity[]>(`${this.apiUrl}/top-users`, { params });
  }

  getAnomalies(startDate?: Date, endDate?: Date): Observable<AuditAnomaly[]> {
    let params = new HttpParams();
    if (startDate) {
      params = params.set('startDate', startDate.toISOString());
    }
    if (endDate) {
      params = params.set('endDate', endDate.toISOString());
    }
    return this.http.get<AuditAnomaly[]>(`${this.apiUrl}/anomalies`, { params });
  }

  getActivityHeatmap(startDate: Date, endDate: Date): Observable<any> {
    const params = new HttpParams()
      .set('startDate', startDate.toISOString())
      .set('endDate', endDate.toISOString());
    return this.http.get<any>(`${this.apiUrl}/heatmap`, { params });
  }

  getRiskAnalysis(userId?: string, resourceType?: string): Observable<any> {
    let params = new HttpParams();
    if (userId) {
      params = params.set('userId', userId);
    }
    if (resourceType) {
      params = params.set('resourceType', resourceType);
    }
    return this.http.get<any>(`${this.apiUrl}/risk-analysis`, { params });
  }

  getComplianceReport(startDate: Date, endDate: Date): Observable<any> {
    const params = new HttpParams()
      .set('startDate', startDate.toISOString())
      .set('endDate', endDate.toISOString());
    return this.http.get<any>(`${this.apiUrl}/compliance-report`, { params });
  }

  exportAnalyticsReport(format: 'PDF' | 'EXCEL' | 'CSV', startDate: Date, endDate: Date): Observable<Blob> {
    const params = new HttpParams()
      .set('format', format)
      .set('startDate', startDate.toISOString())
      .set('endDate', endDate.toISOString());
    return this.http.get(`${this.apiUrl}/export`, { 
      params, 
      responseType: 'blob' 
    });
  }
}

