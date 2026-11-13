import { useTranslation } from 'react-i18next';
import { PageHeader, EmptyState } from '../../../components/ui';
import { Package } from 'lucide-react';
export default function PlatformServicesPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <PageHeader title={t('platform.nav.services')} />
      <EmptyState icon={Package} title={t('platform.stub.title')} description={t('platform.stub.description')} />
    </div>
  );
}
