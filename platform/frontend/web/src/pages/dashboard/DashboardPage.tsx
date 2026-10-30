import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { Users, UserCheck, Package, ArrowLeftRight, TrendingUp, AlertTriangle, Clock } from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend, LineChart, Line, CartesianGrid,
} from 'recharts';
import { useDashboard } from '../../../../shared/hooks';
import { StatCard, Spinner, AlertBanner } from '../../components/ui';

const COLORS = ['#6366f1', '#22c55e', '#f59e0b', '#ef4444', '#3b82f6'];

export default function DashboardPage() {
  const { t }      = useTranslation();
  const navigate   = useNavigate();
  const { stats: s, isLoading } = useDashboard();

  if (isLoading) {
    return <div className="flex justify-center items-center h-64"><Spinner size="lg" /></div>;
  }

  const assetData = [
    { name: 'Available', value: s.availableAssets ?? 0 },
    { name: 'Assigned',  value: s.assignedAssets  ?? 0 },
  ];

  return (
    <div className="space-y-6">
      {/* Alerts */}
      {(s.probationEndingSoon > 0 || s.pendingTransfers > 0 || s.pendingPromotions > 0) && (
        <div className="space-y-2">
          {s.probationEndingSoon > 0 && (
            <AlertBanner type="warning" message={`${s.probationEndingSoon} employee(s) probation ending in 30 days`} />
          )}
          {s.pendingTransfers > 0 && (
            <AlertBanner type="info" message={`${s.pendingTransfers} transfer request(s) pending approval`} />
          )}
          {s.pendingPromotions > 0 && (
            <AlertBanner type="info" message={`${s.pendingPromotions} promotion request(s) pending approval`} />
          )}
        </div>
      )}

      {/* Stat Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4">
        <StatCard title={t('dashboard.totalEmployees')}  value={s.totalEmployees  ?? 0} icon={Users}         color="indigo" onClick={() => navigate('/employees')} />
        <StatCard title={t('dashboard.activeEmployees')} value={s.activeEmployees ?? 0} icon={UserCheck}     color="green"  onClick={() => navigate('/employees')} />
        <StatCard title={t('dashboard.onProbation')}     value={s.onProbation     ?? 0} icon={Clock}         color="yellow" onClick={() => navigate('/probation')} />
        <StatCard title="Pending Transfers"              value={s.pendingTransfers ?? 0} icon={ArrowLeftRight} color="blue"  onClick={() => navigate('/transfers')} />
        <StatCard title="Pending Promotions"             value={s.pendingPromotions ?? 0} icon={TrendingUp}   color="indigo" onClick={() => navigate('/promotions')} />
        <StatCard title="In Onboarding"                  value={s.employeesInOnboarding ?? 0} icon={Users}   color="orange" onClick={() => navigate('/onboarding')} />
        <StatCard title="Available Assets"               value={s.availableAssets ?? 0} icon={Package}       color="green"  onClick={() => navigate('/assets')} />
        <StatCard title="Probation Ending Soon"          value={s.probationEndingSoon ?? 0} icon={AlertTriangle} color="red" onClick={() => navigate('/probation')} />
      </div>

      {/* Charts Row 1 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h3 className="text-sm font-semibold text-slate-700 mb-4">{t('dashboard.headcountByDept')}</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={[]} margin={{ top: 0, right: 10, left: -20, bottom: 0 }}>
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="count" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
          <p className="text-xs text-slate-400 text-center mt-2">Department breakdown coming with analytics module</p>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h3 className="text-sm font-semibold text-slate-700 mb-4">Asset Utilization</h3>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={assetData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                {assetData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Pie>
              <Legend />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-xl border border-slate-200 p-5">
        <h3 className="text-sm font-semibold text-slate-700 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {[
            { label: 'Add Employee',    path: '/employees',   color: 'bg-indigo-50 text-indigo-700 hover:bg-indigo-100' },
            { label: 'New Transfer',    path: '/transfers',   color: 'bg-blue-50 text-blue-700 hover:bg-blue-100' },
            { label: 'New Promotion',   path: '/promotions',  color: 'bg-green-50 text-green-700 hover:bg-green-100' },
            { label: 'View Reports',    path: '/reports',     color: 'bg-orange-50 text-orange-700 hover:bg-orange-100' },
          ].map((action) => (
            <button
              key={action.path}
              onClick={() => navigate(action.path)}
              className={`${action.color} rounded-lg px-4 py-3 text-sm font-medium transition-colors text-left`}
            >
              {action.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
