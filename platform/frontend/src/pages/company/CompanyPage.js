import { useQuery, useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { companyApi } from '../../api/services';
import { useAuth } from '../../context/AuthContext';
import { Button, Input, Badge, Spinner } from '../../components/ui';
import { toastSuccess, toastError } from '../../utils/errorHandler';

export default function CompanyPage() {
  const { t } = useTranslation();
  const { user } = useAuth();

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['company', user?.companyId],
    queryFn: () => companyApi.getById(user?.companyId),
    enabled: !!user?.companyId,
  });

  const company = data?.data?.data;

  const { register, handleSubmit, formState: { isSubmitting } } = useForm({ values: company });

  const updateMutation = useMutation({
    mutationFn: (formData) => companyApi.update(user.companyId, formData),
    onSuccess: () => { refetch(); toastSuccess(t('common.updateSuccess', { entity: t('company.entity') })); },
    onError: toastError,
  });

  if (isLoading) return <div className="flex justify-center items-center h-64"><Spinner size="lg" /></div>;

  return (
    <div className="max-w-2xl space-y-6">
      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <div className="flex items-center gap-4 mb-6">
          {company?.logoUrl
            ? <img src={company.logoUrl} alt="logo" className="w-16 h-16 rounded-xl object-cover" />
            : <div className="w-16 h-16 bg-indigo-100 rounded-xl flex items-center justify-center text-indigo-600 text-2xl font-bold">{company?.name?.[0]}</div>
          }
          <div>
            <h2 className="text-lg font-bold text-slate-800">{company?.name}</h2>
            <Badge label={company?.status} color={company?.status === 'ACTIVE' ? 'green' : 'red'} />
          </div>
        </div>

        <form onSubmit={handleSubmit((d) => updateMutation.mutate(d))} className="grid grid-cols-2 gap-4">
          <Input label={t('company.name')}               {...register('name')} />
          <Input label={t('company.legalName')}          {...register('legalName')} />
          <Input label={t('company.email')} type="email" {...register('email')} />
          <Input label={t('company.phone')}              {...register('phone')} />
          <Input label={t('company.website')}            {...register('website')} />
          <Input label={t('company.industry')}           {...register('industry')} />
          <Input label={t('company.city')}               {...register('city')} />
          <Input label={t('company.country')}            {...register('country')} />
          <div className="col-span-2">
            <Input label={t('company.address')}          {...register('address')} />
          </div>
          <div className="col-span-2 flex justify-end">
            <Button type="submit" disabled={isSubmitting}>{t('common.save')}</Button>
          </div>
        </form>
      </div>
    </div>
  );
}
