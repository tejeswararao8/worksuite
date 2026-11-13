import { useTranslation } from 'react-i18next';
import { Building2, CheckSquare, AlertTriangle, XCircle, Users, LifeBuoy } from 'lucide-react';
import { usePlatformDashboard } from '../../hooks/usePlatformDashboard';
import { StatCard, Skeleton, PageHeader } from '../../components/ui';

function UnavailableCard({ title }) {
  const { t } = useTranslation();
  return (
    <div className="bg-white rounded-xl border border-dashed border-slate-200 p-6 flex flex-col items-center justify-center text-center min-h-[120px]">
      <p className="text-sm font-medium text-slate-500">{title}</p>
      <p className="text-xs text-slate-400 mt-1">{t('platform.dashboard.comingSoon')}</p>
    </div>
  );
}

export default function PlatformDashboardPage() {
  const { t } = useTranslation();
  const { summary, isLoading, isError } = usePlatformDashboard();

  const kpis = [
    {
      label:  t('platform.dashboard.totalTenants'),
      value:  summary?.totalTenants,
      icon:   Building2,
      color:  'violet',
    },
    {
      label:  t('platform.dashboard.activeTenants'),
      value:  summary?.activeTenants,
      icon:   Building2,
      color:  'green',
    },
    {
      label:  t('platform.dashboard.pendingApprovals'),
      value:  summary?.pendingApprovals,
      icon:   CheckSquare,
      color:  'amber',
    },
    {
      label:  t('platform.dashboard.suspendedTenants'),
      value:  summary?.suspendedTenants,
      icon:   AlertTriangle,
      color:  'red',
    },
    {
      label:  t('platform.dashboard.platformEmployees'),
      value:  summary?.totalPlatformEmployees,
      icon:   Users,
      color:  'blue',
    },
    {
      label:  t('platform.dashboard.openTickets'),
      value:  summary?.openSupportTickets,
      icon:   LifeBuoy,
      color:  'orange',
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader title={t('platform.nav.dashboard')} />

      {isError && (
        <div className="flex items-center gap-2 p-4 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700">
          <XCircle size={16} className="shrink-0" />
          {t('platform.dashboard.loadError')}
        </div>
      )}

      {/* KPI grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-6 gap-4">
        {kpis.map(({ label, value, icon: Icon, color }) =>
          isLoading ? (
            <Skeleton key={label} className="h-24 rounded-xl" />
          ) : (
            <StatCard
              key={label}
              label={label}
              value={value ?? 0}
              icon={Icon}
              color={color}
            />
          )
        )}
      </div>

      {/* Sections — implemented in later subphases */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <UnavailableCard title={t('platform.dashboard.recentRegistrations')} />
        <UnavailableCard title={t('platform.dashboard.approvalQueue')} />
        <UnavailableCard title={t('platform.dashboard.subscriptionOverview')} />
        <UnavailableCard title={t('platform.dashboard.recentActivity')} />
        <UnavailableCard title={t('platform.dashboard.onboardingProgress')} />
        <UnavailableCard title={t('platform.dashboard.supportSummary')} />
      </div>
    </div>
  );
}
