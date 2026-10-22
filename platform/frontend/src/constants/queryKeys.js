export const QUERY_KEYS = {
  // Dashboard
  DASHBOARD_SUMMARY:    ['dashboard-summary'],
  HEADCOUNT_DEPT:       ['headcount-dept'],
  HEADCOUNT_BRANCH:     ['headcount-branch'],
  JOINING_TREND:        ['joining-trend'],
  DOC_EXPIRY_ALERTS:    ['doc-expiry-alerts'],
  ASSET_UTILIZATION:    ['asset-utilization'],

  // Employee
  EMPLOYEES:            (page, q, deptId) => ['employees', page, q, deptId],
  EMPLOYEE_BY_ID:       (id) => ['employee', id],

  // Company
  COMPANIES:            (page) => ['companies', page],

  // Org structure
  DEPARTMENTS:          (page) => ['departments', page],
  DEPARTMENTS_ALL:      ['departments-all'],
  BRANCHES:             (page) => ['branches', page],
  BRANCHES_ALL:         ['branches-all'],
  DESIGNATIONS:         (page) => ['designations', page],
  DESIGNATIONS_ALL:     ['designations-all'],
  TEAMS:                (page) => ['teams', page],
  TEAMS_ALL:            ['teams-all'],

  // Assets
  ASSETS:               (page) => ['assets', page],
  ASSETS_BY_EMPLOYEE:   (empId) => ['assets-employee', empId],

  // Documents
  DOCUMENTS:            (page) => ['documents', page],

  // Lifecycle
  TRANSFERS:            (page) => ['transfers', page],
  TRANSFER_BY_EMPLOYEE: (empId) => ['transfers-employee', empId],
  PROMOTIONS:           (page) => ['promotions', page],
  PROMOTION_BY_EMPLOYEE:(empId) => ['promotions-employee', empId],
  PROBATION_BY_EMPLOYEE:(empId) => ['probation-employee', empId],
  ONBOARDING:           (page) => ['onboarding', page],
  ONBOARDING_BY_EMPLOYEE:(empId) => ['onboarding-employee', empId],
  OFFBOARDING_BY_EMPLOYEE:(empId) => ['offboarding-employee', empId],

  // Skills
  SKILLS_BY_EMPLOYEE:   (empId) => ['skills-employee', empId],
  CERTS_BY_EMPLOYEE:    (empId) => ['certs-employee', empId],

  // Users
  USER_BY_ID:           (id) => ['user', id],

  // Roles
  ROLES:                (page) => ['roles', page],
  PERMISSIONS:          ['permissions'],

  // Notifications
  NOTIFICATIONS:        ['notifications'],
  UNREAD_COUNT:         ['unread-count'],

  // Audit
  AUDIT_LOGS:           (page) => ['audit-logs', page],
  AUDIT_BY_ENTITY:      (entity, page) => ['audit-entity', entity, page],
};
