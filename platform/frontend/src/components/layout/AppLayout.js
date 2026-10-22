import { useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import { NAV_ITEMS } from '../../constants/routes';

export default function AppLayout({ children }) {
  const { pathname } = useLocation();
  const { t } = useTranslation();

  // Derive title from NAV_ITEMS — no separate pageTitles map needed
  const currentNav = NAV_ITEMS.find((item) => item.to === pathname);
  const title = currentNav ? t(currentNav.labelKey) : 'HRMS';

  return (
    <div className="flex min-h-screen bg-slate-50">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Navbar title={title} />
        <main className="flex-1 overflow-y-auto p-6">{children}</main>
      </div>
    </div>
  );
}
