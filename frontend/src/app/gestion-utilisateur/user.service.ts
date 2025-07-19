import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { User, CreateUserRequest, UpdateUserRequest, UserResponse, UserFilter } from './user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = '/api/users';
  private usersSubject = new BehaviorSubject<User[]>([]);
  public users$ = this.usersSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Récupération de tous les utilisateurs
  getUsers(filter?: UserFilter): Observable<UserResponse> {
    let params = new HttpParams();
    
    if (filter) {
      if (filter.search) params = params.set('search', filter.search);
      if (filter.role) params = params.set('role', filter.role);
      if (filter.isActive !== undefined) params = params.set('isActive', filter.isActive.toString());
      if (filter.project) params = params.set('project', filter.project);
      if (filter.page) params = params.set('page', filter.page.toString());
      if (filter.size) params = params.set('size', filter.size.toString());
    }

    return this.http.get<UserResponse>(this.apiUrl, { params }).pipe(
      tap(response => this.usersSubject.next(response.users))
    );
  }

  // Récupération d'un utilisateur par ID
  getUserById(id: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  // Récupération des utilisateurs actifs
  getActiveUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/active`);
  }

  // Récupération des utilisateurs par rôle
  getUsersByRole(role: string): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/role/${role}`);
  }

  // Récupération des utilisateurs par projet
  getUsersByProject(projectId: string): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/project/${projectId}`);
  }

  // Création d'un utilisateur
  createUser(user: CreateUserRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, user).pipe(
      tap(() => this.refreshUsers())
    );
  }

  // Mise à jour d'un utilisateur
  updateUser(id: string, user: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, user).pipe(
      tap(() => this.refreshUsers())
    );
  }

  // Suppression d'un utilisateur
  deleteUser(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.refreshUsers())
    );
  }

  // Activation/Désactivation d'un utilisateur
  toggleUserStatus(id: string): Observable<User> {
    return this.http.patch<User>(`${this.apiUrl}/${id}/toggle-status`, {}).pipe(
      tap(() => this.refreshUsers())
    );
  }

  // Récupération du profil de l'utilisateur connecté
  getCurrentUserProfile(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/profile`);
  }

  // Mise à jour du profil de l'utilisateur connecté
  updateCurrentUserProfile(user: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/profile`, user);
  }

  // Méthodes utilitaires
  private refreshUsers(): void {
    this.getUsers().subscribe();
  }

  // Formatage du nom complet
  getFullName(user: User): string {
    return `${user.firstName} ${user.lastName}`;
  }

  // Formatage des rôles
  formatRoles(roles: string[]): string {
    return roles.map(role => this.translateRole(role)).join(', ');
  }

  // Traduction des rôles
  translateRole(role: string): string {
    const roleTranslations: { [key: string]: string } = {
      'ADMIN_GENERAL': 'Administrateur Général',
      'CHEF_PROJET': 'Chef de Projet',
      'EYA_EXECUTANTE': 'Eya (Exécutante)'
    };
    return roleTranslations[role] || role;
  }

  // Vérification des permissions
  hasRole(user: User, role: string): boolean {
    return user.roles.includes(role);
  }

  // Vérification si l'utilisateur est administrateur
  isAdmin(user: User): boolean {
    return this.hasRole(user, 'ADMIN_GENERAL');
  }

  // Vérification si l'utilisateur est chef de projet
  isProjectManager(user: User): boolean {
    return this.hasRole(user, 'CHEF_PROJET');
  }

  // Vérification si l'utilisateur est exécutant
  isExecutor(user: User): boolean {
    return this.hasRole(user, 'EYA_EXECUTANTE');
  }

  // Obtenir la couleur du badge pour un rôle
  getRoleBadgeClass(role: string): string {
    const roleClasses: { [key: string]: string } = {
      'ADMIN_GENERAL': 'badge-danger',
      'CHEF_PROJET': 'badge-primary',
      'EYA_EXECUTANTE': 'badge-success'
    };
    return roleClasses[role] || 'badge-secondary';
  }

  // Obtenir l'icône pour un rôle
  getRoleIcon(role: string): string {
    const roleIcons: { [key: string]: string } = {
      'ADMIN_GENERAL': 'fas fa-crown',
      'CHEF_PROJET': 'fas fa-user-tie',
      'EYA_EXECUTANTE': 'fas fa-user'
    };
    return roleIcons[role] || 'fas fa-user';
  }
}

