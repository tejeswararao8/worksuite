const SIZE_MAP = {
  sm: 'w-4 h-4 border-2',
  md: 'w-8 h-8 border-2',
  lg: 'w-12 h-12 border-[3px]',
};

export function Spinner({ size = 'md', className = '' }) {
  return (
    <div
      role="status"
      aria-label="Loading"
      className={`${SIZE_MAP[size] ?? SIZE_MAP.md} border-indigo-200 border-t-indigo-600 rounded-full animate-spin ${className}`}
    />
  );
}
