import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LanguageProvider } from './context/LanguageContext';
import AppLayout from './components/layout/AppLayout';
import { ROUTES } from './constants/routes';
import { ErrorBoundary, PageLoader } from './components/ui';

// ─── Eagerly loaded — auth-critical, tiny, always needed ─────────────────────
import HomePage  from './pages/HomePage';
import LoginPage from './pages/auth/LoginPage';

// ─── Lazy-loaded dashboard pages ─────────────────────────────────────────────
const DashboardPage    = lazy(() => import('./pages/dashboard/DashboardPage'));
const EmployeesPage    = lazy(() => import('./pages/employees/EmployeesPage'));
const DepartmentsPage  = lazy(() => import('./pages/departments/DepartmentsPage'));
const BranchesPage     = lazy(() => import('./pages/branches/BranchesPage'));
const DesignationsPage = lazy(() => import('./pages/designations/DesignationsPage'));
const TeamsPage        = lazy(() => import('./pages/teams/TeamsPage'));
const CompanyPage      = lazy(() => import('./pages/company/CompanyPage'));
const AssetsPage       = lazy(() => import('./pages/assets/AssetsPage'));
const DocumentsPage    = lazy(() => import('./pages/documents/DocumentsPage'));
const TransfersPage    = lazy(() => import('./pages/transfers/TransfersPage'));
const PromotionsPage   = lazy(() => import('./pages/promotions/PromotionsPage'));
const ProbationPage    = lazy(() => import('./pages/probation/ProbationPage'));
const OnboardingPage   = lazy(() => import('./pages/onboarding/OnboardingPage'));
const OffboardingPage  = lazy(() => import('./pages/offboarding/OffboardingPage'));
const SkillsPage       = lazy(() => import('./pages/skills/SkillsPage'));
const UsersPage        = lazy(() => import('./pages/users/UsersPage'));
const NotificationsPage = lazy(() => import('./pages/notifications/NotificationsPage'));
const ReportsPage      = lazy(() => import('./pages/reports/ReportsPage'));
const RolesPage        = lazy(() => import('./pages/roles/RolesPage'));
const AuditPage        = lazy(() => import('./pages/audit/AuditPage'));
const ProfilePage      = lazy(() => import('./pages/profile/ProfilePage'));

// ─── QueryClient ─────────────────────────────────────────────────────────────
// Tiered stale-time strategy:
//   • Default (60 s)  — most paginated lists; fresh enough, avoids hammering.
//   • Reference data  — handled per-query in useReferenceData (5 min).
//   • Notifications   — handled per-query in useNotifications (poll 30 s).
//   • Dashboard       — inherits default; charts are not real-time critical.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 60_000,        // 60 s global default
      refetchOnWindowFocus: false, // prevents surprise refetches on tab switch
    },
  },
});

// ─── Protected route ─────────────────────────────────────────────────────────
function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to={ROUTES.LOGIN} replace />;
}

// ─── Routes ──────────────────────────────────────────────────────────────────
function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      <Route
        path="/dashboard/*"
        element={
          <ProtectedRoute>
            <AppLayout>
              {/* Single Suspense boundary wraps all lazy dashboard pages.
                  ErrorBoundary catches lazy-import network failures and
                  renders a retry button — no stack trace exposed. */}
              <ErrorBoundary>
                <Suspense fallback={<PageLoader />}>
                  <Routes>
                    <Route index                    element={<DashboardPage />} />
                    <Route path="employees"         element={<EmployeesPage />} />
                    <Route path="employees/:id"     element={<DashboardPage />} />
                    <Route path="departments"       element={<DepartmentsPage />} />
                    <Route path="branches"          element={<BranchesPage />} />
                    <Route path="designations"      element={<DesignationsPage />} />
                    <Route path="teams"             element={<TeamsPage />} />
                    <Route path="company"           element={<CompanyPage />} />
                    <Route path="assets"            element={<AssetsPage />} />
                    <Route path="documents"         element={<DocumentsPage />} />
                    <Route path="transfers"         element={<TransfersPage />} />
                    <Route path="promotions"        element={<PromotionsPage />} />
                    <Route path="probation"         element={<ProbationPage />} />
                    <Route path="onboarding"        element={<OnboardingPage />} />
                    <Route path="offboarding"       element={<OffboardingPage />} />
                    <Route path="skills"            element={<SkillsPage />} />
                    <Route path="users"             element={<UsersPage />} />
                    <Route path="notifications"     element={<NotificationsPage />} />
                    <Route path="reports"           element={<ReportsPage />} />
                    <Route path="roles"             element={<RolesPage />} />
                    <Route path="audit"             element={<AuditPage />} />
                    <Route path="profile"           element={<ProfilePage />} />
                    <Route path="*"                 element={<Navigate to="/" replace />} />
                  </Routes>
                </Suspense>
              </ErrorBoundary>
            </AppLayout>
          </ProtectedRoute>
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
