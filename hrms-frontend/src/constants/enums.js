// ─── Employment ──────────────────────────────────────────────────────────────
export const EMPLOYMENT_STATUS = {
  ACTIVE:      'ACTIVE',
  PROBATION:   'PROBATION',
  RESIGNED:    'RESIGNED',
  TERMINATED:  'TERMINATED',
  ON_LEAVE:    'ON_LEAVE',
};

export const EMPLOYMENT_TYPE = {
  FULL_TIME: 'FULL_TIME',
  PART_TIME: 'PART_TIME',
  CONTRACT:  'CONTRACT',
  INTERN:    'INTERN',
};

export const GENDER = { MALE: 'MALE', FEMALE: 'FEMALE', OTHER: 'OTHER' };

export const MARITAL_STATUS = { SINGLE: 'SINGLE', MARRIED: 'MARRIED', DIVORCED: 'DIVORCED' };

// ─── Asset ───────────────────────────────────────────────────────────────────
export const ASSET_STATUS = {
  AVAILABLE: 'AVAILABLE',
  ASSIGNED:  'ASSIGNED',
  IN_REPAIR: 'IN_REPAIR',
  RETIRED:   'RETIRED',
};

export const ASSET_TYPE = {
  LAPTOP:    'LAPTOP',
  MOBILE:    'MOBILE',
  VEHICLE:   'VEHICLE',
  FURNITURE: 'FURNITURE',
  OTHER:     'OTHER',
};

// ─── Document ─────────────────────────────────────────────────────────────────
export const DOCUMENT_TYPE = {
  PASSPORT:    'PASSPORT',
  VISA:        'VISA',
  CONTRACT:    'CONTRACT',
  CERTIFICATE: 'CERTIFICATE',
  OTHER:       'OTHER',
};

// ─── Transfer / Promotion ─────────────────────────────────────────────────────
export const APPROVAL_STATUS = {
  PENDING:  'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
};

// ─── Notification ─────────────────────────────────────────────────────────────
export const NOTIFICATION_TYPE = {
  DOCUMENT_EXPIRY: 'DOCUMENT_EXPIRY',
  PROBATION:       'PROBATION',
  TRANSFER:        'TRANSFER',
  PROMOTION:       'PROMOTION',
  GENERAL:         'GENERAL',
};

// ─── Badge color maps (centralized — no per-page duplication) ─────────────────
export const EMPLOYMENT_STATUS_COLOR = {
  [EMPLOYMENT_STATUS.ACTIVE]:     'green',
  [EMPLOYMENT_STATUS.PROBATION]:  'yellow',
  [EMPLOYMENT_STATUS.RESIGNED]:   'red',
  [EMPLOYMENT_STATUS.TERMINATED]: 'red',
  [EMPLOYMENT_STATUS.ON_LEAVE]:   'blue',
};

export const ASSET_STATUS_COLOR = {
  [ASSET_STATUS.AVAILABLE]: 'green',
  [ASSET_STATUS.ASSIGNED]:  'blue',
  [ASSET_STATUS.IN_REPAIR]: 'yellow',
  [ASSET_STATUS.RETIRED]:   'gray',
};

export const DOCUMENT_TYPE_COLOR = {
  [DOCUMENT_TYPE.PASSPORT]:    'blue',
  [DOCUMENT_TYPE.VISA]:        'indigo',
  [DOCUMENT_TYPE.CONTRACT]:    'green',
  [DOCUMENT_TYPE.CERTIFICATE]: 'yellow',
  [DOCUMENT_TYPE.OTHER]:       'gray',
};

export const APPROVAL_STATUS_COLOR = {
  [APPROVAL_STATUS.PENDING]:  'yellow',
  [APPROVAL_STATUS.APPROVED]: 'green',
  [APPROVAL_STATUS.REJECTED]: 'red',
};

export const NOTIFICATION_TYPE_COLOR = {
  [NOTIFICATION_TYPE.DOCUMENT_EXPIRY]: 'red',
  [NOTIFICATION_TYPE.PROBATION]:       'yellow',
  [NOTIFICATION_TYPE.TRANSFER]:        'blue',
  [NOTIFICATION_TYPE.PROMOTION]:       'green',
  [NOTIFICATION_TYPE.GENERAL]:         'gray',
};

// ─── Pagination ───────────────────────────────────────────────────────────────
export const DEFAULT_PAGE_SIZE = 20;
