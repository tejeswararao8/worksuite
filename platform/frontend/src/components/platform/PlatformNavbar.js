import { Menu, ChevronDown, LogOut, UserCircle, Shield } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useRef, useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import LanguageSwitcher from '../ui/LanguageSwitcher';
import { Avatar } from '../ui';

function ProfileMenu({ user, logout }) {
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    if (!open) return;
    const handler = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const handler = (e) => { if (e.key === 'Escape') setOpen(false); };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [open]);

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setOpen(!open)}
        className="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-violet-50 transition-colors"
        aria-haspopup="true"
        aria-expanded={open}
      >
        <Avatar name={user?.email || 'User'} size="sm" />
        <div className="hidden sm:block text-left">
          <p className="text-xs font-medium text-slate-700 leading-tight max-w-[120px] truncate">{user?.email}</p>
          <p className="text-xs text-violet-500 leading-tight">Platform Admin</p>
        </div>
        <ChevronDown size={14} className={`text-slate-400 transition-transform duration-200 ${open ? 'rotate-180' : ''}`} />
      </button>

      {open && (
        <div className="absolute ltr:right-0 rtl:left-0 mt-1 w-48 bg-white rounded-xl shadow-lg border border-slate-200 py-1 z-50">
          <div className="px-3 py-2 border-b border-slate-100">
            <p className="text-xs font-semibold text-slate-700 truncate">{user?.email}</p>
            <p className="text-xs text-violet-500">Platform Admin</p>
          </div>
          <button
            onClick={() => { setOpen(false); logout(); }}
            className="flex items-center gap-2 w-full px-3 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
          >
            <LogOut size={15} className="text-red-400" />
            {t('nav.logout')}
          </button>
        </div>
      )}
    </div>
  );
}

export default function PlatformNavbar({ title, onMobileMenuToggle }) {
  const { t } = useTranslation();
  const { user, logout } = useAuth();

  return (
    <header className="h-14 bg-white border-b border-violet-100 flex items-center justify-between px-4 lg:px-6 sticky top-0 z-30 shrink-0">
      <div className="flex items-center gap-3 min-w-0">
        <button
          onClick={onMobileMenuToggle}
          className="lg:hidden p-2 rounded-lg hover:bg-violet-50 text-slate-600 transition-colors"
          aria-label="Open menu"
        >
          <Menu size={20} />
        </button>
        <div className="flex items-center gap-2">
          <Shield size={16} className="text-violet-500 shrink-0" />
          <h1 className="text-base font-semibold text-slate-800 truncate">{title}</h1>
        </div>
      </div>

      <div className="flex items-center gap-1 sm:gap-2 shrink-0">
        <LanguageSwitcher />
        <ProfileMenu user={user} logout={logout} />
      </div>
    </header>
  );
}
