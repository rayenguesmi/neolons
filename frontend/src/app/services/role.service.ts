import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Role {
  id?: string;
  name: string;
  displayName: string;
  description: string;
  permissions: string[];
  isActive: boolean;
  isSystemRole?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private apiUrl = '/api/roles';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-ID': 'ADMIN' // TODO: Récupérer l'ID utilisateur depuis l'authentification
    });
  }

  /**
   * Récupère tous les rôles
   */
  getAllRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(this.apiUrl);
  }

  /**
   * Récupère tous les rôles actifs
   */
  getActiveRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/active`);
  }

  /**
   * Récupère un rôle par son ID
   */
  getRoleById(id: string): Observable<Role> {
    return this.http.get<Role>(`${this.apiUrl}/${id}`);
  }

  /**
   * Récupère un rôle par son nom
   */
  getRoleByName(name: string): Observable<Role> {
    return this.http.get<Role>(`${this.apiUrl}/name/${name}`);
  }

  /**
   * Crée un nouveau rôle
   */
  createRole(role: Role): Observable<Role> {
    return this.http.post<Role>(this.apiUrl, role, { headers: this.getHeaders() });
  }

  /**
   * Met à jour un rôle existant
   */
  updateRole(id: string, role: Role): Observable<Role> {
    return this.http.put<Role>(`${this.apiUrl}/${id}`, role, { headers: this.getHeaders() });
  }

  /**
   * Active ou désactive un rôle
   */
  toggleRoleStatus(id: string): Observable<Role> {
    return this.http.patch<Role>(`${this.apiUrl}/${id}/toggle-status`, {}, { headers: this.getHeaders() });
  }

  /**
   * Supprime un rôle
   */
  deleteRole(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  /**
   * Recherche des rôles par nom d'affichage
   */
  searchRoles(searchTerm: string): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/search?q=${encodeURIComponent(searchTerm)}`);
  }

  /**
   * Récupère les rôles contenant une permission spécifique
   */
  getRolesWithPermission(permission: string): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/with-permission/${permission}`);
  }

  /**
   * Récupère les rôles système
   */
  getSystemRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/system`);
  }

  /**
   * Récupère les rôles personnalisés (non-système)
   */
  getCustomRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/custom`);
  }

  /**
   * Initialise les rôles système par défaut
   */
  initializeDefaultRoles(): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/initialize-defaults`, {}, { headers: this.getHeaders() });
  }
}

