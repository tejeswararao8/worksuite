import api from './axios';

// ─── Auth ─────────────────────────────────────────────────────────────────────
export const authApi = {
  login:          (data) => api.post('/auth/login', data),
  refresh:        (data) => api.post('/auth/refresh', data),
  changePassword: (data) => api.post('/auth/change-password', data),
};

// ─── Employee ─────────────────────────────────────────────────────────────────
export const employeeApi = {
  create:      (data)     => api.post('/hr/employees', data),
  getById:     (id)       => api.get(`/hr/employees/${id}`),
  search:      (params)   => api.get('/hr/employees', { params }),
  update:      (id, data) => api.put(`/hr/employees/${id}`, data),
  delete:      (id)       => api.delete(`/hr/employees/${id}`),
  uploadPhoto: (id, file) => {
    const form = new FormData();
    form.append('file', file);
    return api.post(`/hr/employees/${id}/photo`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

// ─── Department ───────────────────────────────────────────────────────────────
export const departmentApi = {
  create:  (data)     => api.post('/hr/departments', data),
  getAll:  (params)   => api.get('/hr/departments', { params }),
  getById: (id)       => api.get(`/hr/departments/${id}`),
  update:  (id, data) => api.put(`/hr/departments/${id}`, data),
  delete:  (id)       => api.delete(`/hr/departments/${id}`),
};

// ─── Branch ───────────────────────────────────────────────────────────────────
export const branchApi = {
  create:  (data)     => api.post('/hr/branches', data),
  getAll:  (params)   => api.get('/hr/branches', { params }),
  getById: (id)       => api.get(`/hr/branches/${id}`),
  update:  (id, data) => api.put(`/hr/branches/${id}`, data),
  delete:  (id)       => api.delete(`/hr/branches/${id}`),
};

// ─── Designation ──────────────────────────────────────────────────────────────
export const designationApi = {
  create:  (data)     => api.post('/hr/designations', data),
  getAll:  (params)   => api.get('/hr/designations', { params }),
  getById: (id)       => api.get(`/hr/designations/${id}`),
  update:  (id, data) => api.put(`/hr/designations/${id}`, data),
  delete:  (id)       => api.delete(`/hr/designations/${id}`),
};

// ─── Team ─────────────────────────────────────────────────────────────────────
export const teamApi = {
  create:  (data)     => api.post('/hr/teams', data),
  getAll:  (params)   => api.get('/hr/teams', { params }),
  getById: (id)       => api.get(`/hr/teams/${id}`),
  update:  (id, data) => api.put(`/hr/teams/${id}`, data),
  delete:  (id)       => api.delete(`/hr/teams/${id}`),
};

// ─── Asset ────────────────────────────────────────────────────────────────────
export const assetApi = {
  create:        (data)     => api.post('/hr/assets', data),
  getAll:        (params)   => api.get('/hr/assets', { params }),
  getById:       (id)       => api.get(`/hr/assets/${id}`),
  getByEmployee: (empId)    => api.get(`/hr/assets/employee/${empId}`),
  update:        (id, data) => api.put(`/hr/assets/${id}`, data),
  delete:        (id)       => api.delete(`/hr/assets/${id}`),
  assign:        (id, data) => api.post(`/hr/assets/${id}/assign`, data),
  returnAsset:   (id)       => api.post(`/hr/assets/${id}/return`),
};

// ─── Document ─────────────────────────────────────────────────────────────────
export const documentApi = {
  upload:   (empId, data)   => api.post(`/documents/entity/${empId}`, data, { headers: { 'Content-Type': 'multipart/form-data' } }),
  getAll:   (empId, params) => api.get(`/documents/entity/${empId}`, { params }),
  download: (id)            => api.get(`/documents/${id}/download`, { responseType: 'blob' }),
  delete:   (id)            => api.delete(`/documents/${id}`),
};

// ─── Skills & Certifications ──────────────────────────────────────────────────
export const skillsApi = {
  addSkill:            (empId, data)    => api.post(`/hr/employees/${empId}/skills`, data),
  getSkills:           (empId)          => api.get(`/hr/employees/${empId}/skills`),
  removeSkill:         (empId, skillId) => api.delete(`/hr/employees/${empId}/skills/${skillId}`),
  addCertification:    (empId, data)    => api.post(`/hr/employees/${empId}/certifications`, data),
  getCertifications:   (empId)          => api.get(`/hr/employees/${empId}/certifications`),
  removeCertification: (empId, certId)  => api.delete(`/hr/employees/${empId}/certifications/${certId}`),
};

// ─── User ─────────────────────────────────────────────────────────────────────
export const userApi = {
  create:        (data)     => api.post('/users', data),
  getById:       (id)       => api.get(`/users/${id}`),
  changeRole:    (id, data) => api.put(`/users/${id}/role`, data),
  deactivate:    (id)       => api.put(`/users/${id}/deactivate`),
  activate:      (id)       => api.put(`/users/${id}/activate`),
  resetPassword: (id)       => api.post(`/users/${id}/reset-password`),
};

// ─── Role ─────────────────────────────────────────────────────────────────────
export const roleApi = {
  create:            (data)     => api.post('/roles', data),
  getAll:            ()         => api.get('/roles'),
  getById:           (id)       => api.get(`/roles/${id}`),
  updatePermissions: (id, data) => api.put(`/roles/${id}/permissions`, data),
  listPermissions:   ()         => api.get('/roles/permissions'),
};

// ─── Transfer ─────────────────────────────────────────────────────────────────
export const transferApi = {
  initiate:      (data)     => api.post('/hr/transfers', data),
  getAll:        (params)   => api.get('/hr/transfers', { params }),
  getByEmployee: (empId)    => api.get(`/hr/transfers/employee/${empId}`),
  approve:       (id)       => api.post(`/hr/transfers/${id}/approve`),
  reject:        (id, data) => api.post(`/hr/transfers/${id}/reject`, data),
};

// ─── Promotion ────────────────────────────────────────────────────────────────
export const promotionApi = {
  initiate:      (data)     => api.post('/hr/promotions', data),
  getAll:        (params)   => api.get('/hr/promotions', { params }),
  getByEmployee: (empId)    => api.get(`/hr/promotions/employee/${empId}`),
  approve:       (id)       => api.post(`/hr/promotions/${id}/approve`),
  reject:        (id, data) => api.post(`/hr/promotions/${id}/reject`, data),
};

// ─── Probation ────────────────────────────────────────────────────────────────
export const probationApi = {
  initiate:      (data)     => api.post('/hr/probations', data),
  getByEmployee: (empId)    => api.get(`/hr/probations/employee/${empId}`),
  confirm:       (id)       => api.post(`/hr/probations/${id}/confirm`),
  extend:        (id, data) => api.post(`/hr/probations/${id}/extend`, data),
  reject:        (id, data) => api.post(`/hr/probations/${id}/reject`, data),
};

// ─── Onboarding ───────────────────────────────────────────────────────────────
export const onboardingApi = {
  initiate:     (empId)         => api.post(`/hr/onboarding/employee/${empId}/initiate`),
  getChecklist: (empId)         => api.get(`/hr/onboarding/employee/${empId}`),
  completeTask: (taskId)        => api.put(`/hr/onboarding/tasks/${taskId}/complete`),
  addTask:      (empId, data)   => api.post(`/hr/onboarding/employee/${empId}/tasks`, data),
};

// ─── Offboarding ──────────────────────────────────────────────────────────────
export const offboardingApi = {
  initiate:     (empId, data) => api.post(`/hr/offboarding/employee/${empId}/initiate`, data),
  getChecklist: (empId)       => api.get(`/hr/offboarding/employee/${empId}`),
  completeTask: (taskId)      => api.put(`/hr/offboarding/tasks/${taskId}/complete`),
  complete:     (empId)       => api.post(`/hr/offboarding/employee/${empId}/complete`),
};

// ─── Notification ─────────────────────────────────────────────────────────────
export const notificationApi = {
  getAll:         (params) => api.get('/notifications', { params }),
  getUnreadCount: ()       => api.get('/notifications/unread-count'),
  markRead:       (id)     => api.put(`/notifications/${id}/read`),
  markAllRead:    ()       => api.put('/notifications/mark-all-read'),
};

// ─── Dashboard ────────────────────────────────────────────────────────────────
export const dashboardApi = {
  getStats: () => api.get('/hr/dashboard/stats'),
};

// ─── Reports ──────────────────────────────────────────────────────────────────
export const reportApi = {
  employees:    (params) => api.get('/hr/reports/employees',    { params, responseType: 'blob' }),
  departments:  (params) => api.get('/hr/reports/departments',  { params, responseType: 'blob' }),
  branches:     (params) => api.get('/hr/reports/branches',     { params, responseType: 'blob' }),
  assets:       (params) => api.get('/hr/reports/assets',       { params, responseType: 'blob' }),
  transfers:    (params) => api.get('/hr/reports/transfers',    { params, responseType: 'blob' }),
  promotions:   (params) => api.get('/hr/reports/promotions',   { params, responseType: 'blob' }),
};

// ─── Audit Logs ───────────────────────────────────────────────────────────────
export const auditApi = {
  getAll:      (params)            => api.get('/audit-logs', { params }),
  getByEntity: (entityName, params) => api.get(`/audit-logs/entity/${entityName}`, { params }),
  getByRecord: (entityId, params)   => api.get(`/audit-logs/record/${entityId}`, { params }),
};
