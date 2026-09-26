import { format, isValid, parseISO } from 'date-fns';

const DEFAULT_FORMAT = 'dd MMM yyyy';
const DATETIME_FORMAT = 'dd MMM yyyy, HH:mm';

/**
 * Safely formats a date string or Date object.
 * Returns '—' for null/invalid dates.
 */
export function formatDate(value, pattern = DEFAULT_FORMAT) {
  if (!value) return '—';
  const date = typeof value === 'string' ? parseISO(value) : value;
  return isValid(date) ? format(date, pattern) : '—';
}

export function formatDateTime(value) {
  return formatDate(value, DATETIME_FORMAT);
}
