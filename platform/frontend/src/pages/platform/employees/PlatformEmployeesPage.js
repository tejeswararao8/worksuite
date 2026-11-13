import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import {
  Users, UserPlus, CheckCircle, PauseCircle, PlayCircle,
  XCircle, AlertTriangle, Mail, RefreshCw,
} from 'lucide-react';
import {
  PageHeader, Badge, Button, Table, Pagination,
  Modal, EmptyState, Skeleton,
} from '../../../components/ui';
import {
  usePlatformEmployees, usePlatformInvitations, usePlatformEmployeeMutations,
} from '../../../hooks/usePlatformEmployees';

const PLATFORM_ROLES = [
  'PLATFORM_SUPER_ADMIN',
  'PLATFORM_ADMIN',
  'PLATFORM_SUPPORT',
  'PLATFORM_IMPLEMENTATION',
];

const ROLE_COLOR = {
  PLATFORM_SUPER_ADMIN:    'purple',
  PLATFORM_ADMIN:          'indigo',
  PLATFORM_SUPPORT:        'blue',
  PLATFORM_IMPLEMENTATION: 'green',
};

const STATUS_COLOR = {
  ACTIVE:    'green',
  SUSPENDED: 'orange',
  REVOKED:   'red',
  PENDING:   'yellow',
};

function RoleBadge({ role }) {
  const { t } = useTranslation();
  return (
    <Badge
      label={t(`platform.employee.roles.${role}`, { defaultValue: role })}
      color={ROLE_COLOR[role] ?? 'gray'}
    />
  );
}

function StatusBadge({ status }) {
  const { t } = useTranslation();
  return (
    <Badge
      label={t(`platform.employee.membershipStatus.${status}`, { defaultValue: status })}
      color={STATUS_COLOR[status] ?? 'gray'}
    />
  );
}

// ── Invite modal ──────────────────────────────────────────────────────────────
function InviteModal({ open, onClose, onSubmit, loading }) {
  const { t } = useTranslation();
  const [form, setForm] = useState({ email: '', firstName: '', lastName: '', roleName: '' });
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!form.email) e.email = t('common.required');
    if (!form.firstName) e.firstName = t('common.required');
    if (!form.lastName) e.lastName = t('common.required');
    if (!form.roleName) e.roleName = t('common.required');
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = () => {
    if (!validate()) return;
    onSubmit(form);
  };

  const field = (key, label, type = 'text') => (
    <div>
      <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
      {type === 'select' ? (
        <select
          className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          value={form[key]}
          onChange={(e) => setForm(f => ({ ...f, [key]: e.target.value }))}
        >
          <option value="">{t('common.select')}</option>
          {PLATFORM_ROLES.map(r => (
            <option key={r} value={r}>
              {t(`platform.employee.roles.${r}`, { defaultValue: r })}
            </option>
          ))}
        </select>
      ) : (
        <input
          type={type}
          className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          value={form[key]}
          onChange={(e) => setForm(f => ({ ...f, [key]: e.target.value }))}
        />
      )}
      {errors[key] && <p className="text-xs text-red-500 mt-1">{errors[key]}</p>}
    </div>
  );

  return (
    <Modal open={open} onClose={onClose} title={t('platform.employee.inviteTitle')} size="sm">
      <div className="space-y-4">
        {field('email', t('platform.employee.email'), 'email')}
        {field('firstName', t('platform.employee.firstName'))}
        {field('lastName', t('platform.employee.lastName'))}
        {field('roleName', t('platform.employee.role'), 'select')}
        <div className="flex justify-end gap-3 pt-2">
          <Button variant="secondary" onClick={onClose}>{t('common.cancel')}</Button>
          <Button onClick={handleSubmit} loading={loading}>
            <Mail size={14} /> {t('platform.employee.sendInvite')}
          </Button>
        </div>
      </div>
    </Modal>
  );
}

// ── Role change modal ─────────────────────────────────────────────────────────
function RoleModal({ open, onClose, onSubmit, currentRole, loading }) {
  const { t } = useTranslation();
  const [roleName, setRoleName] = useState(currentRole ?? '');

  return (
    <Modal open={open} onClose={onClose} title={t('platform.employee.changeRoleTitle')} size="sm">
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">
            {t('platform.employee.role')}
          </label>
          <select
            className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={roleName}
            onChange={(e) => setRoleName(e.target.value)}
          >
            <option value="">{t('common.select')}</option>
            {PLATFORM_ROLES.map(r => (
              <option key={r} value={r}>
                {t(`platform.employee.roles.${r}`, { defaultValue: r })}
              </option>
            ))}
          </select>
        </div>
        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={onClose}>{t('common.cancel')}</Button>
          <Button onClick={() => onSubmit(roleName)} loading={loading} disabled={!roleName}>
            {t('common.save')}
          </Button>
        </div>
      </div>
    </Modal>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────
export default function PlatformEmployeesPage() {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.dir() === 'rtl';

  const [tab, setTab]           = useState('employees'); // 'employees' | 'invitations'
  const [page, setPage]         = useState(0);
  const [invPage, setInvPage]   = useState(0);
  const [inviteOpen, setInviteOpen] = useState(false);
  const [roleModal, setRoleModal]   = useState(null); // { id, currentRole }

  const { employees, totalPages, isLoading, isError } = usePlatformEmployees({ page });
  const { invitations, totalPages: invTotalPages, isLoading: invLoading } =
      usePlatformInvitations({ page: invPage });

  const { invite, resendInvitation, revokeInvitation, changeRole, suspend, reactivate, revoke } =
      usePlatformEmployeeMutations();

  const handleInvite = async (data) => {
    try {
      await invite.mutateAsync(data);
      toast.success(t('platform.employee.inviteSuccess'));
      setInviteOpen(false);
    } catch {
      toast.error(t('platform.employee.actionError'));
    }
  };

  const handleAction = async (action, id, extra) => {
    try {
      if (action === 'suspend')    await suspend.mutateAsync(id);
      if (action === 'reactivate') await reactivate.mutateAsync(id);
      if (action === 'revoke')     await revoke.mutateAsync(id);
      if (action === 'changeRole') await changeRole.mutateAsync({ id, roleName: extra });
      if (action === 'resend')     await resendInvitation.mutateAsync(id);
      if (action === 'revokeInv')  await revokeInvitation.mutateAsync(id);
      toast.success(t(`platform.employee.${action}Success`));
    } catch {
      toast.error(t('platform.employee.actionError'));
    }
  };

  const employeeColumns = [
    { key: 'keycloakUserId', label: t('platform.employee.keycloakId'),
      render: (r) => <span className="font-mono text-xs text-slate-500">{r.keycloakUserId}</span> },
    { key: 'roleName', label: t('platform.employee.role'),
      render: (r) => <RoleBadge role={r.roleName} /> },
    { key: 'membershipStatus', label: t('common.status'),
      render: (r) => <StatusBadge status={r.membershipStatus} /> },
    { key: 'joinedAt', label: t('platform.employee.joinedAt'),
      render: (r) => r.joinedAt ? new Date(r.joinedAt).toLocaleDateString() : '—' },
    {
      key: 'actions', label: t('common.actions'),
      render: (r) => (
        <div className="flex gap-1 flex-wrap">
          <Button size="sm" variant="ghost"
              onClick={() => setRoleModal({ id: r.membershipId, currentRole: r.roleName })}>
            {t('platform.employee.changeRole')}
          </Button>
          {r.membershipStatus === 'ACTIVE' && (
            <Button size="sm" variant="outline"
                onClick={() => handleAction('suspend', r.membershipId)}>
              <PauseCircle size={12} />
            </Button>
          )}
          {r.membershipStatus === 'SUSPENDED' && (
            <Button size="sm" onClick={() => handleAction('reactivate', r.membershipId)}>
              <PlayCircle size={12} />
            </Button>
          )}
          {r.membershipStatus !== 'REVOKED' && (
            <Button size="sm" variant="danger"
                onClick={() => handleAction('revoke', r.membershipId)}>
              <XCircle size={12} />
            </Button>
          )}
        </div>
      ),
    },
  ];

  const invitationColumns = [
    { key: 'email', label: t('platform.employee.email') },
    { key: 'firstName', label: t('platform.employee.firstName') },
    { key: 'roleName', label: t('platform.employee.role'),
      render: (r) => <RoleBadge role={r.roleName} /> },
    { key: 'status', label: t('common.status'),
      render: (r) => <StatusBadge status={r.status} /> },
    { key: 'expiresAt', label: t('platform.employee.expiresAt'),
      render: (r) => r.expiresAt ? new Date(r.expiresAt).toLocaleDateString() : '—' },
    {
      key: 'actions', label: t('common.actions'),
      render: (r) => r.status === 'PENDING' ? (
        <div className="flex gap-1">
          <Button size="sm" variant="ghost"
              onClick={() => handleAction('resend', r.id)}>
            <RefreshCw size={12} /> {t('platform.employee.resend')}
          </Button>
          <Button size="sm" variant="danger"
              onClick={() => handleAction('revokeInv', r.id)}>
            {t('platform.employee.revokeInvite')}
          </Button>
        </div>
      ) : null,
    },
  ];

  return (
    <div className={`space-y-6 ${isRtl ? 'rtl' : ''}`}>
      <PageHeader
        title={t('platform.nav.employees')}
        action={
          <Button onClick={() => setInviteOpen(true)}>
            <UserPlus size={14} /> {t('platform.employee.invite')}
          </Button>
        }
      />

      {/* Tabs */}
      <div className="flex gap-1 border-b border-slate-200">
        {['employees', 'invitations'].map((t_) => (
          <button
            key={t_}
            onClick={() => setTab(t_)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
              tab === t_
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}
          >
            {t(`platform.employee.tab.${t_}`)}
          </button>
        ))}
      </div>

      {/* Error */}
      {isError && (
        <div className="flex items-center gap-2 p-4 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700">
          <AlertTriangle size={16} className="shrink-0" />
          {t('platform.employee.loadError')}
        </div>
      )}

      {/* Employees tab */}
      {tab === 'employees' && (
        <>
          <Table columns={employeeColumns} data={employees} loading={isLoading} />
          {!isLoading && employees.length === 0 && (
            <EmptyState icon={Users}
              title={t('platform.employee.emptyTitle')}
              description={t('platform.employee.emptyDescription')} />
          )}
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      {/* Invitations tab */}
      {tab === 'invitations' && (
        <>
          <Table columns={invitationColumns} data={invitations} loading={invLoading} />
          {!invLoading && invitations.length === 0 && (
            <EmptyState icon={Mail}
              title={t('platform.employee.noInvitations')}
              description={t('platform.employee.noInvitationsDesc')} />
          )}
          <Pagination page={invPage} totalPages={invTotalPages} onPageChange={setInvPage} />
        </>
      )}

      {/* Invite modal */}
      <InviteModal
        open={inviteOpen}
        onClose={() => setInviteOpen(false)}
        onSubmit={handleInvite}
        loading={invite.isPending}
      />

      {/* Role change modal */}
      <RoleModal
        open={!!roleModal}
        onClose={() => setRoleModal(null)}
        currentRole={roleModal?.currentRole}
        loading={changeRole.isPending}
        onSubmit={(roleName) => {
          handleAction('changeRole', roleModal.id, roleName);
          setRoleModal(null);
        }}
      />
    </div>
  );
}
