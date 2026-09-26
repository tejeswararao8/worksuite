import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LanguageProvider } from './context/LanguageContext';
import AppLayout from './components/layout/AppLayout';
import { ROUTES } from './constants/routes';

import LoginPage         from './pages/auth/LoginPage';
import DashboardPage     from './pages/dashboard/DashboardPage';
import EmployeesPage     from './pages/employees/EmployeesPage';
import DepartmentsPage   from './pages/departments/DepartmentsPage';
import BranchesPage      from './pages/branches/BranchesPage';
import DesignationsPage  from './pages/designations/DesignationsPage';
import TeamsPage         from './pages/teams/TeamsPage';
import CompanyPage       from './pages/company/CompanyPage';
import AssetsPage        from './pages/assets/AssetsPage';
import DocumentsPage     from './pages/documents/DocumentsPage';
import TransfersPage     from './pages/transfers/TransfersPage';
import PromotionsPage    from './pages/promotions/PromotionsPage';
import ProbationPage     from './pages/probation/ProbationPage';
import OnboardingPage    from './pages/onboarding/OnboardingPage';
import OffboardingPage   from './pages/offboarding/OffboardingPage';
import SkillsPage        from './pages/skills/SkillsPage';
import UsersPage         from './pages/users/UsersPage';
import NotificationsPage from './pages/notifications/NotificationsPage';
import ReportsPage       from './pages/reports/ReportsPage';
import RolesPage         from './pages/roles/RolesPage';
import AuditPage         from './pages/audit/AuditPage';
import ProfilePage       from './pages/profile/ProfilePage';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 60000 } },
});

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to={ROUTES.LOGIN} replace />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <AppLayout>
              <Routes>
                <Route path="/dashboard"    element={<DashboardPage />} />
                <Route path="/employees"    element={<EmployeesPage />} />
                <Route path="/departments"  element={<DepartmentsPage />} />
                <Route path="/branches"     element={<BranchesPage />} />
                <Route path="/designations" element={<DesignationsPage />} />
                <Route path="/teams"        element={<TeamsPage />} />
                <Route path="/company"      element={<CompanyPage />} />
                <Route path="/assets"       element={<AssetsPage />} />
                <Route path="/documents"    element={<DocumentsPage />} />
                <Route path="/transfers"    element={<TransfersPage />} />
                <Route path="/promotions"   element={<PromotionsPage />} />
                <Route path="/probation"    element={<ProbationPage />} />
                <Route path="/onboarding"   element={<OnboardingPage />} />
                <Route path="/offboarding"  element={<OffboardingPage />} />
                <Route path="/skills"       element={<SkillsPage />} />
                <Route path="/users"        element={<UsersPage />} />
                <Route path="/notifications"element={<NotificationsPage />} />
                <Route path="/reports"      element={<ReportsPage />} />
                <Route path="/roles"        element={<RolesPage />} />
                <Route path="/audit"        element={<AuditPage />} />
                <Route path="/profile"      element={<ProfilePage />} />
                <Route path="*"             element={<Navigate to={ROUTES.DASHBOARD} replace />} />
              </Routes>
            </AppLayout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <LanguageProvider>
            <AppRoutes />
            <Toaster position="top-right" toastOptions={{ duration: 3000 }} />
          </LanguageProvider>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
