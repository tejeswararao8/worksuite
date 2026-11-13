import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { CheckSquare, CheckCircle, XCircle, AlertTriangle, Clock } from 'lucide-react';
import {
  PageHeader, Button, Table, Pagination,
  EmptyState, Modal,
} from '../../../components/ui';
import { useTenants, useTenantMutations } from '../../../hooks/useTenants';

function ReasonModal({ open, onClose, onConfirm, title, required, loading }) {
  const { t } = useTranslation();
  const [reason, setReason] = useState('');
  const [error, setError]   = useState('');

  const handleConfirm = () => {
    if (required && !reason.trim()) { setError(t('common.required')); return; }
    onConfirm(reason.trim() || undefined);
    setReason(''); setError('');
  };

  return (
    <Modal open={open} onClose={() => { setReason(''); setError(''); onClose(); }} title={title} size="sm">
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
          <Button variant="secondary" onClick={onClose}>{t('common.cancel')}</Button>
          <Button onClick={handleConfirm} loading={loading}>{t('common.confirm')}</Button>
        </div>
      </div>
    </Modal>
  );
}

export default function TenantApprovalsPage() {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.dir() === 'rtl';

  const [page, setPage] = useState(0);
  const [action, setAction] = useState(null);
  const [reasonOpen, setReasonOpen] = useState(false);

  const { tenants, totalPages, isLoading, isError } = useTenants({
    page, size: 20, status: 'PENDING_REVIEW',
  });
  const { approve, reject } = useTenantMutations();

  const handleAction = (type, tenant) => {
    setAction({ type, tenant });
    setReasonOpen(true);
  };

  const handleConfirm = async (reason) => {
    const { type, tenant } = action;
    try {
      if (type === 'approve') await approve.mutateAsync({ id: tenant.id, reason });
      if (type === 'reject')  await reject.mutateAsync({ id: tenant.id, reason });
      toast.success(t(`platform.tenant.${type}Success`));
    } catch {
      toast.error(t('platform.tenant.actionError'));
    } finally {
      setReasonOpen(false);
      setAction(null);
    }
  };

  const columns = [
    { key: 'name',  label: t('platform.tenant.name') },
    { key: 'slug',  label: t('platform.tenant.slug') },
    { key: 'email', label: t('platform.tenant.email') },
    {
      key: 'createdAt',
      label: t('common.createdAt'),
      render: (row) => row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '—',
    },
    {
      key: 'actions',
      label: t('common.actions'),
      render: (row) => (
        <div className="flex gap-2">
          <Button size="sm" onClick={() => handleAction('approve', row)}>
            <CheckCircle size={12} /> {t('platform.tenant.approve')}
          </Button>
          <Button size="sm" variant="danger" onClick={() => handleAction('reject', row)}>
            <XCircle size={12} /> {t('platform.tenant.reject')}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className={`space-y-6 ${isRtl ? 'rtl' : ''}`}>
      <PageHeader
        title={t('platform.nav.approvals')}
        subtitle={t('platform.tenant.approvalsSubtitle')}
      />

      {isError && (
        <div className="flex items-center gap-2 p-4 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700">
          <AlertTriangle size={16} className="shrink-0" />
          {t('platform.tenant.loadError')}
        </div>
      )}

      {!isError && (
        <>
          <div className="flex items-center gap-2 p-3 bg-amber-50 border border-amber-200 rounded-xl text-sm text-amber-700">
            <Clock size={15} className="shrink-0" />
            {t('platform.tenant.pendingCount', { count: tenants.length })}
          </div>
          <Table columns={columns} data={tenants} loading={isLoading} />
          {!isLoading && tenants.length === 0 && (
            <EmptyState
              icon={CheckSquare}
              title={t('platform.tenant.noApprovals')}
              description={t('platform.tenant.noApprovalsDesc')}
            />
          )}
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      <ReasonModal
        open={reasonOpen}
        onClose={() => { setReasonOpen(false); setAction(null); }}
        onConfirm={handleConfirm}
        title={action?.type === 'approve' ? t('platform.tenant.approveTitle') : t('platform.tenant.rejectTitle')}
        required={action?.type === 'reject'}
        loading={approve.isPending || reject.isPending}
      />
    </div>
  );
}
