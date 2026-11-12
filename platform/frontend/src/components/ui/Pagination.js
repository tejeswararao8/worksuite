import { useTranslation } from 'react-i18next';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export function Pagination({ page, totalPages, onPageChange }) {
  const { t } = useTranslation();
  if (totalPages <= 1) return null;

  // Build a compact page window: always show first, last, current ±1
  const pages = [];
  const addPage = (n) => { if (n >= 0 && n < totalPages && !pages.includes(n)) pages.push(n); };
  addPage(0);
  addPage(page - 1);
  addPage(page);
  addPage(page + 1);
  addPage(totalPages - 1);
  pages.sort((a, b) => a - b);

  // Insert ellipsis markers
  const items = [];
  pages.forEach((p, i) => {
    if (i > 0 && p - pages[i - 1] > 1) items.push('...');
    items.push(p);
  });

  return (
    <div className="flex items-center justify-between mt-4 flex-wrap gap-3">
      <p className="text-sm text-slate-500">
        {t('common.page')} {page + 1} {t('common.of')} {totalPages}
      </p>
      <div className="flex items-center gap-1">
        <button
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
          className="p-1.5 rounded-lg border border-slate-200 disabled:opacity-40 hover:bg-slate-50 transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500"
          aria-label={t('common.previous')}
        >
          <ChevronLeft size={16} />
        </button>

        {items.map((item, i) =>
          item === '...' ? (
            <span key={`ellipsis-${i}`} className="px-2 text-slate-400 text-sm select-none">…</span>
          ) : (
            <button
              key={item}
              onClick={() => onPageChange(item)}
              className={`min-w-[32px] h-8 px-2 rounded-lg text-sm border transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 ${
                item === page
                  ? 'bg-indigo-600 text-white border-indigo-600'
                  : 'border-slate-200 text-slate-600 hover:bg-slate-50'
              }`}
              aria-current={item === page ? 'page' : undefined}
            >
              {item + 1}
            </button>
          )
        )}

        <button
          disabled={page + 1 >= totalPages}
          onClick={() => onPageChange(page + 1)}
          className="p-1.5 rounded-lg border border-slate-200 disabled:opacity-40 hover:bg-slate-50 transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500"
          aria-label={t('common.next')}
        >
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  );
}
