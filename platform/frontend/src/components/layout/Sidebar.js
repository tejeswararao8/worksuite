import { NavLink } from 'react-router-dom';
import { LogOut, ChevronLeft, ChevronRight, UserCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { NAV_ITEMS } from '../../constants/routes';
import { useState } from 'react';

export default function Sidebar() {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);

  return (
    <aside className={`flex flex-col bg-slate-900 text-white transition-all duration-300 ${collapsed ? 'w-16' : 'w-64'} min-h-screen shrink-0`}>
      {/* Logo */}
      <div className="flex items-center justify-between px-4 py-5 border-b border-slate-700">
        {!collapsed && <span className="text-xl font-bold text-indigo-400">HRMS</span>}
        <button onClick={() => setCollapsed(!collapsed)} className="p-1 rounded hover:bg-slate-700 ml-auto">
          {collapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
        </button>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-4 space-y-1 px-2">
        {NAV_ITEMS.map(({ to, icon: Icon, labelKey }) => (
          <NavLink
            key={to}
            to={to}
            title={collapsed ? t(labelKey) : undefined}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-700 hover:text-white'
              }`
            }
          >
            <Icon size={18} className="shrink-0" />
            {!collapsed && <span>{t(labelKey)}</span>}
          </NavLink>
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
          className={({ isActive }) =>
            `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
              isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-700 hover:text-white'
            }`
          }
          title={collapsed ? 'My Profile' : undefined}
        >
          <UserCircle size={18} className="shrink-0" />
          {!collapsed && <span>My Profile</span>}
        </NavLink>
        <button
          onClick={logout}
          title={collapsed ? t('nav.logout') : undefined}
          className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-slate-300 hover:bg-red-600 hover:text-white transition-colors"
        >
          <LogOut size={18} className="shrink-0" />
          {!collapsed && <span>{t('nav.logout')}</span>}
        </button>
      </div>
    </aside>
  );
}
