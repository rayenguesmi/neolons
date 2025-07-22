export interface AuditChange {
  id: string;
  auditLogId: string;
  fieldName: string;
  oldValue: string;
  newValue: string;
  dataType: string;
  timestamp: Date;
}

export interface AuditLogExtended {
  id: string;
  userId: string;
  username: string;
  action: string;
  resourceType: string;
  resourceId: string;
  details: string;
  ipAddress: string;
  timestamp: Date;
  success: boolean;
  errorMessage?: string;
  changes?: AuditChange[];
  parentLogId?: string;
  metadata?: { [key: string]: any };
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  tags?: string[];
  userAgent?: string;
  sessionId?: string;
  projectId?: string;
}

export interface AuditAnalytics {
  timeSeriesData: TimeSeriesPoint[];
  topUsers: UserActivity[];
  actionDistribution: ActionCount[];
  resourceTypeDistribution: ResourceTypeCount[];
  riskLevelDistribution: RiskLevelCount[];
  hourlyActivity: HourlyActivity[];
  anomalies: AuditAnomaly[];
}

export interface TimeSeriesPoint {
  timestamp: Date;
  count: number;
  successCount: number;
  failureCount: number;
}

export interface UserActivity {
  userId: string;
  username: string;
  actionCount: number;
  lastActivity: Date;
  riskScore: number;
}

export interface ActionCount {
  action: string;
  count: number;
  percentage: number;
}

export interface ResourceTypeCount {
  resourceType: string;
  count: number;
  percentage: number;
}

export interface RiskLevelCount {
  riskLevel: string;
  count: number;
  percentage: number;
}

export interface HourlyActivity {
  hour: number;
  count: number;
  averageResponseTime: number;
}

export interface AuditAnomaly {
  id: string;
  type: 'UNUSUAL_ACTIVITY' | 'FAILED_ATTEMPTS' | 'SUSPICIOUS_PATTERN' | 'HIGH_VOLUME';
  description: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  timestamp: Date;
  userId?: string;
  resourceType?: string;
  details: { [key: string]: any };
}

export interface AuditAlert {
  id: string;
  name: string;
  description: string;
  conditions: AlertCondition[];
  actions: AlertAction[];
  enabled: boolean;
  lastTriggered?: Date;
  triggerCount: number;
}

export interface AlertCondition {
  field: string;
  operator: 'EQUALS' | 'NOT_EQUALS' | 'GREATER_THAN' | 'LESS_THAN' | 'CONTAINS' | 'REGEX';
  value: string;
  timeWindow?: number; // minutes
}

export interface AlertAction {
  type: 'EMAIL' | 'WEBHOOK' | 'LOG' | 'NOTIFICATION';
  configuration: { [key: string]: any };
}

export interface AuditFilter {
  id?: string;
  name: string;
  description?: string;
  conditions: FilterCondition[];
  saved: boolean;
  userId?: string;
}

export interface FilterCondition {
  field: string;
  operator: 'EQUALS' | 'NOT_EQUALS' | 'CONTAINS' | 'NOT_CONTAINS' | 'GREATER_THAN' | 'LESS_THAN' | 'BETWEEN' | 'IN' | 'NOT_IN';
  value: any;
  logicalOperator?: 'AND' | 'OR';
}

