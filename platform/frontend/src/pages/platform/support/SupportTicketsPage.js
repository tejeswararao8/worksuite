import { useTranslation } from 'react-i18next';
import { PageHeader, EmptyState } from '../../../components/ui';
import { LifeBuoy } from 'lucide-react';
export default function SupportTicketsPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <PageHeader title={t('platform.nav.support')} />
      <EmptyState icon={LifeBuoy} title={t('platform.stub.title')} description={t('platform.stub.description')} />
    </div>
  );
}
