import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { Plus, Pencil, UserPlus, RotateCcw, Archive } from 'lucide-react';
import { useAssets } from '../../../../shared/hooks';
import { ASSET_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import type { Asset } from '../../../../shared/types';
import { Button, Input, Select, Modal, Table, Badge, Pagination, ConfirmDialog, PageHeader, Textarea } from '../../components/ui';
import type { Column } from '../../components/ui';

const toastService = { success: toast.success, error: toast.error };

export default function AssetsPage() {
  const { t } = useTranslation();
  const { assets, totalPages, isLoading, page, setPage, search, setSearch, statusFilter, setStatus, typeFilter, setType, createMutation, updateMutation, assignMutation, returnMutation, retireMutation } = useAssets(toastService);

  const [createOpen, setCreateOpen] = useState(false);
  const [assignId, setAssignId]     = useState<string | null>(null);
  const [returnId, setReturnId]     = useState<string | null>(null);
  const [retireId, setRetireId]     = useState<string | null>(null);

  const { register: regCreate, handleSubmit: handleCreate, reset: resetCreate, formState: { isSubmitting } } = useForm();
  const { register: regAssign, handleSubmit: handleAssign, reset: resetAssign } = useForm();
  const { register: regReturn, handleSubmit: handleReturn, reset: resetReturn } = useForm();

  const columns: Column<Asset>[] = [
    { key: 'assetCode',              label: 'Code' },
    { key: 'name',                   label: 'Name' },
    { key: 'assetType',              label: 'Type',    render: (r) => <Badge label={r.assetType} color="blue" /> },
    { key: 'serialNumber',           label: 'Serial',  render: (r) => r.serialNumber || '—' },
    { key: 'assignedToEmployeeName', label: 'Assigned To', render: (r) => r.assignedToEmployeeName || '—' },
    { key: 'warrantyExpiry',         label: 'Warranty', render: (r) => (
      <span className={r.warrantyExpiringSoon ? 'text-yellow-600 font-medium' : ''}>
        {formatDate(r.warrantyExpiry)}
        {r.warrantyExpiringSoon && ' ⚠'}
      </span>
    )},
    { key: 'status', label: 'Status', render: (r) => <Badge label={r.status} color={ASSET_STATUS_COLOR[r.status] as never ?? 'gray'} /> },
    {
      key: 'actions', label: t('common.actions'),
      render: (r) => (
        <div className="flex gap-1">
          {r.status === 'AVAILABLE' && <button onClick={() => setAssignId(r.id)} className="p-1.5 rounded hover:bg-indigo-50 text-indigo-500" title="Assign"><UserPlus size={14} /></button>}
          {r.status === 'ASSIGNED'  && <button onClick={() => setReturnId(r.id)} className="p-1.5 rounded hover:bg-green-50 text-green-500" title="Return"><RotateCcw size={14} /></button>}
          {r.status !== 'RETIRED'   && <button onClick={() => setRetireId(r.id)} className="p-1.5 rounded hover:bg-red-50 text-red-400" title="Retire"><Archive size={14} /></button>}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader title={t('nav.assets')} action={<Button onClick={() => setCreateOpen(true)}><Plus size={16} />Add Asset</Button>} />

      <div className="flex flex-wrap gap-3 bg-white p-4 rounded-xl border border-slate-200">
        <input className="border border-slate-300 rounded-lg px-3 py-2 text-sm w-48 focus:ring-2 focus:ring-indigo-500" placeholder="Search assets..." value={search} onChange={(e) => setSearch(e.target.value)} />
        <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white" value={statusFilter} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All Statuses</option>
          {['AVAILABLE','ASSIGNED','IN_REPAIR','RETIRED'].map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
        <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white" value={typeFilter} onChange={(e) => setType(e.target.value)}>
          <option value="">All Types</option>
          {['LAPTOP','DESKTOP','MOBILE','MONITOR','SIM','ACCESS_CARD','OTHER'].map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      <Table columns={columns} data={assets} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      {/* Create Asset */}
      <Modal open={createOpen} onClose={() => { setCreateOpen(false); resetCreate(); }} title="Add Asset" size="lg">
        <form onSubmit={handleCreate((d) => createMutation.mutate(d, { onSuccess: () => { setCreateOpen(false); resetCreate(); } }))} className="grid grid-cols-2 gap-4">
          <Input label="Asset Name" {...regCreate('name', { required: true })} />
          <Select label="Asset Type" {...regCreate('assetType', { required: true })}>
            <option value="">Select Type</option>
            {['LAPTOP','DESKTOP','MOBILE','MONITOR','SIM','ACCESS_CARD','OTHER'].map((s) => <option key={s} value={s}>{s}</option>)}
          </Select>
          <Input label="Serial Number" {...regCreate('serialNumber')} />
          <Input label="Model" {...regCreate('model')} />
          <Input label="Brand" {...regCreate('brand')} />
          <Input label="Purchase Date" type="date" {...regCreate('purchaseDate')} />
          <Input label="Warranty Expiry" type="date" {...regCreate('warrantyExpiry')} />
          <div className="col-span-2"><Textarea label="Notes" {...regCreate('notes')} /></div>
          <div className="col-span-2 flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setCreateOpen(false)}>Cancel</Button>
            <Button type="submit" loading={isSubmitting}>Create</Button>
          </div>
        </form>
      </Modal>

      {/* Assign */}
      <Modal open={!!assignId} onClose={() => { setAssignId(null); resetAssign(); }} title="Assign Asset">
        <form onSubmit={handleAssign((d) => assignMutation.mutate({ id: assignId!, data: d }, { onSuccess: () => { setAssignId(null); resetAssign(); } }))} className="space-y-4">
          <Input label="Employee ID" {...regAssign('employeeId', { required: true })} />
          <Textarea label="Notes (optional)" {...regAssign('notes')} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setAssignId(null)}>Cancel</Button>
            <Button type="submit">Assign</Button>
          </div>
        </form>
      </Modal>

      {/* Return */}
      <Modal open={!!returnId} onClose={() => { setReturnId(null); resetReturn(); }} title="Return Asset">
        <form onSubmit={handleReturn((d) => returnMutation.mutate({ id: returnId!, data: d }, { onSuccess: () => { setReturnId(null); resetReturn(); } }))} className="space-y-4">
          <Select label="New Status" {...regReturn('newStatus')}>
            <option value="AVAILABLE">Available</option>
            <option value="IN_REPAIR">Send for Repair</option>
          </Select>
          <Textarea label="Notes (optional)" {...regReturn('notes')} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setReturnId(null)}>Cancel</Button>
            <Button type="submit" variant="success">Return Asset</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={!!retireId} message="Retire this asset? This cannot be undone." variant="danger" confirmLabel="Retire" onConfirm={() => { if (retireId) { retireMutation.mutate(retireId); setRetireId(null); } }} onCancel={() => setRetireId(null)} />
    </div>
  );
}
