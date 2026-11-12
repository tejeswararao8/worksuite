import { NavLink } from 'react-router-dom';
import { LogOut, ChevronLeft, ChevronRight, UserCircle, X, ChevronDown } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { useLanguage } from '../../context/LanguageContext';
import { NAV_ITEMS } from '../../constants/routes';
import { Avatar } from '../ui';
import { useState, useRef } from 'react';

// Group display order and i18n keys
const GROUP_ORDER = [null, 'People', 'Organization', 'Operations', 'Lifecycle', 'Admin'];
const GROUP_LABEL_KEYS = {
  People:       'nav.group.people',
  Organization: 'nav.group.organization',
  Operations:   'nav.group.operations',
  Lifecycle:    'nav.group.lifecycle',
  Admin:        'nav.group.admin',
};

function NavTooltip({ label, children, show }) {
  const [visible, setVisible] = useState(false);
  const ref = useRef(null);
  const [top, setTop] = useState(0);

  if (!show) return children;

  return (
    <div
      className="relative"
      onMouseEnter={() => {
        if (ref.current) setTop(ref.current.getBoundingClientRect().top + window.scrollY);
        setVisible(true);
      }}
      onMouseLeave={() => setVisible(false)}
      ref={ref}
    >
      {children}
      {visible && (
        <div
          className="fixed z-50 ltr:left-16 rtl:right-16 bg-slate-800 text-white text-xs px-2 py-1 rounded shadow-lg whitespace-nowrap pointer-events-none"
          style={{ top: top + 6 }}
        >
          {label}
        </div>
      )}
    </div>
  );
}

function NavGroup({ group, items, collapsed, openGroups, toggleGroup }) {
  const { t } = useTranslation();
  const isOpen = openGroups[group] !== false; // default open

  if (group === null) {
    return items.map(({ to, icon: Icon, labelKey }) => (
      <NavTooltip key={to} label={t(labelKey)} show={collapsed}>
        <NavLink
          to={to}
          end={to === '/dashboard'}
          className={({ isActive }) =>
            `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
              isActive
                ? 'bg-indigo-600 text-white'
                : 'text-slate-300 hover:bg-slate-700/70 hover:text-white'
            }`
          }
        >
          <Icon size={18} className="shrink-0" />
          {!collapsed && <span className="truncate">{t(labelKey)}</span>}
        </NavLink>
      </NavTooltip>
    ));
  }

  return (
    <div>
      {!collapsed && (
        <button
          onClick={() => toggleGroup(group)}
          className="flex items-center justify-between w-full px-3 py-1.5 text-xs font-semibold uppercase tracking-wider text-slate-500 hover:text-slate-300 transition-colors"
        >
          <span>{t(GROUP_LABEL_KEYS[group])}</span>
          <ChevronDown
            size={14}
            className={`transition-transform duration-200 ${isOpen ? 'rotate-0' : '-rotate-90'}`}
          />
        </button>
      )}
      {collapsed && <div className="my-1 mx-3 border-t border-slate-700/60" />}
      {(isOpen || collapsed) &&
        items.map(({ to, icon: Icon, labelKey }) => (
          <NavTooltip key={to} label={t(labelKey)} show={collapsed}>
            <NavLink
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-indigo-600 text-white'
                    : 'text-slate-300 hover:bg-slate-700/70 hover:text-white'
                }`
              }
            >
              <Icon size={18} className="shrink-0" />
              {!collapsed && <span className="truncate">{t(labelKey)}</span>}
            </NavLink>
          </NavTooltip>
        ))}
    </div>
  );
}

export default function Sidebar({ mobileOpen, onMobileClose }) {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const { isRtl } = useLanguage();
  const [collapsed, setCollapsed] = useState(false);
  const [openGroups, setOpenGroups] = useState({});

  const toggleGroup = (group) =>
    setOpenGroups((prev) => ({ ...prev, [group]: prev[group] === false ? true : false }));

  // Group items by group key, preserving GROUP_ORDER
  const grouped = GROUP_ORDER.map((group) => ({
    group,
    items: NAV_ITEMS.filter((item) => item.group === group),
  })).filter(({ items }) => items.length > 0);

  const sidebarContent = (
    <div className={`flex flex-col h-full bg-slate-900 text-white ${collapsed ? 'w-16' : 'w-64'} transition-all duration-300`}>
      {/* Logo + collapse toggle */}
      <div className="flex items-center justify-between px-4 py-4 border-b border-slate-700/80 shrink-0">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 bg-indigo-600 rounded-lg flex items-center justify-center shrink-0">
              <span className="text-white text-xs font-bold">WS</span>
            </div>
            <span className="text-base font-bold text-white tracking-tight">WorkSuite</span>
          </div>
        )}
        {collapsed && (
          <div className="w-7 h-7 bg-indigo-600 rounded-lg flex items-center justify-center mx-auto">
            <span className="text-white text-xs font-bold">WS</span>
          </div>
        )}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className={`p-1.5 rounded-lg hover:bg-slate-700 text-slate-400 hover:text-white transition-colors ${collapsed ? 'mx-auto mt-1' : ''}`}
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {isRtl
            ? (collapsed ? <ChevronLeft size={16} /> : <ChevronRight size={16} />)
            : (collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />)
          }
        </button>
      </div>

      {/* Nav groups */}
      <nav className="flex-1 overflow-y-auto py-3 px-2 space-y-0.5 scrollbar-thin">
        {grouped.map(({ group, items }) => (
          <NavGroup
            key={group ?? '__root__'}
            group={group}
            items={items}
            collapsed={collapsed}
            openGroups={openGroups}
            toggleGroup={toggleGroup}
          />
        ))}
      </nav>

      {/* User footer */}
      <div className="border-t border-slate-700/80 p-2 space-y-0.5 shrink-0">
        {!collapsed && user && (
          <div className="flex items-center gap-2 px-3 py-2 mb-1">
            <Avatar name={user.email || 'User'} size="sm" />
            <div className="min-w-0">
              <p className="text-xs font-medium text-slate-200 truncate">{user.email}</p>
              <p className="text-xs text-indigo-400 truncate capitalize">{user.role}</p>
            </div>
          </div>
        )}
        <NavTooltip label={t('profile.title')} show={collapsed}>
          <NavLink
            to="/dashboard/profile"
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-indigo-600 text-white'
                  : 'text-slate-300 hover:bg-slate-700/70 hover:text-white'
              }`
            }
          >
            <UserCircle size={18} className="shrink-0" />
            {!collapsed && <span>{t('profile.title')}</span>}
          </NavLink>
        </NavTooltip>
        <NavTooltip label={t('nav.logout')} show={collapsed}>
          <button
            onClick={logout}
            className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-slate-300 hover:bg-red-600/80 hover:text-white transition-colors"
          >
            <LogOut size={18} className="shrink-0" />
            {!collapsed && <span>{t('nav.logout')}</span>}
          </button>
        </NavTooltip>
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop sidebar */}
      <aside className="hidden lg:flex flex-col shrink-0 min-h-screen sticky top-0 h-screen overflow-hidden">
        {sidebarContent}
      </aside>

      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="lg:hidden fixed inset-0 z-40 flex">
          {/* Backdrop */}
          <div
            className="fixed inset-0 bg-black/50 backdrop-blur-sm"
            onClick={onMobileClose}
            aria-hidden="true"
          />
          {/* Drawer */}
          <aside className={`relative z-50 flex flex-col h-full bg-slate-900 w-64 shadow-2xl ${isRtl ? 'mr-auto' : 'ml-0'}`}>
            <button
              onClick={onMobileClose}
              className="absolute top-3 right-3 p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-700 transition-colors z-10"
              aria-label="Close menu"
            >
              <X size={18} />
            </button>
            {/* Mobile always expanded */}
            <div className="flex flex-col h-full bg-slate-900 text-white w-64">
              <div className="flex items-center gap-2 px-4 py-4 border-b border-slate-700/80 shrink-0">
                <div className="w-7 h-7 bg-indigo-600 rounded-lg flex items-center justify-center shrink-0">
                  <span className="text-white text-xs font-bold">WS</span>
                </div>
                <span className="text-base font-bold text-white tracking-tight">WorkSuite</span>
              </div>
              <nav className="flex-1 overflow-y-auto py-3 px-2 space-y-0.5">
                {grouped.map(({ group, items }) => (
                  <NavGroup
                    key={group ?? '__root__'}
                    group={group}
                    items={items}
                    collapsed={false}
                    openGroups={openGroups}
                    toggleGroup={toggleGroup}
                  />
                ))}
              </nav>
              <div className="border-t border-slate-700/80 p-2 space-y-0.5 shrink-0">
                {user && (
                  <div className="flex items-center gap-2 px-3 py-2 mb-1">
                    <Avatar name={user.email || 'User'} size="sm" />
                    <div className="min-w-0">
                      <p className="text-xs font-medium text-slate-200 truncate">{user.email}</p>
                      <p className="text-xs text-indigo-400 truncate capitalize">{user.role}</p>
                    </div>
                  </div>
                )}
                <NavLink
                  to="/dashboard/profile"
                  onClick={onMobileClose}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                      isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-700/70 hover:text-white'
                    }`
                  }
                >
                  <UserCircle size={18} className="shrink-0" />
                  <span>{t('profile.title')}</span>
                </NavLink>
                <button
                  onClick={() => { onMobileClose(); logout(); }}
                  className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-slate-300 hover:bg-red-600/80 hover:text-white transition-colors"
                >
                  <LogOut size={18} className="shrink-0" />
                  <span>{t('nav.logout')}</span>
                </button>
              </div>
            </div>
          </aside>
        </div>
      )}
    </>
  );
}
