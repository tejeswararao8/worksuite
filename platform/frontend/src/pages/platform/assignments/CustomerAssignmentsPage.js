import { useTranslation } from 'react-i18next';
import { PageHeader, EmptyState } from '../../../components/ui';
import { UserCheck } from 'lucide-react';
export default function CustomerAssignmentsPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <PageHeader title={t('platform.nav.assignments')} />
      <EmptyState icon={UserCheck} title={t('platform.stub.title')} description={t('platform.stub.description')} />
    </div>
  );
}
