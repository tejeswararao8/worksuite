import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { Plus, Pencil, Trash2, Search } from 'lucide-react';
import { useDesignations } from '../../../../shared/hooks';
import type { Designation, CreateDesignationRequest } from '../../../../shared/types';
import { Button, Input, Modal, Table, Pagination, ConfirmDialog, PageHeader } from '../../components/ui';
import type { Column } from '../../components/ui';

const toastService = { success: toast.success, error: toast.error };

export default function DesignationsPage() {
  const { t } = useTranslation();
  const { designations, totalPages, isLoading, page, setPage, search, setSearch, createMutation, updateMutation, deleteMutation } = useDesignations(toastService);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing]     = useState<Designation | null>(null);
  const [confirmId, setConfirmId] = useState<string | null>(null);

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<CreateDesignationRequest>();

  const openCreate = () => { setEditing(null); reset({}); setModalOpen(true); };
  const openEdit   = (d: Designation) => { setEditing(d); reset(d as never); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); reset({}); };

  const onSubmit = (data: CreateDesignationRequest) => {
    if (editing) updateMutation.mutate({ id: editing.id, data }, { onSuccess: closeModal });
    else createMutation.mutate(data, { onSuccess: closeModal });
  };

  const columns: Column<Designation>[] = [
    { key: 'title',         label: 'Title' },
    { key: 'level',         label: 'Level', render: (r) => r.level ? `Level ${r.level}` : '—' },
    { key: 'grade',         label: 'Grade', render: (r) => r.grade || '—' },
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
      <PageHeader title={t('nav.designations')} action={<Button onClick={openCreate}><Plus size={16} />Add Designation</Button>} />

      <div className="flex gap-3 bg-white p-4 rounded-xl border border-slate-200">
        <div className="relative">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input className="pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 w-56" placeholder="Search designations..." value={search} onChange={(e) => setSearch(e.target.value)} />
        </div>
      </div>

      <Table columns={columns} data={designations} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal open={modalOpen} onClose={closeModal} title={editing ? 'Edit Designation' : 'Add Designation'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Title" error={errors.title?.message} {...register('title', { required: t('common.required') })} />
          <Input label="Description" {...register('description')} />
          <div className="grid grid-cols-2 gap-4">
            <Input label="Level (1-10)" type="number" {...register('level', { valueAsNumber: true })} />
            <Input label="Grade (A, B, C...)" {...register('grade')} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={closeModal}>{t('common.cancel')}</Button>
            <Button type="submit" loading={isSubmitting}>{editing ? t('common.update') : t('common.create')}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={!!confirmId} message="Deactivate this designation?" onConfirm={() => { if (confirmId) { deleteMutation.mutate(confirmId); setConfirmId(null); } }} onCancel={() => setConfirmId(null)} />
    </div>
  );
}
