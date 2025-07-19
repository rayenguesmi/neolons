import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface License {
  id?: string;
  licenseName: string;
  licenseKey?: string;
  licenseType: string;
  productName: string;
  productVersion?: string;
  customerName: string;
  customerEmail: string;
  organization?: string;
  maxUsers?: number;
  maxProjects?: number;
  features?: string[];
  issueDate?: Date;
  expiryDate?: Date;
  activationDate?: Date;
  isActive?: boolean;
  isTrial?: boolean;
  usageCount?: number;
  maxUsage?: number;
  hardwareFingerprint?: string;
  ipRestrictions?: string[];
  domainRestrictions?: string[];
  notes?: string;
  createdBy?: string;
  createdAt?: Date;
  updatedBy?: string;
  updatedAt?: Date;
  lastValidation?: Date;
  validationStatus?: string;
  valid?: boolean;
  expired?: boolean;
  daysUntilExpiry?: number;
}

export interface LicenseValidationResult {
  valid: boolean;
  message: string;
  license?: License;
}

export interface LicenseStatistics {
  totalLicenses: number;
  activeLicenses: number;
  inactiveLicenses: number;
  trialLicenses: number;
  expiredLicenses: number;
  licensesByType: { [key: string]: number };
}

@Injectable({
  providedIn: 'root'
})
export class LicenseService {
  private apiUrl = 'http://localhost:8090/api/licenses';

  constructor(private http: HttpClient) { }

  // Récupérer toutes les licences
  getAllLicenses(): Observable<License[]> {
    return this.http.get<License[]>(this.apiUrl);
  }

  // Récupérer une licence par ID
  getLicenseById(id: string): Observable<License> {
    return this.http.get<License>(`${this.apiUrl}/${id}`);
  }

  // Créer une nouvelle licence
  createLicense(license: License): Observable<License> {
    return this.http.post<License>(this.apiUrl, license);
  }

  // Mettre à jour une licence
  updateLicense(id: string, license: License): Observable<License> {
    return this.http.put<License>(`${this.apiUrl}/${id}`, license);
  }

  // Supprimer une licence
  deleteLicense(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Valider une licence
  validateLicense(licenseKey: string): Observable<LicenseValidationResult> {
    return this.http.post<LicenseValidationResult>(`${this.apiUrl}/validate`, { licenseKey });
  }

  // Activer une licence
  activateLicense(licenseKey: string): Observable<License> {
    return this.http.post<License>(`${this.apiUrl}/activate`, { licenseKey });
  }

  // Désactiver une licence
  deactivateLicense(id: string): Observable<License> {
    return this.http.post<License>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  // Rechercher des licences
  searchLicenses(searchTerm: string): Observable<License[]> {
    return this.http.get<License[]>(`${this.apiUrl}/search?q=${encodeURIComponent(searchTerm)}`);
  }

  // Récupérer les licences par type
  getLicensesByType(licenseType: string): Observable<License[]> {
    return this.http.get<License[]>(`${this.apiUrl}/type/${licenseType}`);
  }

  // Récupérer les licences actives
  getActiveLicenses(): Observable<License[]> {
    return this.http.get<License[]>(`${this.apiUrl}/active`);
  }

  // Récupérer les licences expirées
  getExpiredLicenses(): Observable<License[]> {
    return this.http.get<License[]>(`${this.apiUrl}/expired`);
  }

  // Récupérer les licences qui expirent bientôt
  getLicensesExpiringSoon(days: number = 30): Observable<License[]> {
    return this.http.get<License[]>(`${this.apiUrl}/expiring-soon?days=${days}`);
  }

  // Récupérer les statistiques des licences
  getLicenseStatistics(): Observable<LicenseStatistics> {
    return this.http.get<LicenseStatistics>(`${this.apiUrl}/statistics`);
  }

  // Initialiser les licences par défaut
  initializeDefaultLicenses(): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/initialize-defaults`, {});
  }
}

