import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import api from '../../api/axios';
import { userApi, roleApi } from '../../api/services';
import { Button, Input, Select, Modal, Table, Badge } from '../../components/ui';
import { Plus, UserCheck, UserX, KeyRound, Pencil } from 'lucide-react';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { QUERY_KEYS } from '../../constants/queryKeys';

const statusColor = { ACTIVE: 'green', INACTIVE: 'red', LOCKED: 'yellow' };

export default function UsersPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [roleModalId, setRoleModalId] = useState(null);

  const { data, isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get('/users', { params: { page: 0, size: 50 } }),
  });

  const { data: rolesData } = useQuery({
    queryKey: QUERY_KEYS.ROLES(0),
    queryFn: roleApi.getAll,
  });

  const users = data?.data?.data?.content || data?.data?.data || [];
  const roles = rolesData?.data?.data?.content || rolesData?.data?.data || [];

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm();
  const { register: regRole, handleSubmit: handleRole, reset: resetRole } = useForm();

  const invalidate = () => qc.invalidateQueries({ queryKey: ['users'] });

  const createMutation = useMutation({
    mutationFn: userApi.create,
    onSuccess: () => { invalidate(); toastSuccess(t('common.createSuccess', { entity: t('user.entity') })); setModalOpen(false); reset(); },
    onError: toastError,
  });

  const activateMutation  = useMutation({ mutationFn: userApi.activate,      onSuccess: () => { invalidate(); toastSuccess(t('user.activateSuccess')); },   onError: toastError });
  const deactivateMutation= useMutation({ mutationFn: userApi.deactivate,    onSuccess: () => { invalidate(); toastSuccess(t('user.deactivateSuccess')); }, onError: toastError });
  const resetPwdMutation  = useMutation({ mutationFn: userApi.resetPassword, onSuccess: () => toastSuccess(t('user.resetSuccess')),                          onError: toastError });
  const changeRoleMutation= useMutation({
    mutationFn: ({ id, data }) => userApi.changeRole(id, data),
    onSuccess: () => { invalidate(); toastSuccess(t('common.updateSuccess', { entity: t('user.entity') })); setRoleModalId(null); resetRole(); },
    onError: toastError,
  });

  const columns = [
    { key: 'email',     label: t('employee.email') },
    { key: 'role',      label: t('user.roleId') },
    { key: 'status',    label: t('user.status'), render: (r) => <Badge label={r.status} color={statusColor[r.status] || 'gray'} /> },
    { key: 'lastLogin', label: t('user.lastLogin') },
    {
      key: 'actions', label: t('common.actions'), render: (r) => (
        <div className="flex gap-1 flex-wrap">
          <button onClick={() => { setRoleModalId(r.id); resetRole({ roleId: r.roleId }); }} className="p-1.5 rounded hover:bg-slate-100 text-slate-500" title={t('user.changeRole')}><Pencil size={14} /></button>
          <button onClick={() => activateMutation.mutate(r.id)}   className="p-1.5 rounded hover:bg-green-50 text-green-500" title={t('user.activate')}><UserCheck size={14} /></button>
          <button onClick={() => deactivateMutation.mutate(r.id)} className="p-1.5 rounded hover:bg-red-50 text-red-400"   title={t('user.deactivate')}><UserX size={14} /></button>
          <button onClick={() => resetPwdMutation.mutate(r.id)}   className="p-1.5 rounded hover:bg-yellow-50 text-yellow-500" title={t('user.resetPassword')}><KeyRound size={14} /></button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button onClick={() => setModalOpen(true)}><Plus size={16} /> {t('common.add')} {t('user.entity')}</Button>
      </div>

      <Table columns={columns} data={users} loading={isLoading} />

      {/* Create User Modal */}
      <Modal open={modalOpen} onClose={() => { setModalOpen(false); reset(); }} title={`${t('common.add')} ${t('user.entity')}`}>
        <form onSubmit={handleSubmit((d) => createMutation.mutate(d))} className="space-y-4">
          <Input label={t('employee.email')} type="email" error={errors.email?.message} {...register('email', { required: t('common.required') })} />
          <Input label={t('employee.entity') + ' ID'} error={errors.employeeId?.message} {...register('employeeId', { required: t('common.required') })} />
          <Select label={t('user.roleId')} error={errors.roleId?.message} {...register('roleId', { required: t('common.required') })}>
            <option value="">{t('common.filter')}</option>
            {roles.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
          </Select>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => { setModalOpen(false); reset(); }}>{t('common.cancel')}</Button>
            <Button type="submit" disabled={isSubmitting}>{t('common.create')}</Button>
          </div>
        </form>
      </Modal>

      {/* Change Role Modal */}
      <Modal open={!!roleModalId} onClose={() => setRoleModalId(null)} title={t('user.changeRole')}>
        <form onSubmit={handleRole((d) => changeRoleMutation.mutate({ id: roleModalId, data: d }))} className="space-y-4">
          <Select label={t('user.roleId')} {...regRole('roleId', { required: t('common.required') })}>
            <option value="">{t('common.filter')}</option>
            {roles.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
          </Select>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setRoleModalId(null)}>{t('common.cancel')}</Button>
            <Button type="submit">{t('common.update')}</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
