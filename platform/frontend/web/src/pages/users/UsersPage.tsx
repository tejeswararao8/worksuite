import { useTranslation } from 'react-i18next';
import { UserCog } from 'lucide-react';
import { PageHeader, EmptyState } from '../../components/ui';

export default function UsersPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-4">
      <PageHeader title={t('nav.users')} subtitle="Manage system users and their access" />
      <div className="bg-white rounded-xl border border-slate-200">
        <EmptyState
          icon={UserCog}
          title="User Management"
          description="User management is handled through Keycloak. Users are automatically created when employees are onboarded."
          action={
            <a href="http://localhost:8080" target="_blank" rel="noreferrer" className="text-sm text-indigo-600 hover:underline">
              Open Keycloak Admin Console →
            </a>
          }
        />
      </div>
    </div>
  );
}
