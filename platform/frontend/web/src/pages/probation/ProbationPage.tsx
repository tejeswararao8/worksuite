import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { CheckCircle, Clock, XCircle } from 'lucide-react';
import { useProbations } from '../../../../shared/hooks';
import { PROBATION_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import type { Probation } from '../../../../shared/types';
import { Button, Input, Modal, Table, Badge, Pagination, ConfirmDialog, PageHeader, ProgressBar, Textarea } from '../../components/ui';
import type { Column } from '../../components/ui';

const toastService = { success: toast.success, error: toast.error };

export default function ProbationPage() {
  const { t } = useTranslation();
  const { probations, totalPages, isLoading, page, setPage, statusFilter, setStatus, confirmMutation, extendMutation, rejectMutation } = useProbations(toastService);

  const [confirmId, setConfirmId] = useState<string | null>(null);
  const [extendId, setExtendId]   = useState<string | null>(null);
  const [rejectId, setRejectId]   = useState<string | null>(null);

  const { register: regExt, handleSubmit: handleExt, reset: resetExt } = useForm();
  const { register: regRej, handleSubmit: handleRej, reset: resetRej } = useForm();
  const { register: regCon, handleSubmit: handleCon, reset: resetCon } = useForm();

  const columns: Column<Probation>[] = [
    { key: 'employeeName', label: 'Employee',   render: (r) => `${r.employeeName || ''} (${r.employeeCode || ''})` },
    { key: 'startDate',    label: 'Start',      render: (r) => formatDate(r.startDate) },
    { key: 'endDate',      label: 'End',        render: (r) => formatDate(r.extendedEndDate || r.endDate) },
    {
      key: 'daysRemaining', label: 'Days Left',
      render: (r) => (
        <div className="w-32">
          <p className="text-xs text-slate-500 mb-1">{r.daysRemaining} days</p>
          <ProgressBar value={r.daysRemaining} max={90} color={r.daysRemaining <= 7 ? 'bg-red-500' : r.daysRemaining <= 30 ? 'bg-yellow-500' : 'bg-green-500'} showLabel={false} />
        </div>
      ),
    },
    { key: 'status', label: 'Status', render: (r) => <Badge label={r.status} color={PROBATION_STATUS_COLOR[r.status] as never ?? 'gray'} /> },
    {
      key: 'actions', label: t('common.actions'),
      render: (r) => (r.status === 'IN_PROGRESS' || r.status === 'EXTENDED') ? (
        <div className="flex gap-1">
          <button onClick={() => setConfirmId(r.id)} className="p-1.5 rounded hover:bg-green-50 text-green-500" title="Confirm"><CheckCircle size={14} /></button>
          <button onClick={() => setExtendId(r.id)}  className="p-1.5 rounded hover:bg-yellow-50 text-yellow-500" title="Extend"><Clock size={14} /></button>
          <button onClick={() => setRejectId(r.id)}  className="p-1.5 rounded hover:bg-red-50 text-red-400" title="Reject"><XCircle size={14} /></button>
        </div>
      ) : null,
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader title={t('nav.probation')} subtitle="Manage employee probation periods" />

      <div className="flex gap-3 bg-white p-4 rounded-xl border border-slate-200">
        <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white" value={statusFilter} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All Statuses</option>
          {['IN_PROGRESS','CONFIRMED','EXTENDED','REJECTED'].map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      <Table columns={columns} data={probations} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      {/* Confirm */}
      <Modal open={!!confirmId} onClose={() => { setConfirmId(null); resetCon(); }} title="Confirm Probation">
        <form onSubmit={handleCon((d) => confirmMutation.mutate({ id: confirmId!, data: d }, { onSuccess: () => { setConfirmId(null); resetCon(); } }))} className="space-y-4">
          <p className="text-sm text-slate-600">Confirming probation will change employee status to <strong>ACTIVE</strong>.</p>
          <Textarea label="Remarks (optional)" {...regCon('remarks')} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setConfirmId(null)}>Cancel</Button>
            <Button type="submit" variant="success">Confirm Probation</Button>
          </div>
        </form>
      </Modal>

      {/* Extend */}
      <Modal open={!!extendId} onClose={() => { setExtendId(null); resetExt(); }} title="Extend Probation">
        <form onSubmit={handleExt((d) => extendMutation.mutate({ id: extendId!, data: d }, { onSuccess: () => { setExtendId(null); resetExt(); } }))} className="space-y-4">
          <Input label="New End Date" type="date" {...regExt('newEndDate', { required: true })} />
          <Textarea label="Reason (required)" {...regExt('reason', { required: true })} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setExtendId(null)}>Cancel</Button>
            <Button type="submit">Extend</Button>
          </div>
        </form>
      </Modal>

      {/* Reject */}
      <Modal open={!!rejectId} onClose={() => { setRejectId(null); resetRej(); }} title="Reject Probation">
        <form onSubmit={handleRej((d) => rejectMutation.mutate({ id: rejectId!, data: d }, { onSuccess: () => { setRejectId(null); resetRej(); } }))} className="space-y-4">
          <p className="text-sm text-red-600">⚠ Rejecting probation will change employee status to <strong>TERMINATED</strong>.</p>
          <Textarea label="Reason (required)" {...regRej('reason', { required: true })} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setRejectId(null)}>Cancel</Button>
            <Button type="submit" variant="danger">Reject Probation</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
