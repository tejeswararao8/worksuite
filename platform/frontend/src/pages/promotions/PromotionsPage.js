import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { promotionApi } from '../../api/services';
import { Button, Table, Badge, Modal, Input, Select, Pagination, ConfirmDialog } from '../../components/ui';
import { Plus, CheckCircle, XCircle } from 'lucide-react';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { formatDate } from '../../utils/dateUtils';
import { QUERY_KEYS } from '../../constants/queryKeys';
import { APPROVAL_STATUS_COLOR } from '../../constants/enums';
import { useReferenceData } from '../../hooks/useReferenceData';

export default function PromotionsPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [initiateOpen, setInitiateOpen] = useState(false);
  const [rejectId, setRejectId] = useState(null);
  const [approveId, setApproveId] = useState(null);
  const { designations } = useReferenceData();

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.PROMOTIONS(page),
    queryFn: () => promotionApi.getAll({ page, size: 20 }),
  });

  const promotions = data?.data?.data?.content || data?.data?.data || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  const invalidate = () => qc.invalidateQueries({ queryKey: ['promotions'] });

  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm();
  const { register: regRej, handleSubmit: handleRej, reset: resetRej } = useForm();

  const initiateMutation = useMutation({ mutationFn: promotionApi.initiate, onSuccess: () => { invalidate(); toastSuccess(t('common.createSuccess', { entity: t('promotion.entity') })); setInitiateOpen(false); reset(); }, onError: toastError });
  const approveMutation  = useMutation({ mutationFn: promotionApi.approve,  onSuccess: () => { invalidate(); toastSuccess('Promotion approved'); setApproveId(null); }, onError: toastError });
  const rejectMutation   = useMutation({ mutationFn: ({ id, data }) => promotionApi.reject(id, data), onSuccess: () => { invalidate(); toastSuccess('Promotion rejected'); setRejectId(null); resetRej(); }, onError: toastError });

  const columns = [
    { key: 'employeeCode',    label: t('employee.code') },
    { key: 'fromDesignation', label: t('promotion.fromDesignation') },
    { key: 'toDesignation',   label: t('promotion.toDesignation') },
    { key: 'effectiveDate',   label: t('promotion.effectiveDate'), render: (r) => formatDate(r.effectiveDate) },
    { key: 'status',          label: t('common.status'), render: (r) => <Badge label={r.status} color={APPROVAL_STATUS_COLOR[r.status] || 'gray'} /> },
    {
      key: 'actions', label: t('common.actions'), render: (r) => r.status === 'PENDING' ? (
        <div className="flex gap-1">
          <button onClick={() => setApproveId(r.id)} className="p-1.5 rounded hover:bg-green-50 text-green-500"><CheckCircle size={14} /></button>
          <button onClick={() => setRejectId(r.id)}  className="p-1.5 rounded hover:bg-red-50 text-red-400"><XCircle size={14} /></button>
        </div>
      ) : null,
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button onClick={() => setInitiateOpen(true)}><Plus size={16} /> {t('common.add')} {t('promotion.entity')}</Button>
      </div>
      <Table columns={columns} data={promotions} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal open={initiateOpen} onClose={() => { setInitiateOpen(false); reset(); }} title={`Initiate ${t('promotion.entity')}`}>
        <form onSubmit={handleSubmit((d) => initiateMutation.mutate(d))} className="space-y-4">
          <Input label="Employee ID" {...register('employeeId', { required: t('common.required') })} />
          <Select label={t('promotion.toDesignation')} {...register('toDesignationId', { required: t('common.required') })}>
            <option value="">Select</option>
            {designations.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </Select>
          <Input label={t('promotion.effectiveDate')} type="date" {...register('effectiveDate', { required: t('common.required') })} />
          <Input label="Reason" {...register('reason')} />
          <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={() => setInitiateOpen(false)}>{t('common.cancel')}</Button><Button type="submit" disabled={isSubmitting}>{t('common.create')}</Button></div>
        </form>
      </Modal>

      <Modal open={!!rejectId} onClose={() => { setRejectId(null); resetRej(); }} title="Reject Promotion">
        <form onSubmit={handleRej((d) => rejectMutation.mutate({ id: rejectId, data: d }))} className="space-y-4">
          <Input label="Remarks" {...regRej('remarks', { required: t('common.required') })} />
          <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={() => setRejectId(null)}>{t('common.cancel')}</Button><Button type="submit" variant="danger">Reject</Button></div>
        </form>
      </Modal>

      <ConfirmDialog open={!!approveId} message="Approve this promotion?" onConfirm={() => approveMutation.mutate(approveId)} onCancel={() => setApproveId(null)} />
    </div>
  );
}
