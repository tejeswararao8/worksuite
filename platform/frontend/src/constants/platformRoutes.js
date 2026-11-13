import {
  LayoutDashboard, Building2, CheckSquare, Users,
  UserCheck, Package, CreditCard, Puzzle,
  LifeBuoy, ScrollText,
} from 'lucide-react';

export const PLATFORM_ROUTES = {
  DASHBOARD:     '/platform/dashboard',
  TENANTS:       '/platform/tenants',
  APPROVALS:     '/platform/approvals',
  EMPLOYEES:     '/platform/employees',
  ASSIGNMENTS:   '/platform/assignments',
  SERVICES:      '/platform/services',
  SUBSCRIPTIONS: '/platform/subscriptions',
  MODULES:       '/platform/modules',
  SUPPORT:       '/platform/support',
  AUDIT:         '/platform/audit',
};

// roles that can see each nav item — empty means all platform roles
export const PLATFORM_NAV_ITEMS = [
  {
    to:       PLATFORM_ROUTES.DASHBOARD,
    icon:     LayoutDashboard,
    labelKey: 'platform.nav.dashboard',
    roles:    [],
  },
  {
    to:       PLATFORM_ROUTES.TENANTS,
    icon:     Building2,
    labelKey: 'platform.nav.tenants',
    roles:    ['PLATFORM_SUPER_ADMIN', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'PLATFORM_IMPLEMENTATION'],
  },
  {
    to:       PLATFORM_ROUTES.APPROVALS,
    icon:     CheckSquare,
    labelKey: 'platform.nav.approvals',
    roles:    ['PLATFORM_SUPER_ADMIN', 'PLATFORM_ADMIN'],
  },
  {
    to:       PLATFORM_ROUTES.EMPLOYEES,
    icon:     Users,
    labelKey: 'platform.nav.employees',
    roles:    ['PLATFORM_SUPER_ADMIN', 'PLATFORM_ADMIN'],
  },
  {
    to:       PLATFORM_ROUTES.ASSIGNMENTS,
    icon:     UserCheck,
    labelKey: 'platform.nav.assignments',
    roles:    ['PLATFORM_SUPER_ADMIN', 'PLATFORM_ADMIN'],
  },
  {
    to:       PLATFORM_ROUTES.SERVICES,
    icon:     Package,
    labelKey: 'platform.nav.services',
    roles:    ['PLATFORM_SUPER_ADMIN', 'PLATFORM_ADMIN'],
  },
  {
    to:       PLATFORM_ROUTES.SUBSCRIPTIONS,
    icon:     CreditCard,
    labelKey: 'platform.nav.subscriptions',
    roles:    ['PLATFORM_SUPER_ADMIN', 'PLATFORM_ADMIN'],
  },
  {
    to:       PLATFORM_ROUTES.MODULES,
    icon:     Puzzle,
    labelKey: 'platform.nav.modules',
    roles:    ['PLATFORM_SUPER_ADMIN', 'PLATFORM_ADMIN'],
  },
  {
    to:       PLATFORM_ROUTES.SUPPORT,
    icon:     LifeBuoy,
    labelKey: 'platform.nav.support',
    roles:    [],
  },
  {
    to:       PLATFORM_ROUTES.AUDIT,
    icon:     ScrollText,
    labelKey: 'platform.nav.audit',
    roles:    ['PLATFORM_SUPER_ADMIN', 'PLATFORM_ADMIN'],
  },
];
