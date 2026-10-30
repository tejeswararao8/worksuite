// ─── Common ───────────────────────────────────────────────────────────────────
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  traceId?: string;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface SelectOption {
  id: string;
  name: string;
  code?: string;
}

// ─── Auth ─────────────────────────────────────────────────────────────────────
export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthUser {
  userId: string;
  companyId: string;
  email: string;
  role: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  companyId: string;
  email: string;
  role: string;
}

// ─── Employee ─────────────────────────────────────────────────────────────────
export type Gender = 'MALE' | 'FEMALE' | 'OTHER';
export type MaritalStatus = 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
export type EmploymentStatus = 'ACTIVE' | 'PROBATION' | 'ON_LEAVE' | 'RESIGNED' | 'TERMINATED' | 'RETIRED';
export type EmploymentType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERN';

export interface Employee {
  id: string;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  mobile?: string;
  alternateMobile?: string;
  gender?: Gender;
  dateOfBirth?: string;
  nationality?: string;
  maritalStatus?: MaritalStatus;
  bloodGroup?: string;
  photoUrl?: string;
  currentAddress?: string;
  permanentAddress?: string;
  joiningDate: string;
  branchId?: string;
  departmentId?: string;
  teamId?: string;
  designationId?: string;
  managerId?: string;
  workLocation?: string;
  employmentStatus: EmploymentStatus;
  employmentType?: EmploymentType;
  createdAt: string;
}

export interface CreateEmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
  mobile?: string;
  gender?: Gender;
  dateOfBirth?: string;
  nationality?: string;
  maritalStatus?: MaritalStatus;
  bloodGroup?: string;
  joiningDate: string;
  branchId?: string;
  departmentId: string;
  teamId?: string;
  designationId: string;
  managerId?: string;
  workLocation?: string;
  employmentType?: EmploymentType;
  currentAddress?: string;
  permanentAddress?: string;
}

export interface UpdateEmployeeRequest {
  firstName?: string;
  lastName?: string;
  mobile?: string;
  alternateMobile?: string;
  gender?: Gender;
  dateOfBirth?: string;
  nationality?: string;
  maritalStatus?: MaritalStatus;
  bloodGroup?: string;
  currentAddress?: string;
  permanentAddress?: string;
  workLocation?: string;
}

export interface EmployeeSummary {
  id: string;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  employmentStatus: EmploymentStatus;
}

// ─── Branch ───────────────────────────────────────────────────────────────────
export interface Branch {
  id: string;
  name: string;
  code: string;
  address?: string;
  city?: string;
  country?: string;
  phone?: string;
  email?: string;
  headOffice: boolean;
  employeeCount: number;
  createdAt: string;
}

export interface BranchSummary {
  id: string;
  name: string;
  code: string;
  city?: string;
  country?: string;
}

export interface CreateBranchRequest {
  name: string;
  code: string;
  address?: string;
  city?: string;
  country?: string;
  phone?: string;
  email?: string;
  headOffice?: boolean;
}

// ─── Department ───────────────────────────────────────────────────────────────
export interface Department {
  id: string;
  name: string;
  code: string;
  description?: string;
  parentDepartmentId?: string;
  parentDepartmentName?: string;
  headEmployeeId?: string;
  headEmployeeName?: string;
  employeeCount: number;
  teamCount: number;
  createdAt: string;
}

export interface DepartmentSummary {
  id: string;
  name: string;
  code: string;
  parentDepartmentId?: string;
}

export interface OrgNode {
  id: string;
  name: string;
  code: string;
  headEmployeeName?: string;
  employeeCount: number;
  children: OrgNode[];
}

export interface CreateDepartmentRequest {
  name: string;
  code: string;
  description?: string;
  parentDepartmentId?: string;
  headEmployeeId?: string;
}

// ─── Designation ──────────────────────────────────────────────────────────────
export interface Designation {
  id: string;
  title: string;
  description?: string;
  level?: number;
  grade?: string;
  employeeCount: number;
  createdAt: string;
}

export interface DesignationSummary {
  id: string;
  title: string;
  level?: number;
  grade?: string;
}

export interface CreateDesignationRequest {
  title: string;
  description?: string;
  level?: number;
  grade?: string;
}

// ─── Team ─────────────────────────────────────────────────────────────────────
export interface Team {
  id: string;
  name: string;
  description?: string;
  departmentId: string;
  departmentName?: string;
  leadEmployeeId?: string;
  leadEmployeeName?: string;
  memberCount: number;
  createdAt: string;
}

export interface TeamSummary {
  id: string;
  name: string;
  departmentId: string;
}

export interface TeamMember {
  employeeId: string;
  employeeCode: string;
  firstName: string;
  lastName: string;
  designation?: string;
}

export interface CreateTeamRequest {
  name: string;
  description?: string;
  departmentId: string;
  leadEmployeeId?: string;
}

// ─── Emergency Contact ────────────────────────────────────────────────────────
export interface EmergencyContact {
  id: string;
  employeeId: string;
  name: string;
  relationship: string;
  phone: string;
  alternatePhone?: string;
  email?: string;
  address?: string;
  isPrimary: boolean;
}

// ─── Bank Detail ──────────────────────────────────────────────────────────────
export interface BankDetail {
  id: string;
  employeeId: string;
  bankName: string;
  accountHolderName: string;
  accountNumber: string;
  iban?: string;
  swiftCode?: string;
  branchName?: string;
  currency: string;
  isPrimary: boolean;
}

// ─── Probation ────────────────────────────────────────────────────────────────
export type ProbationStatus = 'IN_PROGRESS' | 'CONFIRMED' | 'EXTENDED' | 'REJECTED';

export interface Probation {
  id: string;
  employeeId: string;
  employeeName?: string;
  employeeCode?: string;
  startDate: string;
  endDate: string;
  extendedEndDate?: string;
  status: ProbationStatus;
  remarks?: string;
  approvedBy?: string;
  approvedAt?: string;
  daysRemaining: number;
}

// ─── Transfer ─────────────────────────────────────────────────────────────────
export type TransferType = 'DEPARTMENT' | 'BRANCH' | 'MANAGER' | 'TEAM';
export type TransferStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';

export interface Transfer {
  id: string;
  employeeId: string;
  employeeName?: string;
  employeeCode?: string;
  transferType: TransferType;
  fromDepartmentId?: string;
  fromDepartmentName?: string;
  toDepartmentId?: string;
  toDepartmentName?: string;
  fromBranchId?: string;
  fromBranchName?: string;
  toBranchId?: string;
  toBranchName?: string;
  fromManagerId?: string;
  fromManagerName?: string;
  toManagerId?: string;
  toManagerName?: string;
  effectiveDate: string;
  reason?: string;
  status: TransferStatus;
  approvedBy?: string;
  approvedAt?: string;
}

// ─── Promotion ────────────────────────────────────────────────────────────────
export type PromotionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Promotion {
  id: string;
  employeeId: string;
  employeeName?: string;
  employeeCode?: string;
  fromDesignationId?: string;
  fromDesignationTitle?: string;
  toDesignationId: string;
  toDesignationTitle?: string;
  effectiveDate: string;
  reason?: string;
  status: PromotionStatus;
  approvedBy?: string;
  approvedAt?: string;
}

// ─── Onboarding / Offboarding ─────────────────────────────────────────────────
export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';

export interface OnboardingTask {
  id: string;
  employeeId: string;
  employeeName?: string;
  taskName: string;
  description?: string;
  status: TaskStatus;
  assignedTo?: string;
  assignedToName?: string;
  dueDate?: string;
  completedAt?: string;
  overdue: boolean;
}

export interface OnboardingProgress {
  employeeId: string;
  employeeName?: string;
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
  overdueTasks: number;
  completionPercentage: number;
}

export interface OffboardingTask {
  id: string;
  employeeId: string;
  employeeName?: string;
  taskName: string;
  description?: string;
  status: TaskStatus;
  assignedTo?: string;
  assignedToName?: string;
  dueDate?: string;
  completedAt?: string;
  overdue: boolean;
}

// ─── Asset ────────────────────────────────────────────────────────────────────
export type AssetType = 'LAPTOP' | 'DESKTOP' | 'MOBILE' | 'MONITOR' | 'SIM' | 'ACCESS_CARD' | 'OTHER';
export type AssetStatus = 'AVAILABLE' | 'ASSIGNED' | 'IN_REPAIR' | 'RETIRED';

export interface Asset {
  id: string;
  assetCode: string;
  name: string;
  assetType: AssetType;
  serialNumber?: string;
  model?: string;
  brand?: string;
  purchaseDate?: string;
  warrantyExpiry?: string;
  status: AssetStatus;
  assignedToEmployeeId?: string;
  assignedToEmployeeName?: string;
  assignedAt?: string;
  returnedAt?: string;
  notes?: string;
  warrantyExpiringSoon: boolean;
}

// ─── Skills ───────────────────────────────────────────────────────────────────
export type Proficiency = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export interface EmployeeSkill {
  id: string;
  employeeId: string;
  skillName: string;
  skillType?: string;
  proficiency?: Proficiency;
  yearsOfExperience?: number;
}

export interface EmployeeCertification {
  id: string;
  employeeId: string;
  name: string;
  issuingOrg?: string;
  issueDate?: string;
  expiryDate?: string;
  credentialUrl?: string;
  expired: boolean;
  expiringSoon: boolean;
}

// ─── Notification ─────────────────────────────────────────────────────────────
export type NotificationType =
  | 'DOCUMENT_EXPIRY' | 'WORKFLOW_APPROVAL' | 'ONBOARDING_TASK'
  | 'OFFBOARDING_TASK' | 'SYSTEM' | 'GENERAL';

export interface Notification {
  id: string;
  recipientId: string;
  title: string;
  message: string;
  type: NotificationType;
  channel: 'IN_APP' | 'EMAIL';
  isRead: boolean;
  referenceId?: string;
  referenceType?: string;
  createdAt: string;
}

// ─── Dashboard ────────────────────────────────────────────────────────────────
export interface DashboardStats {
  totalEmployees: number;
  activeEmployees: number;
  onProbation: number;
  onLeave: number;
  resigned: number;
  pendingTransfers: number;
  pendingPromotions: number;
  probationEndingSoon: number;
  employeesInOnboarding: number;
  availableAssets: number;
  assignedAssets: number;
}

// ─── Audit ────────────────────────────────────────────────────────────────────
export type AuditAction = 'CREATE' | 'UPDATE' | 'DELETE' | 'LOGIN' | 'LOGOUT' | 'EXPORT';

export interface AuditLog {
  id: string;
  entityName: string;
  entityId: string;
  action: AuditAction;
  oldValue?: string;
  newValue?: string;
  performedBy?: string;
  ipAddress?: string;
  createdAt: string;
}
