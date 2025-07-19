import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Permission {
  id?: string;
  name: string;
  displayName: string;
  description: string;
  module: string;
  resource: string;
  action: string;
  isActive: boolean;
  isSystemPermission?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
}

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  private apiUrl = '/api/permissions';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-ID': 'ADMIN' // TODO: Récupérer l'ID utilisateur depuis l'authentification
    });
  }

  /**
   * Récupère toutes les permissions
   */
  getAllPermissions(): Observable<Permission[]> {
    return this.http.get<Permission[]>(this.apiUrl);
  }

  /**
   * Récupère toutes les permissions actives
   */
  getActivePermissions(): Observable<Permission[]> {
    return this.http.get<Permission[]>(`${this.apiUrl}/active`);
  }

  /**
   * Récupère une permission par son ID
   */
  getPermissionById(id: string): Observable<Permission> {
    return this.http.get<Permission>(`${this.apiUrl}/${id}`);
  }

  /**
   * Récupère une permission par son nom
   */
  getPermissionByName(name: string): Observable<Permission> {
    return this.http.get<Permission>(`${this.apiUrl}/name/${name}`);
  }

  /**
   * Récupère les permissions par module
   */
  getPermissionsByModule(module: string): Observable<Permission[]> {
    return this.http.get<Permission[]>(`${this.apiUrl}/module/${module}`);
  }

  /**
   * Récupère les permissions par ressource
   */
  getPermissionsByResource(resource: string): Observable<Permission[]> {
    return this.http.get<Permission[]>(`${this.apiUrl}/resource/${resource}`);
  }

  /**
   * Crée une nouvelle permission
   */
  createPermission(permission: Permission): Observable<Permission> {
    return this.http.post<Permission>(this.apiUrl, permission, { headers: this.getHeaders() });
  }

  /**
   * Met à jour une permission existante
   */
  updatePermission(id: string, permission: Permission): Observable<Permission> {
    return this.http.put<Permission>(`${this.apiUrl}/${id}`, permission, { headers: this.getHeaders() });
  }

  /**
   * Active ou désactive une permission
   */
  togglePermissionStatus(id: string): Observable<Permission> {
    return this.http.patch<Permission>(`${this.apiUrl}/${id}/toggle-status`, {}, { headers: this.getHeaders() });
  }

  /**
   * Supprime une permission
   */
  deletePermission(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  /**
   * Recherche des permissions par nom d'affichage
   */
  searchPermissions(searchTerm: string): Observable<Permission[]> {
    return this.http.get<Permission[]>(`${this.apiUrl}/search?q=${encodeURIComponent(searchTerm)}`);
  }

  /**
   * Récupère les permissions système
   */
  getSystemPermissions(): Observable<Permission[]> {
    return this.http.get<Permission[]>(`${this.apiUrl}/system`);
  }

  /**
   * Récupère les permissions personnalisées (non-système)
   */
  getCustomPermissions(): Observable<Permission[]> {
    return this.http.get<Permission[]>(`${this.apiUrl}/custom`);
  }

  /**
   * Initialise les permissions système par défaut
   */
  initializeDefaultPermissions(): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/initialize-defaults`, {}, { headers: this.getHeaders() });
  }
}

