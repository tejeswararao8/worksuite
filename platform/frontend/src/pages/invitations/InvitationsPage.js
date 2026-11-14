import { useState, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { Mail, RefreshCw, XCircle, UserPlus } from 'lucide-react';
import { tenantInvitationApi } from '../../api/services';
import { employeeApi } from '../../api/services';
import { QUERY_KEYS } from '../../constants/queryKeys';
import { INVITATION_STATUS, INVITATION_STATUS_COLOR, ALLOWED_TENANT_ROLES } from '../../constants/enums';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import {
  Button, Badge, Table, Pagination, Modal,
  EmptyState, ConfirmDialog, Spinner,
} from '../../components/ui';

const PAGE_SIZE = 20;

export default function InvitationsPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();

  const [page, setPage]               = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [inviteOpen, setInviteOpen]   = useState(false);
  const [confirmRevoke, setConfirmRevoke] = useState(null);
  const [confirmResend, setConfirmResend] = useState(null);

  // ── Fetch invitations ──────────────────────────────────────────────────────
  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.TENANT_INVITATIONS(page, statusFilter),
    queryFn: () => tenantInvitationApi.list({
      page,
      size: PAGE_SIZE,
      ...(statusFilter ? { status: statusFilter } : {}),
    }),
  });

  const invitations  = data?.data?.data?.content ?? [];
  const totalPages   = data?.data?.data?.totalPages ?? 0;

  // ── Fetch employees for invite form ───────────────────────────────────────
  const { data: empData } = useQuery({
    queryKey: QUERY_KEYS.EMPLOYEES(0, '', ''),
    queryFn: () => employeeApi.search({ page: 0, size: 200 }),
  });
  const employees = empData?.data?.data?.content ?? [];

  const invalidate = useCallback(() =>
    qc.invalidateQueries({ queryKey: ['tenant-invitations'] }), [qc]);

  // ── Mutations ──────────────────────────────────────────────────────────────
  const inviteMutation = useMutation({
    mutationFn: tenantInvitationApi.invite,
    onSuccess: (res) => {
      invalidate();
      setInviteOpen(false);
      const delivered = res?.data?.data?.emailDelivered;
      if (delivered === false) {
        toastSuccess(t('invitation.invitedEmailFailed'));
      } else {
        toastSuccess(t('invitation.inviteSuccess'));
      }
    },
    onError: toastError,
  });

  const resendMutation = useMutation({
    mutationFn: tenantInvitationApi.resend,
    onSuccess: (res) => {
      invalidate();
      setConfirmResend(null);
      const delivered = res?.data?.data?.emailDelivered;
      if (delivered === false) {
        toastSuccess(t('invitation.resendEmailFailed'));
      } else {
        toastSuccess(t('invitation.resendSuccess'));
      }
    },
    onError: toastError,
  });

  const revokeMutation = useMutation({
    mutationFn: tenantInvitationApi.revoke,
    onSuccess: () => { invalidate(); setConfirmRevoke(null); toastSuccess(t('invitation.revokeSuccess')); },
    onError: toastError,
  });

  // ── Invite form ────────────────────────────────────────────────────────────
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm();

  const openInvite  = useCallback(() => { reset({}); setInviteOpen(true); }, [reset]);
  const closeInvite = useCallback(() => { setInviteOpen(false); reset({}); }, [reset]);

  const onInviteSubmit = useCallback((data) => {
    inviteMutation.mutate(data);
  }, [inviteMutation]);

  // ── Table columns ──────────────────────────────────────────────────────────
  const columns = [
    { key: 'email',     label: t('invitation.email') },
    { key: 'roleName',  label: t('invitation.role') },
    {
      key: 'status', label: t('common.status'),
      render: (r) => (
        <Badge
          label={t(`invitation.status.${r.status}`, { defaultValue: r.status })}
          color={INVITATION_STATUS_COLOR[r.status] ?? 'gray'}
        />
      ),
    },
    {
      key: 'expiresAt', label: t('invitation.expiresAt'),
      render: (r) => r.expiresAt ? new Date(r.expiresAt).toLocaleDateString() : '—',
    },
    {
      key: 'createdAt', label: t('common.createdAt'),
      render: (r) => r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—',
    },
    {
      key: 'actions', label: t('common.actions'),
      render: (r) => (
        <div className="flex gap-2">
          {r.status === INVITATION_STATUS.PENDING && (
            <>
              <button
                title={t('invitation.resend')}
                onClick={() => setConfirmResend(r.id)}
                className="p-1.5 rounded hover:bg-slate-100 text-slate-500"
              >
                <RefreshCw size={14} />
              </button>
              <button
                title={t('invitation.revoke')}
                onClick={() => setConfirmRevoke(r.id)}
                className="p-1.5 rounded hover:bg-red-50 text-red-400"
              >
                <XCircle size={14} />
              </button>
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="flex flex-wrap gap-3 items-center justify-between">
        <div className="flex gap-2 flex-wrap items-center">
          <select
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
            value={statusFilter}
            onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
          >
            <option value="">{t('invitation.allStatuses')}</option>
            {Object.values(INVITATION_STATUS).map((s) => (
              <option key={s} value={s}>{t(`invitation.status.${s}`, { defaultValue: s })}</option>
            ))}
          </select>
        </div>
        <Button onClick={openInvite}>
          <UserPlus size={16} /> {t('invitation.invite')}
        </Button>
      </div>

      {/* Table */}
      {isLoading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : invitations.length === 0 ? (
        <EmptyState
          icon={Mail}
          title={t('invitation.emptyTitle')}
          description={t('invitation.emptyDescription')}
          action={<Button onClick={openInvite}><UserPlus size={16} /> {t('invitation.invite')}</Button>}
        />
      ) : (
        <>
          <Table columns={columns} data={invitations} loading={false} />
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      {/* Invite Modal */}
      <Modal open={inviteOpen} onClose={closeInvite} title={t('invitation.inviteTitle')}>
        <form onSubmit={handleSubmit(onInviteSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              {t('invitation.employee')} <span className="text-red-500">*</span>
            </label>
            <select
              className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
              {...register('employeeId', { required: t('common.required') })}
            >
              <option value="">{t('common.select')}</option>
              {employees.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.firstName} {e.lastName} — {e.email}
                </option>
              ))}
            </select>
            {errors.employeeId && (
              <p className="text-xs text-red-500 mt-1">{errors.employeeId.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              {t('invitation.role')} <span className="text-red-500">*</span>
            </label>
            <select
              className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
              {...register('roleName', { required: t('common.required') })}
            >
              <option value="">{t('common.select')}</option>
              {ALLOWED_TENANT_ROLES.map((r) => (
                <option key={r} value={r}>
                  {t(`invitation.roles.${r}`, { defaultValue: r })}
                </option>
              ))}
            </select>
            {errors.roleName && (
              <p className="text-xs text-red-500 mt-1">{errors.roleName.message}</p>
            )}
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={closeInvite}>{t('common.cancel')}</Button>
            <Button type="submit" loading={isSubmitting || inviteMutation.isPending}>
              {t('invitation.sendInvite')}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Resend confirm */}
      <ConfirmDialog
        open={!!confirmResend}
        message={t('invitation.resendConfirm')}
        confirmLabel={t('invitation.resend')}
        variant="primary"
        onConfirm={() => resendMutation.mutate(confirmResend)}
        onCancel={() => setConfirmResend(null)}
      />

      {/* Revoke confirm */}
      <ConfirmDialog
        open={!!confirmRevoke}
        message={t('invitation.revokeConfirm')}
        confirmLabel={t('invitation.revoke')}
        variant="danger"
        onConfirm={() => revokeMutation.mutate(confirmRevoke)}
        onCancel={() => setConfirmRevoke(null)}
      />
    </div>
  );
}
