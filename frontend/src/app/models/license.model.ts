export interface License {
  id?: string;
  licenseKey: string;
  clientName: string;
  projectId?: string;
  licenseType: LicenseType;
  status: LicenseStatus;
  maxUsers: number;
  currentUsers: number;
  features?: string;
  startDate: Date;
  endDate: Date;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  lastCheck?: Date;
  alertSent?: boolean;
  daysBeforeExpiryAlert?: number;
}

export interface LicenseCreateRequest {
  licenseKey?: string;
  clientName: string;
  licenseType: LicenseType;
  maxUsers: number;
  features?: string;
  startDate: Date;
  endDate: Date;
  daysBeforeExpiryAlert?: number;
}

export interface LicenseUpdateRequest {
  licenseKey?: string;
  clientName?: string;
  licenseType?: LicenseType;
  maxUsers?: number;
  features?: string;
  startDate?: Date;
  endDate?: Date;
  daysBeforeExpiryAlert?: number;
}

export enum LicenseType {
  STANDARD = 'STANDARD',
  PREMIUM = 'PREMIUM',
  ENTERPRISE = 'ENTERPRISE'
}

export enum LicenseStatus {
  ACTIVE = 'ACTIVE',
  EXPIRED = 'EXPIRED',
  SUSPENDED = 'SUSPENDED',
  REVOKED = 'REVOKED'
}

// Correction: ajout de signature d'index pour éviter l'erreur TS7053
export const LICENSE_TYPE_LABELS: { [key: string]: string } = {
  [LicenseType.STANDARD]: 'Standard',
  [LicenseType.PREMIUM]: 'Premium',
  [LicenseType.ENTERPRISE]: 'Enterprise'
};

export const LICENSE_STATUS_LABELS: { [key: string]: string } = {
  [LicenseStatus.ACTIVE]: 'Active',
  [LicenseStatus.EXPIRED]: 'Expirée',
  [LicenseStatus.SUSPENDED]: 'Suspendue',
  [LicenseStatus.REVOKED]: 'Révoquée'
};

export interface LicenseStats {
  activeCount: number;
  expiredCount: number;
  suspendedCount: number;
  standardCount: number;
  premiumCount: number;
  enterpriseCount: number;
}
