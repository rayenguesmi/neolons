export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[]; // Use UserRole[] if you always use the enum
  projects: string[];
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
  lastLogin?: Date;
  keycloakId?: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  projects?: string[];
}

export interface UpdateUserRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  roles?: string[];
  projects?: string[];
  isActive?: boolean;
}

export interface UserResponse {
  users: User[];
  total: number;
  page: number;
  size: number;
}

export enum UserRole {
  ADMIN_GENERAL = 'ADMIN_GENERAL',
  CHEF_PROJET = 'CHEF_PROJET',
  EYA_EXECUTANTE = 'EYA_EXECUTANTE'
}

export interface UserFilter {
  search?: string;
  role?: UserRole;
  isActive?: boolean;
  project?: string;
  page?: number;
  size?: number;
}
