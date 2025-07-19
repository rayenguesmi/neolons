import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-gestion-doc',
  templateUrl: './gestion-doc.component.html',
  styleUrls: ['./gestion-doc.component.css'],
})
export class GestionDocComponent implements OnInit {
  uploadedFiles: File[] = []; // Tableau pour stocker les fichiers réels
  fileDetails: Array<{ filename: string; type: string; uploadDate: string }> =
    []; // Tableau pour les informations sur les fichiers
  popupMessage = '';
  showPopup = false;
  roles: string[] = [];
  hasPdfRole = false;
  hasCsvRole = false;
  hasXmlRole = false;
  message = '';
  userId: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    console.log('Component initialized');
    this.extractRolesFromToken();
    this.getUserFiles();
  }

  // Extraction des rôles et de l'ID de l'utilisateur à partir du token JWT
  extractRolesFromToken() {
    const token = localStorage.getItem('token');
    console.log('le token', token);
    if (token) {
      const payload = this.decodeJwtPayload(token);
      this.roles = payload?.realm_access?.roles || [];
      const allowedRoles = ['ROLE_PDF', 'ROLE_CSV', 'ROLE_XML'];
      const userRoles = this.roles.filter((role) =>
        allowedRoles.includes(role)
      );

      if (userRoles.length > 0) {
        this.roles = userRoles;
      } else {
        this.roles = [];
      }

      this.hasPdfRole = this.roles.includes('ROLE_PDF');
      this.hasCsvRole = this.roles.includes('ROLE_CSV');
      this.hasXmlRole = this.roles.includes('ROLE_XML');

      this.userId = payload?.sub;
      console.log("ID de l'utilisateur extrait:", this.userId);
      this.setMessage();
    } else {
      this.message = 'Aucun token trouvé. Veuillez vous reconnecter.';
    }
  }

  // Décoder le payload du JWT
  private decodeJwtPayload(token: string): any {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace('-', '+').replace('_', '/');
    const jsonPayload = atob(base64);
    return JSON.parse(jsonPayload);
  }

  // Définir un message selon les rôles de l'utilisateur
  private setMessage() {
    if (this.hasPdfRole && this.hasCsvRole) {
      this.message =
        'Vous pouvez télécharger à la fois des fichiers PDF et CSV.';
    } else if (this.hasPdfRole) {
      this.message = 'Vous pouvez télécharger des fichiers PDF.';
    } else if (this.hasCsvRole) {
      this.message = 'Vous pouvez télécharger des fichiers CSV.';
    } else {
      this.message =
        "Vous n'avez pas de permission pour télécharger des fichiers.";
    }
  }

  // Gérer les fichiers déposés
  onFileDropped(event: DragEvent) {
    event.preventDefault();
    if (event.dataTransfer && event.dataTransfer.files.length > 0) {
      this.handleFiles(event.dataTransfer.files);
    }
  }

  // Gérer les fichiers sélectionnés
  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.handleFiles(input.files);
    }
  }

  // Traiter les fichiers (validation et ajout)
  private handleFiles(files: FileList) {
    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      if (this.isAllowedFileType(file)) {
        // Ajouter les fichiers réels au tableau `uploadedFiles` (les objets de type `File`)
        this.uploadedFiles.push(file);

        // Ajouter les informations supplémentaires du fichier au tableau `fileDetails`
        this.fileDetails.push({
          filename: file.name,
          type: file.type,
          uploadDate: new Date().toISOString(),
        });
      } else {
        this.showPopupMessage(
          `Type de fichier non autorisé : ${file.name}`,
          'warning'
        );
      }
    }
    this.uploadFiles(); // Appel à la méthode pour télécharger les fichiers après validation
  }

  // Vérification du type de fichier
  isAllowedFileType(file: File): boolean {
    const allowedExtensions = ['csv', 'xml', 'pdf'];
    const allowedTypes = [
      'text/csv',
      'application/xml',
      'application/pdf',
      'text/xml',
    ];
    const fileExtension = file.name.split('.').pop()?.toLowerCase() || '';
    return (
      allowedTypes.includes(file.type) ||
      allowedExtensions.includes(fileExtension)
    );
  }

  // Téléchargement des fichiers
  uploadFiles() {
    if (this.uploadedFiles.length === 0) {
      this.showPopupMessage(
        'Veuillez sélectionner des fichiers à télécharger.',
        'info'
      );
      return;
    }

    const formData = new FormData();

    // Ajoutez le userId au FormData
    if (this.userId) {
      formData.append('userId', this.userId);
    }

    // Ajouter les fichiers réels au FormData
    this.uploadedFiles.forEach((file) => {
      const validTypes = ['application/pdf', 'text/csv', 'application/xml'];
      if (!validTypes.includes(file.type)) {
        this.showPopupMessage('Type de fichier non autorisé.', 'error');
        return;
      }
      formData.append('file', file, file.name); // Ajoute chaque fichier en utilisant l'objet `File` réel
    });

    // Envoi des fichiers au serveur via POST
    this.http
      .post<string>('http://localhost:8090/api/upload', formData)
      .subscribe({
        next: (response: any) => {
          console.log('Réponse du serveur:', response);
          const message =
            response && response.message
              ? response.message
              : 'Téléchargement réussi'; // Utiliser la réponse directement
          this.showPopupMessage(message, 'success');
          this.uploadedFiles = []; // Réinitialiser la liste des fichiers après upload
          this.fileDetails = []; // Réinitialiser les détails des fichiers
          this.getUserFiles();
        },
        error: (error) => {
          this.showPopupMessage("Échec de l'upload.", 'error');
          console.error("Erreur lors de l'upload:", error);
        },
      });
  }

  // Récupérer les fichiers de l'utilisateur
  getUserFiles() {
    if (!this.userId) {
      this.showPopupMessage('Utilisateur non authentifié', 'error');
      return;
    }

    // Log pour vérifier l'URL générée
    console.log(
      `URL pour récupérer les fichiers de l'utilisateur: http://localhost:8090/api/files/${this.userId}`
    );

    this.http
      .get<any[]>(`http://localhost:8090/api/files/${this.userId}`)
      .subscribe({
        next: (files) => {
          if (files.length === 0) {
            // Si aucun fichier n'est trouvé, affichez un message d'information
            this.showPopupMessage(
              'Aucun fichier trouvé pour cet utilisateur.',
              'info'
            );
          } else {
            console.log('Fichiers récupérés du serveur:', files);
            this.uploadedFiles = files.map(
              (file) => new File([file], file.filename)
            );
            this.fileDetails = files.map((file) => ({
              filename: file.filename,
              type: file.type,
              uploadDate: file.uploadDate,
            }));
          }
        },
        error: (error) => {
          // Vérifiez si l'erreur est une erreur 404
          if (error.status === 404) {
            this.showPopupMessage(
              'Aucun fichier trouvé pour cet utilisateur.',
              'info'
            );
          } else {
            // Pour d'autres erreurs, affichez le message d'erreur
            console.error(
              'Erreur lors de la récupération des fichiers:',
              error
            );
            this.showPopupMessage(
              `Erreur lors de la récupération des fichiers: ${error.message}`,
              'error'
            );
          }
        },
      });
  }

  // Affichage du popup
  showPopupMessage(
    message: string,
    type: 'success' | 'error' | 'warning' | 'info'
  ) {
    this.popupMessage = `${type.toUpperCase()}: ${message}`;
    this.showPopup = true;
    setTimeout(() => (this.showPopup = false), 4000);
  }

  // Méthode pour empêcher le comportement par défaut du dragover
  preventDefault(event: DragEvent): void {
    event.preventDefault();
  }

  // Méthode pour déclencher l'input de fichier
  triggerFileInput(): void {
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.click();
    } else {
      console.error("Le bouton de téléchargement n'est pas trouvé");
    }
  }
}
