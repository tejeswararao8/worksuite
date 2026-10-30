import { Bell } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useNotifications } from '../../hooks/useNotifications';
import LanguageSwitcher from '../ui/LanguageSwitcher';
import { ROUTES } from '../../constants/routes';

export default function Navbar({ title }) {
  const navigate = useNavigate();
  const { unreadCount } = useNotifications();

  return (
    <header className="h-14 bg-white border-b border-slate-200 flex items-center justify-between px-6 sticky top-0 z-10">
      <h1 className="text-lg font-semibold text-slate-800">{title}</h1>
      <div className="flex items-center gap-4">
        <LanguageSwitcher />
        <button
          onClick={() => navigate(ROUTES.NOTIFICATIONS)}
          className="relative p-2 rounded-full hover:bg-slate-100 transition-colors"
          aria-label="Notifications"
        >
          <Bell size={20} className="text-slate-600" />
          {unreadCount > 0 && (
            <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </button>
      </div>
    </header>
  );
}
