import { useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import PlatformSidebar from './PlatformSidebar';
import PlatformNavbar from './PlatformNavbar';
import { PLATFORM_NAV_ITEMS } from '../../constants/platformRoutes';

export default function PlatformLayout({ children }) {
  const { pathname } = useLocation();
  const { t } = useTranslation();
  const [mobileOpen, setMobileOpen] = useState(false);

  const currentNav = PLATFORM_NAV_ITEMS.find((item) => pathname.startsWith(item.to));
  const title = currentNav ? t(currentNav.labelKey) : t('platform.nav.dashboard');

  return (
    <div className="flex h-screen overflow-hidden bg-slate-50">
      <PlatformSidebar
        mobileOpen={mobileOpen}
        onMobileClose={() => setMobileOpen(false)}
      />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <PlatformNavbar
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
