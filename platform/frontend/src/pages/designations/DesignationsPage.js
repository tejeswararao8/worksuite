import { useTranslation } from 'react-i18next';
import CrudPage from '../../components/ui/CrudPage';
import { designationApi } from '../../api/services';
import { Input } from '../../components/ui';
import { QUERY_KEYS } from '../../constants/queryKeys';

function FormFields({ register, errors }) {
  const { t } = useTranslation();
  return (
    <>
      <Input label={t('designation.name')}        error={errors.name?.message} {...register('name', { required: t('common.required') })} />
      <Input label={t('designation.code')}        error={errors.code?.message} {...register('code', { required: t('common.required') })} />
      <Input label={t('designation.level')}       type="number"                {...register('level')} />
      <Input label={t('designation.description')}                              {...register('description')} />
    </>
  );
}

export default function DesignationsPage() {
  return (
    <CrudPage
      queryKey={QUERY_KEYS.DESIGNATIONS(0).slice(0, 1)}
      api={designationApi}
      columns={[
        { key: 'name',  label: 'Name' },
        { key: 'code',  label: 'Code' },
        { key: 'level', label: 'Level' },
      ]}
      FormFields={FormFields}
      entityI18nKey="designation.entity"
    />
  );
}
