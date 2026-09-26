import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { authApi } from '../../api/services';
import { useAuth } from '../../context/AuthContext';
import { Button, Input, Badge } from '../../components/ui';
import { toastSuccess, toastError } from '../../utils/errorHandler';

export default function ProfilePage() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm();

  const onSubmit = async (data) => {
    try {
      await authApi.changePassword(data);
      toastSuccess(t('profile.passwordChanged'));
      reset();
    } catch (err) {
      toastError(err);
    }
  };

  return (
    <div className="max-w-md space-y-6">
      {/* User info card */}
      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <div className="flex items-center gap-4 mb-4">
          <div className="w-14 h-14 bg-indigo-100 rounded-full flex items-center justify-center text-indigo-600 text-xl font-bold">
            {user?.email?.[0]?.toUpperCase()}
          </div>
          <div>
            <p className="font-semibold text-slate-800">{user?.email}</p>
            <Badge label={user?.role} color="indigo" />
          </div>
        </div>
      </div>

      {/* Change password */}
      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <h2 className="text-base font-semibold text-slate-800 mb-4">{t('profile.changePassword')}</h2>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
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
            {...register('newPassword', { required: t('common.required'), minLength: { value: 8, message: 'Min 8 characters' } })}
          />
          <Button type="submit" disabled={isSubmitting}>{t('common.save')}</Button>
        </form>
      </div>
    </div>
  );
}
