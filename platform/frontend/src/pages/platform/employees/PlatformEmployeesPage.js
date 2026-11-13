import { useTranslation } from 'react-i18next';
import { PageHeader, EmptyState } from '../../../components/ui';
import { Users } from 'lucide-react';
export default function PlatformEmployeesPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <PageHeader title={t('platform.nav.employees')} />
      <EmptyState icon={Users} title={t('platform.stub.title')} description={t('platform.stub.description')} />
    </div>
  );
}
