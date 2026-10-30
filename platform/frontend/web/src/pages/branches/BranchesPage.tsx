import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { Plus, Pencil, Trash2, Search } from 'lucide-react';
import { useBranches } from '../../../../shared/hooks';
import type { Branch, CreateBranchRequest } from '../../../../shared/types';
import { Button, Input, Select, Modal, Table, Badge, Pagination, ConfirmDialog, PageHeader } from '../../components/ui';
import type { Column } from '../../components/ui';

const toastService = { success: toast.success, error: toast.error };

export default function BranchesPage() {
  const { t } = useTranslation();
  const { branches, totalPages, isLoading, page, setPage, search, setSearch, createMutation, updateMutation, deleteMutation } = useBranches(toastService);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing]     = useState<Branch | null>(null);
  const [confirmId, setConfirmId] = useState<string | null>(null);

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<CreateBranchRequest>();

  const openCreate = () => { setEditing(null); reset({}); setModalOpen(true); };
  const openEdit   = (b: Branch) => { setEditing(b); reset(b as never); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); reset({}); };

  const onSubmit = (data: CreateBranchRequest) => {
    if (editing) updateMutation.mutate({ id: editing.id, data }, { onSuccess: closeModal });
    else createMutation.mutate(data, { onSuccess: closeModal });
  };

  const columns: Column<Branch>[] = [
    { key: 'name',          label: t('branch.name') },
    { key: 'code',          label: t('branch.code') },
    { key: 'city',          label: t('branch.city') },
    { key: 'country',       label: t('branch.country') },
    { key: 'headOffice',    label: 'Head Office', render: (r) => r.headOffice ? <Badge label="HQ" color="indigo" /> : null },
    { key: 'employeeCount', label: 'Employees' },
    {
      key: 'actions', label: t('common.actions'),
      render: (r) => (
        <div className="flex gap-1">
          <button onClick={() => openEdit(r)} className="p-1.5 rounded hover:bg-slate-100 text-slate-500"><Pencil size={14} /></button>
          <button onClick={() => setConfirmId(r.id)} className="p-1.5 rounded hover:bg-red-50 text-red-400"><Trash2 size={14} /></button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader title={t('nav.branches')} action={<Button onClick={openCreate}><Plus size={16} />Add Branch</Button>} />

      <div className="flex gap-3 bg-white p-4 rounded-xl border border-slate-200">
        <div className="relative">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input className="pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 w-56" placeholder="Search branches..." value={search} onChange={(e) => setSearch(e.target.value)} />
        </div>
      </div>

      <Table columns={columns} data={branches} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal open={modalOpen} onClose={closeModal} title={editing ? 'Edit Branch' : 'Add Branch'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
          <Input label="Branch Name" error={errors.name?.message} {...register('name', { required: t('common.required') })} />
          <Input label="Branch Code" error={errors.code?.message} {...register('code', { required: t('common.required') })} />
          <Input label="Address" {...register('address')} />
          <Input label="City" {...register('city')} />
          <Input label="Country" {...register('country')} />
          <Input label="Phone" {...register('phone')} />
          <Input label="Email" type="email" {...register('email')} />
          <div className="flex items-center gap-2 pt-6">
            <input type="checkbox" id="headOffice" {...register('headOffice')} className="rounded" />
            <label htmlFor="headOffice" className="text-sm text-slate-700">Head Office</label>
          </div>
          <div className="col-span-2 flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={closeModal}>{t('common.cancel')}</Button>
            <Button type="submit" loading={isSubmitting}>{editing ? t('common.update') : t('common.create')}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={!!confirmId} message="Deactivate this branch? All employees must be reassigned first." onConfirm={() => { if (confirmId) { deleteMutation.mutate(confirmId); setConfirmId(null); } }} onCancel={() => setConfirmId(null)} />
    </div>
  );
}
