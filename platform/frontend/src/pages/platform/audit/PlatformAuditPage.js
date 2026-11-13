import { useTranslation } from 'react-i18next';
import { PageHeader, EmptyState } from '../../../components/ui';
import { ScrollText } from 'lucide-react';
export default function PlatformAuditPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <PageHeader title={t('platform.nav.audit')} />
      <EmptyState icon={ScrollText} title={t('platform.stub.title')} description={t('platform.stub.description')} />
    </div>
  );
}
