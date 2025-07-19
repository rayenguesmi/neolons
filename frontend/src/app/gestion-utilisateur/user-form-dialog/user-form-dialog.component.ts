import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { User, UserRole } from '../user.model';

export interface UserFormDialogData {
  mode: 'create' | 'edit';
  user?: User;
}

@Component({
  selector: 'app-user-form-dialog',
  templateUrl: './user-form-dialog.component.html',
  styleUrls: ['./user-form-dialog.component.css']
})
export class UserFormDialogComponent implements OnInit {
  userForm!: FormGroup;
  isEditMode: boolean;

  roleOptions = [
    { value: UserRole.ADMIN_GENERAL, label: 'Administrateur Général', icon: 'fas fa-crown', description: 'Accès complet à toutes les fonctionnalités' },
    { value: UserRole.CHEF_PROJET, label: 'Chef de Projet', icon: 'fas fa-user-tie', description: 'Gestion des projets et des équipes' },
    { value: UserRole.EYA_EXECUTANTE, label: 'Eya (Exécutante)', icon: 'fas fa-user', description: 'Exécution des campagnes et tests' }
  ];

  projectOptions = [
    { value: 'project1', label: 'Projet Alpha' },
    { value: 'project2', label: 'Projet Beta' },
    { value: 'project3', label: 'Projet Gamma' },
    { value: 'project4', label: 'Projet Delta' }
  ];

  // Validator personnalisé en arrow function pour bien binder le contexte this
  private atLeastOneRoleValidator = (control: AbstractControl): ValidationErrors | null => {
    const roles = control.value;
    return roles && roles.length > 0 ? null : { atLeastOneRole: true };
  }

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<UserFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserFormDialogData
  ) {
    this.isEditMode = data.mode === 'edit';
    this.initializeForm();
  }

  ngOnInit(): void {
    if (this.isEditMode && this.data.user) {
      this.populateForm(this.data.user);
    }
  }

  private initializeForm(): void {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-zA-Z0-9._-]+$/)]],
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      roles: [[], [Validators.required, this.atLeastOneRoleValidator]],
      projects: [[]],
      isActive: [true]
    });

    if (this.isEditMode) {
      this.userForm.get('username')?.disable();
    }
  }

  private populateForm(user: User): void {
    this.userForm.patchValue({
      username: user.username,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      roles: user.roles || [],
      projects: user.projects || [],
      isActive: user.isActive
    });
  }

  onRoleChange(role: UserRole, checked: boolean): void {
    const rolesControl = this.userForm.get('roles');
    const currentRoles = rolesControl?.value || [];

    if (checked) {
      if (!currentRoles.includes(role)) {
        rolesControl?.setValue([...currentRoles, role]);
      }
    } else {
      rolesControl?.setValue(currentRoles.filter((r: UserRole) => r !== role));
    }
  }

  isRoleSelected(role: UserRole): boolean {
    const roles = this.userForm.get('roles')?.value || [];
    return roles.includes(role);
  }

  onProjectChange(project: string, checked: boolean): void {
    const projectsControl = this.userForm.get('projects');
    const currentProjects = projectsControl?.value || [];

    if (checked) {
      if (!currentProjects.includes(project)) {
        projectsControl?.setValue([...currentProjects, project]);
      }
    } else {
      projectsControl?.setValue(currentProjects.filter((p: string) => p !== project));
    }
  }

  isProjectSelected(project: string): boolean {
    const projects = this.userForm.get('projects')?.value || [];
    return projects.includes(project);
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      const formValue = this.userForm.getRawValue(); // Inclut le username même désactivé
      this.dialogRef.close(formValue);
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  private markFormGroupTouched(): void {
    Object.keys(this.userForm.controls).forEach(key => {
      const control = this.userForm.get(key);
      control?.markAsTouched();
    });
  }

  // Getters pour simplifier le template
  get username() { return this.userForm.get('username'); }
  get email() { return this.userForm.get('email'); }
  get firstName() { return this.userForm.get('firstName'); }
  get lastName() { return this.userForm.get('lastName'); }
  get roles() { return this.userForm.get('roles'); }
  get projects() { return this.userForm.get('projects'); }
  get isActive() { return this.userForm.get('isActive'); }

  getErrorMessage(controlName: string): string {
    const control = this.userForm.get(controlName);
    if (control?.hasError('required')) return `${this.getFieldLabel(controlName)} est requis`;
    if (control?.hasError('email')) return 'Format d\'email invalide';
    if (control?.hasError('minlength')) return `Minimum ${control.errors?.['minlength'].requiredLength} caractères requis`;
    if (control?.hasError('pattern')) return 'Format invalide (lettres, chiffres, points, tirets et underscores uniquement)';
    if (control?.hasError('atLeastOneRole')) return 'Au moins un rôle doit être sélectionné';
    return '';
  }

  private getFieldLabel(controlName: string): string {
    const labels: { [key: string]: string } = {
      'username': 'Nom d\'utilisateur',
      'email': 'Email',
      'firstName': 'Prénom',
      'lastName': 'Nom',
      'roles': 'Rôles'
    };
    return labels[controlName] || controlName;
  }

  getRoleIcon(role: UserRole): string {
    return this.roleOptions.find(option => option.value === role)?.icon || 'fas fa-user';
  }

  getRoleLabel(role: UserRole): string {
    return this.roleOptions.find(option => option.value === role)?.label || role;
  }

  getRoleDescription(role: UserRole): string {
    return this.roleOptions.find(option => option.value === role)?.description || '';
  }
}
