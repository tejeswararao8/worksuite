import { useTranslation } from 'react-i18next';
import { PageHeader, EmptyState } from '../../../components/ui';
import { CheckSquare } from 'lucide-react';
export default function TenantApprovalsPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <PageHeader title={t('platform.nav.approvals')} />
      <EmptyState icon={CheckSquare} title={t('platform.stub.title')} description={t('platform.stub.description')} />
    </div>
  );
}
