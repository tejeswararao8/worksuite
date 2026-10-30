import { useTranslation } from 'react-i18next';
import { Shield } from 'lucide-react';
import { PageHeader, EmptyState } from '../../components/ui';

export default function RolesPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-4">
      <PageHeader title={t('nav.roles')} subtitle="Roles and permissions management" />
      <div className="bg-white rounded-xl border border-slate-200">
        <EmptyState
          icon={Shield}
          title="Roles & Permissions"
          description="Roles are managed in Keycloak. Assign roles to users through the Keycloak admin console. The platform enforces these roles on every API call."
          action={
            <div className="space-y-2 text-sm text-slate-500">
              <p>Available roles: SUPER_ADMIN, HR_ADMIN, HR_MANAGER, EMPLOYEE, FINANCE_ADMIN, MANAGER</p>
              <a href="http://localhost:8080" target="_blank" rel="noreferrer" className="text-indigo-600 hover:underline block">
                Open Keycloak Admin Console →
              </a>
            </div>
          }
        />
      </div>
    </div>
  );
}
