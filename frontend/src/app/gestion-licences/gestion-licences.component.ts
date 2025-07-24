import { Component, OnInit } from '@angular/core';
import { License, LicenseService, LicenseStatistics, LicenseValidationResult } from '../services/license.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-gestion-licences',
  templateUrl: './gestion-licences.component.html',
  styleUrls: ['./gestion-licences.component.css']
})
export class GestionLicencesComponent implements OnInit {
  licenses: License[] = [];
  filteredLicenses: License[] = [];
  statistics: LicenseStatistics | null = null;
  
  // Formulaire de création/modification
  showForm = false;
  editingLicense: License | null = null;
  newLicense: License = this.initializeNewLicense();
  
  // Filtres et recherche
  searchTerm = '';
  filterType = 'ALL';
  filterStatus = 'ALL';
  
  // Validation de licence
  showValidationForm = false;
  validationKey = '';
  validationResult: LicenseValidationResult | null = null;
  
  // Messages
  message = '';
  messageType: 'success' | 'error' | 'info' = 'info';
  
  // Types de licence disponibles
  licenseTypes = ['STANDARD', 'PREMIUM', 'ENTERPRISE', 'TRIAL'];
  
  // Fonctionnalités disponibles
  availableFeatures = [
    'USER_MANAGEMENT',
    'PROJECT_MANAGEMENT', 
    'BASIC_REPORTING',
    'ADVANCED_REPORTING',
    'API_ACCESS',
    'CUSTOM_ROLES',
    'AUDIT_LOGS',
    'BACKUP_RESTORE',
    'MULTI_TENANT',
    'SSO_INTEGRATION'
  ];

  constructor(private licenseService: LicenseService) { }

  ngOnInit(): void {
    this.loadLicenses();
    this.loadStatistics();
  }

  // Chargement des données
  loadLicenses(): void {
    this.licenseService.getAllLicenses().subscribe({
      next: (licenses) => {
        this.licenses = licenses;
        this.applyFilters();
      },
      error: (error) => {
        this.showMessage('Erreur lors du chargement des licences', 'error');
        console.error('Erreur:', error);
      }
    });
  }

  loadStatistics(): void {
    this.licenseService.getLicenseStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des statistiques:', error);
      }
    });
  }

  // Filtrage et recherche
  applyFilters(): void {
    this.filteredLicenses = this.licenses.filter(license => {
      // Filtre par terme de recherche
      const matchesSearch = !this.searchTerm || 
        license.licenseName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        license.customerName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (license.organization && license.organization.toLowerCase().includes(this.searchTerm.toLowerCase())) ||
        (license.licenseKey && license.licenseKey.toLowerCase().includes(this.searchTerm.toLowerCase()));

      // Filtre par type
      const matchesType = this.filterType === 'ALL' || license.licenseType === this.filterType;

      // Filtre par statut
      let matchesStatus = true;
      if (this.filterStatus === 'ACTIVE') {
        matchesStatus = license.isActive === true;
      } else if (this.filterStatus === 'INACTIVE') {
        matchesStatus = license.isActive === false;
      } else if (this.filterStatus === 'EXPIRED') {
        matchesStatus = license.expired === true;
      } else if (this.filterStatus === 'TRIAL') {
        matchesStatus = license.isTrial === true;
      }

      return matchesSearch && matchesType && matchesStatus;
    });
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  // Gestion du formulaire
  showCreateForm(): void {
    this.editingLicense = null;
    this.newLicense = this.initializeNewLicense();
    this.showForm = true;
  }

  showEditForm(license: License): void {
    this.editingLicense = license;
    this.newLicense = { ...license };
    this.showForm = true;
  }

  hideForm(): void {
    this.showForm = false;
    this.editingLicense = null;
    this.newLicense = this.initializeNewLicense();
  }

  // CRUD Operations
  saveLicense(): void {
    if (this.editingLicense) {
      // Modification
      this.licenseService.updateLicense(this.editingLicense.id!, this.newLicense).subscribe({
        next: (updatedLicense) => {
          this.showMessage('Licence mise à jour avec succès', 'success');
          this.loadLicenses();
          this.hideForm();
        },
        error: (error) => {
          this.showMessage('Erreur lors de la mise à jour de la licence', 'error');
          console.error('Erreur:', error);
        }
      });
    } else {
      // Création
      this.licenseService.createLicense(this.newLicense).subscribe({
        next: (createdLicense) => {
          this.showMessage('Licence créée avec succès', 'success');
          this.loadLicenses();
          this.loadStatistics();
          this.hideForm();
        },
        error: (error) => {
          this.showMessage('Erreur lors de la création de la licence', 'error');
          console.error('Erreur:', error);
        }
      });
    }
  }

  deleteLicense(license: License): void {
    Swal.fire({
      title: 'Confirmer la suppression',
      html: `
        <div class="text-center">
          <div class="mb-3">
            <i class="fas fa-exclamation-triangle text-warning" style="font-size: 3rem;"></i>
          </div>
          <p class="mb-2">Êtes-vous sûr de vouloir supprimer cette licence ?</p>
          <div class="alert alert-info mt-3">
            <strong>Licence :</strong> ${license.licenseName}<br>
            <strong>Client :</strong> ${license.customerName}<br>
            <strong>Type :</strong> ${license.licenseType}
          </div>
          <p class="text-danger small mt-2">
            <i class="fas fa-warning"></i> Cette action est irréversible
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
        this.licenseService.deleteLicense(license.id!).subscribe({
          next: () => {
            Swal.fire({
              title: 'Suppression réussie !',
              text: `La licence "${license.licenseName}" a été supprimée avec succès.`,
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
            this.loadLicenses();
            this.loadStatistics();
          },
          error: (error) => {
            Swal.fire({
              title: 'Erreur de suppression',
              text: 'Une erreur est survenue lors de la suppression de la licence.',
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

  // Actions sur les licences
  toggleLicenseStatus(license: License): void {
    if (license.isActive) {
      this.licenseService.deactivateLicense(license.id!).subscribe({
        next: (updatedLicense) => {
          Swal.fire({
            title: 'Licence désactivée',
            text: `La licence "${license.licenseName}" a été désactivée avec succès.`,
            icon: 'success',
            confirmButtonColor: '#28a745',
            timer: 2000,
            timerProgressBar: true,
            showConfirmButton: false
          });
          this.loadLicenses();
        },
        error: (error) => {
          Swal.fire({
            title: 'Erreur',
            text: 'Erreur lors de la désactivation de la licence.',
            icon: 'error',
            confirmButtonColor: '#dc3545'
          });
          console.error('Erreur:', error);
        }
      });
    } else {
      // Pour réactiver, on peut utiliser une mise à jour
      const updatedLicense = { ...license, isActive: true };
      this.licenseService.updateLicense(license.id!, updatedLicense).subscribe({
        next: (result) => {
          Swal.fire({
            title: 'Licence réactivée',
            text: `La licence "${license.licenseName}" a été réactivée avec succès.`,
            icon: 'success',
            confirmButtonColor: '#28a745',
            timer: 2000,
            timerProgressBar: true,
            showConfirmButton: false
          });
          this.loadLicenses();
        },
        error: (error) => {
          Swal.fire({
            title: 'Erreur',
            text: 'Erreur lors de la réactivation de la licence.',
            icon: 'error',
            confirmButtonColor: '#dc3545'
          });
          console.error('Erreur:', error);
        }
      });
    }
  }

  // Validation de licence
  showValidationDialog(): void {
    this.showValidationForm = true;
    this.validationKey = '';
    this.validationResult = null;
  }

  hideValidationDialog(): void {
    this.showValidationForm = false;
    this.validationKey = '';
    this.validationResult = null;
  }

  validateLicense(): void {
    if (!this.validationKey.trim()) {
      Swal.fire({
        title: 'Clé manquante',
        text: 'Veuillez saisir une clé de licence à valider.',
        icon: 'warning',
        confirmButtonColor: '#ffc107'
      });
      return;
    }

    this.licenseService.validateLicense(this.validationKey).subscribe({
      next: (result) => {
        this.validationResult = result;
        if (result.valid) {
          Swal.fire({
            title: 'Licence valide !',
            text: 'La clé de licence est valide et active.',
            icon: 'success',
            confirmButtonColor: '#28a745'
          });
        } else {
          Swal.fire({
            title: 'Licence invalide',
            text: `La clé de licence n'est pas valide : ${result.message}`,
            icon: 'error',
            confirmButtonColor: '#dc3545'
          });
        }
      },
      error: (error) => {
        Swal.fire({
          title: 'Erreur de validation',
          text: 'Une erreur est survenue lors de la validation de la licence.',
          icon: 'error',
          confirmButtonColor: '#dc3545'
        });
        console.error('Erreur:', error);
      }
    });
  }

  // Gestion des fonctionnalités
  toggleFeature(feature: string): void {
    if (!this.newLicense.features) {
      this.newLicense.features = [];
    }

    const index = this.newLicense.features.indexOf(feature);
    if (index > -1) {
      this.newLicense.features.splice(index, 1);
    } else {
      this.newLicense.features.push(feature);
    }
  }

  isFeatureSelected(feature: string): boolean {
    return this.newLicense.features?.includes(feature) || false;
  }

  // Utilitaires
  initializeNewLicense(): License {
    return {
      licenseName: '',
      licenseType: 'STANDARD',
      productName: 'Neo.TM',
      productVersion: '1.0.0',
      customerName: '',
      customerEmail: '',
      organization: '',
      maxUsers: 10,
      maxProjects: 5,
      features: ['USER_MANAGEMENT', 'PROJECT_MANAGEMENT'],
      isActive: true,
      isTrial: false
    };
  }

  showMessage(text: string, type: 'success' | 'error' | 'info'): void {
    this.message = text;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }

  formatDate(date: Date | string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('fr-FR');
  }

  getStatusClass(license: License): string {
    if (license.expired) return 'status-expired';
    if (!license.isActive) return 'status-inactive';
    if (license.isTrial) return 'status-trial';
    return 'status-active';
  }

  getStatusText(license: License): string {
    if (license.expired) return 'Expirée';
    if (!license.isActive) return 'Inactive';
    if (license.isTrial) return 'Essai';
    return 'Active';
  }

  getDaysUntilExpiryClass(days: number | undefined): string {
    if (!days) return '';
    if (days < 0) return 'text-danger';
    if (days < 30) return 'text-warning';
    return 'text-success';
  }
}
