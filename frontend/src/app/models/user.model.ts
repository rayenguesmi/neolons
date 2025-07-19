export interface User {
  id?: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  projects?: string[];
  isActive: boolean;
  createdAt?: Date;
  updatedAt?: Date;
  lastLogin?: Date;
  keycloakId?: string;
}

export interface UserCreateRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  projects?: string[];
}

export interface UserUpdateRequest {
  username?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  roles?: string[];
  projects?: string[];
}

export enum UserRole {
  EYA_EXECUTANTE = 'EYA_EXECUTANTE',
  CHEF_PROJET = 'CHEF_PROJET',
  ADMIN_GENERAL = 'ADMIN_GENERAL'
}

export const USER_ROLE_LABELS = {
  [UserRole.EYA_EXECUTANTE]: 'Eya (Exécutante)',
  [UserRole.CHEF_PROJET]: 'Chef de Projet',
  [UserRole.ADMIN_GENERAL]: 'Administrateur Général'
};

