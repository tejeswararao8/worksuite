import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { useEmployees } from '../../hooks/useEmployees';
import { useReferenceData } from '../../hooks/useReferenceData';
import { Button, Input, Select, Modal, Table, Badge, Pagination, ConfirmDialog } from '../../components/ui';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import { EMPLOYMENT_STATUS_COLOR } from '../../constants/enums';

export default function EmployeesPage() {
  const { t } = useTranslation();
  const {
    employees, totalPages, isLoading,
    page, setPage,
    search, handleSearch,
    deptFilter, handleDeptFilter,
    createMutation, updateMutation, deleteMutation,
  } = useEmployees();

  const { departments, branches, designations } = useReferenceData();

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing]     = useState(null);
  const [confirmId, setConfirmId] = useState(null);

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm();

  const openCreate = () => { setEditing(null); reset({}); setModalOpen(true); };
  const openEdit   = (emp) => { setEditing(emp); reset(emp); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); reset({}); };

  const onSubmit = (formData) => {
    const mutation = editing ? updateMutation : createMutation;
    const payload  = editing ? { id: editing.id, data: formData } : formData;
    mutation.mutate(payload, { onSuccess: closeModal });
  };

  const columns = [
    { key: 'employeeCode',     label: t('employee.code') },
    { key: 'name',             label: t('common.name'), render: (r) => `${r.firstName} ${r.lastName}` },
    { key: 'email',            label: t('employee.email') },
    { key: 'mobile',           label: t('employee.mobile') },
    { key: 'employmentStatus', label: t('employee.employmentStatus'), render: (r) => <Badge label={r.employmentStatus} color={EMPLOYMENT_STATUS_COLOR[r.employmentStatus] || 'gray'} /> },
    { key: 'employmentType',   label: t('employee.employmentType'),   render: (r) => <Badge label={r.employmentType} color="indigo" /> },
    {
      key: 'actions', label: t('common.actions'), render: (r) => (
        <div className="flex gap-2">
          <button onClick={() => openEdit(r)} className="p-1.5 rounded hover:bg-slate-100 text-slate-500"><Pencil size={14} /></button>
          <button onClick={() => setConfirmId(r.id)} className="p-1.5 rounded hover:bg-red-50 text-red-400"><Trash2 size={14} /></button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="flex flex-wrap gap-3 items-center justify-between">
        <div className="flex gap-2 flex-wrap">
          <div className="relative">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              className="pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 w-56"
              placeholder={t('employee.searchPlaceholder')}
              value={search}
              onChange={(e) => handleSearch(e.target.value)}
            />
          </div>
          <select
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
            value={deptFilter}
            onChange={(e) => handleDeptFilter(e.target.value)}
          >
            <option value="">{t('common.allDepartments')}</option>
            {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </select>
        </div>
        <Button onClick={openCreate}><Plus size={16} /> {t('common.add')} {t('employee.entity')}</Button>
      </div>

      <Table columns={columns} data={employees} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      {/* Create / Edit Modal */}
      <Modal open={modalOpen} onClose={closeModal} title={editing ? `${t('common.edit')} ${t('employee.entity')}` : `${t('common.add')} ${t('employee.entity')}`} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
          <Input label={t('employee.firstName')} error={errors.firstName?.message} {...register('firstName', { required: t('common.required') })} />
          <Input label={t('employee.lastName')}  error={errors.lastName?.message}  {...register('lastName',  { required: t('common.required') })} />
          <Input label={t('employee.email')} type="email" error={errors.email?.message} {...register('email', { required: t('common.required') })} />
          <Input label={t('employee.mobile')} {...register('mobile')} />
          <Select label={t('employee.gender')} {...register('gender')}>
            <option value="">{t('common.filter')}</option>
            <option value="MALE">{t('employee.male')}</option>
            <option value="FEMALE">{t('employee.female')}</option>
            <option value="OTHER">{t('employee.other')}</option>
          </Select>
          <Input label={t('employee.dateOfBirth')} type="date" {...register('dateOfBirth')} />
          <Input label={t('employee.joiningDate')} type="date" error={errors.joiningDate?.message} {...register('joiningDate', { required: t('common.required') })} />
          <Input label={t('employee.nationality')} {...register('nationality')} />
          <Select label={t('employee.department')} error={errors.departmentId?.message} {...register('departmentId', { required: t('common.required') })}>
            <option value="">{t('common.filter')}</option>
            {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </Select>
          <Select label={t('employee.designation')} error={errors.designationId?.message} {...register('designationId', { required: t('common.required') })}>
            <option value="">{t('common.filter')}</option>
            {designations.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </Select>
          <Select label={t('employee.branch')} {...register('branchId')}>
            <option value="">{t('common.filter')}</option>
            {branches.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
          </Select>
          <Select label={t('employee.employmentType')} {...register('employmentType')}>
            <option value="">{t('common.filter')}</option>
            <option value="FULL_TIME">{t('employee.fullTime')}</option>
            <option value="PART_TIME">{t('employee.partTime')}</option>
            <option value="CONTRACT">{t('employee.contract')}</option>
            <option value="INTERN">{t('employee.intern')}</option>
          </Select>
          <Select label={t('employee.maritalStatus')} {...register('maritalStatus')}>
            <option value="">{t('common.filter')}</option>
            <option value="SINGLE">{t('employee.single')}</option>
            <option value="MARRIED">{t('employee.married')}</option>
            <option value="DIVORCED">{t('employee.divorced')}</option>
          </Select>
          <Input label={t('employee.workLocation')} {...register('workLocation')} />
          <div className="col-span-2">
            <Input label={t('employee.currentAddress')} {...register('currentAddress')} />
          </div>
          <div className="col-span-2 flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={closeModal}>{t('common.cancel')}</Button>
            <Button type="submit" disabled={isSubmitting}>{editing ? t('common.update') : t('common.create')}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!confirmId}
        message={t('common.confirmDelete', { entity: t('employee.entity') })}
        onConfirm={() => { deleteMutation.mutate(confirmId); setConfirmId(null); }}
        onCancel={() => setConfirmId(null)}
      />
    </div>
  );
}
