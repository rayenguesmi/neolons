import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpEventType, HttpResponse } from '@angular/common/http';
import { DocumentService } from '../../services/document.service';
// ou '../../document.service' selon l'arborescence

@Component({
  selector: 'app-upload-document',
  templateUrl: './upload-document.component.html',
})
export class UploadDocumentComponent implements OnInit {
  selectedFile: File | null = null;
  nomDocument = '';
  typeDocument = '';
  uploadedBy = 'rh-user';

  message = '';
  fileDetails: Array<{ filename: string; type: string; uploadDate: Date }> = [];

  constructor(private documentService: DocumentService) {}

  ngOnInit(): void {
    // Optionnel : charger la liste des fichiers déjà uploadés si API dispo
    // this.loadUploadedFiles();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    } else {
      this.selectedFile = null;
    }
  }

  onUpload(): void {
    if (!this.selectedFile || !this.nomDocument || !this.typeDocument) {
      this.message = 'Veuillez remplir tous les champs et sélectionner un fichier';
      return;
    }
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('nom', this.nomDocument);
    formData.append('type', this.typeDocument);
    formData.append('uploadedBy', this.uploadedBy);

    this.documentService.uploadDocument(formData).subscribe(
      (event: HttpEvent<any>) => {
        if (event.type === HttpEventType.UploadProgress) {
          this.message = `Progression : ${Math.round(
            (event.loaded / (event.total ?? 1)) * 100
          )} %`;
        } else if (event instanceof HttpResponse) {
          this.message = 'Fichier importé avec succès';
          this.fileDetails.push({
            filename: this.selectedFile!.name,
            type: this.typeDocument,
            uploadDate: new Date(),
          });
          this.nomDocument = '';
          this.typeDocument = '';
          this.selectedFile = null;
          // Reset input file pour pouvoir re-sélectionner même fichier si besoin
          const fileInput = document.getElementById('fileInput') as HTMLInputElement;
          if (fileInput) {
            fileInput.value = '';
          }
        }
      },
      (error: any) => {
        this.message = 'Erreur lors de l\'upload : ' + error.message;
      }
    );
  }

  // Méthode optionnelle si tu veux récupérer la liste des fichiers côté serveur
  /*
  loadUploadedFiles(): void {
    this.documentService.getUploadedFiles().subscribe(
      data => this.fileDetails = data,
      err => console.error(err)
    );
  }
  */
}
