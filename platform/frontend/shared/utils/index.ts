import { format, isValid, parseISO } from 'date-fns';

// ─── Date Utils ───────────────────────────────────────────────────────────────
const DATE_FORMAT     = 'dd MMM yyyy';
const DATETIME_FORMAT = 'dd MMM yyyy, HH:mm';

export function formatDate(value?: string | Date | null, pattern = DATE_FORMAT): string {
  if (!value) return '—';
  const date = typeof value === 'string' ? parseISO(value) : value;
  return isValid(date) ? format(date, pattern) : '—';
}

export function formatDateTime(value?: string | Date | null): string {
  return formatDate(value, DATETIME_FORMAT);
}

export function daysUntil(dateStr?: string | null): number {
  if (!dateStr) return 0;
  const date = parseISO(dateStr);
  if (!isValid(date)) return 0;
  const diff = Math.ceil((date.getTime() - Date.now()) / (1000 * 60 * 60 * 24));
  return Math.max(0, diff);
}

export function isExpired(dateStr?: string | null): boolean {
  if (!dateStr) return false;
  const date = parseISO(dateStr);
  return isValid(date) && date < new Date();
}

export function isExpiringSoon(dateStr?: string | null, days = 30): boolean {
  if (!dateStr) return false;
  const date = parseISO(dateStr);
  if (!isValid(date)) return false;
  const now = new Date();
  const threshold = new Date(now.getTime() + days * 24 * 60 * 60 * 1000);
  return date >= now && date <= threshold;
}

// ─── Error Handler ────────────────────────────────────────────────────────────
export function getErrorMessage(error: unknown): string {
  if (!error) return 'An unexpected error occurred';

  const axiosError = error as {
    response?: { status: number; data?: { message?: string; errors?: Record<string, string> } };
    message?: string;
  };

  if (!axiosError.response) return 'Network error. Please check your connection.';

  const { status, data } = axiosError.response;

  if (data?.message) return data.message;
  if (data?.errors) {
    const first = Object.values(data.errors)[0];
    if (first) return first;
  }

  const messages: Record<number, string> = {
    400: 'Invalid request. Please check your input.',
    401: 'Session expired. Please log in again.',
    403: 'You do not have permission to perform this action.',
    404: 'The requested resource was not found.',
    409: 'A record with this information already exists.',
    500: 'Server error. Please try again later.',
  };

  return messages[status] ?? `Error ${status}. Please try again.`;
}

// ─── Storage Service ──────────────────────────────────────────────────────────
const KEYS = {
  ACCESS_TOKEN:  'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  USER:          'user',
  LANGUAGE:      'i18nextLng',
} as const;

export const storageService = {
  getAccessToken:  (): string | null => localStorage.getItem(KEYS.ACCESS_TOKEN),
  setAccessToken:  (token: string): void => { localStorage.setItem(KEYS.ACCESS_TOKEN, token); },
  getRefreshToken: (): string | null => localStorage.getItem(KEYS.REFRESH_TOKEN),
  setRefreshToken: (token: string): void => { localStorage.setItem(KEYS.REFRESH_TOKEN, token); },
  getUser: <T>(): T | null => {
    const raw = localStorage.getItem(KEYS.USER);
    try { return raw ? JSON.parse(raw) as T : null; } catch { return null; }
  },
  setUser: (user: unknown): void => { localStorage.setItem(KEYS.USER, JSON.stringify(user)); },
  getLanguage: (): string | null => localStorage.getItem(KEYS.LANGUAGE),
  setLanguage: (lang: string): void => { localStorage.setItem(KEYS.LANGUAGE, lang); },
  clear: (): void => { localStorage.clear(); },
};

// ─── Download Helper ──────────────────────────────────────────────────────────
export function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const a   = document.createElement('a');
  a.href     = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

// ─── Formatters ───────────────────────────────────────────────────────────────
export function fullName(firstName?: string, lastName?: string): string {
  return [firstName, lastName].filter(Boolean).join(' ') || '—';
}

export function extractPagedData<T>(response: unknown): { content: T[]; totalPages: number } {
  const r = response as { data?: { data?: { content?: T[]; totalPages?: number } | T[] } };
  const d = r?.data?.data;
  if (d && typeof d === 'object' && 'content' in d) {
    return { content: (d as { content: T[]; totalPages: number }).content || [], totalPages: (d as { content: T[]; totalPages: number }).totalPages || 0 };
  }
  return { content: Array.isArray(d) ? d : [], totalPages: 0 };
}

export function extractData<T>(response: unknown): T | null {
  const r = response as { data?: { data?: T } };
  return r?.data?.data ?? null;
}
