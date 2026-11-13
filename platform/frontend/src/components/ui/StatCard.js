const COLOR_MAP = {
  indigo:  'bg-indigo-50 text-indigo-600',
  green:   'bg-green-50 text-green-600',
  yellow:  'bg-yellow-50 text-yellow-600',
  amber:   'bg-amber-50 text-amber-600',
  red:     'bg-red-50 text-red-600',
  blue:    'bg-blue-50 text-blue-600',
  orange:  'bg-orange-50 text-orange-600',
  violet:  'bg-violet-50 text-violet-600',
};

export function StatCard({ title, label, value, icon: Icon, color = 'indigo', subtitle }) {
  const heading = title ?? label;
  return (
    <div className="bg-white rounded-xl border border-slate-200 p-5 flex items-center gap-4">
      <div className={`p-3 rounded-xl shrink-0 ${COLOR_MAP[color] ?? COLOR_MAP.indigo}`}>
        <Icon size={22} />
      </div>
      <div className="min-w-0">
        <p className="text-sm text-slate-500 truncate">{heading}</p>
        <p className="text-2xl font-bold text-slate-800">{value ?? '—'}</p>
        {subtitle && <p className="text-xs text-slate-400 mt-0.5 truncate">{subtitle}</p>}
      </div>
    </div>
  );
}
