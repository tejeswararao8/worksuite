const SIZE_MAP = {
  xs: 'w-6 h-6 text-[10px]',
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-14 h-14 text-xl',
  xl: 'w-20 h-20 text-2xl',
};

const COLOR_MAP = [
  'bg-indigo-100 text-indigo-600',
  'bg-emerald-100 text-emerald-600',
  'bg-amber-100 text-amber-600',
  'bg-sky-100 text-sky-600',
  'bg-rose-100 text-rose-600',
  'bg-violet-100 text-violet-600',
];

function getInitials(name = '') {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join('');
}

function getColorIndex(name = '') {
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return Math.abs(hash) % COLOR_MAP.length;
}

export function Avatar({ name = '', src, size = 'md', className = '' }) {
  const sizeClass  = SIZE_MAP[size] ?? SIZE_MAP.md;
  const colorClass = COLOR_MAP[getColorIndex(name)];
  const initials   = getInitials(name);

  if (src) {
    return (
      <img
        src={src}
        alt={name}
        className={`${sizeClass} rounded-full object-cover shrink-0 ${className}`}
      />
    );
  }

  return (
    <div
      className={`${sizeClass} ${colorClass} rounded-full flex items-center justify-center font-semibold shrink-0 ${className}`}
      aria-label={name}
      title={name}
    >
      {initials || '?'}
    </div>
  );
}
