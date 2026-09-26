import { useTranslation } from 'react-i18next';
import CrudPage from '../../components/ui/CrudPage';
import { teamApi } from '../../api/services';
import { Input, Select } from '../../components/ui';
import { QUERY_KEYS } from '../../constants/queryKeys';
import { useReferenceData } from '../../hooks/useReferenceData';

function FormFields({ register, errors }) {
  const { t } = useTranslation();
  const { departments } = useReferenceData();
  return (
    <>
      <Input label={t('team.name')} error={errors.name?.message} {...register('name', { required: t('common.required') })} />
      <Select label={t('team.department')} error={errors.departmentId?.message} {...register('departmentId', { required: t('common.required') })}>
        <option value="">{t('common.filter')}</option>
        {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
      </Select>
      <Input label={t('team.description')} {...register('description')} />
    </>
  );
}

export default function TeamsPage() {
  const { t } = useTranslation();
  return (
    <CrudPage
      queryKey={QUERY_KEYS.TEAMS(0).slice(0, 1)}
      api={teamApi}
      columns={[
        { key: 'name',        label: t('team.name') },
        { key: 'description', label: t('team.description') },
      ]}
      FormFields={FormFields}
      entityI18nKey="team.entity"
    />
  );
}
