import { Component, OnInit } from '@angular/core';
import { Role, RoleService } from '../services/role.service';
import { Permission, PermissionService } from '../services/permission.service';

@Component({
  selector: 'app-gestion-roles',
  templateUrl: './gestion-roles.component.html',
  styleUrls: ['./gestion-roles.component.css']
})
export class GestionRolesComponent implements OnInit {
  roles: Role[] = [];
  permissions: Permission[] = [];
  filteredRoles: Role[] = [];
  
  // Formulaire de création/modification
  currentRole: Role = {
    name: '',
    displayName: '',
    description: '',
    permissions: [],
    isActive: true
  };
  
  isEditing = false;
  editingRoleId: string | null = null;
  showForm = false;
  
  // Recherche et filtres
  searchTerm = '';
  showSystemRoles = true;
  showCustomRoles = true;
  showActiveOnly = false;
  
  // Messages
  successMessage = '';
  errorMessage = '';
  
  // Permissions groupées par module
  permissionsByModule: { [key: string]: Permission[] } = {};

  constructor(
    private roleService: RoleService,
    private permissionService: PermissionService
  ) { }

  ngOnInit(): void {
    this.loadRoles();
    this.loadPermissions();
  }

  /**
   * Charge tous les rôles
   */
  loadRoles(): void {
    this.roleService.getAllRoles().subscribe({
      next: (roles) => {
        this.roles = roles;
        this.applyFilters();
      },
      error: (error) => {
        this.showError('Erreur lors du chargement des rôles');
        console.error('Erreur:', error);
      }
    });
  }

  /**
   * Charge toutes les permissions
   */
  loadPermissions(): void {
    this.permissionService.getAllPermissions().subscribe({
      next: (permissions) => {
        this.permissions = permissions;
        this.groupPermissionsByModule();
      },
      error: (error) => {
        this.showError('Erreur lors du chargement des permissions');
        console.error('Erreur:', error);
      }
    });
  }

  /**
   * Groupe les permissions par module
   */
  groupPermissionsByModule(): void {
    this.permissionsByModule = {};
    this.permissions.forEach(permission => {
      if (!this.permissionsByModule[permission.module]) {
        this.permissionsByModule[permission.module] = [];
      }
      this.permissionsByModule[permission.module].push(permission);
    });
  }

  /**
   * Applique les filtres de recherche et d'affichage
   */
  applyFilters(): void {
    this.filteredRoles = this.roles.filter(role => {
      // Filtre par terme de recherche
      const matchesSearch = !this.searchTerm || 
        role.displayName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        role.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        role.description.toLowerCase().includes(this.searchTerm.toLowerCase());

      // Filtre par type de rôle
      const matchesType = (this.showSystemRoles && role.isSystemRole) || 
                         (this.showCustomRoles && !role.isSystemRole);

      // Filtre par statut actif
      const matchesStatus = !this.showActiveOnly || role.isActive;

      return matchesSearch && matchesType && matchesStatus;
    });
  }

  /**
   * Recherche les rôles
   */
  onSearch(): void {
    this.applyFilters();
  }

  /**
   * Change les filtres d'affichage
   */
  onFilterChange(): void {
    this.applyFilters();
  }

  /**
   * Affiche le formulaire de création
   */
  showCreateForm(): void {
    this.currentRole = {
      name: '',
      displayName: '',
      description: '',
      permissions: [],
      isActive: true
    };
    this.isEditing = false;
    this.editingRoleId = null;
    this.showForm = true;
  }

  /**
   * Affiche le formulaire de modification
   */
  editRole(role: Role): void {
    if (role.isSystemRole) {
      this.showError('Impossible de modifier un rôle système');
      return;
    }

    this.currentRole = { ...role };
    this.isEditing = true;
    this.editingRoleId = role.id || null;
    this.showForm = true;
  }

  /**
   * Annule la création/modification
   */
  cancelForm(): void {
    this.showForm = false;
    this.isEditing = false;
    this.editingRoleId = null;
    this.clearMessages();
  }

  /**
   * Sauvegarde le rôle (création ou modification)
   */
  saveRole(): void {
    if (!this.validateRole()) {
      return;
    }

    if (this.isEditing && this.editingRoleId) {
      this.updateRole();
    } else {
      this.createRole();
    }
  }

  /**
   * Valide les données du rôle
   */
  validateRole(): boolean {
    if (!this.currentRole.name.trim()) {
      this.showError('Le nom du rôle est obligatoire');
      return false;
    }

    if (!this.currentRole.displayName.trim()) {
      this.showError('Le nom d\'affichage est obligatoire');
      return false;
    }

    if (!this.currentRole.description.trim()) {
      this.showError('La description est obligatoire');
      return false;
    }

    return true;
  }

  /**
   * Crée un nouveau rôle
   */
  createRole(): void {
    this.roleService.createRole(this.currentRole).subscribe({
      next: (role) => {
        this.showSuccess('Rôle créé avec succès');
        this.loadRoles();
        this.cancelForm();
      },
      error: (error) => {
        this.showError('Erreur lors de la création du rôle: ' + (error.error || error.message));
        console.error('Erreur:', error);
      }
    });
  }

  /**
   * Met à jour un rôle existant
   */
  updateRole(): void {
    if (!this.editingRoleId) return;

    this.roleService.updateRole(this.editingRoleId, this.currentRole).subscribe({
      next: (role) => {
        this.showSuccess('Rôle modifié avec succès');
        this.loadRoles();
        this.cancelForm();
      },
      error: (error) => {
        this.showError('Erreur lors de la modification du rôle: ' + (error.error || error.message));
        console.error('Erreur:', error);
      }
    });
  }

  /**
   * Active ou désactive un rôle
   */
  toggleRoleStatus(role: Role): void {
    if (role.isSystemRole) {
      this.showError('Impossible de modifier le statut d\'un rôle système');
      return;
    }

    if (!role.id) return;

    this.roleService.toggleRoleStatus(role.id).subscribe({
      next: (updatedRole) => {
        this.showSuccess(`Rôle ${updatedRole.isActive ? 'activé' : 'désactivé'} avec succès`);
        this.loadRoles();
      },
      error: (error) => {
        this.showError('Erreur lors du changement de statut: ' + (error.error || error.message));
        console.error('Erreur:', error);
      }
    });
  }

  /**
   * Supprime un rôle
   */
  deleteRole(role: Role): void {
    if (role.isSystemRole) {
      this.showError('Impossible de supprimer un rôle système');
      return;
    }

    if (!role.id) return;

    if (confirm(`Êtes-vous sûr de vouloir supprimer le rôle "${role.displayName}" ?`)) {
      this.roleService.deleteRole(role.id).subscribe({
        next: () => {
          this.showSuccess('Rôle supprimé avec succès');
          this.loadRoles();
        },
        error: (error) => {
          this.showError('Erreur lors de la suppression: ' + (error.error || error.message));
          console.error('Erreur:', error);
        }
      });
    }
  }

  /**
   * Gère la sélection/désélection des permissions
   */
  onPermissionChange(permissionName: string, isChecked: boolean): void {
    if (isChecked) {
      if (!this.currentRole.permissions.includes(permissionName)) {
        this.currentRole.permissions.push(permissionName);
      }
    } else {
      const index = this.currentRole.permissions.indexOf(permissionName);
      if (index > -1) {
        this.currentRole.permissions.splice(index, 1);
      }
    }
  }

  /**
   * Vérifie si une permission est sélectionnée
   */
  isPermissionSelected(permissionName: string): boolean {
    return this.currentRole.permissions.includes(permissionName);
  }

  /**
   * Sélectionne/désélectionne toutes les permissions d'un module
   */
  toggleModulePermissions(module: string, isChecked: boolean): void {
    const modulePermissions = this.permissionsByModule[module] || [];
    
    modulePermissions.forEach(permission => {
      this.onPermissionChange(permission.name, isChecked);
    });
  }

  /**
   * Vérifie si toutes les permissions d'un module sont sélectionnées
   */
  isModuleFullySelected(module: string): boolean {
    const modulePermissions = this.permissionsByModule[module] || [];
    return modulePermissions.length > 0 && 
           modulePermissions.every(permission => this.isPermissionSelected(permission.name));
  }

  /**
   * Vérifie si certaines permissions d'un module sont sélectionnées
   */
  isModulePartiallySelected(module: string): boolean {
    const modulePermissions = this.permissionsByModule[module] || [];
    const selectedCount = modulePermissions.filter(permission => 
      this.isPermissionSelected(permission.name)
    ).length;
    
    return selectedCount > 0 && selectedCount < modulePermissions.length;
  }

  /**
   * Affiche un message de succès
   */
  showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    setTimeout(() => this.successMessage = '', 5000);
  }

  /**
   * Affiche un message d'erreur
   */
  showError(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';
    setTimeout(() => this.errorMessage = '', 5000);
  }

  /**
   * Efface les messages
   */
  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  /**
   * Retourne les clés des modules pour l'itération
   */
  getModuleKeys(): string[] {
    return Object.keys(this.permissionsByModule);
  }
}

