import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import { useMutation } from '@tanstack/react-query';
import { authApi } from '../../../../shared/api/services';
import { getErrorMessage } from '../../../../shared/utils';
import type { RootState } from '../../../../shared/store';
import { Button, Input, Tabs, PageHeader } from '../../components/ui';

export default function ProfilePage() {
  const { t }  = useTranslation();
  const user   = useSelector((s: RootState) => s.auth.user);
  const [tab, setTab] = useState('info');

  const { register, handleSubmit, reset, setError, formState: { errors, isSubmitting } } = useForm<{
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
  }>();

  const changePwMutation = useMutation({
    mutationFn: authApi.changePassword,
    onSuccess:  () => { toast.success('Password changed successfully'); reset(); },
    onError:    (e) => toast.error(getErrorMessage(e)),
  });

  const onChangePw = (data: { currentPassword: string; newPassword: string; confirmPassword: string }) => {
    if (data.newPassword !== data.confirmPassword) {
      setError('confirmPassword', { message: 'Passwords do not match' });
      return;
    }
    changePwMutation.mutate({ currentPassword: data.currentPassword, newPassword: data.newPassword });
  };

  return (
    <div className="max-w-2xl space-y-6">
      <PageHeader title={t('profile.title')} />

      {/* Avatar */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 flex items-center gap-6">
        <div className="w-20 h-20 rounded-full bg-indigo-100 flex items-center justify-center">
          <span className="text-2xl font-bold text-indigo-600">
            {user?.email?.[0]?.toUpperCase() ?? 'U'}
          </span>
        </div>
        <div>
          <p className="text-lg font-semibold text-slate-800">{user?.email}</p>
          <p className="text-sm text-indigo-600 font-medium mt-0.5">{user?.role}</p>
          <p className="text-xs text-slate-400 mt-1">Company ID: {user?.companyId}</p>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <Tabs
          tabs={[{ key: 'info', label: 'Account Info' }, { key: 'password', label: t('profile.changePassword') }]}
          active={tab}
          onChange={setTab}
        />

        {tab === 'info' && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-xs text-slate-400 font-medium uppercase tracking-wide">Email</p>
                <p className="text-sm text-slate-700 mt-0.5">{user?.email}</p>
              </div>
              <div>
                <p className="text-xs text-slate-400 font-medium uppercase tracking-wide">Role</p>
                <p className="text-sm text-slate-700 mt-0.5">{user?.role}</p>
              </div>
              <div>
                <p className="text-xs text-slate-400 font-medium uppercase tracking-wide">User ID</p>
                <p className="text-sm text-slate-700 mt-0.5 font-mono text-xs">{user?.userId}</p>
              </div>
              <div>
                <p className="text-xs text-slate-400 font-medium uppercase tracking-wide">Company ID</p>
                <p className="text-sm text-slate-700 mt-0.5 font-mono text-xs">{user?.companyId}</p>
              </div>
            </div>
          </div>
        )}

        {tab === 'password' && (
          <form onSubmit={handleSubmit(onChangePw)} className="space-y-4 max-w-sm">
            <Input
              label={t('profile.currentPassword')}
              type="password"
              error={errors.currentPassword?.message}
              {...register('currentPassword', { required: t('common.required') })}
            />
            <Input
              label={t('profile.newPassword')}
              type="password"
              error={errors.newPassword?.message}
              {...register('newPassword', { required: t('common.required'), minLength: { value: 8, message: 'Minimum 8 characters' } })}
            />
            <Input
              label="Confirm New Password"
              type="password"
              error={errors.confirmPassword?.message}
              {...register('confirmPassword', { required: t('common.required') })}
            />
            <Button type="submit" loading={isSubmitting}>Update Password</Button>
          </form>
        )}
      </div>
    </div>
  );
}
