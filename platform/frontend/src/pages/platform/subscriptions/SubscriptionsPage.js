import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { CreditCard, Package, Plus, Edit2, CheckCircle, XCircle, AlertTriangle } from 'lucide-react';
import {
  PageHeader, Badge, Button, Table, Pagination,
  Modal, EmptyState, StatCard,
} from '../../../components/ui';
import { platformApi } from '../../../api/services';
import { QUERY_KEYS } from '../../../constants/queryKeys';

// ── Helpers ───────────────────────────────────────────────────────────────────

const STATUS_COLOR = {
  TRIAL: 'yellow', ACTIVE: 'green', PAST_DUE: 'orange',
  SUSPENDED: 'red', EXPIRED: 'gray', CANCELLED: 'gray',
};
const PLAN_STATUS_COLOR = { ACTIVE: 'green', INACTIVE: 'gray' };

function fmt(cents) {
  return cents === 0 ? 'Free' : `$${(cents / 100).toFixed(2)}`;
}
function fmtLimit(val) {
  return val === -1 ? '∞' : val;
}

// ── Plan form modal ───────────────────────────────────────────────────────────

function PlanModal({ open, onClose, initial, onSubmit, loading }) {
  const { t } = useTranslation();
  const [form, setForm] = useState(initial ?? {
    name: '', description: '', monthlyPriceCents: 0, annualPriceCents: 0,
    trialDays: 14, maxEmployees: 50, storageGb: 5, enabledModules: 'HR',
  });

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const field = (key, label, type = 'text') => (
    <div>
      <label className="block text-xs font-medium text-slate-600 mb-1">{label}</label>
      <input
        type={type}
        className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        value={form[key]}
        onChange={e => set(key, type === 'number' ? Number(e.target.value) : e.target.value)}
      />
    </div>
  );

  return (
    <Modal open={open} onClose={onClose}
      title={initial ? t('platform.subscription.editPlan') : t('platform.subscription.createPlan')}
      size="md">
      <div className="grid grid-cols-2 gap-4">
        <div className="col-span-2">{field('name', t('platform.subscription.planName'))}</div>
        <div className="col-span-2">{field('description', t('platform.subscription.description'))}</div>
        {field('monthlyPriceCents', t('platform.subscription.monthlyPrice') + ' (cents)', 'number')}
        {field('annualPriceCents',  t('platform.subscription.annualPrice')  + ' (cents)', 'number')}
        {field('trialDays',    t('platform.subscription.trialDays'),    'number')}
        {field('maxEmployees', t('platform.subscription.maxEmployees'), 'number')}
        {field('storageGb',    t('platform.subscription.storageGb'),    'number')}
        <div className="col-span-2">{field('enabledModules', t('platform.subscription.enabledModules'))}</div>
      </div>
      <div className="flex justify-end gap-3 pt-4">
        <Button variant="secondary" onClick={onClose}>{t('common.cancel')}</Button>
        <Button onClick={() => onSubmit(form)} loading={loading}>{t('common.save')}</Button>
      </div>
    </Modal>
  );
}

// ── Assign subscription modal ─────────────────────────────────────────────────

function AssignModal({ open, onClose, tenantId, tenantName, plans, onSubmit, loading }) {
  const { t } = useTranslation();
  const [form, setForm] = useState({ planId: '', billingCycle: 'MONTHLY', startTrial: false, notes: '' });

  return (
    <Modal open={open} onClose={onClose}
      title={`${t('platform.subscription.assignPlan')} — ${tenantName ?? ''}`}
      size="sm">
      <div className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-slate-600 mb-1">{t('platform.subscription.planName')}</label>
          <select
            className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={form.planId}
            onChange={e => setForm(f => ({ ...f, planId: e.target.value }))}>
            <option value="">{t('common.select')}</option>
            {(plans ?? []).filter(p => p.planStatus === 'ACTIVE').map(p => (
              <option key={p.id} value={p.id}>{p.name} — {fmt(p.monthlyPriceCents)}/mo</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-slate-600 mb-1">{t('platform.subscription.billingCycle')}</label>
          <select
            className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={form.billingCycle}
            onChange={e => setForm(f => ({ ...f, billingCycle: e.target.value }))}>
            <option value="MONTHLY">{t('platform.subscription.billing.MONTHLY')}</option>
            <option value="ANNUAL">{t('platform.subscription.billing.ANNUAL')}</option>
          </select>
        </div>
        <label className="flex items-center gap-2 text-sm text-slate-700 cursor-pointer">
          <input type="checkbox" checked={form.startTrial}
            onChange={e => setForm(f => ({ ...f, startTrial: e.target.checked }))} />
          {t('platform.subscription.startTrial')}
        </label>
        <div>
          <label className="block text-xs font-medium text-slate-600 mb-1">{t('platform.subscription.notes')}</label>
          <textarea
            className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            rows={2}
            value={form.notes}
            onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} />
        </div>
        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={onClose}>{t('common.cancel')}</Button>
          <Button onClick={() => onSubmit(form)} loading={loading} disabled={!form.planId}>
            {t('platform.subscription.assignPlan')}
          </Button>
        </div>
      </div>
    </Modal>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────

export default function SubscriptionsPage() {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.dir() === 'rtl';
  const qc = useQueryClient();

  const [tab, setTab]           = useState('plans');
  const [planPage, setPlanPage] = useState(0);
  const [subPage, setSubPage]   = useState(0);
  const [planModal, setPlanModal]   = useState(null); // null | 'create' | plan object
  const [assignModal, setAssignModal] = useState(null); // null | { tenantId, tenantName }

  // ── Queries ──────────────────────────────────────────────────────────────

  const { data: plansData, isLoading: plansLoading, isError: plansError } = useQuery({
    queryKey: QUERY_KEYS.SUBSCRIPTION_PLANS(planPage, null),
    queryFn: () => platformApi.getPlans({ page: planPage, size: 20 }),
  });

  const { data: subsData, isLoading: subsLoading } = useQuery({
    queryKey: QUERY_KEYS.ALL_SUBSCRIPTIONS(subPage),
    queryFn: () => platformApi.getAllSubscriptions({ page: subPage, size: 20 }),
    enabled: tab === 'subscriptions',
  });

  const plans     = plansData?.data?.data?.content ?? [];
  const planPages = plansData?.data?.data?.totalPages ?? 0;
  const subs      = subsData?.data?.data?.content ?? [];
  const subPages  = subsData?.data?.data?.totalPages ?? 0;

  // ── Mutations ─────────────────────────────────────────────────────────────

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['subscription-plans'] });
    qc.invalidateQueries({ queryKey: ['all-subscriptions'] });
  };

  const createPlan = useMutation({
    mutationFn: (data) => platformApi.createPlan(data),
    onSuccess: () => { toast.success(t('platform.subscription.createSuccess')); setPlanModal(null); invalidate(); },
    onError:   () => toast.error(t('platform.subscription.actionError')),
  });

  const updatePlan = useMutation({
    mutationFn: ({ id, data }) => platformApi.updatePlan(id, data),
    onSuccess: () => { toast.success(t('platform.subscription.updateSuccess')); setPlanModal(null); invalidate(); },
    onError:   () => toast.error(t('platform.subscription.actionError')),
  });

  const togglePlan = useMutation({
    mutationFn: ({ id, active }) => active ? platformApi.deactivatePlan(id) : platformApi.activatePlan(id),
    onSuccess: () => { toast.success(t('platform.subscription.actionSuccess')); invalidate(); },
    onError:   () => toast.error(t('platform.subscription.actionError')),
  });

  const assignSub = useMutation({
    mutationFn: ({ tenantId, data }) => platformApi.assignSubscription(tenantId, data),
    onSuccess: () => { toast.success(t('platform.subscription.assignSuccess')); setAssignModal(null); invalidate(); },
    onError:   () => toast.error(t('platform.subscription.actionError')),
  });

  const subAction = useMutation({
    mutationFn: ({ action, id }) => {
      if (action === 'cancel')     return platformApi.cancelSubscription(id, {});
      if (action === 'suspend')    return platformApi.suspendSubscription(id, {});
      if (action === 'reactivate') return platformApi.reactivateSubscription(id, {});
      if (action === 'renew')      return platformApi.renewSubscription(id, {});
      if (action === 'activate')   return platformApi.activateSubscription(id, {});
      return Promise.reject(new Error('Unknown action'));
    },
    onSuccess: () => { toast.success(t('platform.subscription.actionSuccess')); invalidate(); },
    onError:   () => toast.error(t('platform.subscription.actionError')),
  });

  // ── Columns ───────────────────────────────────────────────────────────────

  const planColumns = [
    { key: 'name',   label: t('platform.subscription.planName'),
      render: r => <span className="font-medium text-slate-800">{r.name}</span> },
    { key: 'monthly', label: t('platform.subscription.monthlyPrice'),
      render: r => <span className="font-mono text-sm">{fmt(r.monthlyPriceCents)}</span> },
    { key: 'annual',  label: t('platform.subscription.annualPrice'),
      render: r => <span className="font-mono text-sm">{fmt(r.annualPriceCents)}</span> },
    { key: 'maxEmp',  label: t('platform.subscription.maxEmployees'),
      render: r => fmtLimit(r.maxEmployees) },
    { key: 'storage', label: t('platform.subscription.storageGb'),
      render: r => fmtLimit(r.storageGb) + ' GB' },
    { key: 'modules', label: t('platform.subscription.enabledModules'),
      render: r => (r.enabledModules ?? []).join(', ') },
    { key: 'status',  label: t('platform.subscription.planStatus'),
      render: r => <Badge label={t(`platform.subscription.planStatus.${r.planStatus}`, { defaultValue: r.planStatus })}
                          color={PLAN_STATUS_COLOR[r.planStatus] ?? 'gray'} /> },
    { key: 'actions', label: t('common.actions'),
      render: r => (
        <div className="flex gap-1">
          <Button size="sm" variant="ghost" onClick={() => setPlanModal(r)}>
            <Edit2 size={12} />
          </Button>
          <Button size="sm" variant={r.planStatus === 'ACTIVE' ? 'outline' : 'ghost'}
            onClick={() => togglePlan.mutate({ id: r.id, active: r.planStatus === 'ACTIVE' })}>
            {r.planStatus === 'ACTIVE' ? <XCircle size={12} /> : <CheckCircle size={12} />}
          </Button>
        </div>
      ),
    },
  ];

  const subColumns = [
    { key: 'tenant', label: t('platform.subscription.tenantName'),
      render: r => <span className="font-medium text-slate-800">{r.tenantName}</span> },
    { key: 'plan',   label: t('platform.subscription.planName'),
      render: r => r.plan?.name },
    { key: 'status', label: t('common.status'),
      render: r => <Badge label={t(`platform.subscription.status.${r.status}`, { defaultValue: r.status })}
                          color={STATUS_COLOR[r.status] ?? 'gray'} /> },
    { key: 'billing', label: t('platform.subscription.billingCycle'),
      render: r => t(`platform.subscription.billing.${r.billingCycle}`, { defaultValue: r.billingCycle }) },
    { key: 'periodEnd', label: t('platform.subscription.periodEnd'),
      render: r => r.currentPeriodEnd ? new Date(r.currentPeriodEnd).toLocaleDateString() : '—' },
    { key: 'actions', label: t('common.actions'),
      render: r => (
        <div className="flex gap-1 flex-wrap">
          {r.status === 'TRIAL' && (
            <Button size="sm" onClick={() => subAction.mutate({ action: 'activate', id: r.id })}>
              {t('platform.subscription.activateSub')}
            </Button>
          )}
          {(r.status === 'ACTIVE' || r.status === 'PAST_DUE') && (
            <Button size="sm" variant="outline" onClick={() => subAction.mutate({ action: 'renew', id: r.id })}>
              {t('platform.subscription.renewSub')}
            </Button>
          )}
          {(r.status === 'ACTIVE' || r.status === 'PAST_DUE') && (
            <Button size="sm" variant="outline" onClick={() => subAction.mutate({ action: 'suspend', id: r.id })}>
              {t('platform.subscription.suspendSub')}
            </Button>
          )}
          {r.status === 'SUSPENDED' && (
            <Button size="sm" onClick={() => subAction.mutate({ action: 'reactivate', id: r.id })}>
              {t('platform.subscription.reactivateSub')}
            </Button>
          )}
          {!['EXPIRED', 'CANCELLED'].includes(r.status) && (
            <Button size="sm" variant="danger" onClick={() => subAction.mutate({ action: 'cancel', id: r.id })}>
              {t('platform.subscription.cancelSub')}
            </Button>
          )}
          <Button size="sm" variant="ghost"
            onClick={() => setAssignModal({ tenantId: r.tenantId, tenantName: r.tenantName })}>
            {t('platform.subscription.changePlan')}
          </Button>
        </div>
      ),
    },
  ];

  // ── Stats ─────────────────────────────────────────────────────────────────

  const activePlans = plans.filter(p => p.planStatus === 'ACTIVE').length;

  return (
    <div className={`space-y-6 ${isRtl ? 'rtl' : ''}`}>
      <PageHeader
        title={t('platform.subscription.title')}
        action={tab === 'plans' ? (
          <Button onClick={() => setPlanModal('create')}>
            <Plus size={14} /> {t('platform.subscription.createPlan')}
          </Button>
        ) : null}
      />

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard icon={Package}  label={t('platform.subscription.plansTab')}
                  value={plans.length} color="indigo" />
        <StatCard icon={CheckCircle} label={t('platform.subscription.planStatus.ACTIVE')}
                  value={activePlans} color="green" />
        <StatCard icon={CreditCard} label={t('platform.subscription.subscriptionsTab')}
                  value={subs.length} color="blue" />
        <StatCard icon={AlertTriangle}
                  label={t('platform.subscription.status.PAST_DUE')}
                  value={subs.filter(s => s.status === 'PAST_DUE').length} color="orange" />
      </div>

      {/* Tabs */}
      <div className="flex gap-1 border-b border-slate-200">
        {['plans', 'subscriptions'].map(tab_ => (
          <button key={tab_} onClick={() => setTab(tab_)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
              tab === tab_
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}>
            {t(`platform.subscription.${tab_}Tab`)}
          </button>
        ))}
      </div>

      {/* Error */}
      {plansError && (
        <div className="flex items-center gap-2 p-4 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700">
          <AlertTriangle size={16} className="shrink-0" />
          {t('platform.subscription.loadError')}
        </div>
      )}

      {/* Plans tab */}
      {tab === 'plans' && (
        <>
          <Table columns={planColumns} data={plans} loading={plansLoading} />
          {!plansLoading && plans.length === 0 && (
            <EmptyState icon={Package}
              title={t('platform.subscription.noPlans')}
              description={t('platform.subscription.noPlansDesc')} />
          )}
          <Pagination page={planPage} totalPages={planPages} onPageChange={setPlanPage} />
        </>
      )}

      {/* Subscriptions tab */}
      {tab === 'subscriptions' && (
        <>
          <Table columns={subColumns} data={subs} loading={subsLoading} />
          {!subsLoading && subs.length === 0 && (
            <EmptyState icon={CreditCard}
              title={t('platform.subscription.noSubscription')}
              description={t('platform.subscription.noSubscriptionDesc')} />
          )}
          <Pagination page={subPage} totalPages={subPages} onPageChange={setSubPage} />
        </>
      )}

      {/* Plan create/edit modal */}
      {planModal !== null && (
        <PlanModal
          open
          onClose={() => setPlanModal(null)}
          initial={planModal === 'create' ? null : planModal}
          loading={createPlan.isPending || updatePlan.isPending}
          onSubmit={(data) => {
            if (planModal === 'create') {
              createPlan.mutate(data);
            } else {
              updatePlan.mutate({ id: planModal.id, data });
            }
          }}
        />
      )}

      {/* Assign subscription modal */}
      {assignModal && (
        <AssignModal
          open
          onClose={() => setAssignModal(null)}
          tenantId={assignModal.tenantId}
          tenantName={assignModal.tenantName}
          plans={plans}
          loading={assignSub.isPending}
          onSubmit={(data) => assignSub.mutate({ tenantId: assignModal.tenantId, data })}
        />
      )}
    </div>
  );
}
