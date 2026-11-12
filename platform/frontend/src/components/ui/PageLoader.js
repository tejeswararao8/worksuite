import { Spinner } from './Spinner';

/**
 * Full-page loading fallback used by React.lazy Suspense boundaries.
 * Matches the AppLayout content area height so there is no layout shift.
 */
export function PageLoader() {
  return (
    <div
      className="flex items-center justify-center w-full"
      style={{ minHeight: '60vh' }}
      role="status"
      aria-label="Loading page"
    >
      <div className="flex flex-col items-center gap-3">
        <Spinner size="lg" />
        <p className="text-sm text-slate-400 font-medium">Loading…</p>
      </div>
    </div>
  );
}
