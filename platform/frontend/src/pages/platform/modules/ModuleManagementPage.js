import { useTranslation } from 'react-i18next';
import { PageHeader, EmptyState } from '../../../components/ui';
import { Puzzle } from 'lucide-react';
export default function ModuleManagementPage() {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <PageHeader title={t('platform.nav.modules')} />
      <EmptyState icon={Puzzle} title={t('platform.stub.title')} description={t('platform.stub.description')} />
    </div>
  );
}
