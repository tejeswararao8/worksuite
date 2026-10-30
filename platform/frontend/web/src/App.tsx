import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Provider as ReduxProvider } from 'react-redux';
import { Toaster } from 'react-hot-toast';
import { I18nextProvider } from 'react-i18next';
import { store } from '../../shared/store';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import AppLayout from './layouts/AppLayout';
import { Spinner } from './components/ui';
import i18n from './i18n';

// ─── Lazy-loaded pages ────────────────────────────────────────────────────────
const LoginPage         = lazy(() => import('./pages/auth/LoginPage'));
const DashboardPage     = lazy(() => import('./pages/dashboard/DashboardPage'));
const EmployeesPage     = lazy(() => import('./pages/employees/EmployeesPage'));
const EmployeeDetailPage= lazy(() => import('./pages/employees/EmployeeDetailPage'));
const DepartmentsPage   = lazy(() => import('./pages/departments/DepartmentsPage'));
const BranchesPage      = lazy(() => import('./pages/branches/BranchesPage'));
const DesignationsPage  = lazy(() => import('./pages/designations/DesignationsPage'));
const TeamsPage         = lazy(() => import('./pages/teams/TeamsPage'));
const TransfersPage     = lazy(() => import('./pages/transfers/TransfersPage'));
const PromotionsPage    = lazy(() => import('./pages/promotions/PromotionsPage'));
const ProbationPage     = lazy(() => import('./pages/probation/ProbationPage'));
const OnboardingPage    = lazy(() => import('./pages/onboarding/OnboardingPage'));
const OffboardingPage   = lazy(() => import('./pages/offboarding/OffboardingPage'));
const AssetsPage        = lazy(() => import('./pages/assets/AssetsPage'));
const SkillsPage        = lazy(() => import('./pages/skills/SkillsPage'));
const ReportsPage       = lazy(() => import('./pages/reports/ReportsPage'));
const NotificationsPage = lazy(() => import('./pages/notifications/NotificationsPage'));
const AuditPage         = lazy(() => import('./pages/audit/AuditPage'));
const ProfilePage       = lazy(() => import('./pages/profile/ProfilePage'));
const UsersPage         = lazy(() => import('./pages/users/UsersPage'));
const RolesPage         = lazy(() => import('./pages/roles/RolesPage'));

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 60_000 } },
});

const PageLoader = () => (
  <div className="flex justify-center items-center h-64">
    <Spinner size="lg" />
  </div>
);

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Suspense fallback={<PageLoader />}><LoginPage /></Suspense>} />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <AppLayout>
              <Suspense fallback={<PageLoader />}>
                <Routes>
                  <Route path="/dashboard"          element={<DashboardPage />} />
                  <Route path="/employees"          element={<EmployeesPage />} />
                  <Route path="/employees/:id"      element={<EmployeeDetailPage />} />
                  <Route path="/departments"        element={<DepartmentsPage />} />
                  <Route path="/branches"           element={<BranchesPage />} />
                  <Route path="/designations"       element={<DesignationsPage />} />
                  <Route path="/teams"              element={<TeamsPage />} />
                  <Route path="/transfers"          element={<TransfersPage />} />
                  <Route path="/promotions"         element={<PromotionsPage />} />
                  <Route path="/probation"          element={<ProbationPage />} />
                  <Route path="/onboarding"         element={<OnboardingPage />} />
                  <Route path="/offboarding"        element={<OffboardingPage />} />
                  <Route path="/assets"             element={<AssetsPage />} />
                  <Route path="/skills"             element={<SkillsPage />} />
                  <Route path="/reports"            element={<ReportsPage />} />
                  <Route path="/notifications"      element={<NotificationsPage />} />
                  <Route path="/audit"              element={<AuditPage />} />
                  <Route path="/profile"            element={<ProfilePage />} />
                  <Route path="/users"              element={<UsersPage />} />
                  <Route path="/roles"              element={<RolesPage />} />
                  <Route path="*"                   element={<Navigate to="/dashboard" replace />} />
                </Routes>
              </Suspense>
            </AppLayout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default function App() {
  return (
    <ReduxProvider store={store}>
      <QueryClientProvider client={queryClient}>
        <I18nextProvider i18n={i18n}>
          <BrowserRouter>
            <AuthProvider>
              <AppRoutes />
              <Toaster position="top-right" toastOptions={{ duration: 3000 }} />
            </AuthProvider>
          </BrowserRouter>
        </I18nextProvider>
      </QueryClientProvider>
    </ReduxProvider>
  );
}
