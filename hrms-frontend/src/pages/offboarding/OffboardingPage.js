import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import api from '../../api/axios';
import { offboardingApi } from '../../api/services';
import { Button, Table, Badge, Modal, Input, Select, Pagination } from '../../components/ui';
import { Plus } from 'lucide-react';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { formatDate } from '../../utils/dateUtils';
import { APPROVAL_STATUS_COLOR } from '../../constants/enums';

export default function OffboardingPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [initiateOpen, setInitiateOpen] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ['offboarding', page],
    queryFn: () => api.get('/offboarding', { params: { page, size: 20 } }),
  });

  const items = data?.data?.data?.content || data?.data?.data || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  const invalidate = () => qc.invalidateQueries({ queryKey: ['offboarding'] });

  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm();

  const initiateMutation = useMutation({
    mutationFn: ({ employeeId, ...rest }) => offboardingApi.initiate(employeeId, rest),
    onSuccess: () => { invalidate(); toastSuccess('Offboarding initiated'); setInitiateOpen(false); reset(); },
    onError: toastError,
  });

  const completeMutation = useMutation({
    mutationFn: offboardingApi.complete,
    onSuccess: () => { invalidate(); toastSuccess('Offboarding completed'); },
    onError: toastError,
  });

  const columns = [
    { key: 'employeeCode',      label: t('employee.code') },
    { key: 'reason',            label: t('offboarding.reason') },
    { key: 'lastWorkingDate',   label: t('offboarding.lastWorkingDate'), render: (r) => formatDate(r.lastWorkingDate) },
    { key: 'status',            label: t('common.status'), render: (r) => <Badge label={r.status} color={APPROVAL_STATUS_COLOR[r.status] || 'gray'} /> },
    {
      key: 'actions', label: t('common.actions'), render: (r) => (
        <div className="flex gap-1">
          {r.status === 'IN_PROGRESS' && (
            <Button size="sm" onClick={() => completeMutation.mutate(r.employeeId)}>{t('offboarding.complete')}</Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button onClick={() => setInitiateOpen(true)}><Plus size={16} /> {t('offboarding.initiate')}</Button>
      </div>
      <Table columns={columns} data={items} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal open={initiateOpen} onClose={() => { setInitiateOpen(false); reset(); }} title={t('offboarding.initiate')}>
        <form onSubmit={handleSubmit((d) => initiateMutation.mutate(d))} className="space-y-4">
          <Input label="Employee ID" {...register('employeeId', { required: t('common.required') })} />
          <Select label={t('offboarding.reason')} {...register('reason', { required: t('common.required') })}>
            <option value="">{t('common.filter')}</option>
            <option value="RESIGNATION">{t('offboarding.resignation')}</option>
            <option value="TERMINATION">{t('offboarding.termination')}</option>
          </Select>
          <Input label={t('offboarding.resignationDate')}    type="date" {...register('resignationDate')} />
          <Input label={t('offboarding.noticePeriodEndDate')} type="date" {...register('noticePeriodEndDate')} />
          <Input label={t('offboarding.lastWorkingDate')}    type="date" {...register('lastWorkingDate', { required: t('common.required') })} />
          <Input label={t('offboarding.remarks')} {...register('remarks')} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setInitiateOpen(false)}>{t('common.cancel')}</Button>
            <Button type="submit" disabled={isSubmitting}>{t('common.create')}</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
