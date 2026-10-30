// ─── Enums ────────────────────────────────────────────────────────────────────
export const EMPLOYMENT_STATUS_COLOR: Record<string, string> = {
  ACTIVE:     'green',
  PROBATION:  'yellow',
  ON_LEAVE:   'blue',
  RESIGNED:   'red',
  TERMINATED: 'red',
  RETIRED:    'gray',
};

export const ASSET_STATUS_COLOR: Record<string, string> = {
  AVAILABLE: 'green',
  ASSIGNED:  'blue',
  IN_REPAIR: 'yellow',
  RETIRED:   'gray',
};

export const APPROVAL_STATUS_COLOR: Record<string, string> = {
  PENDING:  'yellow',
  APPROVED: 'green',
  REJECTED: 'red',
};

export const PROBATION_STATUS_COLOR: Record<string, string> = {
  IN_PROGRESS: 'yellow',
  CONFIRMED:   'green',
  EXTENDED:    'blue',
  REJECTED:    'red',
};

export const TASK_STATUS_COLOR: Record<string, string> = {
  PENDING:     'yellow',
  IN_PROGRESS: 'blue',
  COMPLETED:   'green',
};

export const NOTIFICATION_TYPE_COLOR: Record<string, string> = {
  DOCUMENT_EXPIRY:   'red',
  WORKFLOW_APPROVAL: 'blue',
  ONBOARDING_TASK:   'indigo',
  OFFBOARDING_TASK:  'orange',
  SYSTEM:            'gray',
  GENERAL:           'gray',
};

export const DEFAULT_PAGE_SIZE = 20;

// ─── Query Keys ───────────────────────────────────────────────────────────────
export const QUERY_KEYS = {
  DASHBOARD_STATS:          ['dashboard-stats'] as const,
  EMPLOYEES:                (page: number, q?: string, deptId?: string, branchId?: string, status?: string) =>
                              ['employees', page, q, deptId, branchId, status] as const,
  EMPLOYEE_BY_ID:           (id: string) => ['employee', id] as const,
  DEPARTMENTS_ALL:          ['departments-all'] as const,
  DEPARTMENTS:              (page: number, q?: string) => ['departments', page, q] as const,
  BRANCHES_ALL:             ['branches-all'] as const,
  BRANCHES:                 (page: number, q?: string) => ['branches', page, q] as const,
  DESIGNATIONS_ALL:         ['designations-all'] as const,
  DESIGNATIONS:             (page: number, q?: string) => ['designations', page, q] as const,
  TEAMS_ALL:                ['teams-all'] as const,
  TEAMS:                    (page: number, q?: string, deptId?: string) => ['teams', page, q, deptId] as const,
  TEAM_MEMBERS:             (teamId: string) => ['team-members', teamId] as const,
  ORG_CHART:                ['org-chart'] as const,
  ASSETS:                   (page: number, q?: string, status?: string, type?: string) =>
                              ['assets', page, q, status, type] as const,
  ASSETS_BY_EMPLOYEE:       (empId: string) => ['assets-employee', empId] as const,
  TRANSFERS:                (page: number, status?: string) => ['transfers', page, status] as const,
  TRANSFERS_BY_EMPLOYEE:    (empId: string) => ['transfers-employee', empId] as const,
  PROMOTIONS:               (page: number, status?: string) => ['promotions', page, status] as const,
  PROMOTIONS_BY_EMPLOYEE:   (empId: string) => ['promotions-employee', empId] as const,
  PROBATIONS:               (page: number, status?: string) => ['probations', page, status] as const,
  PROBATION_BY_EMPLOYEE:    (empId: string) => ['probation-employee', empId] as const,
  ONBOARDING_BY_EMPLOYEE:   (empId: string) => ['onboarding-employee', empId] as const,
  ONBOARDING_PROGRESS:      (empId: string) => ['onboarding-progress', empId] as const,
  MY_ONBOARDING_TASKS:      ['my-onboarding-tasks'] as const,
  OFFBOARDING_BY_EMPLOYEE:  (empId: string) => ['offboarding-employee', empId] as const,
  MY_OFFBOARDING_TASKS:     ['my-offboarding-tasks'] as const,
  SKILLS_BY_EMPLOYEE:       (empId: string) => ['skills-employee', empId] as const,
  CERTS_BY_EMPLOYEE:        (empId: string) => ['certs-employee', empId] as const,
  EMERGENCY_CONTACTS:       (empId: string) => ['emergency-contacts', empId] as const,
  BANK_DETAILS:             (empId: string) => ['bank-details', empId] as const,
  NOTIFICATIONS:            (page: number) => ['notifications', page] as const,
  UNREAD_COUNT:             ['unread-count'] as const,
  AUDIT_LOGS:               (page: number, entity?: string) => ['audit-logs', page, entity] as const,
  ROLES:                    ['roles'] as const,
  PERMISSIONS:              ['permissions'] as const,
};

// ─── API Routes ───────────────────────────────────────────────────────────────
export const API = {
  AUTH: {
    LOGIN:           '/auth/login',
    REFRESH:         '/auth/refresh',
    CHANGE_PASSWORD: '/auth/change-password',
  },
  EMPLOYEES: {
    BASE:            '/hr/employees',
    BY_ID:           (id: string) => `/hr/employees/${id}`,
    PHOTO:           (id: string) => `/hr/employees/${id}/photo`,
    EMERGENCY:       (id: string) => `/hr/employees/${id}/emergency-contacts`,
    EMERGENCY_BY_ID: (id: string, cId: string) => `/hr/employees/${id}/emergency-contacts/${cId}`,
    BANK:            (id: string) => `/hr/employees/${id}/bank-details`,
    BANK_BY_ID:      (id: string, bId: string) => `/hr/employees/${id}/bank-details/${bId}`,
    SKILLS:          (id: string) => `/hr/employees/${id}/skills`,
    SKILL_BY_ID:     (id: string, sId: string) => `/hr/employees/${id}/skills/${sId}`,
    CERTS:           (id: string) => `/hr/employees/${id}/certifications`,
    CERT_BY_ID:      (id: string, cId: string) => `/hr/employees/${id}/certifications/${cId}`,
  },
  DEPARTMENTS: {
    BASE:      '/hr/departments',
    ALL:       '/hr/departments/all',
    BY_ID:     (id: string) => `/hr/departments/${id}`,
    ORG_CHART: '/hr/departments/org-chart',
  },
  BRANCHES: {
    BASE:  '/hr/branches',
    ALL:   '/hr/branches/all',
    BY_ID: (id: string) => `/hr/branches/${id}`,
  },
  DESIGNATIONS: {
    BASE:  '/hr/designations',
    ALL:   '/hr/designations/all',
    BY_ID: (id: string) => `/hr/designations/${id}`,
  },
  TEAMS: {
    BASE:      '/hr/teams',
    ALL:       '/hr/teams/all',
    BY_ID:     (id: string) => `/hr/teams/${id}`,
    MEMBERS:   (id: string) => `/hr/teams/${id}/members`,
    MEMBER:    (id: string, empId: string) => `/hr/teams/${id}/members/${empId}`,
  },
  ASSETS: {
    BASE:        '/hr/assets',
    BY_ID:       (id: string) => `/hr/assets/${id}`,
    BY_EMPLOYEE: (empId: string) => `/hr/assets/employee/${empId}`,
    ASSIGN:      (id: string) => `/hr/assets/${id}/assign`,
    RETURN:      (id: string) => `/hr/assets/${id}/return`,
    RETIRE:      (id: string) => `/hr/assets/${id}/retire`,
  },
  TRANSFERS: {
    BASE:        '/hr/transfers',
    BY_ID:       (id: string) => `/hr/transfers/${id}`,
    BY_EMPLOYEE: (empId: string) => `/hr/transfers/employee/${empId}`,
    APPROVE:     (id: string) => `/hr/transfers/${id}/approve`,
    REJECT:      (id: string) => `/hr/transfers/${id}/reject`,
  },
  PROMOTIONS: {
    BASE:        '/hr/promotions',
    BY_EMPLOYEE: (empId: string) => `/hr/promotions/employee/${empId}`,
    APPROVE:     (id: string) => `/hr/promotions/${id}/approve`,
    REJECT:      (id: string) => `/hr/promotions/${id}/reject`,
  },
  PROBATIONS: {
    BASE:        '/hr/probations',
    BY_EMPLOYEE: (empId: string) => `/hr/probations/employee/${empId}`,
    CONFIRM:     (id: string) => `/hr/probations/${id}/confirm`,
    EXTEND:      (id: string) => `/hr/probations/${id}/extend`,
    REJECT:      (id: string) => `/hr/probations/${id}/reject`,
  },
  ONBOARDING: {
    TASKS:       (empId: string) => `/hr/onboarding/employees/${empId}/tasks`,
    PROGRESS:    (empId: string) => `/hr/onboarding/employees/${empId}/progress`,
    MY_TASKS:    '/hr/onboarding/my-tasks',
    COMPLETE:    (taskId: string) => `/hr/onboarding/tasks/${taskId}/complete`,
    UPDATE_TASK: (taskId: string) => `/hr/onboarding/tasks/${taskId}`,
  },
  OFFBOARDING: {
    INITIATE:    '/hr/offboarding/initiate',
    TASKS:       (empId: string) => `/hr/offboarding/employees/${empId}/tasks`,
    MY_TASKS:    '/hr/offboarding/my-tasks',
    COMPLETE:    (taskId: string) => `/hr/offboarding/tasks/${taskId}/complete`,
    ADD_TASK:    (empId: string) => `/hr/offboarding/employees/${empId}/tasks`,
  },
  DASHBOARD:    { STATS: '/hr/dashboard/stats' },
  NOTIFICATIONS: {
    BASE:         '/api/v1/hr/notifications',
    UNREAD_COUNT: '/api/v1/hr/notifications/unread-count',
    MARK_READ:    (id: string) => `/api/v1/hr/notifications/${id}/read`,
    MARK_ALL:     '/api/v1/hr/notifications/mark-all-read',
  },
  REPORTS: {
    EMPLOYEES:  '/hr/reports/employees',
    NEW_JOINERS:'/hr/reports/new-joiners',
    PROBATION:  '/hr/reports/probation',
    TRANSFERS:  '/hr/reports/transfers',
    PROMOTIONS: '/hr/reports/promotions',
    ASSETS:     '/hr/reports/assets',
  },
  AUDIT: { BASE: '/audit-logs' },
};
