import { useNavigate } from 'react-router-dom';
import { Bell, Globe } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import type { RootState } from '../../../shared/store';

const LANGUAGES = [
  { code: 'en', label: 'EN' },
  { code: 'ar', label: 'AR' },
  { code: 'hi', label: 'HI' },
];

interface NavbarProps { title: string; }

export default function Navbar({ title }: NavbarProps) {
  const navigate    = useNavigate();
  const { i18n }    = useTranslation();
  const unreadCount = useSelector((s: RootState) => s.notification.unreadCount);

  return (
    <header className="h-14 bg-white border-b border-slate-200 flex items-center justify-between px-6 sticky top-0 z-10">
      <h1 className="text-lg font-semibold text-slate-800">{title}</h1>
      <div className="flex items-center gap-3">
        {/* Language switcher */}
        <div className="flex items-center gap-1 border border-slate-200 rounded-lg px-2 py-1">
          <Globe size={14} className="text-slate-400" />
          {LANGUAGES.map((lang) => (
            <button
              key={lang.code}
              onClick={() => i18n.changeLanguage(lang.code)}
              className={`text-xs px-1.5 py-0.5 rounded transition-colors ${
                i18n.language?.startsWith(lang.code)
                  ? 'bg-indigo-600 text-white'
                  : 'text-slate-500 hover:text-slate-700'
              }`}
            >
              {lang.label}
            </button>
          ))}
        </div>

        {/* Notification bell */}
        <button
          onClick={() => navigate('/notifications')}
          className="relative p-2 rounded-full hover:bg-slate-100 transition-colors"
          aria-label="Notifications"
        >
          <Bell size={20} className="text-slate-600" />
          {unreadCount > 0 && (
            <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center font-medium">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </button>
      </div>
    </header>
  );
}
