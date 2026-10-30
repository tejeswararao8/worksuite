import { ReactNode } from 'react';
import { useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Sidebar from './Sidebar';
import Navbar  from './Navbar';

const PAGE_TITLES: Record<string, string> = {
  '/dashboard':    'nav.dashboard',
  '/employees':    'nav.employees',
  '/departments':  'nav.departments',
  '/branches':     'nav.branches',
  '/designations': 'nav.designations',
  '/teams':        'nav.teams',
  '/assets':       'nav.assets',
  '/documents':    'nav.documents',
  '/transfers':    'nav.transfers',
  '/promotions':   'nav.promotions',
  '/probation':    'nav.probation',
  '/onboarding':   'nav.onboarding',
  '/offboarding':  'nav.offboarding',
  '/skills':       'nav.skills',
  '/users':        'nav.users',
  '/notifications':'nav.notifications',
  '/reports':      'nav.reports',
  '/roles':        'nav.roles',
  '/audit':        'nav.audit',
  '/profile':      'profile.title',
};

export default function AppLayout({ children }: { children: ReactNode }) {
  const { pathname } = useLocation();
  const { t }        = useTranslation();

  // Match exact or prefix (e.g. /employees/uuid)
  const key   = Object.keys(PAGE_TITLES).find((k) => pathname === k || pathname.startsWith(k + '/'));
  const title = key ? t(PAGE_TITLES[key]) : 'WorkSuite';

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
