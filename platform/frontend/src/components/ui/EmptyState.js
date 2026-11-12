import { Inbox } from 'lucide-react';

export function EmptyState({
  icon: Icon = Inbox,
  title,
  description,
  action,
  className = '',
}) {
  return (
    <div
      className={`bg-white rounded-xl border border-slate-200 p-10 flex flex-col items-center justify-center text-center gap-3 ${className}`}
    >
      <div className="w-12 h-12 rounded-full bg-slate-100 flex items-center justify-center">
        <Icon size={22} className="text-slate-400" />
      </div>
      {title && (
        <p className="text-sm font-semibold text-slate-700">{title}</p>
      )}
      {description && (
        <p className="text-sm text-slate-400 max-w-xs">{description}</p>
      )}
      {action && <div className="mt-1">{action}</div>}
    </div>
  );
}
