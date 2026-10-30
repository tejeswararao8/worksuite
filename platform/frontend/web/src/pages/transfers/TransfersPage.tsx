import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { Plus, CheckCircle, XCircle } from 'lucide-react';
import { useTransfers, useReferenceData } from '../../../../shared/hooks';
import { APPROVAL_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import type { Transfer } from '../../../../shared/types';
import { Button, Input, Select, Modal, Table, Badge, Pagination, PageHeader, Textarea } from '../../components/ui';
import type { Column } from '../../components/ui';

const toastService = { success: toast.success, error: toast.error };

export default function TransfersPage() {
  const { t } = useTranslation();
  const { transfers, totalPages, isLoading, page, setPage, statusFilter, setStatus, createMutation, approveMutation, rejectMutation } = useTransfers(toastService);
  const { departments, branches } = useReferenceData();

  const [createOpen, setCreateOpen] = useState(false);
  const [approveId, setApproveId]   = useState<string | null>(null);
  const [rejectId, setRejectId]     = useState<string | null>(null);

  const { register: regCreate, handleSubmit: handleCreate, reset: resetCreate, formState: { isSubmitting: createSubmitting } } = useForm();
  const { register: regApprove, handleSubmit: handleApprove, reset: resetApprove } = useForm();
  const { register: regReject,  handleSubmit: handleReject,  reset: resetReject  } = useForm();

  const columns: Column<Transfer>[] = [
    { key: 'employeeName',      label: 'Employee',   render: (r) => `${r.employeeName || ''} (${r.employeeCode || ''})` },
    { key: 'transferType',      label: 'Type',       render: (r) => <Badge label={r.transferType} color="blue" /> },
    { key: 'from',              label: 'From → To',  render: (r) => `${r.fromDepartmentName || r.fromBranchName || '—'} → ${r.toDepartmentName || r.toBranchName || '—'}` },
    { key: 'effectiveDate',     label: 'Effective',  render: (r) => formatDate(r.effectiveDate) },
    { key: 'status',            label: 'Status',     render: (r) => <Badge label={r.status} color={APPROVAL_STATUS_COLOR[r.status] as never ?? 'gray'} /> },
    {
      key: 'actions', label: t('common.actions'),
      render: (r) => r.status === 'PENDING' ? (
        <div className="flex gap-1">
          <button onClick={() => setApproveId(r.id)} className="p-1.5 rounded hover:bg-green-50 text-green-500" title="Approve"><CheckCircle size={14} /></button>
          <button onClick={() => setRejectId(r.id)}  className="p-1.5 rounded hover:bg-red-50 text-red-400"   title="Reject"><XCircle size={14} /></button>
        </div>
      ) : null,
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader title={t('nav.transfers')} action={<Button onClick={() => setCreateOpen(true)}><Plus size={16} />New Transfer</Button>} />

      <div className="flex gap-3 bg-white p-4 rounded-xl border border-slate-200">
        <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white" value={statusFilter} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All Statuses</option>
          {['PENDING','APPROVED','REJECTED','COMPLETED'].map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      <Table columns={columns} data={transfers} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      {/* Create Transfer */}
      <Modal open={createOpen} onClose={() => { setCreateOpen(false); resetCreate(); }} title="New Transfer Request" size="lg">
        <form onSubmit={handleCreate((d) => createMutation.mutate(d, { onSuccess: () => { setCreateOpen(false); resetCreate(); } }))} className="grid grid-cols-2 gap-4">
          <Input label="Employee ID" {...regCreate('employeeId', { required: true })} />
          <Select label="Transfer Type" {...regCreate('transferType', { required: true })}>
            <option value="">Select Type</option>
            <option value="DEPARTMENT">Department</option>
            <option value="BRANCH">Branch</option>
            <option value="MANAGER">Manager</option>
            <option value="TEAM">Team</option>
          </Select>
          <Select label="To Department" {...regCreate('toDepartmentId')}>
            <option value="">Select</option>
            {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </Select>
          <Select label="To Branch" {...regCreate('toBranchId')}>
            <option value="">Select</option>
            {branches.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
          </Select>
          <Input label="Effective Date" type="date" {...regCreate('effectiveDate', { required: true })} />
          <div className="col-span-2"><Textarea label="Reason" {...regCreate('reason')} /></div>
          <div className="col-span-2 flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setCreateOpen(false)}>Cancel</Button>
            <Button type="submit" loading={createSubmitting}>Submit</Button>
          </div>
        </form>
      </Modal>

      {/* Approve */}
      <Modal open={!!approveId} onClose={() => { setApproveId(null); resetApprove(); }} title="Approve Transfer">
        <form onSubmit={handleApprove((d) => approveMutation.mutate({ id: approveId!, data: d }, { onSuccess: () => { setApproveId(null); resetApprove(); } }))} className="space-y-4">
          <Textarea label="Comments (optional)" {...regApprove('comments')} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setApproveId(null)}>Cancel</Button>
            <Button type="submit" variant="success">Approve</Button>
          </div>
        </form>
      </Modal>

      {/* Reject */}
      <Modal open={!!rejectId} onClose={() => { setRejectId(null); resetReject(); }} title="Reject Transfer">
        <form onSubmit={handleReject((d) => rejectMutation.mutate({ id: rejectId!, data: d }, { onSuccess: () => { setRejectId(null); resetReject(); } }))} className="space-y-4">
          <Textarea label="Reason (required)" {...regReject('reason', { required: true })} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setRejectId(null)}>Cancel</Button>
            <Button type="submit" variant="danger">Reject</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
