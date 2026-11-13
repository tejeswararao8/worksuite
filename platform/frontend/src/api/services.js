import api from './axios';

// ─── Auth (Keycloak ROPC) ─────────────────────────────────────────────────────
// SECURITY STATUS: transitional — local development only.
//
// The client secret is NOT read from any REACT_APP_* variable here.
// REACT_APP_* variables are embedded in the JavaScript bundle at build time
// and are fully visible to any browser user. A secret in a browser bundle
// is not a secret.
//
// Current state: the Keycloak client "vve-backend" is configured as
// "confidential" access type, which requires a client_secret on the token
// endpoint. This is incompatible with a browser-based SPA.
//
// Required migration (Phase 1 of auth hardening — pending Keycloak change):
//   Set the Keycloak client access type to "public".
//   Public clients do not use a client_secret. The ROPC flow continues to
//   work with client_id only. No code change is needed beyond removing the
//   client_secret parameter from these calls.
//
// Future migration (Phase 2 — full security):
//   Replace ROPC with Authorization Code + PKCE.
//   Use the Keycloak JS adapter or a BFF proxy so the browser never handles
//   tokens directly and no client_secret is needed in the frontend at all.
//
// Until the Keycloak client is switched to public, the client_secret must be
// supplied at runtime via the local .env file (gitignored) only.
// It is read here via an env var so it is never hardcoded in source.
const KEYCLOAK_TOKEN_URL = `${process.env.REACT_APP_KC_URL}/realms/${process.env.REACT_APP_KC_REALM}/protocol/openid-connect/token`;
const KC_CLIENT_ID     = process.env.REACT_APP_KC_CLIENT_ID;
// Read from local .env only — never commit a value for this variable.
const KC_CLIENT_SECRET = process.env.REACT_APP_KC_CLIENT_SECRET;

function buildTokenParams(base) {
  const params = new URLSearchParams(base);
  // Only append client_secret if the Keycloak client is still "confidential".
  // Remove this block once the client is switched to "public" access type.
  if (KC_CLIENT_SECRET) {
    params.append('client_secret', KC_CLIENT_SECRET);
  }
  return params;
}

export const authApi = {
  login: ({ email, password }) => {
    const params = buildTokenParams({
      grant_type: 'password',
      client_id:  KC_CLIENT_ID,
      username:   email,
      password:   password,
    });
    return import('axios').then(({ default: axios }) =>
      axios.post(KEYCLOAK_TOKEN_URL, params, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })
    );
  },
  refresh: ({ refreshToken }) => {
    const params = buildTokenParams({
      grant_type:    'refresh_token',
      client_id:     KC_CLIENT_ID,
      refresh_token: refreshToken,
    });
    return import('axios').then(({ default: axios }) =>
      axios.post(KEYCLOAK_TOKEN_URL, params, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })
    );
  },
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
  getSummary:              () => api.get('/hr/dashboard/summary'),
  headcountByDepartment:   () => api.get('/hr/dashboard/headcount/department'),
  headcountByBranch:       () => api.get('/hr/dashboard/headcount/branch'),
  joiningTrend:            () => api.get('/hr/dashboard/joining-trend'),
  documentExpiryAlerts:    () => api.get('/hr/dashboard/document-expiry-alerts'),
  assetUtilization:        () => api.get('/hr/dashboard/asset-utilization'),
};

// ─── Reports ──────────────────────────────────────────────────────────────────
export const reportApi = {
  employees:      (params) => api.get('/hr/reports/employees',      { params, responseType: 'blob' }),
  departments:    (params) => api.get('/hr/reports/departments',    { params, responseType: 'blob' }),
  branches:       (params) => api.get('/hr/reports/branches',       { params, responseType: 'blob' }),
  designations:   (params) => api.get('/hr/reports/designations',   { params, responseType: 'blob' }),
  assets:         (params) => api.get('/hr/reports/assets',         { params, responseType: 'blob' }),
  transfers:      (params) => api.get('/hr/reports/transfers',      { params, responseType: 'blob' }),
  promotions:     (params) => api.get('/hr/reports/promotions',     { params, responseType: 'blob' }),
  documentExpiry: (params) => api.get('/hr/reports/document-expiry', { params, responseType: 'blob' }),
};

// ─── Company ──────────────────────────────────────────────────────────────────
export const companyApi = {
  getById: (id)       => api.get(`/companies/${id}`),
  update:  (id, data) => api.put(`/companies/${id}`, data),
};

// ─── Audit Logs ───────────────────────────────────────────────────────────────
export const auditApi = {
  getAll:      (params)            => api.get('/audit-logs', { params }),
  getByEntity: (entityName, params) => api.get(`/audit-logs/entity/${entityName}`, { params }),
  getByRecord: (entityId, params)   => api.get(`/audit-logs/record/${entityId}`, { params }),
};

// ─── Platform Admin ───────────────────────────────────────────────────────────
export const platformApi = {
  getDashboardSummary: () => api.get('/platform/dashboard/summary'),
  getTenants:          (params) => api.get('/platform/tenants', { params }),
  getTenantById:       (id)     => api.get(`/platform/tenants/${id}`),
  registerTenant:      (data)   => api.post('/platform/tenants/register', data),
  approveTenant:       (id, data) => api.post(`/platform/tenants/${id}/approve`, data ?? {}),
  rejectTenant:        (id, data) => api.post(`/platform/tenants/${id}/reject`, data),
  suspendTenant:       (id, data) => api.post(`/platform/tenants/${id}/suspend`, data),
  reactivateTenant:    (id, data) => api.post(`/platform/tenants/${id}/reactivate`, data ?? {}),
  getTenantHistory:    (id)     => api.get(`/platform/tenants/${id}/history`),
  // Platform employees
  getPlatformEmployees:(params) => api.get('/platform/employees', { params }),
  getPlatformEmployee: (id)     => api.get(`/platform/employees/${id}`),
  inviteEmployee:      (data)   => api.post('/platform/employees/invite', data),
  getInvitations:      (params) => api.get('/platform/employees/invitations', { params }),
  resendInvitation:    (id)     => api.post(`/platform/employees/invitations/${id}/resend`),
  revokeInvitation:    (id)     => api.post(`/platform/employees/invitations/${id}/revoke`),
  changeEmployeeRole:  (id, data) => api.put(`/platform/employees/${id}/role`, data),
  suspendEmployee:     (id)     => api.post(`/platform/employees/${id}/suspend`),
  reactivateEmployee:  (id)     => api.post(`/platform/employees/${id}/reactivate`),
  revokeEmployee:      (id)     => api.post(`/platform/employees/${id}/revoke`),
};
