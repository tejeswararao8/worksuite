import { useTranslation } from 'react-i18next';
import CrudPage from '../../components/ui/CrudPage';
import { branchApi } from '../../api/services';
import { Input } from '../../components/ui';
import { QUERY_KEYS } from '../../constants/queryKeys';

function FormFields({ register, errors }) {
  const { t } = useTranslation();
  return (
    <>
      <Input label={t('branch.name')}    error={errors.name?.message} {...register('name',    { required: t('common.required') })} />
      <Input label={t('branch.code')}    error={errors.code?.message} {...register('code',    { required: t('common.required') })} />
      <Input label={t('branch.city')}    {...register('city')} />
      <Input label={t('branch.country')} {...register('country')} />
      <Input label={t('branch.address')} {...register('address')} />
    </>
  );
}

export default function BranchesPage() {
  return (
    <CrudPage
      queryKey={QUERY_KEYS.BRANCHES(0).slice(0, 1)}
      api={branchApi}
      columns={[
        { key: 'name',    label: 'Name' },
        { key: 'code',    label: 'Code' },
        { key: 'city',    label: 'City' },
        { key: 'country', label: 'Country' },
      ]}
      FormFields={FormFields}
      entityI18nKey="branch.entity"
    />
  );
}
