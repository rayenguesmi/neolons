import { Component, OnInit } from '@angular/core';
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
    roles: ['USER']  // ou 'ADMIN_GENERAL' si nécessaire
  };

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (data: User[]) => this.users = data,
      error: (err: any) => this.errorMessage = "Erreur de chargement des utilisateurs"
    });
  }

  addUser(): void {
    this.userService.createUser(this.newUser).subscribe({
      next: (createdUser: User) => {
        this.users.push(createdUser);
        this.newUser = {
          firstName: '',
          lastName: '',
          email: '',
          username: '',
          isActive: true,
          roles: ['USER']
        };
      },
      error: (err: any) => this.errorMessage = "Erreur lors de l'ajout de l'utilisateur"
    });
  }

  toggleStatus(user: User): void {
    if (!user.id) return;

    this.userService.toggleUserStatus(user.id).subscribe({
      next: (updatedUser: User) => {
        user.isActive = updatedUser.isActive;
      },
      error: (err: any) => this.errorMessage = "Erreur lors du changement de statut"
    });
  }

  deleteUser(user: User): void {
    if (!user.id || !confirm("Confirmer la suppression ?")) return;

    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== user.id);
      },
      error: (err: any) => this.errorMessage = "Erreur lors de la suppression"
    });
  }
}
