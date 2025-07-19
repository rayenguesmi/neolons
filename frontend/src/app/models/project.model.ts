export interface Project {
  id?: string;
  name: string;
  description: string;
  code: string;
  status: ProjectStatus;
  adminIds?: string[];
  userIds?: string[];
  licenseId?: string;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  startDate?: Date;
  endDate?: Date;
}

export interface ProjectCreateRequest {
  name: string;
  description: string;
  code: string;
  startDate?: Date;
  endDate?: Date;
}

export interface ProjectUpdateRequest {
  name?: string;
  description?: string;
  code?: string;
  status?: ProjectStatus;
  startDate?: Date;
  endDate?: Date;
}

export enum ProjectStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  ARCHIVED = 'ARCHIVED'
}

export const PROJECT_STATUS_LABELS = {
  [ProjectStatus.ACTIVE]: 'Actif',
  [ProjectStatus.INACTIVE]: 'Inactif',
  [ProjectStatus.ARCHIVED]: 'Archivé'
};

