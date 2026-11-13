import { useTranslation } from 'react-i18next';
import { PageHeader, EmptyState } from '../../../components/ui';
import { CreditCard } from 'lucide-react';
export default function SubscriptionsPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <PageHeader title={t('platform.nav.subscriptions')} />
      <EmptyState icon={CreditCard} title={t('platform.stub.title')} description={t('platform.stub.description')} />
    </div>
  );
}
