import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-gestion-camp',
  templateUrl: './gestion-camp.component.html',
  styleUrls: ['./gestion-camp.component.css'],
})
export class GestionCampComponent implements OnInit {
  searchQuery: string = '';
  tableData: any[] = []; // Liste principale des campagnes/documents
  pagedTableData: any[] = []; // Données paginées
  currentPage: number = 1;
  itemsPerPage: number = 5;
  totalPages: number = 1;
  activeMenuId: number | null = null;
  menuVisible: boolean = false;
  showEditModal: boolean = false;
  showAddModal: boolean = false;
  showPopup = false;
  popupMessage: string = '';
  confirmationVisible: boolean = false;
  confirmationMessage: string = '';
  confirmationAction: () => void = () => {};
  editData: any = {};
  isEditMode: boolean = false;
  userId: string | null = null;
  roles: string[] = [];
  hasCampaignRole = false;
  showAssignDocsModal: boolean = false;
  userFiles: any[] = []; // Liste des fichiers disponibles
  selectedFiles: any[] = []; // Fichiers sélectionnés pour affectation
  campagneId: string = ''; // Define campagneId
  showDetailsModal = false;
  selectedCampaign: any = null;
  assignedDocs: string[] = [];
  sortOrder: { [key: string]: 'asc' | 'desc' } = {};
  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.extractRolesFromToken();
    this.getCampaigns();
  }

  private extractRolesFromToken(): void {
    const token = localStorage.getItem('token');
    if (token) {
      const payload = this.decodeJwtPayload(token);
      this.roles = payload?.realm_access?.roles || [];
      this.userId = payload?.sub;
      this.hasCampaignRole = this.roles.includes('ROLE_CAMPAIGN');
    } else {
      this.showPopupMessage(
        'Aucun token trouvé. Veuillez vous reconnecter.',
        'error'
      );
    }
  }

  private decodeJwtPayload(token: string): any {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace('-', '+').replace('_', '/');
    const jsonPayload = atob(base64);
    return JSON.parse(jsonPayload);
  }

  getCampaigns(): void {
    if (!this.userId) {
      this.showPopupMessage('Utilisateur non authentifié.', 'error');
      return;
    }

    this.http
      .get<any[]>(`http://localhost:8090/api/campaigns?userId=${this.userId}`)
      .subscribe({
        next: (data) => {
          this.tableData = data;
          this.updatePagedTableData();
        },
        error: (error) => {
          console.error('Erreur lors de la récupération des campagnes:', error);
          this.showPopupMessage(
            'Erreur lors du chargement des campagnes.',
            'error'
          );
        },
      });
  }

  updatePagedTableData(): void {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.pagedTableData = this.tableData.slice(startIndex, endIndex);
    this.totalPages = Math.ceil(this.tableData.length / this.itemsPerPage);
  }

  generateNewId(): number {
    return this.tableData.length > 0
      ? Math.max(...this.tableData.map((item) => item.id || 0)) + 1
      : 1;
  }

  showPopupMessage(
    message: string,
    type: 'success' | 'error' | 'info' | 'warning'
  ): void {
    this.popupMessage = `${type.toUpperCase()}: ${message}`;
    this.showPopup = true;
    setTimeout(() => (this.showPopup = false), 4000);
  }

  exportData(): void {
    this.http
      .get('http://localhost:8090/api/campaigns/export', {
        responseType: 'blob',
      })
      .subscribe({
        next: (response) => {
          const link = document.createElement('a');
          const url = window.URL.createObjectURL(response);
          link.href = url;
          link.setAttribute('download', 'campaigns.csv');
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);

          this.showPopupMessage(
            'Les campagnes ont été exportées avec succès.',
            'success'
          );
        },
        error: (error) => {
          console.error("Erreur lors de l'exportation:", error);
          this.showPopupMessage("Erreur lors de l'exportation.", 'error');
        },
      });
  }

  duplicateCampaign(item: any): void {
    if (!this.userId) {
      this.showPopupMessage('Utilisateur non authentifié.', 'error');
      return;
    }

    // Demander confirmation avant de dupliquer
    this.confirmAction('Voulez-vous vraiment dupliquer cet élément ?', () => {
      this.http
        .put<any>(
          `http://localhost:8090/api/campaigns/${item.id}/duplicate`,
          {}
        )
        .subscribe({
          next: (duplicatedCampaign) => {
            // Ajouter la campagne dupliquée à la table locale
            this.tableData.push(duplicatedCampaign);
            console.log('ID de la campagne à dupliquer :', item.id);

            // Mettre à jour les données paginées pour refléter la nouvelle ligne
            this.updatePagedTableData();

            // Afficher un message de succès
            this.showPopupMessage('Campagne dupliquée avec succès.', 'success');
          },
          error: (error) => {
            console.error('Erreur lors de la duplication:', error);
            this.showPopupMessage('Échec de la duplication.', 'error');
          },
        });
    });
  }

  openAddCamp(): void {
    this.showAddModal = true;
    this.isEditMode = false;

    this.editData = {};
  }

  closeModal(): void {
    this.showAddModal = false;
    this.showEditModal = false;
  }

  submitEditForm(): void {
    if (this.isEditMode) {
      // Si on est en mode édition, envoyer les modifications au backend
      const updatedItem = { ...this.editData }; // Récupérer les données modifiées

      this.http
        .put(
          `http://localhost:8090/api/campaigns/${updatedItem.id}`,
          updatedItem
        )
        .subscribe({
          next: () => {
            // Afficher un message de succès
            this.showPopupMessage('Campagne modifiée avec succès.', 'success');
            // Rafraîchir les campagnes après la modification
            this.getCampaigns();
          },
          error: (error) => {
            console.error('Erreur lors de la modification:', error);
            this.showPopupMessage('Erreur lors de la modification.', 'error');
          },
        });
    } else {
      // Sinon, c'est une nouvelle campagne, on l'ajoute
      if (
        !this.editData.titre ||
        !this.editData.version ||
        !this.editData.date ||
        !this.editData.nbExecution
      ) {
        this.showPopupMessage(
          'Tous les champs obligatoires doivent être remplis!',
          'error'
        );
        return;
      }

      const generatedId = this.generateNewId(); // Générer un ID entier simple
      const newItem = {
        ...this.editData,
        id: generatedId, // ID entier
      };
      this.http.post('http://localhost:8090/api/campaigns', newItem).subscribe({
        next: () => {
          this.showPopupMessage(
            'Nouvelle campagne ajoutée avec succès',
            'success'
          );
          this.getCampaigns(); // Rafraîchir les données
        },
        error: (error) => {
          console.error("Erreur lors de l'ajout de la campagne:", error);
          this.showPopupMessage(
            "Erreur lors de l'ajout de la campagne",
            'error'
          );
        },
      });
    }

    // Mettre à jour la table paginée après l'ajout ou la modification
    this.updatePagedTableData();
    this.closeModal();
  }

  openModal(item: any): void {
    this.showEditModal = true;
    this.isEditMode = true;

    this.showDetailsModal = false;
    this.editData = { ...item };
  }

  filterTable(): void {
    const query = this.searchQuery.toLowerCase().trim();
    if (query) {
      this.pagedTableData = this.tableData.filter(
        (item) =>
          item.titre.toLowerCase().includes(query) ||
          item.version.toLowerCase().includes(query) ||
          item.nbRapports.toString().includes(query) ||
          item.date.includes(query) ||
          item.nbExecution.toString().includes(query)
      );
    } else {
      this.updatePagedTableData();
    }
  }

  sortByColumn(column: string): void {
    this.sortOrder[column] = this.sortOrder[column] === 'asc' ? 'desc' : 'asc';

    const orderMultiplier = this.sortOrder[column] === 'asc' ? 1 : -1;

    this.tableData.sort((a, b) => {
      if (a[column] > b[column]) return orderMultiplier;
      if (a[column] < b[column]) return -orderMultiplier;
      return 0;
    });

    // Mettre à jour les données paginées
    this.updatePagedTableData();
  }

  toggleMenu(event: Event, id: number): void {
    event.stopPropagation();
    this.menuVisible = !this.menuVisible || this.activeMenuId !== id;
    this.activeMenuId = this.menuVisible ? id : null;
  }
  confirmDuplication(item: any): void {
    this.confirmAction('Voulez-vous vraiment dupliquer cet élément ?', () => {
      // Générer un nouvel ID unique pour la duplication
      const duplicatedItem = {
        ...item,
        id: this.generateNewId(), // Utiliser un ID unique
      };

      this.tableData.push(duplicatedItem);

      this.updatePagedTableData();

      this.showPopupMessage('Document dupliqué avec succès', 'success');

      this.closeConfirmation();
    });
  }

  // Méthode générique de confirmation qui montre le message et exécute l'action
  confirmAction(message: string, action: () => void): void {
    this.confirmationMessage = message;
    this.confirmationAction = action;
    this.confirmationVisible = true; // Afficher la popup de confirmation
  }

  confirmActivation(item: any): void {
    this.confirmAction('Voulez-vous vraiment activer cet élément ?', () => {
      item.disabled = false;
      this.showPopupMessage('Document réactivé', 'success');
    });
  }

  confirmDeactivation(item: any): void {
    this.confirmAction('Voulez-vous vraiment désactiver cet élément ?', () => {
      item.disabled = true;
      this.showPopupMessage('Document désactivé', 'info');
    });
  }

  confirmDeletion(item: any): void {
    this.confirmAction('Voulez-vous vraiment supprimer cet élément ?', () => {
      this.http
        .delete(`http://localhost:8090/api/campaigns/${item.id}`)
        .subscribe({
          next: () => {
            const index = this.tableData.indexOf(item);
            if (index !== -1) {
              this.tableData.splice(index, 1);
              this.updatePagedTableData();
            }
            this.showPopupMessage('Document supprimé avec succès.', 'success');
          },
          error: (error) => {
            console.error('Erreur lors de la suppression:', error);
            this.showPopupMessage('Erreur lors de la suppression.', 'error');
          },
        });
    });
  }

  executeTask(item: any): void {
    this.confirmAction('Voulez-vous vraiment exécuter cette tâche ?', () => {
      this.showPopupMessage(`Tâche exécutée pour ${item.titre}`, 'info');
    });
  }

  closeConfirmation(): void {
    this.confirmationVisible = false;
    this.confirmationMessage = '';
    this.confirmationAction = () => {};
  }

  proceedWithConfirmation(): void {
    if (this.confirmationAction) {
      this.confirmationAction(); // Exécuter l'action (par exemple, duplication, suppression)
    }
    this.closeConfirmation(); // Fermer la popup après avoir confirmé l'action
  }

  reloadPage(): void {
    this.showPopupMessage('La page a été rechargée', 'info');
    window.location.reload();
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagedTableData();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updatePagedTableData();
    }
  }

  assignDocuments(campagne: any): void {
    this.campagneId = campagne.id;
    this.getUserFiles(); // Récupère les fichiers utilisateur
    this.showDetailsModal = false;
    this.showAssignDocsModal = true;
  }

  getUserFiles(): void {
    if (!this.userId) {
      this.showPopupMessage('Utilisateur non authentifié', 'error');
      return;
    }

    console.log(
      `URL pour récupérer les fichiers de l'utilisateur: http://localhost:8090/api/files/${this.userId}`
    );

    this.http
      .get<any[]>(`http://localhost:8090/api/files/${this.userId}`)
      .subscribe({
        next: (files) => {
          if (files.length === 0) {
            this.showPopupMessage(
              'Aucun fichier trouvé pour cet utilisateur.',
              'info'
            );
          } else {
            console.log('Fichiers récupérés:', files);
            this.userFiles = files.map((file) => ({
              filename: file.filename,
              selected: false, // Initialisation de la sélection à false
            }));
            this.openAssignDocsModal(this.campagneId);
          }
        },
        error: (error) => {
          console.error('Erreur lors de la récupération des fichiers:', error);
          this.showPopupMessage(
            'Erreur lors de la récupération des fichiers.',
            'error'
          );
        },
      });
  }
  // Méthode pour confirmer l'affectation des documents
  confirmAssignDocs(): void {
    const selectedDocs = this.userFiles.filter((file) => file.selected); // Filtrer les fichiers sélectionnés

    if (selectedDocs.length === 0) {
      this.showPopupMessage(
        'Veuillez sélectionner des documents à affecter.',
        'warning'
      );
      return;
    }

    // Appel de l'API pour associer les fichiers à la campagne
    this.http
      .post(
        `http://localhost:8090/api/campaigns/${this.campagneId}/assignDocs`,
        selectedDocs.map((file) => file.filename) // Extraire les noms de fichiers sélectionnés
      )
      .subscribe({
        next: (response) => {
          console.log('Réponse complète du backend:', response); // Affiche la réponse complète
          this.showPopupMessage('Documents assignés avec succès.', 'success');
          this.showAssignDocsModal = false; // Fermer le modal après l'affectation réussie
        },
        error: (err) => {
          console.error("Erreur lors de l'affectation des documents:", err); // Affiche l'erreur complète
          if (err.status === 400) {
            console.error("Détails de l'erreur:", err.error);
          }
          this.showPopupMessage(
            "Erreur lors de l'affectation des documents.",
            'error'
          );
        },
      });
  }
  // Vérifie si tous les fichiers sont sélectionnés
  isAllSelected(): boolean {
    return (
      this.userFiles.length > 0 && this.userFiles.every((file) => file.selected)
    );
  }

  // Sélectionne ou désélectionne tous les fichiers
  selectAllFiles(event: Event): void {
    const target = event.target as HTMLInputElement;
    const selectAll = target.checked; // Vérifie si la case "Tout sélectionner" est cochée
    this.userFiles.forEach((file) => (file.selected = selectAll));
  }
  // Méthode pour ouvrir le modal et marquer les fichiers affectés comme sélectionnés
  openAssignDocsModal(campagneId: string): void {
    this.userFiles.forEach((file) => (file.selected = false));
    this.http
      .get<string[]>(
        `http://localhost:8090/api/campaigns/${campagneId}/assignedDocs`
      )
      .subscribe({
        next: (assignedDocs: string[]) => {
          // Mettre à jour les fichiers sélectionnés en fonction des fichiers affectés
          this.userFiles.forEach((file) => {
            file.selected = assignedDocs.includes(file.filename);
          });

          // Afficher le modal avec les fichiers déjà sélectionnés
          this.showAssignDocsModal = true;
        },
        error: (error) => {
          console.error(
            'Erreur lors de la récupération des fichiers assignés:',
            error
          );
          this.showPopupMessage(
            'Erreur lors de la récupération des fichiers assignés.',
            'error'
          );
        },
      });
  }

  // Méthode pour fermer le modal
  closeAssignDocsModal(): void {
    this.showAssignDocsModal = false;
    // Vous pouvez également réinitialiser les sélections si nécessaire
    this.userFiles.forEach((file) => {
      file.selected = false; // Réinitialiser si nécessaire
    });
  }

  showDetails(campaign: any): void {
    this.selectedCampaign = campaign;
    this.fetchAssignedDocs(campaign.id);
    this.showDetailsModal = true;
    this.showEditModal = false; // Masque la modal de modification
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedCampaign = null;
    this.assignedDocs = [];
  }

  fetchAssignedDocs(campaignId: string): void {
    this.http
      .get<string[]>(
        `http://localhost:8090/api/campaigns/${campaignId}/assignedDocs`
      )
      .subscribe({
        next: (docs) => {
          this.assignedDocs = docs;
        },
        error: (err) => {
          console.error(
            'Erreur lors de la récupération des documents assignés:',
            err
          );
          this.showPopupMessage(
            'Erreur lors de la récupération des documents assignés.',
            'error'
          );
        },
      });
  }

  closeEditModal(): void {
    this.showEditModal = false;
  }
}
