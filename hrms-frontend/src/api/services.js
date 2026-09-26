import api from './axios';

// ─── Auth ─────────────────────────────────────────────────────────────────────
export const authApi = {
  login:          (data) => api.post('/auth/login', data),
  refresh:        (data) => api.post('/auth/refresh', data),
  changePassword: (data) => api.post('/auth/change-password', data),
};

// ─── Company ──────────────────────────────────────────────────────────────────
export const companyApi = {
  create:   (data)     => api.post('/companies', data),
  getAll:   (params)   => api.get('/companies', { params }),
  getById:  (id)       => api.get(`/companies/${id}`),
  update:   (id, data) => api.put(`/companies/${id}`, data),
  delete:   (id)       => api.delete(`/companies/${id}`),
};

// ─── Employee ─────────────────────────────────────────────────────────────────
export const employeeApi = {
  create:      (data)       => api.post('/employees', data),
  getById:     (id)         => api.get(`/employees/${id}`),
  search:      (params)     => api.get('/employees', { params }),
  update:      (id, data)   => api.put(`/employees/${id}`, data),
  delete:      (id)         => api.delete(`/employees/${id}`),
  uploadPhoto: (id, file)   => {
    const form = new FormData();
    form.append('file', file);
    return api.post(`/employees/${id}/photo`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

// ─── Department ───────────────────────────────────────────────────────────────
export const departmentApi = {
  create:  (data)     => api.post('/departments', data),
  getAll:  (params)   => api.get('/departments', { params }),
  getById: (id)       => api.get(`/departments/${id}`),
  update:  (id, data) => api.put(`/departments/${id}`, data),
  delete:  (id)       => api.delete(`/departments/${id}`),
};

// ─── Branch ───────────────────────────────────────────────────────────────────
export const branchApi = {
  create:  (data)     => api.post('/branches', data),
  getAll:  (params)   => api.get('/branches', { params }),
  getById: (id)       => api.get(`/branches/${id}`),
  update:  (id, data) => api.put(`/branches/${id}`, data),
  delete:  (id)       => api.delete(`/branches/${id}`),
};

// ─── Designation ──────────────────────────────────────────────────────────────
export const designationApi = {
  create:  (data)     => api.post('/designations', data),
  getAll:  (params)   => api.get('/designations', { params }),
  getById: (id)       => api.get(`/designations/${id}`),
  update:  (id, data) => api.put(`/designations/${id}`, data),
  delete:  (id)       => api.delete(`/designations/${id}`),
};

// ─── Team ─────────────────────────────────────────────────────────────────────
export const teamApi = {
  create:  (data)     => api.post('/teams', data),
  getAll:  (params)   => api.get('/teams', { params }),
  getById: (id)       => api.get(`/teams/${id}`),
  update:  (id, data) => api.put(`/teams/${id}`, data),
  delete:  (id)       => api.delete(`/teams/${id}`),
};

// ─── Asset ────────────────────────────────────────────────────────────────────
export const assetApi = {
  create:          (data)     => api.post('/assets', data),
  getAll:          (params)   => api.get('/assets', { params }),
  getById:         (id)       => api.get(`/assets/${id}`),
  getByEmployee:   (empId)    => api.get(`/assets/employee/${empId}`),
  update:          (id, data) => api.put(`/assets/${id}`, data),
  delete:          (id)       => api.delete(`/assets/${id}`),
  assign:          (id, data) => api.post(`/assets/${id}/assign`, data),
  returnAsset:     (id)       => api.post(`/assets/${id}/return`),
};

// ─── Document ─────────────────────────────────────────────────────────────────
export const documentApi = {
  upload:  (empId, data) => api.post(`/employees/${empId}/documents`, data, { headers: { 'Content-Type': 'multipart/form-data' } }),
  getAll:  (empId, params) => api.get(`/employees/${empId}/documents`, { params }),
  getById: (empId, id)   => api.get(`/employees/${empId}/documents/${id}`),
  download:(empId, id)   => api.get(`/employees/${empId}/documents/${id}/download`),
  delete:  (empId, id)   => api.delete(`/employees/${empId}/documents/${id}`),
};

// ─── Skills & Certifications ──────────────────────────────────────────────────
export const skillsApi = {
  addSkill:           (empId, data) => api.post(`/employees/${empId}/skills`, data),
  getSkills:          (empId)       => api.get(`/employees/${empId}/skills`),
  removeSkill:        (empId, skillId) => api.delete(`/employees/${empId}/skills/${skillId}`),
  addCertification:   (empId, data) => api.post(`/employees/${empId}/skills/certifications`, data),
  getCertifications:  (empId)       => api.get(`/employees/${empId}/skills/certifications`),
  removeCertification:(empId, certId) => api.delete(`/employees/${empId}/skills/certifications/${certId}`),
};

// ─── User ─────────────────────────────────────────────────────────────────────
export const userApi = {
  create:        (data) => api.post('/users', data),
  getById:       (id)   => api.get(`/users/${id}`),
  changeRole:    (id, data) => api.put(`/users/${id}/role`, data),
  deactivate:    (id)   => api.put(`/users/${id}/deactivate`),
  activate:      (id)   => api.put(`/users/${id}/activate`),
  resetPassword: (id)   => api.post(`/users/${id}/reset-password`),
};

// ─── Role ─────────────────────────────────────────────────────────────────────
export const roleApi = {
  create:           (data)     => api.post('/roles', data),
  getAll:           ()         => api.get('/roles'),
  getById:          (id)       => api.get(`/roles/${id}`),
  updatePermissions:(id, data) => api.put(`/roles/${id}/permissions`, data),
  listPermissions:  ()         => api.get('/roles/permissions'),
};

// ─── Transfer ─────────────────────────────────────────────────────────────────
export const transferApi = {
  initiate:      (data)      => api.post('/transfers', data),
  getAll:        (params)    => api.get('/transfers', { params }),
  getByEmployee: (empId)     => api.get(`/transfers/employee/${empId}`),
  approve:       (id)        => api.post(`/transfers/${id}/approve`),
  reject:        (id, data)  => api.post(`/transfers/${id}/reject`, data),
};

// ─── Promotion ────────────────────────────────────────────────────────────────
export const promotionApi = {
  initiate:      (data)      => api.post('/promotions', data),
  getAll:        (params)    => api.get('/promotions', { params }),
  getByEmployee: (empId)     => api.get(`/promotions/employee/${empId}`),
  approve:       (id)        => api.post(`/promotions/${id}/approve`),
  reject:        (id, data)  => api.post(`/promotions/${id}/reject`, data),
};

// ─── Probation ────────────────────────────────────────────────────────────────
export const probationApi = {
  initiate:      (data)     => api.post('/probations', data),
  getByEmployee: (empId)    => api.get(`/probations/employee/${empId}`),
  confirm:       (id)       => api.post(`/probations/${id}/confirm`),
  extend:        (id, data) => api.post(`/probations/${id}/extend`, data),
  reject:        (id, data) => api.post(`/probations/${id}/reject`, data),
};

// ─── Onboarding ───────────────────────────────────────────────────────────────
export const onboardingApi = {
  initiate:     (empId)    => api.post(`/onboarding/employee/${empId}/initiate`),
  getChecklist: (empId)    => api.get(`/onboarding/employee/${empId}`),
  completeTask: (taskId)   => api.put(`/onboarding/tasks/${taskId}/complete`),
  addTask:      (taskId, data) => api.post(`/onboarding/tasks/${taskId}/add`, data),
};

// ─── Offboarding ──────────────────────────────────────────────────────────────
export const offboardingApi = {
  initiate:     (empId, data) => api.post(`/offboarding/employee/${empId}/initiate`, data),
  getChecklist: (empId)       => api.get(`/offboarding/employee/${empId}`),
  completeTask: (taskId)      => api.put(`/offboarding/tasks/${taskId}/complete`),
  complete:     (empId)       => api.post(`/offboarding/employee/${empId}/complete`),
};

// ─── Notification ─────────────────────────────────────────────────────────────
export const notificationApi = {
  getAll:        (params) => api.get('/notifications', { params }),
  getUnreadCount:()       => api.get('/notifications/unread-count'),
  markRead:      (id)     => api.put(`/notifications/${id}/read`),
  markAllRead:   ()       => api.put('/notifications/mark-all-read'),
};

// ─── Dashboard ────────────────────────────────────────────────────────────────
export const dashboardApi = {
  getSummary:             () => api.get('/dashboard/summary'),
  headcountByDepartment:  () => api.get('/dashboard/headcount-by-department'),
  headcountByBranch:      () => api.get('/dashboard/headcount-by-branch'),
  joiningTrend:           () => api.get('/dashboard/joining-trend'),
  documentExpiryAlerts:   () => api.get('/dashboard/document-expiry-alerts'),
  assetUtilization:       () => api.get('/dashboard/asset-utilization'),
};

// ─── Reports ──────────────────────────────────────────────────────────────────
export const reportApi = {
  employees:      (params) => api.get('/reports/employees',    { params, responseType: 'blob' }),
  departments:    (params) => api.get('/reports/departments',  { params, responseType: 'blob' }),
  branches:       (params) => api.get('/reports/branches',     { params, responseType: 'blob' }),
  designations:   (params) => api.get('/reports/designations', { params, responseType: 'blob' }),
  promotions:     (params) => api.get('/reports/promotions',   { params, responseType: 'blob' }),
  transfers:      (params) => api.get('/reports/transfers',    { params, responseType: 'blob' }),
  documentExpiry: (params) => api.get('/reports/document-expiry', { params, responseType: 'blob' }),
  assets:         (params) => api.get('/reports/assets',       { params, responseType: 'blob' }),
};

// ─── Audit Logs ───────────────────────────────────────────────────────────────
export const auditApi = {
  getAll:       (params)     => api.get('/audit-logs', { params }),
  getByEntity:  (entityName, params) => api.get(`/audit-logs/entity/${entityName}`, { params }),
  getByRecord:  (entityId, params)   => api.get(`/audit-logs/record/${entityId}`, { params }),
};
