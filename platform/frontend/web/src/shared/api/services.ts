import api from './axios';
import { API } from '../constants';
import type {
  LoginRequest, CreateEmployeeRequest, UpdateEmployeeRequest,
  CreateBranchRequest, CreateDepartmentRequest, CreateDesignationRequest,
  CreateTeamRequest,
} from '../types';

// ─── Auth ─────────────────────────────────────────────────────────────────────
export const authApi = {
  login:          (data: LoginRequest)          => api.post(API.AUTH.LOGIN, data),
  refresh:        (data: { refreshToken: string }) => api.post(API.AUTH.REFRESH, data),
  changePassword: (data: { currentPassword: string; newPassword: string }) =>
                    api.post(API.AUTH.CHANGE_PASSWORD, data),
};

// ─── Employee ─────────────────────────────────────────────────────────────────
export const employeeApi = {
  create:      (data: CreateEmployeeRequest)                  => api.post(API.EMPLOYEES.BASE, data),
  getById:     (id: string)                                   => api.get(API.EMPLOYEES.BY_ID(id)),
  search:      (params: Record<string, unknown>)              => api.get(API.EMPLOYEES.BASE, { params }),
  update:      (id: string, data: UpdateEmployeeRequest)      => api.put(API.EMPLOYEES.BY_ID(id), data),
  delete:      (id: string)                                   => api.delete(API.EMPLOYEES.BY_ID(id)),
  uploadPhoto: (id: string, file: File) => {
    const form = new FormData();
    form.append('file', file);
    return api.post(API.EMPLOYEES.PHOTO(id), form, { headers: { 'Content-Type': 'multipart/form-data' } });
  },
  // Emergency contacts
  getEmergencyContacts:    (id: string)                       => api.get(API.EMPLOYEES.EMERGENCY(id)),
  addEmergencyContact:     (id: string, data: unknown)        => api.post(API.EMPLOYEES.EMERGENCY(id), data),
  updateEmergencyContact:  (id: string, cId: string, data: unknown) =>
                             api.put(API.EMPLOYEES.EMERGENCY_BY_ID(id, cId), data),
  deleteEmergencyContact:  (id: string, cId: string)          => api.delete(API.EMPLOYEES.EMERGENCY_BY_ID(id, cId)),
  // Bank details
  getBankDetails:          (id: string)                       => api.get(API.EMPLOYEES.BANK(id)),
  addBankDetail:           (id: string, data: unknown)        => api.post(API.EMPLOYEES.BANK(id), data),
  updateBankDetail:        (id: string, bId: string, data: unknown) =>
                             api.put(API.EMPLOYEES.BANK_BY_ID(id, bId), data),
  deleteBankDetail:        (id: string, bId: string)          => api.delete(API.EMPLOYEES.BANK_BY_ID(id, bId)),
  // Skills
  getSkills:               (id: string)                       => api.get(API.EMPLOYEES.SKILLS(id)),
  addSkill:                (id: string, data: unknown)        => api.post(API.EMPLOYEES.SKILLS(id), data),
  updateSkill:             (id: string, sId: string, data: unknown) =>
                             api.put(API.EMPLOYEES.SKILL_BY_ID(id, sId), data),
  deleteSkill:             (id: string, sId: string)          => api.delete(API.EMPLOYEES.SKILL_BY_ID(id, sId)),
  // Certifications
  getCertifications:       (id: string)                       => api.get(API.EMPLOYEES.CERTS(id)),
  addCertification:        (id: string, data: unknown)        => api.post(API.EMPLOYEES.CERTS(id), data),
  updateCertification:     (id: string, cId: string, data: unknown) =>
                             api.put(API.EMPLOYEES.CERT_BY_ID(id, cId), data),
  deleteCertification:     (id: string, cId: string)          => api.delete(API.EMPLOYEES.CERT_BY_ID(id, cId)),
};

// ─── Branch ───────────────────────────────────────────────────────────────────
export const branchApi = {
  create:  (data: CreateBranchRequest)           => api.post(API.BRANCHES.BASE, data),
  getAll:  (params?: Record<string, unknown>)    => api.get(API.BRANCHES.BASE, { params }),
  getAllList: ()                                  => api.get(API.BRANCHES.ALL),
  getById: (id: string)                          => api.get(API.BRANCHES.BY_ID(id)),
  update:  (id: string, data: unknown)           => api.put(API.BRANCHES.BY_ID(id), data),
  delete:  (id: string)                          => api.delete(API.BRANCHES.BY_ID(id)),
};

// ─── Department ───────────────────────────────────────────────────────────────
export const departmentApi = {
  create:    (data: CreateDepartmentRequest)     => api.post(API.DEPARTMENTS.BASE, data),
  getAll:    (params?: Record<string, unknown>)  => api.get(API.DEPARTMENTS.BASE, { params }),
  getAllList: ()                                  => api.get(API.DEPARTMENTS.ALL),
  getById:   (id: string)                        => api.get(API.DEPARTMENTS.BY_ID(id)),
  getOrgChart: ()                                => api.get(API.DEPARTMENTS.ORG_CHART),
  update:    (id: string, data: unknown)         => api.put(API.DEPARTMENTS.BY_ID(id), data),
  delete:    (id: string)                        => api.delete(API.DEPARTMENTS.BY_ID(id)),
};

// ─── Designation ──────────────────────────────────────────────────────────────
export const designationApi = {
  create:    (data: CreateDesignationRequest)    => api.post(API.DESIGNATIONS.BASE, data),
  getAll:    (params?: Record<string, unknown>)  => api.get(API.DESIGNATIONS.BASE, { params }),
  getAllList: ()                                  => api.get(API.DESIGNATIONS.ALL),
  getById:   (id: string)                        => api.get(API.DESIGNATIONS.BY_ID(id)),
  update:    (id: string, data: unknown)         => api.put(API.DESIGNATIONS.BY_ID(id), data),
  delete:    (id: string)                        => api.delete(API.DESIGNATIONS.BY_ID(id)),
};

// ─── Team ─────────────────────────────────────────────────────────────────────
export const teamApi = {
  create:       (data: CreateTeamRequest)        => api.post(API.TEAMS.BASE, data),
  getAll:       (params?: Record<string, unknown>) => api.get(API.TEAMS.BASE, { params }),
  getAllList:    ()                               => api.get(API.TEAMS.ALL),
  getById:      (id: string)                     => api.get(API.TEAMS.BY_ID(id)),
  getMembers:   (id: string)                     => api.get(API.TEAMS.MEMBERS(id)),
  addMember:    (id: string, employeeId: string) => api.post(API.TEAMS.MEMBERS(id), { employeeId }),
  removeMember: (id: string, empId: string)      => api.delete(API.TEAMS.MEMBER(id, empId)),
  update:       (id: string, data: unknown)      => api.put(API.TEAMS.BY_ID(id), data),
  delete:       (id: string)                     => api.delete(API.TEAMS.BY_ID(id)),
};

// ─── Asset ────────────────────────────────────────────────────────────────────
export const assetApi = {
  create:      (data: unknown)                   => api.post(API.ASSETS.BASE, data),
  getAll:      (params?: Record<string, unknown>) => api.get(API.ASSETS.BASE, { params }),
  getById:     (id: string)                      => api.get(API.ASSETS.BY_ID(id)),
  getByEmployee: (empId: string)                 => api.get(API.ASSETS.BY_EMPLOYEE(empId)),
  update:      (id: string, data: unknown)       => api.put(API.ASSETS.BY_ID(id), data),
  assign:      (id: string, data: unknown)       => api.put(API.ASSETS.ASSIGN(id), data),
  return:      (id: string, data: unknown)       => api.put(API.ASSETS.RETURN(id), data),
  retire:      (id: string)                      => api.delete(API.ASSETS.RETIRE(id)),
};

// ─── Transfer ─────────────────────────────────────────────────────────────────
export const transferApi = {
  create:      (data: unknown)                   => api.post(API.TRANSFERS.BASE, data),
  getAll:      (params?: Record<string, unknown>) => api.get(API.TRANSFERS.BASE, { params }),
  getById:     (id: string)                      => api.get(API.TRANSFERS.BY_ID(id)),
  getByEmployee: (empId: string)                 => api.get(API.TRANSFERS.BY_EMPLOYEE(empId)),
  approve:     (id: string, data: unknown)       => api.put(API.TRANSFERS.APPROVE(id), data),
  reject:      (id: string, data: unknown)       => api.put(API.TRANSFERS.REJECT(id), data),
};

// ─── Promotion ────────────────────────────────────────────────────────────────
export const promotionApi = {
  create:      (data: unknown)                   => api.post(API.PROMOTIONS.BASE, data),
  getAll:      (params?: Record<string, unknown>) => api.get(API.PROMOTIONS.BASE, { params }),
  getByEmployee: (empId: string)                 => api.get(API.PROMOTIONS.BY_EMPLOYEE(empId)),
  approve:     (id: string, data: unknown)       => api.put(API.PROMOTIONS.APPROVE(id), data),
  reject:      (id: string, data: unknown)       => api.put(API.PROMOTIONS.REJECT(id), data),
};

// ─── Probation ────────────────────────────────────────────────────────────────
export const probationApi = {
  getAll:      (params?: Record<string, unknown>) => api.get(API.PROBATIONS.BASE, { params }),
  getByEmployee: (empId: string)                 => api.get(API.PROBATIONS.BY_EMPLOYEE(empId)),
  confirm:     (id: string, data: unknown)       => api.put(API.PROBATIONS.CONFIRM(id), data),
  extend:      (id: string, data: unknown)       => api.put(API.PROBATIONS.EXTEND(id), data),
  reject:      (id: string, data: unknown)       => api.put(API.PROBATIONS.REJECT(id), data),
};

// ─── Onboarding ───────────────────────────────────────────────────────────────
export const onboardingApi = {
  getByEmployee:  (empId: string)                => api.get(API.ONBOARDING.TASKS(empId)),
  getProgress:    (empId: string)                => api.get(API.ONBOARDING.PROGRESS(empId)),
  getMyTasks:     ()                             => api.get(API.ONBOARDING.MY_TASKS),
  addTask:        (empId: string, data: unknown) => api.post(API.ONBOARDING.TASKS(empId), data),
  completeTask:   (taskId: string)               => api.put(API.ONBOARDING.COMPLETE(taskId), {}),
  updateTask:     (taskId: string, data: unknown) => api.put(API.ONBOARDING.UPDATE_TASK(taskId), data),
};

// ─── Offboarding ──────────────────────────────────────────────────────────────
export const offboardingApi = {
  initiate:     (data: unknown)                  => api.post(API.OFFBOARDING.INITIATE, data),
  getByEmployee: (empId: string)                 => api.get(API.OFFBOARDING.TASKS(empId)),
  getMyTasks:   ()                               => api.get(API.OFFBOARDING.MY_TASKS),
  addTask:      (empId: string, data: unknown)   => api.post(API.OFFBOARDING.ADD_TASK(empId), data),
  completeTask: (taskId: string)                 => api.put(API.OFFBOARDING.COMPLETE(taskId), {}),
};

// ─── Dashboard ────────────────────────────────────────────────────────────────
export const dashboardApi = {
  getStats: () => api.get(API.DASHBOARD.STATS),
};

// ─── Notifications ────────────────────────────────────────────────────────────
export const notificationApi = {
  getAll:         (params?: Record<string, unknown>) => api.get(API.NOTIFICATIONS.BASE, { params }),
  getUnreadCount: ()                                 => api.get(API.NOTIFICATIONS.UNREAD_COUNT),
  markRead:       (id: string)                       => api.put(API.NOTIFICATIONS.MARK_READ(id), {}),
  markAllRead:    ()                                 => api.put(API.NOTIFICATIONS.MARK_ALL, {}),
};

// ─── Reports ──────────────────────────────────────────────────────────────────
export const reportApi = {
  employees:   (params: Record<string, unknown>) => api.get(API.REPORTS.EMPLOYEES,   { params, responseType: 'blob' }),
  newJoiners:  (params: Record<string, unknown>) => api.get(API.REPORTS.NEW_JOINERS, { params, responseType: 'blob' }),
  probation:   (params: Record<string, unknown>) => api.get(API.REPORTS.PROBATION,   { params, responseType: 'blob' }),
  transfers:   (params: Record<string, unknown>) => api.get(API.REPORTS.TRANSFERS,   { params, responseType: 'blob' }),
  promotions:  (params: Record<string, unknown>) => api.get(API.REPORTS.PROMOTIONS,  { params, responseType: 'blob' }),
  assets:      (params: Record<string, unknown>) => api.get(API.REPORTS.ASSETS,      { params, responseType: 'blob' }),
};

// ─── Audit ────────────────────────────────────────────────────────────────────
export const auditApi = {
  getAll: (params?: Record<string, unknown>) => api.get(API.AUDIT.BASE, { params }),
};
