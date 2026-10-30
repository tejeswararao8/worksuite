import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import {
  LayoutDashboard, Users, Building2, GitBranch, Briefcase,
  Package, Bell, BarChart2, ArrowLeftRight, TrendingUp,
  UserCheck, Shield, UserCog, ClipboardList, UserX,
  Building, ScrollText, UsersRound, ChevronLeft, ChevronRight,
  LogOut, UserCircle, FileText,
} from 'lucide-react';
import type { RootState } from '../../../shared/store';
import { useAuth } from '../contexts/AuthContext';

interface NavItem {
  to: string;
  icon: React.ComponentType<{ size?: number }>;
  labelKey: string;
  group?: string;
}

const NAV_ITEMS: NavItem[] = [
  { to: '/dashboard',    icon: LayoutDashboard, labelKey: 'nav.dashboard',    group: undefined },
  { to: '/employees',    icon: Users,           labelKey: 'nav.employees',    group: 'People' },
  { to: '/users',        icon: UserCog,         labelKey: 'nav.users',        group: 'People' },
  { to: '/skills',       icon: ClipboardList,   labelKey: 'nav.skills',       group: 'People' },
  { to: '/departments',  icon: Building2,       labelKey: 'nav.departments',  group: 'Organization' },
  { to: '/branches',     icon: GitBranch,       labelKey: 'nav.branches',     group: 'Organization' },
  { to: '/designations', icon: Briefcase,       labelKey: 'nav.designations', group: 'Organization' },
  { to: '/teams',        icon: UsersRound,      labelKey: 'nav.teams',        group: 'Organization' },
  { to: '/transfers',    icon: ArrowLeftRight,  labelKey: 'nav.transfers',    group: 'Lifecycle' },
  { to: '/promotions',   icon: TrendingUp,      labelKey: 'nav.promotions',   group: 'Lifecycle' },
  { to: '/probation',    icon: UserCheck,       labelKey: 'nav.probation',    group: 'Lifecycle' },
  { to: '/onboarding',   icon: UserCheck,       labelKey: 'nav.onboarding',   group: 'Lifecycle' },
  { to: '/offboarding',  icon: UserX,           labelKey: 'nav.offboarding',  group: 'Lifecycle' },
  { to: '/assets',       icon: Package,         labelKey: 'nav.assets',       group: 'Operations' },
  { to: '/documents',    icon: FileText,        labelKey: 'nav.documents',    group: 'Operations' },
  { to: '/notifications',icon: Bell,            labelKey: 'nav.notifications',group: undefined },
  { to: '/reports',      icon: BarChart2,       labelKey: 'nav.reports',      group: undefined },
  { to: '/roles',        icon: Shield,          labelKey: 'nav.roles',        group: 'Admin' },
  { to: '/audit',        icon: ScrollText,      labelKey: 'nav.audit',        group: 'Admin' },
];

const GROUPS = ['People', 'Organization', 'Lifecycle', 'Operations', 'Admin'];

export default function Sidebar() {
  const { t }    = useTranslation();
  const { logout } = useAuth();
  const user     = useSelector((s: RootState) => s.auth.user);
  const [collapsed, setCollapsed] = useState(false);

  const ungrouped = NAV_ITEMS.filter((i) => !i.group);
  const grouped   = GROUPS.map((g) => ({ group: g, items: NAV_ITEMS.filter((i) => i.group === g) }));

  const renderItem = (item: NavItem) => (
    <NavLink
      key={item.to}
      to={item.to}
      title={collapsed ? t(item.labelKey) : undefined}
      className={({ isActive }) =>
        `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
          isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-700 hover:text-white'
        }`
      }
    >
      <item.icon size={17} className="shrink-0" />
      {!collapsed && <span>{t(item.labelKey)}</span>}
    </NavLink>
  );

  return (
    <aside className={`flex flex-col bg-slate-900 text-white transition-all duration-300 ${collapsed ? 'w-16' : 'w-64'} min-h-screen shrink-0`}>
      {/* Logo */}
      <div className="flex items-center justify-between px-4 py-5 border-b border-slate-700">
        {!collapsed && <span className="text-xl font-bold text-indigo-400">WorkSuite</span>}
        <button onClick={() => setCollapsed(!collapsed)} className="p-1 rounded hover:bg-slate-700 ml-auto">
          {collapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
        </button>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-4 px-2 space-y-1">
        {/* Ungrouped (Dashboard, Notifications, Reports) */}
        {ungrouped.map(renderItem)}

        {/* Grouped sections */}
        {grouped.map(({ group, items }) => (
          <div key={group} className="pt-3">
            {!collapsed && (
              <p className="px-3 pb-1 text-xs font-semibold text-slate-500 uppercase tracking-wider">{group}</p>
            )}
            {items.map(renderItem)}
          </div>
        ))}
      </nav>

      {/* User + Profile + Logout */}
      <div className="border-t border-slate-700 p-3 space-y-1">
        {!collapsed && (
          <div className="mb-2 px-2">
            <p className="text-xs text-slate-400 truncate">{user?.email}</p>
            <p className="text-xs font-semibold text-indigo-300">{user?.role}</p>
          </div>
        )}
        <NavLink
          to="/profile"
          title={collapsed ? 'My Profile' : undefined}
          className={({ isActive }) =>
            `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
              isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-700 hover:text-white'
            }`
          }
        >
          <UserCircle size={17} className="shrink-0" />
          {!collapsed && <span>My Profile</span>}
        </NavLink>
        <button
          onClick={logout}
          title={collapsed ? 'Logout' : undefined}
          className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-slate-300 hover:bg-red-600 hover:text-white transition-colors"
        >
          <LogOut size={17} className="shrink-0" />
          {!collapsed && <span>{t('nav.logout')}</span>}
        </button>
      </div>
    </aside>
  );
}
