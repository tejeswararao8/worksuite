import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { transferApi } from '../../api/services';
import { Button, Table, Badge, Modal, Input, Select, Pagination, ConfirmDialog } from '../../components/ui';
import { Plus, CheckCircle, XCircle } from 'lucide-react';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { formatDate } from '../../utils/dateUtils';
import { QUERY_KEYS } from '../../constants/queryKeys';
import { APPROVAL_STATUS_COLOR } from '../../constants/enums';
import { useReferenceData } from '../../hooks/useReferenceData';

export default function TransfersPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [initiateOpen, setInitiateOpen] = useState(false);
  const [rejectId, setRejectId] = useState(null);
  const [approveId, setApproveId] = useState(null);
  const { departments, branches } = useReferenceData();

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.TRANSFERS(page),
    queryFn: () => transferApi.getAll({ page, size: 20 }),
  });

  const transfers  = data?.data?.data?.content || data?.data?.data || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  const invalidate = () => qc.invalidateQueries({ queryKey: ['transfers'] });

  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm();
  const { register: regRej, handleSubmit: handleRej, reset: resetRej } = useForm();

  const initiateMutation = useMutation({ mutationFn: transferApi.initiate, onSuccess: () => { invalidate(); toastSuccess(t('common.createSuccess', { entity: t('transfer.entity') })); setInitiateOpen(false); reset(); }, onError: toastError });
  const approveMutation  = useMutation({ mutationFn: transferApi.approve,  onSuccess: () => { invalidate(); toastSuccess('Transfer approved'); setApproveId(null); }, onError: toastError });
  const rejectMutation   = useMutation({ mutationFn: ({ id, data }) => transferApi.reject(id, data), onSuccess: () => { invalidate(); toastSuccess('Transfer rejected'); setRejectId(null); resetRej(); }, onError: toastError });

  const columns = [
    { key: 'employeeCode',   label: t('employee.code') },
    { key: 'transferType',   label: 'Type', render: (r) => <Badge label={r.transferType} color="indigo" /> },
    { key: 'effectiveDate',  label: t('transfer.effectiveDate'), render: (r) => formatDate(r.effectiveDate) },
    { key: 'status',         label: t('common.status'), render: (r) => <Badge label={r.status} color={APPROVAL_STATUS_COLOR[r.status] || 'gray'} /> },
    {
      key: 'actions', label: t('common.actions'), render: (r) => r.status === 'PENDING' ? (
        <div className="flex gap-1">
          <button onClick={() => setApproveId(r.id)} className="p-1.5 rounded hover:bg-green-50 text-green-500" title="Approve"><CheckCircle size={14} /></button>
          <button onClick={() => setRejectId(r.id)}  className="p-1.5 rounded hover:bg-red-50 text-red-400"   title="Reject"><XCircle size={14} /></button>
        </div>
      ) : null,
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button onClick={() => setInitiateOpen(true)}><Plus size={16} /> {t('common.add')} {t('transfer.entity')}</Button>
      </div>
      <Table columns={columns} data={transfers} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal open={initiateOpen} onClose={() => { setInitiateOpen(false); reset(); }} title={`Initiate ${t('transfer.entity')}`} size="lg">
        <form onSubmit={handleSubmit((d) => initiateMutation.mutate(d))} className="grid grid-cols-2 gap-4">
          <Input label="Employee ID" {...register('employeeId', { required: t('common.required') })} />
          <Select label="Transfer Type" {...register('transferType', { required: t('common.required') })}>
            <option value="">Select</option>
            <option value="DEPARTMENT">Department</option>
            <option value="BRANCH">Branch</option>
            <option value="MANAGER">Manager</option>
            <option value="TEAM">Team</option>
          </Select>
          <Select label={t('transfer.fromDept')} {...register('fromDepartmentId')}>
            <option value="">Select</option>
            {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </Select>
          <Select label={t('transfer.toDept')} {...register('toDepartmentId')}>
            <option value="">Select</option>
            {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </Select>
          <Select label={t('transfer.fromBranch')} {...register('fromBranchId')}>
            <option value="">Select</option>
            {branches.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
          </Select>
          <Select label={t('transfer.toBranch')} {...register('toBranchId')}>
            <option value="">Select</option>
            {branches.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
          </Select>
          <Input label={t('transfer.effectiveDate')} type="date" {...register('effectiveDate', { required: t('common.required') })} />
          <Input label="Reason" {...register('reason')} />
          <div className="col-span-2 flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setInitiateOpen(false)}>{t('common.cancel')}</Button>
            <Button type="submit" disabled={isSubmitting}>{t('common.create')}</Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!rejectId} onClose={() => { setRejectId(null); resetRej(); }} title="Reject Transfer">
        <form onSubmit={handleRej((d) => rejectMutation.mutate({ id: rejectId, data: d }))} className="space-y-4">
          <Input label="Remarks" {...regRej('remarks', { required: t('common.required') })} />
          <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={() => setRejectId(null)}>{t('common.cancel')}</Button><Button type="submit" variant="danger">Reject</Button></div>
        </form>
      </Modal>

      <ConfirmDialog open={!!approveId} message="Approve this transfer request?" onConfirm={() => approveMutation.mutate(approveId)} onCancel={() => setApproveId(null)} />
    </div>
  );
}
