import { NavLink } from 'react-router-dom';
import { LogOut, ChevronLeft, ChevronRight, UserCircle, X, Shield } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { useLanguage } from '../../context/LanguageContext';
import { PLATFORM_NAV_ITEMS } from '../../constants/platformRoutes';
import { Avatar } from '../ui';
import { useState, useRef } from 'react';

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

export default function PlatformSidebar({ mobileOpen, onMobileClose }) {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const { isRtl } = useLanguage();
  const [collapsed, setCollapsed] = useState(false);

  const userRoles = user?.roles ?? [];
  const visibleItems = PLATFORM_NAV_ITEMS.filter(
    ({ roles }) => roles.length === 0 || roles.some((r) => userRoles.includes(r))
  );

  const sidebarContent = (
    <div className={`flex flex-col h-full bg-violet-950 text-white ${collapsed ? 'w-16' : 'w-64'} transition-all duration-300`}>
      {/* Logo */}
      <div className="flex items-center justify-between px-4 py-4 border-b border-violet-800/80 shrink-0">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 bg-violet-500 rounded-lg flex items-center justify-center shrink-0">
              <Shield size={14} className="text-white" />
            </div>
            <div>
              <span className="text-sm font-bold text-white tracking-tight block">WorkSuite</span>
              <span className="text-[10px] text-violet-300 uppercase tracking-widest">Platform Admin</span>
            </div>
          </div>
        )}
        {collapsed && (
          <div className="w-7 h-7 bg-violet-500 rounded-lg flex items-center justify-center mx-auto">
            <Shield size={14} className="text-white" />
          </div>
        )}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className={`p-1.5 rounded-lg hover:bg-violet-800 text-violet-300 hover:text-white transition-colors ${collapsed ? 'mx-auto mt-1' : ''}`}
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {isRtl
            ? (collapsed ? <ChevronLeft size={16} /> : <ChevronRight size={16} />)
            : (collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />)
          }
        </button>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-3 px-2 space-y-0.5 scrollbar-thin">
        {visibleItems.map(({ to, icon: Icon, labelKey }) => (
          <NavTooltip key={to} label={t(labelKey)} show={collapsed}>
            <NavLink
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-violet-600 text-white'
                    : 'text-violet-200 hover:bg-violet-800/70 hover:text-white'
                }`
              }
            >
              <Icon size={18} className="shrink-0" />
              {!collapsed && <span className="truncate">{t(labelKey)}</span>}
            </NavLink>
          </NavTooltip>
        ))}
      </nav>

      {/* Footer */}
      <div className="border-t border-violet-800/80 p-2 space-y-0.5 shrink-0">
        {!collapsed && user && (
          <div className="flex items-center gap-2 px-3 py-2 mb-1">
            <Avatar name={user.email || 'User'} size="sm" />
            <div className="min-w-0">
              <p className="text-xs font-medium text-violet-100 truncate">{user.email}</p>
              <p className="text-xs text-violet-400 truncate">Platform Admin</p>
            </div>
          </div>
        )}
        <NavTooltip label={t('profile.title')} show={collapsed}>
          <NavLink
            to="/platform/profile"
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive ? 'bg-violet-600 text-white' : 'text-violet-200 hover:bg-violet-800/70 hover:text-white'
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
            className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-violet-200 hover:bg-red-600/80 hover:text-white transition-colors"
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
      <aside className="hidden lg:flex flex-col shrink-0 min-h-screen sticky top-0 h-screen overflow-hidden">
        {sidebarContent}
      </aside>

      {mobileOpen && (
        <div className="lg:hidden fixed inset-0 z-40 flex">
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm" onClick={onMobileClose} aria-hidden="true" />
          <aside className={`relative z-50 flex flex-col h-full bg-violet-950 w-64 shadow-2xl ${isRtl ? 'mr-auto' : 'ml-0'}`}>
            <button
              onClick={onMobileClose}
              className="absolute top-3 right-3 p-1.5 rounded-lg text-violet-300 hover:text-white hover:bg-violet-800 transition-colors z-10"
              aria-label="Close menu"
            >
              <X size={18} />
            </button>
            <div className="flex flex-col h-full bg-violet-950 text-white w-64">
              <div className="flex items-center gap-2 px-4 py-4 border-b border-violet-800/80 shrink-0">
                <div className="w-7 h-7 bg-violet-500 rounded-lg flex items-center justify-center shrink-0">
                  <Shield size={14} className="text-white" />
                </div>
                <div>
                  <span className="text-sm font-bold text-white tracking-tight block">WorkSuite</span>
                  <span className="text-[10px] text-violet-300 uppercase tracking-widest">Platform Admin</span>
                </div>
              </div>
              <nav className="flex-1 overflow-y-auto py-3 px-2 space-y-0.5">
                {visibleItems.map(({ to, icon: Icon, labelKey }) => (
                  <NavLink
                    key={to}
                    to={to}
                    onClick={onMobileClose}
                    className={({ isActive }) =>
                      `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                        isActive ? 'bg-violet-600 text-white' : 'text-violet-200 hover:bg-violet-800/70 hover:text-white'
                      }`
                    }
                  >
                    <Icon size={18} className="shrink-0" />
                    <span className="truncate">{t(labelKey)}</span>
                  </NavLink>
                ))}
              </nav>
              <div className="border-t border-violet-800/80 p-2 space-y-0.5 shrink-0">
                {user && (
                  <div className="flex items-center gap-2 px-3 py-2 mb-1">
                    <Avatar name={user.email || 'User'} size="sm" />
                    <div className="min-w-0">
                      <p className="text-xs font-medium text-violet-100 truncate">{user.email}</p>
                      <p className="text-xs text-violet-400 truncate">Platform Admin</p>
                    </div>
                  </div>
                )}
                <button
                  onClick={() => { onMobileClose(); logout(); }}
                  className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-violet-200 hover:bg-red-600/80 hover:text-white transition-colors"
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
