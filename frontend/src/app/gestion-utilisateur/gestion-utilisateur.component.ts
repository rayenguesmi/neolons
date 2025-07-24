import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { UserService, User } from '../services/user.service';

@Component({
  selector: 'app-gestion-utilisateur',
  templateUrl: './gestion-utilisateur.component.html',
  styleUrls: ['./gestion-utilisateur.component.css']
})
export class GestionUtilisateurComponent implements OnInit {

  users: User[] = [];
  errorMessage: string = '';

  newUser: User = {
    firstName: '',
    lastName: '',
    email: '',
    username: '',
    isActive: true,
    roles: ['USER']
  };

  constructor(
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  // Fonction trackBy pour optimiser le *ngFor
  trackByUserId(index: number, user: User): any {
    return user.id;
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (data: User[]) => {
        this.users = data;
        console.log('Utilisateurs chargés:', this.users.length);
      },
      error: (err: any) => {
        console.error('Erreur de chargement:', err);
        this.errorMessage = "Erreur de chargement des utilisateurs";
      }
    });
  }

  addUser(): void {
    this.userService.createUser(this.newUser).subscribe({
      next: (createdUser: User) => {
        // Ajouter le nouvel utilisateur à la liste
        this.users = [...this.users, createdUser]; // Créer un nouveau tableau
        
        // Réinitialiser le formulaire
        this.newUser = {
          firstName: '',
          lastName: '',
          email: '',
          username: '',
          isActive: true,
          roles: ['USER']
        };
        
        // Effacer les erreurs
        this.errorMessage = '';
        
        console.log('Utilisateur ajouté, nouvelle taille:', this.users.length);
      },
      error: (err: any) => {
        console.error('Erreur d\'ajout:', err);
        this.errorMessage = "Erreur lors de l'ajout de l'utilisateur";
      }
    });
  }

  toggleStatus(user: User): void {
    if (!user.id) return;

    this.userService.toggleUserStatus(user.id).subscribe({
      next: (updatedUser: User) => {
        // Mettre à jour l'utilisateur dans la liste
        this.users = this.users.map(u => 
          u.id === user.id ? { ...u, isActive: updatedUser.isActive } : u
        );
        
        this.errorMessage = '';
        console.log('Statut mis à jour pour:', updatedUser);
      },
      error: (err: any) => {
        console.error('Erreur de changement de statut:', err);
        this.errorMessage = "Erreur lors du changement de statut";
      }
    });
  }

  deleteUser(user: User): void {
  if (!user.id || !confirm("Confirmer la suppression ?")) return;

  this.userService.deleteUser(user.id).subscribe({
    next: () => {
      // Conversion explicite pour éviter les problèmes de type
      const userIdToDelete = String(user.id);
      this.users = this.users.filter(u => String(u.id) !== userIdToDelete);
      
      this.errorMessage = '';
      console.log('Utilisateur supprimé, nouvelle taille:', this.users.length);
    },
    error: (err: any) => {
      console.error('Erreur lors de la suppression:', err);
      this.errorMessage = "Erreur lors de la suppression";
    }
  });
}
}