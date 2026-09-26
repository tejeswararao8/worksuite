import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import api from '../../api/axios';
import { probationApi } from '../../api/services';
import { Button, Table, Badge, Modal, Input, Pagination, ConfirmDialog } from '../../components/ui';
import { Plus, CheckCircle, Clock, XCircle } from 'lucide-react';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { formatDate } from '../../utils/dateUtils';
import { APPROVAL_STATUS_COLOR } from '../../constants/enums';

export default function ProbationPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [initiateOpen, setInitiateOpen] = useState(false);
  const [extendId, setExtendId] = useState(null);
  const [rejectId, setRejectId] = useState(null);
  const [confirmId, setConfirmId] = useState(null);

  const { data, isLoading } = useQuery({
    queryKey: ['probations', page],
    queryFn: () => api.get('/probations', { params: { page, size: 20 } }),
  });

  const probations = data?.data?.data?.content || data?.data?.data || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  const invalidate = () => qc.invalidateQueries({ queryKey: ['probations'] });

  const { register: regInit, handleSubmit: handleInit, reset: resetInit, formState: { isSubmitting: initSubmitting } } = useForm();
  const { register: regExt,  handleSubmit: handleExt,  reset: resetExt  } = useForm();
  const { register: regRej,  handleSubmit: handleRej,  reset: resetRej  } = useForm();

  const initiateMutation = useMutation({ mutationFn: probationApi.initiate, onSuccess: () => { invalidate(); toastSuccess(t('probation.entity') + ' initiated'); setInitiateOpen(false); resetInit(); }, onError: toastError });
  const confirmMutation  = useMutation({ mutationFn: probationApi.confirm,  onSuccess: () => { invalidate(); toastSuccess(t('probation.confirmSuccess')); setConfirmId(null); }, onError: toastError });
  const extendMutation   = useMutation({ mutationFn: ({ id, data }) => probationApi.extend(id, data),  onSuccess: () => { invalidate(); toastSuccess(t('probation.extendSuccess')); setExtendId(null); resetExt(); }, onError: toastError });
  const rejectMutation   = useMutation({ mutationFn: ({ id, data }) => probationApi.reject(id, data),  onSuccess: () => { invalidate(); toastSuccess(t('probation.rejectSuccess')); setRejectId(null); resetRej(); }, onError: toastError });

  const columns = [
    { key: 'employeeCode', label: t('employee.code') },
    { key: 'startDate',    label: t('probation.startDate'), render: (r) => formatDate(r.startDate) },
    { key: 'endDate',      label: t('probation.endDate'),   render: (r) => formatDate(r.endDate) },
    { key: 'status',       label: t('common.status'), render: (r) => <Badge label={r.status} color={APPROVAL_STATUS_COLOR[r.status] || 'gray'} /> },
    {
      key: 'actions', label: t('common.actions'), render: (r) => r.status === 'PENDING' || r.status === 'IN_PROGRESS' ? (
        <div className="flex gap-1">
          <button onClick={() => setConfirmId(r.id)} className="p-1.5 rounded hover:bg-green-50 text-green-500" title={t('probation.confirm')}><CheckCircle size={14} /></button>
          <button onClick={() => setExtendId(r.id)}  className="p-1.5 rounded hover:bg-yellow-50 text-yellow-500" title={t('probation.extend')}><Clock size={14} /></button>
          <button onClick={() => setRejectId(r.id)}  className="p-1.5 rounded hover:bg-red-50 text-red-400" title={t('probation.reject')}><XCircle size={14} /></button>
        </div>
      ) : null,
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button onClick={() => setInitiateOpen(true)}><Plus size={16} /> {t('probation.entity')}</Button>
      </div>
      <Table columns={columns} data={probations} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      {/* Initiate */}
      <Modal open={initiateOpen} onClose={() => { setInitiateOpen(false); resetInit(); }} title={`Initiate ${t('probation.entity')}`}>
        <form onSubmit={handleInit((d) => initiateMutation.mutate(d))} className="space-y-4">
          <Input label="Employee ID" {...regInit('employeeId', { required: t('common.required') })} />
          <Input label={t('probation.startDate')} type="date" {...regInit('startDate', { required: t('common.required') })} />
          <Input label={t('probation.endDate')}   type="date" {...regInit('endDate',   { required: t('common.required') })} />
          <Input label={t('probation.remarks')}   {...regInit('remarks')} />
          <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={() => setInitiateOpen(false)}>{t('common.cancel')}</Button><Button type="submit" disabled={initSubmitting}>{t('common.create')}</Button></div>
        </form>
      </Modal>

      {/* Extend */}
      <Modal open={!!extendId} onClose={() => { setExtendId(null); resetExt(); }} title={t('probation.extend')}>
        <form onSubmit={handleExt((d) => extendMutation.mutate({ id: extendId, data: d }))} className="space-y-4">
          <Input label={t('probation.extendedEndDate')} type="date" {...regExt('extendedEndDate', { required: t('common.required') })} />
          <Input label={t('probation.remarks')} {...regExt('remarks')} />
          <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={() => setExtendId(null)}>{t('common.cancel')}</Button><Button type="submit">{t('common.save')}</Button></div>
        </form>
      </Modal>

      {/* Reject */}
      <Modal open={!!rejectId} onClose={() => { setRejectId(null); resetRej(); }} title={t('probation.reject')}>
        <form onSubmit={handleRej((d) => rejectMutation.mutate({ id: rejectId, data: d }))} className="space-y-4">
          <Input label={t('probation.remarks')} {...regRej('remarks', { required: t('common.required') })} />
          <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={() => setRejectId(null)}>{t('common.cancel')}</Button><Button type="submit" variant="danger">{t('probation.reject')}</Button></div>
        </form>
      </Modal>

      <ConfirmDialog open={!!confirmId} message={`Confirm this probation?`} onConfirm={() => confirmMutation.mutate(confirmId)} onCancel={() => setConfirmId(null)} />
    </div>
  );
}
