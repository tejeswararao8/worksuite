import { useTranslation } from 'react-i18next';
import { PageHeader, EmptyState } from '../../../components/ui';
import { Building2 } from 'lucide-react';
export default function TenantsPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <PageHeader title={t('platform.nav.tenants')} />
      <EmptyState icon={Building2} title={t('platform.stub.title')} description={t('platform.stub.description')} />
    </div>
  );
}
