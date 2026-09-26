import { useTranslation } from 'react-i18next';
import { useDashboard } from '../../hooks/useDashboard';
import { StatCard, Spinner } from '../../components/ui';
import { Users, FileText, UserCheck } from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend, LineChart, Line, CartesianGrid,
} from 'recharts';

const CHART_COLORS = ['#6366f1', '#22c55e', '#f59e0b', '#ef4444', '#3b82f6', '#8b5cf6'];

const MOCK_DEPT   = [
  { name: 'Engineering', count: 45 }, { name: 'HR', count: 12 },
  { name: 'Finance', count: 18 },     { name: 'Sales', count: 30 },
  { name: 'Operations', count: 22 },
];
const MOCK_BRANCH = [
  { name: 'Dubai HQ', count: 60 }, { name: 'Abu Dhabi', count: 35 },
  { name: 'Sharjah', count: 22 },
];
const MOCK_TREND  = [
  { month: 'Jan', joiners: 5 }, { month: 'Feb', joiners: 8 },
  { month: 'Mar', joiners: 12 }, { month: 'Apr', joiners: 7 },
  { month: 'May', joiners: 15 }, { month: 'Jun', joiners: 10 },
];
const MOCK_ASSETS = [
  { status: 'AVAILABLE', count: 40 }, { status: 'ASSIGNED', count: 85 },
  { status: 'IN_REPAIR', count: 5 },  { status: 'RETIRED', count: 10 },
];

export default function DashboardPage() {
  const { t } = useTranslation();
  const { summary: s, deptChart, branchChart, trend, docAlerts, assetUtil, isLoading } = useDashboard();

  const chartDept   = deptChart.length   ? deptChart   : MOCK_DEPT;
  const chartBranch = branchChart.length ? branchChart : MOCK_BRANCH;
  const chartTrend  = trend.length       ? trend       : MOCK_TREND;
  const chartAssets = assetUtil.length   ? assetUtil   : MOCK_ASSETS;

  if (isLoading) {
    return <div className="flex justify-center items-center h-64"><Spinner size="lg" /></div>;
  }

  return (
    <div className="space-y-6">
      {/* Stat Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4">
        <StatCard title={t('dashboard.totalEmployees')}   value={s.totalEmployees            ?? 0} icon={Users}          color="indigo" />
        <StatCard title={t('dashboard.activeEmployees')}  value={s.activeEmployees           ?? 0} icon={UserCheck}      color="green" />
        <StatCard title={t('dashboard.onProbation')}      value={s.onProbation               ?? 0} icon={Users}          color="yellow" />
        <StatCard title={t('dashboard.docsExpiring')}     value={s.documentsExpiringIn30Days ?? docAlerts.expiring30Days ?? 0} icon={FileText} color="red" subtitle={t('common.needsAttention')} />
      </div>

      {/* Charts row 1 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h3 className="text-sm font-semibold text-slate-700 mb-4">{t('dashboard.headcountByDept')}</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={chartDept} margin={{ top: 0, right: 10, left: -20, bottom: 0 }}>
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="count" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h3 className="text-sm font-semibold text-slate-700 mb-4">{t('dashboard.headcountByBranch')}</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={chartBranch} margin={{ top: 0, right: 10, left: -20, bottom: 0 }}>
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="count" fill="#22c55e" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Charts row 2 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h3 className="text-sm font-semibold text-slate-700 mb-4">{t('dashboard.joiningTrend')}</h3>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={chartTrend} margin={{ top: 0, right: 10, left: -20, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="month" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Line type="monotone" dataKey="joiners" stroke="#6366f1" strokeWidth={2} dot={{ r: 4 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h3 className="text-sm font-semibold text-slate-700 mb-4">{t('dashboard.assetUtilization')}</h3>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={chartAssets} dataKey="count" nameKey="status" cx="50%" cy="50%" outerRadius={80} label>
                {chartAssets.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
              </Pie>
              <Legend />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
