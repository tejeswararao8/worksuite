import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { CreditCard, Users, HardDrive, Puzzle, AlertTriangle } from 'lucide-react';
import { PageHeader, Badge, Card, StatCard, Skeleton } from '../../components/ui';
import { tenantSubscriptionApi } from '../../api/services';
import { QUERY_KEYS } from '../../constants/queryKeys';

const STATUS_COLOR = {
  TRIAL: 'yellow', ACTIVE: 'green', PAST_DUE: 'orange',
  SUSPENDED: 'red', EXPIRED: 'gray', CANCELLED: 'gray',
};

function fmt(cents) {
  return cents === 0 ? 'Free' : `$${(cents / 100).toFixed(2)}`;
}

export default function MySubscriptionPage() {
  const { t, i18n } = useTranslation();
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

  return (
    <div className={`space-y-6 ${isRtl ? 'rtl' : ''}`}>
      <PageHeader title={t('platform.subscription.entitlements.title')} />

      {/* Subscription overview card */}
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
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm text-slate-600">
                {t('platform.subscription.entitlements.usageOf', {
                  current: ent.currentEmployeeCount,
                  max: plan.maxEmployees === -1 ? '∞' : plan.maxEmployees,
                })}
              </span>
              {ent.employeeLimitReached && (
                <span className="text-xs text-red-600 font-medium flex items-center gap-1">
                  <AlertTriangle size={12} />
                  {t('platform.subscription.entitlements.limitReached')}
                </span>
              )}
            </div>
            {plan.maxEmployees !== -1 && (
              <div className="w-full bg-slate-100 rounded-full h-2">
                <div
                  className={`h-2 rounded-full transition-all ${
                    employeeUsagePct >= 90 ? 'bg-red-500' :
                    employeeUsagePct >= 70 ? 'bg-orange-400' : 'bg-indigo-500'
                  }`}
                  style={{ width: `${employeeUsagePct}%` }}
                />
              </div>
            )}
          </div>

          {/* Storage */}
          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
            <div className="flex items-center gap-2 mb-2">
              <HardDrive size={18} className="text-indigo-600" />
              <h3 className="font-semibold text-slate-800">{t('platform.subscription.entitlements.storage')}</h3>
            </div>
            <p className="text-2xl font-bold text-slate-800">
              {plan.storageGb === -1 ? '∞' : `${plan.storageGb} GB`}
            </p>
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
                  className="px-3 py-1 bg-indigo-50 text-indigo-700 text-xs font-medium rounded-full border border-indigo-200">
                  {m}
                </span>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
