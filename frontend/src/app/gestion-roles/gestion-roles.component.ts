import { Component, OnInit } from '@angular/core';
import { Role, RoleService } from '../services/role.service';
import { Permission, PermissionService } from '../services/permission.service';
import Swal from 'sweetalert2';

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
      Swal.fire({
        title: 'Action non autorisée',
        text: 'Impossible de modifier un rôle système.',
        icon: 'warning',
        confirmButtonColor: '#ffc107',
        confirmButtonText: 'Compris',
        customClass: {
          popup: 'swal2-modern-popup',
          confirmButton: 'btn btn-warning'
        },
        buttonsStyling: false
      });
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
      Swal.fire({
        title: 'Champ requis',
        text: 'Le nom du rôle est obligatoire.',
        icon: 'warning',
        confirmButtonColor: '#ffc107'
      });
      return false;
    }

    if (!this.currentRole.displayName.trim()) {
      Swal.fire({
        title: 'Champ requis',
        text: 'Le nom d\'affichage est obligatoire.',
        icon: 'warning',
        confirmButtonColor: '#ffc107'
      });
      return false;
    }

    if (!this.currentRole.description.trim()) {
      Swal.fire({
        title: 'Champ requis',
        text: 'La description est obligatoire.',
        icon: 'warning',
        confirmButtonColor: '#ffc107'
      });
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
        Swal.fire({
          title: 'Rôle créé !',
          text: `Le rôle "${this.currentRole.displayName}" a été créé avec succès.`,
          icon: 'success',
          confirmButtonColor: '#28a745',
          timer: 2500,
          timerProgressBar: true,
          showConfirmButton: false
        });
        this.loadRoles();
        this.cancelForm();
      },
      error: (error) => {
        Swal.fire({
          title: 'Erreur de création',
          text: 'Erreur lors de la création du rôle : ' + (error.error || error.message),
          icon: 'error',
          confirmButtonColor: '#dc3545'
        });
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
        Swal.fire({
          title: 'Rôle modifié !',
          text: `Le rôle "${this.currentRole.displayName}" a été modifié avec succès.`,
          icon: 'success',
          confirmButtonColor: '#28a745',
          timer: 2500,
          timerProgressBar: true,
          showConfirmButton: false
        });
        this.loadRoles();
        this.cancelForm();
      },
      error: (error) => {
        Swal.fire({
          title: 'Erreur de modification',
          text: 'Erreur lors de la modification du rôle : ' + (error.error || error.message),
          icon: 'error',
          confirmButtonColor: '#dc3545'
        });
        console.error('Erreur:', error);
      }
    });
  }

  /**
   * Active ou désactive un rôle
   */
  toggleRoleStatus(role: Role): void {
    if (role.isSystemRole) {
      Swal.fire({
        title: 'Action non autorisée',
        text: 'Impossible de modifier le statut d\'un rôle système.',
        icon: 'warning',
        confirmButtonColor: '#ffc107',
        confirmButtonText: 'Compris',
        customClass: {
          popup: 'swal2-modern-popup',
          confirmButton: 'btn btn-warning'
        },
        buttonsStyling: false
      });
      return;
    }

    if (!role.id) return;

    const action = role.isActive ? 'désactiver' : 'activer';
    const actionPast = role.isActive ? 'désactivé' : 'activé';

    Swal.fire({
      title: `${action.charAt(0).toUpperCase() + action.slice(1)} le rôle`,
      text: `Voulez-vous vraiment ${action} le rôle "${role.displayName}" ?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: role.isActive ? '#dc3545' : '#28a745',
      cancelButtonColor: '#6c757d',
      confirmButtonText: `Oui, ${action}`,
      cancelButtonText: 'Annuler',
      customClass: {
        popup: 'swal2-modern-popup',
        confirmButton: 'btn',
        cancelButton: 'btn btn-secondary'
      },
      buttonsStyling: false
    }).then((result) => {
      if (result.isConfirmed) {
        this.roleService.toggleRoleStatus(role.id!).subscribe({
          next: (updatedRole) => {
            Swal.fire({
              title: `Rôle ${actionPast} !`,
              text: `Le rôle "${role.displayName}" a été ${actionPast} avec succès.`,
              icon: 'success',
              confirmButtonColor: '#28a745',
              timer: 2000,
              timerProgressBar: true,
              showConfirmButton: false
            });
            this.loadRoles();
          },
          error: (error) => {
            Swal.fire({
              title: 'Erreur',
              text: 'Erreur lors du changement de statut : ' + (error.error || error.message),
              icon: 'error',
              confirmButtonColor: '#dc3545'
            });
            console.error('Erreur:', error);
          }
        });
      }
    });
  }

  /**
   * Supprime un rôle
   */
  deleteRole(role: Role): void {
    if (role.isSystemRole) {
      Swal.fire({
        title: 'Action non autorisée',
        text: 'Impossible de supprimer un rôle système.',
        icon: 'warning',
        confirmButtonColor: '#ffc107',
        confirmButtonText: 'Compris',
        customClass: {
          popup: 'swal2-modern-popup',
          confirmButton: 'btn btn-warning'
        },
        buttonsStyling: false
      });
      return;
    }

    if (!role.id) return;

    Swal.fire({
      title: 'Confirmer la suppression',
      html: `
        <div class="text-center">
          <div class="mb-3">
            <i class="fas fa-user-shield text-danger" style="font-size: 3rem;"></i>
          </div>
          <p class="mb-2">Êtes-vous sûr de vouloir supprimer ce rôle ?</p>
          <div class="alert alert-info mt-3">
            <strong>Rôle :</strong> ${role.displayName}<br>
            <strong>Description :</strong> ${role.description}<br>
            <strong>Permissions :</strong> ${role.permissions.length} permission(s)
          </div>
          <p class="text-danger small mt-2">
            <i class="fas fa-exclamation-triangle"></i> Cette action est irréversible et peut affecter les utilisateurs ayant ce rôle
          </p>
        </div>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d',
      confirmButtonText: '<i class="fas fa-trash"></i> Supprimer',
      cancelButtonText: '<i class="fas fa-times"></i> Annuler',
      reverseButtons: true,
      focusCancel: true,
      customClass: {
        popup: 'swal2-modern-popup',
        title: 'swal2-modern-title',
        confirmButton: 'btn btn-danger',
        cancelButton: 'btn btn-secondary'
      },
      buttonsStyling: false
    }).then((result) => {
      if (result.isConfirmed) {
        this.roleService.deleteRole(role.id!).subscribe({
          next: () => {
            Swal.fire({
              title: 'Suppression réussie !',
              text: `Le rôle "${role.displayName}" a été supprimé avec succès.`,
              icon: 'success',
              confirmButtonColor: '#28a745',
              confirmButtonText: 'OK',
              customClass: {
                popup: 'swal2-modern-popup',
                confirmButton: 'btn btn-success'
              },
              buttonsStyling: false,
              timer: 3000,
              timerProgressBar: true
            });
            this.loadRoles();
          },
          error: (error) => {
            Swal.fire({
              title: 'Erreur de suppression',
              text: 'Erreur lors de la suppression : ' + (error.error || error.message),
              icon: 'error',
              confirmButtonColor: '#dc3545',
              confirmButtonText: 'OK',
              customClass: {
                popup: 'swal2-modern-popup',
                confirmButton: 'btn btn-danger'
              },
              buttonsStyling: false
            });
            console.error('Erreur:', error);
          }
        });
      }
    });
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
