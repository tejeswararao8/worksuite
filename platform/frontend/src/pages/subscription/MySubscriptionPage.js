import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Users, HardDrive, Puzzle, AlertTriangle, Clock, XCircle, CheckCircle } from 'lucide-react';
import { PageHeader, Badge, Skeleton } from '../../components/ui';
import { tenantSubscriptionApi } from '../../api/services';
import { QUERY_KEYS } from '../../constants/queryKeys';

const STATUS_COLOR = {
  TRIAL: 'yellow', ACTIVE: 'green', PAST_DUE: 'orange',
  SUSPENDED: 'red', EXPIRED: 'gray', CANCELLED: 'gray',
};

function fmt(cents) {
  return cents === 0 ? 'Free' : `$${(cents / 100).toFixed(2)}`;
}

function fmtBytes(bytes) {
  if (bytes === 0) return '0 B';
  const gb = bytes / (1024 ** 3);
  if (gb >= 1) return `${gb.toFixed(2)} GB`;
  const mb = bytes / (1024 ** 2);
  if (mb >= 1) return `${mb.toFixed(1)} MB`;
  return `${(bytes / 1024).toFixed(0)} KB`;
}

function UsageBar({ pct, label, warning, danger }) {
  const color = pct >= 90 ? 'bg-red-500' : pct >= 70 ? 'bg-orange-400' : 'bg-indigo-500';
  return (
    <div>
      <div className="flex items-center justify-between mb-1">
        <span className="text-sm text-slate-600">{label}</span>
        {(warning || danger) && (
          <span className={`text-xs font-medium flex items-center gap-1 ${danger ? 'text-red-600' : 'text-orange-500'}`}>
            <AlertTriangle size={12} />
            {danger ? 'Limit reached' : 'Approaching limit'}
          </span>
        )}
      </div>
      <div className="w-full bg-slate-100 rounded-full h-2">
        <div className={`h-2 rounded-full transition-all ${color}`} style={{ width: `${Math.min(100, pct)}%` }} />
      </div>
    </div>
  );
}

function StatusBanner({ status, inGracePeriod, gracePeriodDaysRemaining, t }) {
  if (status === 'ACTIVE' || status === 'TRIAL') return null;

  const banners = {
    PAST_DUE: {
      icon: <Clock size={18} className="shrink-0" />,
      bg: 'bg-orange-50 border-orange-200 text-orange-800',
      msg: inGracePeriod
        ? t('platform.subscription.enforcement.pastDueGrace', { days: gracePeriodDaysRemaining })
        : t('platform.subscription.enforcement.pastDueExpired'),
    },
    SUSPENDED: {
      icon: <XCircle size={18} className="shrink-0" />,
      bg: 'bg-red-50 border-red-200 text-red-800',
      msg: t('platform.subscription.enforcement.suspended'),
    },
    EXPIRED: {
      icon: <XCircle size={18} className="shrink-0" />,
      bg: 'bg-gray-50 border-gray-200 text-gray-700',
      msg: t('platform.subscription.enforcement.expired'),
    },
    CANCELLED: {
      icon: <XCircle size={18} className="shrink-0" />,
      bg: 'bg-gray-50 border-gray-200 text-gray-700',
      msg: t('platform.subscription.enforcement.cancelled'),
    },
  };

  const b = banners[status];
  if (!b) return null;

  return (
    <div className={`flex items-start gap-3 p-4 border rounded-xl text-sm ${b.bg}`}>
      {b.icon}
      <div>
        <p className="font-medium">{b.msg}</p>
        <p className="mt-1 text-xs opacity-80">{t('platform.subscription.enforcement.contactAdmin')}</p>
      </div>
    </div>
  );
}

export default function MySubscriptionPage() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const isRtl = i18n.dir() === 'rtl';

  const { data: subData, isLoading: subLoading, isError: subError } = useQuery({
    queryKey: QUERY_KEYS.MY_SUBSCRIPTION,
    queryFn: () => tenantSubscriptionApi.getMy(),
  });

  const { data: entData, isLoading: entLoading } = useQuery({
    queryKey: QUERY_KEYS.MY_ENTITLEMENTS,
    queryFn: () => tenantSubscriptionApi.getEntitlements(),
  });

  const sub = subData?.data?.data;
  const ent = entData?.data?.data;

  if (subLoading || entLoading) {
    return (
      <div className="space-y-6">
        <PageHeader title={t('platform.subscription.entitlements.title')} />
        <Skeleton className="h-40 rounded-xl" />
        <Skeleton className="h-32 rounded-xl" />
      </div>
    );
  }

  if (subError || !sub) {
    return (
      <div className={`space-y-6 ${isRtl ? 'rtl' : ''}`}>
        <PageHeader title={t('platform.subscription.entitlements.title')} />
        <div className="flex items-center gap-3 p-6 bg-amber-50 border border-amber-200 rounded-xl text-amber-800">
          <AlertTriangle size={20} className="shrink-0" />
          <p className="text-sm">{t('platform.subscription.entitlements.noSub')}</p>
        </div>
      </div>
    );
  }

  const plan = sub.plan;
  const employeeUsagePct = ent && plan.maxEmployees !== -1
    ? Math.min(100, Math.round((ent.currentEmployeeCount / plan.maxEmployees) * 100))
    : 0;

  const storageUsagePct = ent && plan.storageGb !== -1
    ? Math.min(100, Math.round((ent.storageUsedBytes / (plan.storageGb * 1073741824)) * 100))
    : 0;

  return (
    <div className={`space-y-6 ${isRtl ? 'rtl' : ''}`}>
      <PageHeader title={t('platform.subscription.entitlements.title')} />

      {/* Status banner for non-active subscriptions */}
      {ent && (
        <StatusBanner
          status={sub.status}
          inGracePeriod={ent.inGracePeriod}
          gracePeriodDaysRemaining={ent.gracePeriodDaysRemaining}
          t={t}
        />
      )}

      {/* Subscription overview */}
      <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
        <div className="flex items-start justify-between flex-wrap gap-4">
          <div>
            <p className="text-xs text-slate-500 uppercase tracking-wide mb-1">
              {t('platform.subscription.entitlements.plan')}
            </p>
            <h2 className="text-2xl font-bold text-slate-800">{plan.name}</h2>
            {plan.description && (
              <p className="text-sm text-slate-500 mt-1">{plan.description}</p>
            )}
          </div>
          <div className="flex flex-col items-end gap-2">
            <Badge
              label={t(`platform.subscription.status.${sub.status}`, { defaultValue: sub.status })}
              color={STATUS_COLOR[sub.status] ?? 'gray'}
            />
            <span className="text-xs text-slate-500">
              {t('platform.subscription.billingCycle')}:{' '}
              {t(`platform.subscription.billing.${sub.billingCycle}`, { defaultValue: sub.billingCycle })}
            </span>
          </div>
        </div>

        <div className="mt-6 grid grid-cols-2 md:grid-cols-4 gap-4 pt-4 border-t border-slate-100">
          <div>
            <p className="text-xs text-slate-500">{t('platform.subscription.monthlyPrice')}</p>
            <p className="text-lg font-semibold text-slate-800">{fmt(plan.monthlyPriceCents)}</p>
          </div>
          <div>
            <p className="text-xs text-slate-500">{t('platform.subscription.startedAt')}</p>
            <p className="text-sm font-medium text-slate-700">
              {sub.startedAt ? new Date(sub.startedAt).toLocaleDateString() : '—'}
            </p>
          </div>
          {sub.trialEndsAt && (
            <div>
              <p className="text-xs text-slate-500">{t('platform.subscription.trialEndsAt')}</p>
              <p className="text-sm font-medium text-slate-700">
                {new Date(sub.trialEndsAt).toLocaleDateString()}
              </p>
            </div>
          )}
          {sub.currentPeriodEnd && (
            <div>
              <p className="text-xs text-slate-500">{t('platform.subscription.periodEnd')}</p>
              <p className="text-sm font-medium text-slate-700">
                {new Date(sub.currentPeriodEnd).toLocaleDateString()}
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Entitlements */}
      {ent && (
        <>
          {/* Employee usage */}
          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
            <div className="flex items-center gap-2 mb-4">
              <Users size={18} className="text-indigo-600" />
              <h3 className="font-semibold text-slate-800">{t('platform.subscription.entitlements.employees')}</h3>
            </div>
            <p className="text-sm text-slate-600 mb-3">
              {t('platform.subscription.entitlements.usageOf', {
                current: ent.currentEmployeeCount,
                max: plan.maxEmployees === -1 ? '∞' : plan.maxEmployees,
              })}
            </p>
            {plan.maxEmployees !== -1 && (
              <UsageBar
                pct={employeeUsagePct}
                label={`${employeeUsagePct}%`}
                warning={employeeUsagePct >= 70 && employeeUsagePct < 90}
                danger={ent.employeeLimitReached}
              />
            )}
            {ent.employeeLimitReached && (
              <p className="mt-3 text-xs text-red-600">
                {t('platform.subscription.entitlements.limitReached')}
              </p>
            )}
          </div>

          {/* Storage usage */}
          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
            <div className="flex items-center gap-2 mb-4">
              <HardDrive size={18} className="text-indigo-600" />
              <h3 className="font-semibold text-slate-800">{t('platform.subscription.entitlements.storage')}</h3>
            </div>
            {plan.storageGb === -1 ? (
              <p className="text-2xl font-bold text-slate-800">∞</p>
            ) : (
              <>
                <p className="text-sm text-slate-600 mb-3">
                  {t('platform.subscription.entitlements.usageOf', {
                    current: fmtBytes(ent.storageUsedBytes),
                    max: `${plan.storageGb} GB`,
                  })}
                </p>
                <UsageBar
                  pct={storageUsagePct}
                  label={`${storageUsagePct}%`}
                  warning={storageUsagePct >= 70 && storageUsagePct < 90}
                  danger={ent.storageQuotaReached}
                />
                {ent.storageQuotaReached && (
                  <p className="mt-3 text-xs text-red-600">
                    {t('platform.subscription.entitlements.storageQuotaReached')}
                  </p>
                )}
              </>
            )}
          </div>

          {/* Enabled modules */}
          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
            <div className="flex items-center gap-2 mb-4">
              <Puzzle size={18} className="text-indigo-600" />
              <h3 className="font-semibold text-slate-800">{t('platform.subscription.entitlements.modules')}</h3>
            </div>
            <div className="flex flex-wrap gap-2">
              {(ent.enabledModules ?? []).map(m => (
                <span key={m}
                  className="inline-flex items-center gap-1 px-3 py-1 bg-indigo-50 text-indigo-700 text-xs font-medium rounded-full border border-indigo-200">
                  <CheckCircle size={10} />
                  {m}
                </span>
              ))}
            </div>
          </div>

          {/* Upgrade CTA when approaching or at limits */}
          {(ent.employeeLimitReached || ent.storageQuotaReached || employeeUsagePct >= 80 || storageUsagePct >= 80) && (
            <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-5 flex items-center justify-between gap-4 flex-wrap">
              <div>
                <p className="font-semibold text-indigo-800 text-sm">
                  {t('platform.subscription.entitlements.upgradeCta')}
                </p>
                <p className="text-xs text-indigo-600 mt-1">
                  {t('platform.subscription.entitlements.upgradeDesc')}
                </p>
              </div>
              <button
                onClick={() => navigate('/dashboard/subscription')}
                className="px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors shrink-0"
              >
                {t('platform.subscription.entitlements.upgradeBtn')}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
