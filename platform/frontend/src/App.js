import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LanguageProvider } from './context/LanguageContext';
import AppLayout from './components/layout/AppLayout';
import PlatformLayout from './components/platform/PlatformLayout';
import { ROUTES } from './constants/routes';
import { PLATFORM_ROUTES } from './constants/platformRoutes';
import { ErrorBoundary, PageLoader } from './components/ui';

// ─── Eagerly loaded ───────────────────────────────────────────────────────────
import HomePage     from './pages/HomePage';
import LoginPage    from './pages/auth/LoginPage';
import AuthCallback from './pages/auth/AuthCallback';

// ─── Lazy: tenant HRMS pages ──────────────────────────────────────────────────
const DashboardPage     = lazy(() => import('./pages/dashboard/DashboardPage'));
const EmployeesPage     = lazy(() => import('./pages/employees/EmployeesPage'));
const DepartmentsPage   = lazy(() => import('./pages/departments/DepartmentsPage'));
const BranchesPage      = lazy(() => import('./pages/branches/BranchesPage'));
const DesignationsPage  = lazy(() => import('./pages/designations/DesignationsPage'));
const TeamsPage         = lazy(() => import('./pages/teams/TeamsPage'));
const CompanyPage       = lazy(() => import('./pages/company/CompanyPage'));
const AssetsPage        = lazy(() => import('./pages/assets/AssetsPage'));
const DocumentsPage     = lazy(() => import('./pages/documents/DocumentsPage'));
const TransfersPage     = lazy(() => import('./pages/transfers/TransfersPage'));
const PromotionsPage    = lazy(() => import('./pages/promotions/PromotionsPage'));
const ProbationPage     = lazy(() => import('./pages/probation/ProbationPage'));
const OnboardingPage    = lazy(() => import('./pages/onboarding/OnboardingPage'));
const OffboardingPage   = lazy(() => import('./pages/offboarding/OffboardingPage'));
const SkillsPage        = lazy(() => import('./pages/skills/SkillsPage'));
const UsersPage         = lazy(() => import('./pages/users/UsersPage'));
const NotificationsPage = lazy(() => import('./pages/notifications/NotificationsPage'));
const ReportsPage       = lazy(() => import('./pages/reports/ReportsPage'));
const RolesPage         = lazy(() => import('./pages/roles/RolesPage'));
const AuditPage         = lazy(() => import('./pages/audit/AuditPage'));
const ProfilePage       = lazy(() => import('./pages/profile/ProfilePage'));

// ─── Lazy: platform admin pages ───────────────────────────────────────────────
const PlatformDashboardPage   = lazy(() => import('./pages/platform/PlatformDashboardPage'));
const TenantsPage             = lazy(() => import('./pages/platform/tenants/TenantsPage'));
const TenantApprovalsPage     = lazy(() => import('./pages/platform/tenants/TenantApprovalsPage'));
const PlatformEmployeesPage   = lazy(() => import('./pages/platform/employees/PlatformEmployeesPage'));
const CustomerAssignmentsPage = lazy(() => import('./pages/platform/assignments/CustomerAssignmentsPage'));
const PlatformServicesPage    = lazy(() => import('./pages/platform/services/PlatformServicesPage'));
const SubscriptionsPage       = lazy(() => import('./pages/platform/subscriptions/SubscriptionsPage'));
const ModuleManagementPage    = lazy(() => import('./pages/platform/modules/ModuleManagementPage'));
const SupportTicketsPage      = lazy(() => import('./pages/platform/support/SupportTicketsPage'));
const PlatformAuditPage       = lazy(() => import('./pages/platform/audit/PlatformAuditPage'));

// ─── QueryClient ──────────────────────────────────────────────────────────────
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 60_000,
      refetchOnWindowFocus: false,
    },
  },
});

const PLATFORM_ROLES = [
  'PLATFORM_SUPER_ADMIN',
  'PLATFORM_ADMIN',
  'PLATFORM_SUPPORT',
  'PLATFORM_IMPLEMENTATION',
];

// ─── Route guards ─────────────────────────────────────────────────────────────
function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to={ROUTES.LOGIN} replace />;
}

function PlatformRoute({ children }) {
  const { isAuthenticated, user } = useAuth();
  if (!isAuthenticated) return <Navigate to={ROUTES.LOGIN} replace />;
  const hasPlatformRole = user?.roles?.some((r) => PLATFORM_ROLES.includes(r));
  if (!hasPlatformRole) return <Navigate to={ROUTES.DASHBOARD} replace />;
  return children;
}

// ─── Routes ───────────────────────────────────────────────────────────────────
function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      <Route path="/auth/callback" element={<AuthCallback />} />

      {/* Tenant HRMS workspace */}
      <Route
        path="/dashboard/*"
        element={
          <ProtectedRoute>
            <AppLayout>
              <ErrorBoundary>
                <Suspense fallback={<PageLoader />}>
                  <Routes>
                    <Route index                element={<DashboardPage />} />
                    <Route path="employees"     element={<EmployeesPage />} />
                    <Route path="employees/:id" element={<DashboardPage />} />
                    <Route path="departments"   element={<DepartmentsPage />} />
                    <Route path="branches"      element={<BranchesPage />} />
                    <Route path="designations"  element={<DesignationsPage />} />
                    <Route path="teams"         element={<TeamsPage />} />
                    <Route path="company"       element={<CompanyPage />} />
                    <Route path="assets"        element={<AssetsPage />} />
                    <Route path="documents"     element={<DocumentsPage />} />
                    <Route path="transfers"     element={<TransfersPage />} />
                    <Route path="promotions"    element={<PromotionsPage />} />
                    <Route path="probation"     element={<ProbationPage />} />
                    <Route path="onboarding"    element={<OnboardingPage />} />
                    <Route path="offboarding"   element={<OffboardingPage />} />
                    <Route path="skills"        element={<SkillsPage />} />
                    <Route path="users"         element={<UsersPage />} />
                    <Route path="notifications" element={<NotificationsPage />} />
                    <Route path="reports"       element={<ReportsPage />} />
                    <Route path="roles"         element={<RolesPage />} />
                    <Route path="audit"         element={<AuditPage />} />
                    <Route path="profile"       element={<ProfilePage />} />
                    <Route path="*"             element={<Navigate to="/" replace />} />
                  </Routes>
                </Suspense>
              </ErrorBoundary>
            </AppLayout>
          </ProtectedRoute>
        }
      />

      {/* Platform Admin workspace */}
      <Route
        path="/platform/*"
        element={
          <PlatformRoute>
            <PlatformLayout>
              <ErrorBoundary>
                <Suspense fallback={<PageLoader />}>
                  <Routes>
                    <Route index                element={<Navigate to={PLATFORM_ROUTES.DASHBOARD} replace />} />
                    <Route path="dashboard"     element={<PlatformDashboardPage />} />
                    <Route path="tenants"       element={<TenantsPage />} />
                    <Route path="approvals"     element={<TenantApprovalsPage />} />
                    <Route path="employees"     element={<PlatformEmployeesPage />} />
                    <Route path="assignments"   element={<CustomerAssignmentsPage />} />
                    <Route path="services"      element={<PlatformServicesPage />} />
                    <Route path="subscriptions" element={<SubscriptionsPage />} />
                    <Route path="modules"       element={<ModuleManagementPage />} />
                    <Route path="support"       element={<SupportTicketsPage />} />
                    <Route path="audit"         element={<PlatformAuditPage />} />
                    <Route path="*"             element={<Navigate to={PLATFORM_ROUTES.DASHBOARD} replace />} />
                  </Routes>
                </Suspense>
              </ErrorBoundary>
            </PlatformLayout>
          </PlatformRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

// ─── App root ─────────────────────────────────────────────────────────────────
export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <LanguageProvider>
            <ErrorBoundary>
              <AppRoutes />
            </ErrorBoundary>
            <Toaster position="top-right" toastOptions={{ duration: 3000 }} />
          </LanguageProvider>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
