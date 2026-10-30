import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { Plus, Search, Pencil, Trash2, Eye } from 'lucide-react';
import { useEmployees, useReferenceData } from '../../../../shared/hooks';
import { EMPLOYMENT_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import type { Employee, CreateEmployeeRequest } from '../../../../shared/types';
import { Button, Input, Select, Modal, Table, Badge, Pagination, ConfirmDialog, PageHeader } from '../../components/ui';
import type { Column } from '../../components/ui';

const toastService = { success: toast.success, error: toast.error };

export default function EmployeesPage() {
  const { t }      = useTranslation();
  const navigate   = useNavigate();
  const {
    employees, totalPages, isLoading,
    page, setPage, search, setSearch,
    deptFilter, setDeptFilter, branchFilter, setBranchFilter,
    statusFilter, setStatusFilter,
    createMutation, updateMutation, deleteMutation,
  } = useEmployees(toastService);

  const { departments, branches, designations } = useReferenceData();

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing]     = useState<Employee | null>(null);
  const [confirmId, setConfirmId] = useState<string | null>(null);

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<CreateEmployeeRequest>();

  const openCreate = () => { setEditing(null); reset({}); setModalOpen(true); };
  const openEdit   = (emp: Employee) => { setEditing(emp); reset(emp as never); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); reset({}); };

  const onSubmit = (data: CreateEmployeeRequest) => {
    if (editing) {
      updateMutation.mutate({ id: editing.id, data }, { onSuccess: closeModal });
    } else {
      createMutation.mutate(data, { onSuccess: closeModal });
    }
  };

  const columns: Column<Employee>[] = [
    { key: 'employeeCode', label: t('employee.code') },
    { key: 'name',         label: t('common.name'), render: (r) => `${r.firstName} ${r.lastName}` },
    { key: 'email',        label: t('employee.email') },
    { key: 'mobile',       label: t('employee.mobile') },
    { key: 'joiningDate',  label: t('employee.joiningDate'), render: (r) => formatDate(r.joiningDate) },
    {
      key: 'employmentStatus', label: t('employee.employmentStatus'),
      render: (r) => <Badge label={r.employmentStatus} color={EMPLOYMENT_STATUS_COLOR[r.employmentStatus] as never ?? 'gray'} />,
    },
    {
      key: 'actions', label: t('common.actions'),
      render: (r) => (
        <div className="flex gap-1">
          <button onClick={() => navigate(`/employees/${r.id}`)} className="p-1.5 rounded hover:bg-indigo-50 text-indigo-500" title="View"><Eye size={14} /></button>
          <button onClick={(e) => { e.stopPropagation(); openEdit(r); }} className="p-1.5 rounded hover:bg-slate-100 text-slate-500" title="Edit"><Pencil size={14} /></button>
          <button onClick={(e) => { e.stopPropagation(); setConfirmId(r.id); }} className="p-1.5 rounded hover:bg-red-50 text-red-400" title="Delete"><Trash2 size={14} /></button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader
        title={t('nav.employees')}
        subtitle={`${employees.length} employees`}
        action={<Button onClick={openCreate}><Plus size={16} />{t('common.add')} {t('employee.entity')}</Button>}
      />

      {/* Filters */}
      <div className="flex flex-wrap gap-3 items-center bg-white p-4 rounded-xl border border-slate-200">
        <div className="relative">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            className="pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 w-56"
            placeholder={t('employee.searchPlaceholder')}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:ring-2 focus:ring-indigo-500" value={deptFilter} onChange={(e) => setDeptFilter(e.target.value)}>
          <option value="">{t('common.allDepartments')}</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
        <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:ring-2 focus:ring-indigo-500" value={branchFilter} onChange={(e) => setBranchFilter(e.target.value)}>
          <option value="">All Branches</option>
          {branches.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
        </select>
        <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:ring-2 focus:ring-indigo-500" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="">All Statuses</option>
          {['ACTIVE','PROBATION','ON_LEAVE','RESIGNED','TERMINATED'].map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      <Table columns={columns} data={employees} loading={isLoading} onRowClick={(r) => navigate(`/employees/${r.id}`)} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      {/* Create / Edit Modal */}
      <Modal open={modalOpen} onClose={closeModal} title={editing ? `Edit ${t('employee.entity')}` : `Add ${t('employee.entity')}`} size="xl">
        <form onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-4">
          <Input label={t('employee.firstName')} error={errors.firstName?.message} {...register('firstName', { required: t('common.required') })} />
          <Input label={t('employee.lastName')}  error={errors.lastName?.message}  {...register('lastName',  { required: t('common.required') })} />
          <Input label={t('employee.email')} type="email" error={errors.email?.message} {...register('email', { required: t('common.required') })} />
          <Input label={t('employee.mobile')} {...register('mobile')} />
          <Select label={t('employee.gender')} {...register('gender')}>
            <option value="">Select</option>
            <option value="MALE">Male</option>
            <option value="FEMALE">Female</option>
            <option value="OTHER">Other</option>
          </Select>
          <Input label={t('employee.dateOfBirth')} type="date" {...register('dateOfBirth')} />
          <Input label={t('employee.joiningDate')} type="date" error={errors.joiningDate?.message} {...register('joiningDate', { required: t('common.required') })} />
          <Input label={t('employee.nationality')} {...register('nationality')} />
          <Select label={t('employee.department')} error={errors.departmentId?.message} {...register('departmentId', { required: t('common.required') })}>
            <option value="">Select Department</option>
            {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </Select>
          <Select label={t('employee.designation')} error={errors.designationId?.message} {...register('designationId', { required: t('common.required') })}>
            <option value="">Select Designation</option>
            {designations.map((d) => <option key={d.id} value={d.id}>{d.title}</option>)}
          </Select>
          <Select label={t('employee.branch')} {...register('branchId')}>
            <option value="">Select Branch</option>
            {branches.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
          </Select>
          <Select label={t('employee.employmentType')} {...register('employmentType')}>
            <option value="">Select Type</option>
            <option value="FULL_TIME">Full Time</option>
            <option value="PART_TIME">Part Time</option>
            <option value="CONTRACT">Contract</option>
            <option value="INTERN">Intern</option>
          </Select>
          <Select label={t('employee.maritalStatus')} {...register('maritalStatus')}>
            <option value="">Select</option>
            <option value="SINGLE">Single</option>
            <option value="MARRIED">Married</option>
            <option value="DIVORCED">Divorced</option>
          </Select>
          <Input label={t('employee.workLocation')} {...register('workLocation')} />
          <div className="col-span-2"><Input label={t('employee.currentAddress')} {...register('currentAddress')} /></div>
          <div className="col-span-2 flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={closeModal}>{t('common.cancel')}</Button>
            <Button type="submit" loading={isSubmitting}>{editing ? t('common.update') : t('common.create')}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!confirmId}
        message={`Are you sure you want to delete this employee?`}
        onConfirm={() => { if (confirmId) { deleteMutation.mutate(confirmId); setConfirmId(null); } }}
        onCancel={() => setConfirmId(null)}
      />
    </div>
  );
}
