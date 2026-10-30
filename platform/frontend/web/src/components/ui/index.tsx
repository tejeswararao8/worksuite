import { useTranslation } from 'react-i18next';
import { ReactNode, InputHTMLAttributes, SelectHTMLAttributes, TextareaHTMLAttributes } from 'react';

// ─── Button ───────────────────────────────────────────────────────────────────
type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost' | 'success';
type ButtonSize    = 'sm' | 'md' | 'lg';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
}

export function Button({ children, variant = 'primary', size = 'md', loading, className = '', disabled, ...props }: ButtonProps) {
  const base = 'inline-flex items-center gap-2 font-medium rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-offset-1 disabled:opacity-50 disabled:cursor-not-allowed';
  const variants: Record<ButtonVariant, string> = {
    primary:   'bg-indigo-600 text-white hover:bg-indigo-700 focus:ring-indigo-500',
    secondary: 'bg-slate-100 text-slate-700 hover:bg-slate-200 focus:ring-slate-400',
    danger:    'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
    ghost:     'text-slate-600 hover:bg-slate-100 focus:ring-slate-400',
    success:   'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
  };
  const sizes: Record<ButtonSize, string> = { sm: 'px-3 py-1.5 text-xs', md: 'px-4 py-2 text-sm', lg: 'px-5 py-2.5 text-base' };
  return (
    <button className={`${base} ${variants[variant]} ${sizes[size]} ${className}`} disabled={disabled || loading} {...props}>
      {loading && <span className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />}
      {children}
    </button>
  );
}

// ─── Input ────────────────────────────────────────────────────────────────────
interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  hint?: string;
}

export function Input({ label, error, hint, className = '', ...props }: InputProps) {
  return (
    <div className="flex flex-col gap-1">
      {label && <label className="text-sm font-medium text-slate-700">{label}</label>}
      <input
        className={`border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 ${error ? 'border-red-400 bg-red-50' : 'border-slate-300'} ${className}`}
        {...props}
      />
      {error && <p className="text-xs text-red-500">{error}</p>}
      {hint && !error && <p className="text-xs text-slate-400">{hint}</p>}
    </div>
  );
}

// ─── Textarea ─────────────────────────────────────────────────────────────────
interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
}

export function Textarea({ label, error, className = '', ...props }: TextareaProps) {
  return (
    <div className="flex flex-col gap-1">
      {label && <label className="text-sm font-medium text-slate-700">{label}</label>}
      <textarea
        rows={3}
        className={`border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none ${error ? 'border-red-400' : 'border-slate-300'} ${className}`}
        {...props}
      />
      {error && <p className="text-xs text-red-500">{error}</p>}
    </div>
  );
}

// ─── Select ───────────────────────────────────────────────────────────────────
interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  children: ReactNode;
}

export function Select({ label, error, children, className = '', ...props }: SelectProps) {
  return (
    <div className="flex flex-col gap-1">
      {label && <label className="text-sm font-medium text-slate-700">{label}</label>}
      <select
        className={`border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white ${error ? 'border-red-400' : 'border-slate-300'} ${className}`}
        {...props}
      >
        {children}
      </select>
      {error && <p className="text-xs text-red-500">{error}</p>}
    </div>
  );
}

// ─── Modal ────────────────────────────────────────────────────────────────────
type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | '2xl';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  size?: ModalSize;
}

export function Modal({ open, onClose, title, children, size = 'md' }: ModalProps) {
  if (!open) return null;
  const sizes: Record<ModalSize, string> = { sm: 'max-w-sm', md: 'max-w-lg', lg: 'max-w-2xl', xl: 'max-w-4xl', '2xl': 'max-w-6xl' };
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onClose}>
      <div className={`bg-white rounded-xl shadow-xl w-full ${sizes[size]} max-h-[90vh] flex flex-col`} onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between px-6 py-4 border-b shrink-0">
          <h2 className="text-base font-semibold text-slate-800">{title}</h2>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 text-xl leading-none w-8 h-8 flex items-center justify-center rounded hover:bg-slate-100">&times;</button>
        </div>
        <div className="overflow-y-auto p-6 flex-1">{children}</div>
      </div>
    </div>
  );
}

// ─── ConfirmDialog ────────────────────────────────────────────────────────────
interface ConfirmDialogProps {
  open: boolean;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
  confirmLabel?: string;
  variant?: 'danger' | 'primary';
}

export function ConfirmDialog({ open, message, onConfirm, onCancel, confirmLabel, variant = 'danger' }: ConfirmDialogProps) {
  const { t } = useTranslation();
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-sm p-6">
        <p className="text-sm text-slate-700 mb-6">{message}</p>
        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={onCancel}>{t('common.cancel')}</Button>
          <Button variant={variant} onClick={onConfirm}>{confirmLabel || t('common.confirm')}</Button>
        </div>
      </div>
    </div>
  );
}

// ─── Table ────────────────────────────────────────────────────────────────────
export interface Column<T> {
  key: string;
  label: string;
  render?: (row: T) => ReactNode;
  className?: string;
}

interface TableProps<T> {
  columns: Column<T>[];
  data: T[];
  loading?: boolean;
  onRowClick?: (row: T) => void;
}

export function Table<T extends { id?: string }>({ columns, data, loading, onRowClick }: TableProps<T>) {
  const { t } = useTranslation();
  return (
    <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 border-b border-slate-200">
          <tr>
            {columns.map((col) => (
              <th key={col.key} className={`px-4 py-3 text-left font-semibold text-slate-600 whitespace-nowrap ${col.className ?? ''}`}>
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan={columns.length} className="text-center py-12 text-slate-400">{t('common.loading')}</td></tr>
          ) : !data?.length ? (
            <tr><td colSpan={columns.length} className="text-center py-12 text-slate-400">{t('common.noRecords')}</td></tr>
          ) : (
            data.map((row, i) => (
              <tr
                key={(row as { id?: string }).id || i}
                onClick={() => onRowClick?.(row)}
                className={`border-b border-slate-100 hover:bg-slate-50 transition-colors ${onRowClick ? 'cursor-pointer' : ''}`}
              >
                {columns.map((col) => (
                  <td key={col.key} className={`px-4 py-3 text-slate-700 ${col.className ?? ''}`}>
                    {col.render ? col.render(row) : String((row as Record<string, unknown>)[col.key] ?? '—')}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

// ─── Badge ────────────────────────────────────────────────────────────────────
type BadgeColor = 'green' | 'red' | 'yellow' | 'blue' | 'indigo' | 'gray' | 'orange' | 'purple';

interface BadgeProps { label: string; color?: BadgeColor; }

export function Badge({ label, color = 'gray' }: BadgeProps) {
  const colors: Record<BadgeColor, string> = {
    green:  'bg-green-100 text-green-700',
    red:    'bg-red-100 text-red-700',
    yellow: 'bg-yellow-100 text-yellow-700',
    blue:   'bg-blue-100 text-blue-700',
    indigo: 'bg-indigo-100 text-indigo-700',
    gray:   'bg-slate-100 text-slate-600',
    orange: 'bg-orange-100 text-orange-700',
    purple: 'bg-purple-100 text-purple-700',
  };
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${colors[color]}`}>
      {label}
    </span>
  );
}

// ─── Spinner ──────────────────────────────────────────────────────────────────
export function Spinner({ size = 'md' }: { size?: 'sm' | 'md' | 'lg' }) {
  const sizes = { sm: 'w-4 h-4', md: 'w-8 h-8', lg: 'w-12 h-12' };
  return <div className={`${sizes[size]} border-2 border-indigo-200 border-t-indigo-600 rounded-full animate-spin`} />;
}

// ─── StatCard ─────────────────────────────────────────────────────────────────
interface StatCardProps {
  title: string;
  value: number | string;
  icon: React.ComponentType<{ size?: number }>;
  color?: 'indigo' | 'green' | 'yellow' | 'red' | 'blue' | 'orange';
  subtitle?: string;
  onClick?: () => void;
}

export function StatCard({ title, value, icon: Icon, color = 'indigo', subtitle, onClick }: StatCardProps) {
  const colors = {
    indigo: 'bg-indigo-50 text-indigo-600',
    green:  'bg-green-50 text-green-600',
    yellow: 'bg-yellow-50 text-yellow-600',
    red:    'bg-red-50 text-red-600',
    blue:   'bg-blue-50 text-blue-600',
    orange: 'bg-orange-50 text-orange-600',
  };
  return (
    <div
      onClick={onClick}
      className={`bg-white rounded-xl border border-slate-200 p-5 flex items-center gap-4 ${onClick ? 'cursor-pointer hover:shadow-md transition-shadow' : ''}`}
    >
      <div className={`p-3 rounded-xl ${colors[color]}`}><Icon size={22} /></div>
      <div>
        <p className="text-sm text-slate-500">{title}</p>
        <p className="text-2xl font-bold text-slate-800">{value ?? '—'}</p>
        {subtitle && <p className="text-xs text-slate-400 mt-0.5">{subtitle}</p>}
      </div>
    </div>
  );
}

// ─── Pagination ───────────────────────────────────────────────────────────────
interface PaginationProps { page: number; totalPages: number; onPageChange: (p: number) => void; }

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  const { t } = useTranslation();
  if (totalPages <= 1) return null;
  return (
    <div className="flex items-center justify-between mt-4">
      <p className="text-sm text-slate-500">{t('common.page')} {page + 1} {t('common.of')} {totalPages}</p>
      <div className="flex gap-2">
        <button disabled={page === 0} onClick={() => onPageChange(page - 1)} className="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-40 hover:bg-slate-50">{t('common.previous')}</button>
        <button disabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)} className="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-40 hover:bg-slate-50">{t('common.next')}</button>
      </div>
    </div>
  );
}

// ─── PageHeader ───────────────────────────────────────────────────────────────
interface PageHeaderProps { title: string; action?: ReactNode; subtitle?: string; }

export function PageHeader({ title, action, subtitle }: PageHeaderProps) {
  return (
    <div className="flex items-start justify-between mb-6">
      <div>
        <h2 className="text-xl font-semibold text-slate-800">{title}</h2>
        {subtitle && <p className="text-sm text-slate-500 mt-0.5">{subtitle}</p>}
      </div>
      {action && <div>{action}</div>}
    </div>
  );
}

// ─── Tabs ─────────────────────────────────────────────────────────────────────
interface Tab { key: string; label: string; icon?: React.ComponentType<{ size?: number }>; }
interface TabsProps { tabs: Tab[]; active: string; onChange: (key: string) => void; }

export function Tabs({ tabs, active, onChange }: TabsProps) {
  return (
    <div className="flex gap-1 border-b border-slate-200 mb-6 overflow-x-auto">
      {tabs.map((tab) => (
        <button
          key={tab.key}
          onClick={() => onChange(tab.key)}
          className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium whitespace-nowrap border-b-2 transition-colors ${
            active === tab.key
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300'
          }`}
        >
          {tab.icon && <tab.icon size={15} />}
          {tab.label}
        </button>
      ))}
    </div>
  );
}

// ─── ProgressBar ──────────────────────────────────────────────────────────────
interface ProgressBarProps { value: number; max?: number; color?: string; showLabel?: boolean; }

export function ProgressBar({ value, max = 100, color = 'bg-indigo-500', showLabel = true }: ProgressBarProps) {
  const pct = Math.min(100, Math.round((value / max) * 100));
  return (
    <div className="flex items-center gap-2">
      <div className="flex-1 bg-slate-200 rounded-full h-2">
        <div className={`${color} h-2 rounded-full transition-all`} style={{ width: `${pct}%` }} />
      </div>
      {showLabel && <span className="text-xs text-slate-500 w-10 text-right">{pct}%</span>}
    </div>
  );
}

// ─── EmptyState ───────────────────────────────────────────────────────────────
interface EmptyStateProps { icon?: React.ComponentType<{ size?: number }>; title: string; description?: string; action?: ReactNode; }

export function EmptyState({ icon: Icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      {Icon && <div className="p-4 bg-slate-100 rounded-full mb-4"><Icon size={32} /></div>}
      <h3 className="text-base font-semibold text-slate-700 mb-1">{title}</h3>
      {description && <p className="text-sm text-slate-400 mb-4 max-w-xs">{description}</p>}
      {action}
    </div>
  );
}

// ─── AlertBanner ──────────────────────────────────────────────────────────────
type AlertType = 'info' | 'warning' | 'error' | 'success';
interface AlertBannerProps { type?: AlertType; message: string; onClose?: () => void; }

export function AlertBanner({ type = 'info', message, onClose }: AlertBannerProps) {
  const styles: Record<AlertType, string> = {
    info:    'bg-blue-50 border-blue-200 text-blue-800',
    warning: 'bg-yellow-50 border-yellow-200 text-yellow-800',
    error:   'bg-red-50 border-red-200 text-red-800',
    success: 'bg-green-50 border-green-200 text-green-800',
  };
  return (
    <div className={`flex items-center justify-between px-4 py-3 rounded-lg border text-sm ${styles[type]}`}>
      <span>{message}</span>
      {onClose && <button onClick={onClose} className="ml-4 opacity-60 hover:opacity-100">&times;</button>}
    </div>
  );
}
