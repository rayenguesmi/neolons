import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-gestion-exec',
  templateUrl: './gestion-exec.component.html',
  styleUrls: ['./gestion-exec.component.css'],
})
export class GestionExecComponent implements OnInit {
  executions: any[] = [];

  editExecData: any = {
    nomDeCampagne: '',
    dateMiseAjour: '',
    requestedBy: '',

    status: 'En cours',
    result: 'En attente',
  };
  confirmationVisible: boolean = false;
  confirmationMessage: string = '';
  confirmationAction: () => void = () => {};
  pagedTableData: any[] = [];
  currentPage: number = 1;
  itemsPerPage: number = 8;
  totalPages: number = 1;
  showPopup = false;
  showEditModal: boolean = false;
  editData: any = {};
  showDetailsModal: boolean = false;
  popupMessage: string = '';
  userName: string = '';
  searchQuery: string = '';
  sortOrder: { [key: string]: 'asc' | 'desc' } = {};
  showAddExecModal: boolean = false;
  isEditModeExec: boolean = false;
  listexecutions: any[] = [];
  campaigns: any[] = [];
  userId: string | null = null;
  roles: string[] = [];
  hasCampaignRole = false;
  executionMenuVisible: boolean = false;
  activeExecutionMenuId: number | null = null;
  selectedCampaignName: string = '';
  isEditMode: boolean = false; // Par défaut, on est en mode création
  tableData: any[] = []; // Liste principale des campagnes
  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.extractRolesFromToken();
    this.loadCampaigns();
    this.getExecutions();
    this.updatePagedTableData();
  }
  loadCampaigns(): void {
    this.getCampaigns();
  }

  private extractRolesFromToken(): void {
    const token = localStorage.getItem('token');
    if (token) {
      const payload = this.decodeJwtPayload(token);
      this.roles = payload?.realm_access?.roles || [];
      this.userId = payload?.sub;
      this.userName =
        payload?.name || payload?.preferred_username || 'Utilisateur inconnu'; // Extraction du nom de l'utilisateur
      console.log("Nom de l'utilisateur :", this.userName);
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

  openAddExec(): void {
    this.showAddExecModal = true;
    this.isEditModeExec = false;
    this.selectedCampaignName = 'none';
    this.resetExecForm();
  }
  closeExecModal(): void {
    this.showAddExecModal = false; // Fermer le modal
    this.resetExecForm(); // Réinitialiser les données du formulaire
  }

  // Méthode pour soumettre le formulaire
  openModal(item: any): void {
    this.showAddExecModal = true;
    this.isEditModeExec = true;

    // Trouver la campagne correspondante à partir de `item.nomDeCampagne`
    const selectedCampaign = this.campaigns.find(
      (campaign) => campaign.nomDeCampagne === item.campaignId
    );

    if (selectedCampaign) {
      this.selectedCampaignName = selectedCampaign.id.toString();
    } else {
      console.warn(' Campagne non trouvée pour :', item.nomDeCampagne);
      this.selectedCampaignName = ''; // Réinitialiser si non trouvée
    }

    // Remplir les autres champs du formulaire
    this.editExecData = {
      ...item,
      dateMiseAjour:
        item.dateMiseAjour || new Date().toISOString().split('T')[0],
    };
  }

  submitExecForm(): void {
    console.log('Soumission du formulaire...');

    console.log('editExecData', this.editExecData);

    // Sélection de la campagne associée à l'exécution
    const selectedCampaign = this.campaigns.find(
      (campaign) =>
        campaign.id.toString() === this.selectedCampaignName.toString()
    );

    if (!selectedCampaign) {
      this.showPopupMessage('Campagne sélectionnée invalide.', 'error');
      return;
    }

    if (this.isEditModeExec) {
      const updatedExecItem = {
        ...this.editExecData,
        campaignId: selectedCampaign.id, // Utilise campaignId pour l'envoi
        nomDeCampagne: selectedCampaign.nomDeCampagne, // Utilise nomDeCampagne pour l'affichage
      };

      console.log('Données envoyées pour modification :', updatedExecItem);

      this.http
        .put(
          `http://localhost:8090/api/executions/${updatedExecItem.id}`,
          updatedExecItem
        )
        .subscribe({
          next: () => {
            console.log('Exécution modifiée avec succès.');
            this.showPopupMessage('Exécution modifiée avec succès.', 'success');
            this.getExecutions(); // Rafraîchir les exécutions après modification
          },
          error: (error) => {
            console.error('Erreur lors de la modification:', error);
            this.showPopupMessage(
              "Erreur lors de la modification de l'exécution.",
              'error'
            );
          },
        });
    } else {
      // Sinon, c'est une nouvelle exécution, on l'ajoute
      if (
        !this.selectedCampaignName ||
        !this.editExecData.dateMiseAjour ||
        !this.userName
      ) {
        this.showPopupMessage(
          'Tous les champs obligatoires doivent être remplis.',
          'error'
        );
        return;
      }

      // Sélection de la campagne associée à l'exécution
      const selectedCampaign = this.campaigns.find(
        (campaign) =>
          campaign.id.toString() === this.selectedCampaignName.toString()
      );

      if (!selectedCampaign) {
        this.showPopupMessage('Campagne sélectionnée invalide.', 'error');
        return;
      }

      const newExecItem = {
        id: this.generateNewId().toString(),
        nomDeCampagne: selectedCampaign.nomDeCampagne,
        dateMiseAjour: this.editExecData.dateMiseAjour, // La date envoyée depuis le frontend
        requestedBy: this.userName,
        status: this.editExecData.status,
        result: this.editExecData.result,
        userId: this.userId, // Assurez-vous d'ajouter le userId ici si nécessaire
        demandePar: this.userName, // Si nécessaire
      };
      console.log('Date après:', newExecItem.dateMiseAjour);
      console.log('Données envoyées à l’API:', newExecItem);

      // Envoi de la nouvelle exécution à l'API
      this.http
        .post('http://localhost:8090/api/executions', newExecItem)
        .subscribe({
          next: (response) => {
            console.log("Réponse de l'API:", response);
            this.showPopupMessage(
              'Nouvelle exécution ajoutée avec succès.',
              'success'
            );
            this.getExecutions(); // Rafraîchir les données
            this.showAddExecModal = false;
          },
          error: (error) => {
            console.error("Erreur lors de l'ajout de l'exécution:", error);
            this.showPopupMessage(
              "Erreur lors de l'ajout de l'exécution.",
              'error'
            );
          },
        });
    }

    // Mettre à jour la table paginée après l'ajout ou la modification
    this.updatePagedTableData();
    this.closeExecModal();
  }

  getCampaigns(): void {
    if (!this.userId) {
      this.showPopupMessage('Utilisateur non authentifié.', 'error');
      return;
    }

    this.http
      .get<any[]>(`http://localhost:8090/api/campaigns?userId=${this.userId}`)
      .subscribe({
        next: (data: any[]) => {
          console.log('Campagnes récupérées:', data);

          this.campaigns = data.map((exec) => ({
            id: exec.id,
            statut: exec.status,
            resultat: exec.result,
            nomDeCampagne: exec.titre, // Correction ici
            dateMiseAJour: exec.dateMiseAjour,
            demandePar: exec.requestedBy,
            historique: exec.historique || null,
            userId: exec.userId,
          }));
          this.updatePagedTableData();
        },
        error: (error: any) => {
          console.error('Erreur lors de la récupération des campagnes:', error);
          this.showPopupMessage(
            'Erreur lors du chargement des campagnes.',
            'error'
          );
        },
      });
  }

  generateNewId(): number {
    // Récupérer le maximum ID existant (même après suppression, vous pouvez vérifier l'ID dans la base de données)
    const newId =
      this.executions.length > 0
        ? Math.max(...this.executions.map((exec) => exec.id)) + 1
        : 1;

    return newId;
  }

  resetExecForm(): void {
    this.editExecData = {
      id: null, // Ajout de l'ID
      nomDeCampagne: '', // Remplacement de campaignId
      dateMiseAjour: '',
      requestedBy: this.userName, // Remplir avec le nom de l'utilisateur
      status: 'En cours',
      result: 'En attente',
    };
    this.editExecData = {}; // Réinitialiser les données de l'exécution
  }
  getExecutions(): void {
    this.http
      .get<any[]>(`http://localhost:8090/api/executions?userId=${this.userId}`)
      .subscribe({
        next: (data: any[]) => {
          console.log('Données des exécutions récupérées:', data);

          // Vérifier le maximum d'ID
          const maxId = Math.max(...data.map((exec) => exec.id));

          this.executions = data.map((execution) => {
            // Conversion de la date en format "yyyy-MM-dd"
            const formattedDate = execution.dateMiseAjour
              ? new Date(execution.dateMiseAjour).toISOString().split('T')[0] // "yyyy-MM-dd"
              : null;

            return {
              id: execution.id,
              campaignId: execution.nomDeCampagne,
              dateMiseAjour: formattedDate, // Utilise la date formatée ici
              status: execution.statut,
              requestedBy: execution.demandePar,
              result: execution.resultat,
            };
          });

          this.updatePagedTableData();
        },
        error: (error) => {
          console.error(
            'Erreur lors de la récupération des exécutions:',
            error
          );
          this.showPopupMessage(
            'Erreur lors de la récupération des exécutions.',
            'error'
          );
        },
      });
  }

  toggleExecutionMenu(event: MouseEvent, executionId: number): void {
    event.stopPropagation();
    this.activeExecutionMenuId =
      this.activeExecutionMenuId === executionId ? null : executionId;
    this.executionMenuVisible = this.activeExecutionMenuId !== null;
  }

  sortByColumn(column: string): void {
    this.sortOrder[column] = this.sortOrder[column] === 'asc' ? 'desc' : 'asc';
    const orderMultiplier = this.sortOrder[column] === 'asc' ? 1 : -1;

    this.executions.sort((a: Record<string, any>, b: Record<string, any>) => {
      if (a[column] > b[column]) return orderMultiplier;
      if (a[column] < b[column]) return -orderMultiplier;
      return 0;
    });

    this.updatePagedTableData();
  }

  updatePagedTableData(): void {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.pagedTableData = this.executions.slice(startIndex, endIndex);
    this.totalPages = Math.ceil(this.executions.length / this.itemsPerPage);
  }

  putOnHoldExecution(execution: any): void {
    console.log('Exécution mise en attente', execution);
    execution.status = 'En attente';
  }

  deleteExecution(execution: any): void {
    const index = this.executions.findIndex((e) => e.id === execution.id);

    if (index !== -1) {
      // Supprimer l'exécution localement
      this.executions.splice(index, 1);
      this.confirmAction('Voulez-vous vraiment supprimer cet élément ?', () => {
        // Supprimer l'exécution côté serveur
        this.http
          .delete(`http://localhost:8090/api/executions/${execution.id}`)
          .subscribe({
            next: () => {
              console.log(`Exécution ${execution.id} supprimée avec succès.`);
              this.showPopupMessage(
                'Exécution supprimée avec succès.',
                'success'
              );
            },
            error: (error) => {
              console.error('Erreur lors de la suppression:', error);
              this.showPopupMessage(
                "Erreur lors de la suppression de l'exécution.",
                'error'
              );

              // Si erreur de suppression côté serveur, on réajoute l'exécution à la liste locale
              this.executions.splice(index, 0, execution);
            },
          });
      });
      this.getExecutions();
    }

  }
  closeConfirmation(): void {
    this.confirmationVisible = false;
    this.confirmationMessage = '';
    this.confirmationAction = () => {};
  }
  // Méthode générique de confirmation qui montre le message et exécute l'action
  confirmAction(message: string, action: () => void): void {
    this.confirmationMessage = message;
    this.confirmationAction = action;
    this.confirmationVisible = true; // Afficher la popup de confirmation
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

  showPopupMessage(
    message: string,
    type: 'success' | 'error' | 'info' | 'warning'
  ): void {
    this.popupMessage = `${type.toUpperCase()}: ${message}`;
    this.showPopup = true;
    setTimeout(() => (this.showPopup = false), 4000);
  }

  filterTable(): void {
    const query = this.searchQuery.toLowerCase().trim();
    if (query) {
      this.executions = this.executions.filter(
        (execution) =>
          execution.campaignId.toLowerCase().includes(query) ||
          execution.dateMiseAjour.includes(query) ||
          execution.status.toLowerCase().includes(query) ||
          execution.requestedBy.toLowerCase().includes(query) ||
          execution.result.toLowerCase().includes(query)
      );
    }
  }
  exportData(): void {
    this.http
      .get('http://localhost:8090/api/executions/export', {
        responseType: 'blob',
      })
      .subscribe({
        next: (response) => {
          const link = document.createElement('a');
          const url = window.URL.createObjectURL(response);
          link.href = url;
          link.setAttribute('download', 'executions.csv'); // Change file name to 'executions.csv'
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);

          this.showPopupMessage(
            'Les exécutions ont été exportées avec succès.',
            'success'
          );
        },
        error: (error) => {
          console.error("Erreur lors de l'exportation:", error);
          this.showPopupMessage("Erreur lors de l'exportation.", 'error');
        },
      });
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
}
