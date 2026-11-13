import { useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import {
  Building2, CheckCircle, XCircle, PauseCircle, PlayCircle,
  ChevronRight, History, AlertTriangle,
} from 'lucide-react';
import {
  PageHeader, Badge, Button, Table, Pagination,
  SearchBar, Modal, Skeleton, EmptyState,
} from '../../../components/ui';
import { useTenants, useTenantById, useTenantHistory, useTenantMutations } from '../../../hooks/useTenants';

// ── Status badge ──────────────────────────────────────────────────────────────
const STATUS_COLOR = {
  PENDING_REVIEW: 'yellow',
  ACTIVE:         'green',
  SUSPENDED:      'orange',
  REJECTED:       'red',
  TRIAL:          'blue',
  CANCELLED:      'gray',
};

function StatusBadge({ status }) {
  const { t } = useTranslation();
  return (
    <Badge
      label={t(`platform.tenant.status.${status}`, { defaultValue: status })}
      color={STATUS_COLOR[status] ?? 'gray'}
    />
  );
}

// ── Reason modal ──────────────────────────────────────────────────────────────
function ReasonModal({ open, onClose, onConfirm, title, required = false, loading = false }) {
  const { t } = useTranslation();
  const [reason, setReason] = useState('');
  const [error, setError]   = useState('');

  const handleConfirm = () => {
    if (required && !reason.trim()) {
      setError(t('common.required'));
      return;
    }
    onConfirm(reason.trim() || undefined);
    setReason('');
    setError('');
  };

  const handleClose = () => {
    setReason('');
    setError('');
    onClose();
  };

  return (
    <Modal open={open} onClose={handleClose} title={title} size="sm">
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">
            {t('common.reason')} {required && <span className="text-red-500">*</span>}
          </label>
          <textarea
            className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
            rows={3}
            value={reason}
            onChange={(e) => { setReason(e.target.value); setError(''); }}
            placeholder={t('platform.tenant.reasonPlaceholder')}
          />
          {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
        </div>
        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={handleClose}>{t('common.cancel')}</Button>
          <Button onClick={handleConfirm} loading={loading}>{t('common.confirm')}</Button>
        </div>
      </div>
    </Modal>
  );
}

// ── Tenant detail panel ───────────────────────────────────────────────────────
function TenantDetailModal({ tenantId, onClose, onAction }) {
  const { t } = useTranslation();
  const { tenant, isLoading } = useTenantById(tenantId);
  const { history, isLoading: histLoading } = useTenantHistory(tenantId);

  if (!tenantId) return null;

  return (
    <Modal open={!!tenantId} onClose={onClose} title={t('platform.tenant.details')} size="lg">
      {isLoading ? (
        <div className="space-y-3">
          <Skeleton className="h-5 w-48" />
          <Skeleton className="h-4 w-64" />
          <Skeleton className="h-4 w-40" />
        </div>
      ) : tenant ? (
        <div className="space-y-6">
          {/* Info grid */}
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-slate-500">{t('platform.tenant.name')}</p>
              <p className="font-medium text-slate-800">{tenant.name}</p>
            </div>
            <div>
              <p className="text-slate-500">{t('platform.tenant.slug')}</p>
              <p className="font-medium text-slate-800">{tenant.slug}</p>
            </div>
            <div>
              <p className="text-slate-500">{t('platform.tenant.email')}</p>
              <p className="font-medium text-slate-800">{tenant.email}</p>
            </div>
            <div>
              <p className="text-slate-500">{t('common.status')}</p>
              <StatusBadge status={tenant.status} />
            </div>
            {tenant.phone && (
              <div>
                <p className="text-slate-500">{t('platform.tenant.phone')}</p>
                <p className="font-medium text-slate-800">{tenant.phone}</p>
              </div>
            )}
            <div>
              <p className="text-slate-500">{t('common.createdAt')}</p>
              <p className="font-medium text-slate-800">
                {tenant.createdAt ? new Date(tenant.createdAt).toLocaleDateString() : '—'}
              </p>
            </div>
            {tenant.reviewNotes && (
              <div className="col-span-2">
                <p className="text-slate-500">{t('platform.tenant.reviewNotes')}</p>
                <p className="font-medium text-slate-800">{tenant.reviewNotes}</p>
              </div>
            )}
          </div>

          {/* Action buttons */}
          <div className="flex flex-wrap gap-2 pt-2 border-t border-slate-100">
            {tenant.status === 'PENDING_REVIEW' && (
              <>
                <Button size="sm" onClick={() => onAction('approve', tenant)}>
                  <CheckCircle size={14} /> {t('platform.tenant.approve')}
                </Button>
                <Button size="sm" variant="danger" onClick={() => onAction('reject', tenant)}>
                  <XCircle size={14} /> {t('platform.tenant.reject')}
                </Button>
              </>
            )}
            {tenant.status === 'ACTIVE' && (
              <Button size="sm" variant="danger" onClick={() => onAction('suspend', tenant)}>
                <PauseCircle size={14} /> {t('platform.tenant.suspend')}
              </Button>
            )}
            {tenant.status === 'SUSPENDED' && (
              <Button size="sm" onClick={() => onAction('reactivate', tenant)}>
                <PlayCircle size={14} /> {t('platform.tenant.reactivate')}
              </Button>
            )}
          </div>

          {/* Lifecycle history */}
          <div>
            <h3 className="text-sm font-semibold text-slate-700 mb-3 flex items-center gap-2">
              <History size={14} /> {t('platform.tenant.history')}
            </h3>
            {histLoading ? (
              <Skeleton className="h-20" />
            ) : history.length === 0 ? (
              <p className="text-sm text-slate-400">{t('platform.tenant.noHistory')}</p>
            ) : (
              <ol className="relative border-s border-slate-200 space-y-4 ps-4">
                {history.map((ev) => (
                  <li key={ev.id} className="text-sm">
                    <div className="absolute -start-1.5 mt-1 w-3 h-3 rounded-full bg-indigo-500" />
                    <div className="flex items-center gap-2 flex-wrap">
                      <StatusBadge status={ev.fromStatus} />
                      <ChevronRight size={12} className="text-slate-400" />
                      <StatusBadge status={ev.toStatus} />
                      <span className="text-slate-400 text-xs">
                        {ev.occurredAt ? new Date(ev.occurredAt).toLocaleString() : ''}
                      </span>
                    </div>
                    {ev.reason && (
                      <p className="text-slate-500 mt-0.5 text-xs">{ev.reason}</p>
                    )}
                  </li>
                ))}
              </ol>
            )}
          </div>
        </div>
      ) : null}
    </Modal>
  );
}

// ── Status tabs ───────────────────────────────────────────────────────────────
const STATUS_TABS = ['', 'PENDING_REVIEW', 'ACTIVE', 'SUSPENDED', 'REJECTED'];

// ── Main page ─────────────────────────────────────────────────────────────────
export default function TenantsPage() {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.dir() === 'rtl';

  const [page,   setPage]   = useState(0);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [selectedId, setSelectedId] = useState(null);

  // Action state
  const [action, setAction]   = useState(null); // { type, tenant }
  const [reasonOpen, setReasonOpen] = useState(false);

  const { tenants, totalPages, isLoading, isError } = useTenants({ page, search, status });
  const { approve, reject, suspend, reactivate } = useTenantMutations();

  const handleSearch = useCallback((val) => {
    setSearch(val);
    setPage(0);
  }, []);

  const handleStatusTab = (s) => {
    setStatus(s);
    setPage(0);
  };

  const handleAction = (type, tenant) => {
    setAction({ type, tenant });
    setSelectedId(null);
    setReasonOpen(true);
  };

  const handleActionConfirm = async (reasonText) => {
    const { type, tenant } = action;
    const id = tenant.id;
    const mutationArgs = { id, reason: reasonText };

    try {
      if (type === 'approve')    await approve.mutateAsync(mutationArgs);
      if (type === 'reject')     await reject.mutateAsync(mutationArgs);
      if (type === 'suspend')    await suspend.mutateAsync(mutationArgs);
      if (type === 'reactivate') await reactivate.mutateAsync(mutationArgs);

      toast.success(t(`platform.tenant.${type}Success`));
    } catch {
      toast.error(t('platform.tenant.actionError'));
    } finally {
      setReasonOpen(false);
      setAction(null);
    }
  };

  const columns = [
    {
      key: 'name',
      label: t('platform.tenant.name'),
      render: (row) => (
        <button
          className="text-indigo-600 hover:underline font-medium text-left"
          onClick={() => setSelectedId(row.id)}
        >
          {row.name}
        </button>
      ),
    },
    { key: 'slug',  label: t('platform.tenant.slug') },
    { key: 'email', label: t('platform.tenant.email') },
    {
      key: 'status',
      label: t('common.status'),
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'createdAt',
      label: t('common.createdAt'),
      render: (row) => row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '—',
    },
    {
      key: 'actions',
      label: t('common.actions'),
      render: (row) => (
        <div className="flex gap-2 flex-wrap">
          {row.status === 'PENDING_REVIEW' && (
            <>
              <Button size="sm" onClick={() => handleAction('approve', row)}>
                <CheckCircle size={12} /> {t('platform.tenant.approve')}
              </Button>
              <Button size="sm" variant="danger" onClick={() => handleAction('reject', row)}>
                <XCircle size={12} /> {t('platform.tenant.reject')}
              </Button>
            </>
          )}
          {row.status === 'ACTIVE' && (
            <Button size="sm" variant="outline" onClick={() => handleAction('suspend', row)}>
              <PauseCircle size={12} /> {t('platform.tenant.suspend')}
            </Button>
          )}
          {row.status === 'SUSPENDED' && (
            <Button size="sm" onClick={() => handleAction('reactivate', row)}>
              <PlayCircle size={12} /> {t('platform.tenant.reactivate')}
            </Button>
          )}
          <Button size="sm" variant="ghost" onClick={() => setSelectedId(row.id)}>
            <ChevronRight size={12} />
          </Button>
        </div>
      ),
    },
  ];

  const actionTitles = {
    approve:    t('platform.tenant.approveTitle'),
    reject:     t('platform.tenant.rejectTitle'),
    suspend:    t('platform.tenant.suspendTitle'),
    reactivate: t('platform.tenant.reactivateTitle'),
  };

  return (
    <div className={`space-y-6 ${isRtl ? 'rtl' : ''}`}>
      <PageHeader title={t('platform.nav.tenants')} />

      {/* Status tabs */}
      <div className="flex gap-1 flex-wrap border-b border-slate-200">
        {STATUS_TABS.map((s) => (
          <button
            key={s || 'all'}
            onClick={() => handleStatusTab(s)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
              status === s
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}
          >
            {s ? t(`platform.tenant.status.${s}`, { defaultValue: s }) : t('platform.tenant.allStatuses')}
          </button>
        ))}
      </div>

      {/* Search */}
      <SearchBar
        value={search}
        onChange={handleSearch}
        placeholder={t('platform.tenant.searchPlaceholder')}
        className="max-w-sm"
      />

      {/* Error */}
      {isError && (
        <div className="flex items-center gap-2 p-4 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700">
          <AlertTriangle size={16} className="shrink-0" />
          {t('platform.tenant.loadError')}
        </div>
      )}

      {/* Table */}
      {!isError && (
        <>
          <Table columns={columns} data={tenants} loading={isLoading} />
          {!isLoading && tenants.length === 0 && (
            <EmptyState
              icon={Building2}
              title={t('platform.tenant.emptyTitle')}
              description={t('platform.tenant.emptyDescription')}
            />
          )}
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      {/* Detail modal */}
      <TenantDetailModal
        tenantId={selectedId}
        onClose={() => setSelectedId(null)}
        onAction={handleAction}
      />

      {/* Reason modal */}
      <ReasonModal
        open={reasonOpen}
        onClose={() => { setReasonOpen(false); setAction(null); }}
        onConfirm={handleActionConfirm}
        title={action ? actionTitles[action.type] : ''}
        required={action?.type === 'reject' || action?.type === 'suspend'}
        loading={approve.isPending || reject.isPending || suspend.isPending || reactivate.isPending}
      />
    </div>
  );
}
