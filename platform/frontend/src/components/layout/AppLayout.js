import { useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import { NAV_ITEMS } from '../../constants/routes';

export default function AppLayout({ children }) {
  const { pathname } = useLocation();
  const { t } = useTranslation();
  const [mobileOpen, setMobileOpen] = useState(false);

  // Derive title from NAV_ITEMS — match on the relative path segment
  const currentNav = NAV_ITEMS.find((item) => {
    const rel = item.to.replace(/^\//, '');
    return pathname === `/dashboard/${rel}` || pathname === `/dashboard${item.to}`;
  });
  const title = currentNav ? t(currentNav.labelKey) : t('dashboard.title');

  return (
    <div className="flex h-screen overflow-hidden bg-slate-50">
      <Sidebar
        mobileOpen={mobileOpen}
        onMobileClose={() => setMobileOpen(false)}
      />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Navbar
          title={title}
          onMobileMenuToggle={() => setMobileOpen(true)}
        />
        <main className="flex-1 overflow-y-auto">
          <div className="p-4 sm:p-6 max-w-screen-2xl mx-auto w-full">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
