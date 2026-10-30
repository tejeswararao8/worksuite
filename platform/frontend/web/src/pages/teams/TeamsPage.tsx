import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { Plus, Pencil, Trash2, Users, X } from 'lucide-react';
import { useTeams, useTeamMembers, useReferenceData } from '../../../../shared/hooks';
import type { Team, CreateTeamRequest } from '../../../../shared/types';
import { Button, Input, Select, Modal, Table, Pagination, ConfirmDialog, PageHeader, Spinner } from '../../components/ui';
import type { Column } from '../../components/ui';

const toastService = { success: toast.success, error: toast.error };

export default function TeamsPage() {
  const { t } = useTranslation();
  const { teams, totalPages, isLoading, page, setPage, deptFilter, setDeptFilter, createMutation, updateMutation, deleteMutation, addMemberMutation, removeMemberMutation } = useTeams(toastService);
  const { departments } = useReferenceData();

  const [modalOpen, setModalOpen]       = useState(false);
  const [editing, setEditing]           = useState<Team | null>(null);
  const [confirmId, setConfirmId]       = useState<string | null>(null);
  const [membersTeamId, setMembersTeamId] = useState<string | null>(null);
  const [newMemberId, setNewMemberId]   = useState('');

  const { members, isLoading: membersLoading } = useTeamMembers(membersTeamId ?? '');

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<CreateTeamRequest>();

  const openCreate = () => { setEditing(null); reset({}); setModalOpen(true); };
  const openEdit   = (t: Team) => { setEditing(t); reset(t as never); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); reset({}); };

  const onSubmit = (data: CreateTeamRequest) => {
    if (editing) updateMutation.mutate({ id: editing.id, data }, { onSuccess: closeModal });
    else createMutation.mutate(data, { onSuccess: closeModal });
  };

  const columns: Column<Team>[] = [
    { key: 'name',           label: 'Name' },
    { key: 'departmentName', label: 'Department', render: (r) => r.departmentName || '—' },
    { key: 'leadEmployeeName', label: 'Lead', render: (r) => r.leadEmployeeName || '—' },
    { key: 'memberCount',    label: 'Members' },
    {
      key: 'actions', label: t('common.actions'),
      render: (r) => (
        <div className="flex gap-1">
          <button onClick={() => setMembersTeamId(r.id)} className="p-1.5 rounded hover:bg-indigo-50 text-indigo-500" title="Members"><Users size={14} /></button>
          <button onClick={() => openEdit(r)} className="p-1.5 rounded hover:bg-slate-100 text-slate-500"><Pencil size={14} /></button>
          <button onClick={() => setConfirmId(r.id)} className="p-1.5 rounded hover:bg-red-50 text-red-400"><Trash2 size={14} /></button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader title={t('nav.teams')} action={<Button onClick={openCreate}><Plus size={16} />Add Team</Button>} />

      <div className="flex gap-3 bg-white p-4 rounded-xl border border-slate-200">
        <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:ring-2 focus:ring-indigo-500" value={deptFilter} onChange={(e) => setDeptFilter(e.target.value)}>
          <option value="">All Departments</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
      </div>

      <Table columns={columns} data={teams} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      {/* Members Panel */}
      {membersTeamId && (
        <div className="fixed inset-y-0 right-0 w-96 bg-white shadow-2xl border-l border-slate-200 z-40 flex flex-col">
          <div className="flex items-center justify-between px-6 py-4 border-b">
            <h3 className="font-semibold text-slate-800">Team Members</h3>
            <button onClick={() => setMembersTeamId(null)} className="text-slate-400 hover:text-slate-600"><X size={20} /></button>
          </div>
          <div className="flex-1 overflow-y-auto p-6 space-y-3">
            {membersLoading ? <Spinner /> : members.map((m) => (
              <div key={m.employeeId} className="flex items-center justify-between border border-slate-200 rounded-lg px-4 py-3">
                <div>
                  <p className="text-sm font-medium text-slate-700">{m.firstName} {m.lastName}</p>
                  <p className="text-xs text-slate-400">{m.employeeCode} · {m.designation || '—'}</p>
                </div>
                <button onClick={() => removeMemberMutation.mutate({ id: membersTeamId, empId: m.employeeId })} className="text-red-400 hover:text-red-600 text-xs">Remove</button>
              </div>
            ))}
          </div>
          <div className="p-4 border-t">
            <div className="flex gap-2">
              <input className="flex-1 border border-slate-300 rounded-lg px-3 py-2 text-sm" placeholder="Employee ID" value={newMemberId} onChange={(e) => setNewMemberId(e.target.value)} />
              <Button size="sm" onClick={() => { if (newMemberId) { addMemberMutation.mutate({ id: membersTeamId, empId: newMemberId }); setNewMemberId(''); } }}>Add</Button>
            </div>
          </div>
        </div>
      )}

      <Modal open={modalOpen} onClose={closeModal} title={editing ? 'Edit Team' : 'Add Team'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Team Name" error={errors.name?.message} {...register('name', { required: t('common.required') })} />
          <Input label="Description" {...register('description')} />
          <Select label="Department" error={errors.departmentId?.message} {...register('departmentId', { required: t('common.required') })}>
            <option value="">Select Department</option>
            {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </Select>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={closeModal}>{t('common.cancel')}</Button>
            <Button type="submit" loading={isSubmitting}>{editing ? t('common.update') : t('common.create')}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={!!confirmId} message="Deactivate this team?" onConfirm={() => { if (confirmId) { deleteMutation.mutate(confirmId); setConfirmId(null); } }} onCancel={() => setConfirmId(null)} />
    </div>
  );
}
