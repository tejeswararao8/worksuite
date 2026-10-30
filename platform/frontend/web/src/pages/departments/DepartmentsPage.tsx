import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { Plus, Pencil, Trash2, Search, Network } from 'lucide-react';
import { useDepartments, useReferenceData } from '../../../../shared/hooks';
import type { Department, CreateDepartmentRequest, OrgNode } from '../../../../shared/types';
import { Button, Input, Select, Modal, Table, Pagination, ConfirmDialog, PageHeader } from '../../components/ui';
import type { Column } from '../../components/ui';

const toastService = { success: toast.success, error: toast.error };

function OrgNodeCard({ node, depth = 0 }: { node: OrgNode; depth?: number }) {
  return (
    <div className={`ml-${depth * 6}`}>
      <div className="flex items-center gap-2 py-2 px-3 bg-white border border-slate-200 rounded-lg mb-1 hover:border-indigo-300 transition-colors">
        <div className="w-2 h-2 rounded-full bg-indigo-400 shrink-0" />
        <span className="text-sm font-medium text-slate-700">{node.name}</span>
        <span className="text-xs text-slate-400">({node.code})</span>
        <span className="ml-auto text-xs text-slate-400">{node.employeeCount} employees</span>
      </div>
      {node.children?.map((child) => (
        <div key={child.id} className="ml-6 border-l-2 border-slate-200 pl-4">
          <OrgNodeCard node={child} depth={depth + 1} />
        </div>
      ))}
    </div>
  );
}

export default function DepartmentsPage() {
  const { t } = useTranslation();
  const { departments, totalPages, isLoading, orgChart, page, setPage, search, setSearch, createMutation, updateMutation, deleteMutation } = useDepartments(toastService);
  const { departments: allDepts } = useReferenceData();

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing]     = useState<Department | null>(null);
  const [confirmId, setConfirmId] = useState<string | null>(null);
  const [showOrgChart, setShowOrgChart] = useState(false);

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<CreateDepartmentRequest>();

  const openCreate = () => { setEditing(null); reset({}); setModalOpen(true); };
  const openEdit   = (d: Department) => { setEditing(d); reset(d as never); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); reset({}); };

  const onSubmit = (data: CreateDepartmentRequest) => {
    if (editing) updateMutation.mutate({ id: editing.id, data }, { onSuccess: closeModal });
    else createMutation.mutate(data, { onSuccess: closeModal });
  };

  const columns: Column<Department>[] = [
    { key: 'name',              label: 'Name' },
    { key: 'code',              label: 'Code' },
    { key: 'parentDepartmentName', label: 'Parent', render: (r) => r.parentDepartmentName || '—' },
    { key: 'headEmployeeName',  label: 'Head', render: (r) => r.headEmployeeName || '—' },
    { key: 'employeeCount',     label: 'Employees' },
    { key: 'teamCount',         label: 'Teams' },
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
      <PageHeader
        title={t('nav.departments')}
        action={
          <div className="flex gap-2">
            <Button variant="secondary" onClick={() => setShowOrgChart(!showOrgChart)}><Network size={16} />{showOrgChart ? 'List View' : 'Org Chart'}</Button>
            <Button onClick={openCreate}><Plus size={16} />Add Department</Button>
          </div>
        }
      />

      {!showOrgChart ? (
        <>
          <div className="flex gap-3 bg-white p-4 rounded-xl border border-slate-200">
            <div className="relative">
              <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input className="pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 w-56" placeholder="Search departments..." value={search} onChange={(e) => setSearch(e.target.value)} />
            </div>
          </div>
          <Table columns={columns} data={departments} loading={isLoading} />
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      ) : (
        <div className="bg-white rounded-xl border border-slate-200 p-6">
          <h3 className="text-sm font-semibold text-slate-700 mb-4">Organization Chart</h3>
          {orgChart.length === 0
            ? <p className="text-slate-400 text-sm">No departments found</p>
            : orgChart.map((node) => <OrgNodeCard key={node.id} node={node} />)
          }
        </div>
      )}

      <Modal open={modalOpen} onClose={closeModal} title={editing ? 'Edit Department' : 'Add Department'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
          <Input label="Department Name" error={errors.name?.message} {...register('name', { required: t('common.required') })} />
          <Input label="Department Code" error={errors.code?.message} {...register('code', { required: t('common.required') })} />
          <div className="col-span-2"><Input label="Description" {...register('description')} /></div>
          <Select label="Parent Department" {...register('parentDepartmentId')}>
            <option value="">None (Top Level)</option>
            {allDepts.filter((d) => d.id !== editing?.id).map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </Select>
          <div className="col-span-2 flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={closeModal}>{t('common.cancel')}</Button>
            <Button type="submit" loading={isSubmitting}>{editing ? t('common.update') : t('common.create')}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={!!confirmId} message="Deactivate this department?" onConfirm={() => { if (confirmId) { deleteMutation.mutate(confirmId); setConfirmId(null); } }} onCancel={() => setConfirmId(null)} />
    </div>
  );
}
