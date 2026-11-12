import { useTranslation } from 'react-i18next';
import CrudPage from '../../components/ui/CrudPage';
import { departmentApi } from '../../api/services';
import { Input } from '../../components/ui';
import { QUERY_KEYS } from '../../constants/queryKeys';

function FormFields({ register, errors }) {
  const { t } = useTranslation();
  return (
    <>
      <Input label={t('common.name')}        error={errors.name?.message}  {...register('name',  { required: t('common.required') })} />
      <Input label={t('common.code')}        error={errors.code?.message}  {...register('code',  { required: t('common.required') })} />
      <Input label={t('common.description')}                               {...register('description')} />
    </>
  );
}

export default function DepartmentsPage() {
  const { t } = useTranslation();
  return (
    <CrudPage
      queryKey={QUERY_KEYS.DEPARTMENTS(0).slice(0, 1)}
      api={departmentApi}
      columns={[
        { key: 'name',        label: t('common.name') },
        { key: 'code',        label: t('common.code') },
        { key: 'description', label: t('common.description') },
      ]}
      FormFields={FormFields}
      entityI18nKey="department.entity"
    />
  );
}
