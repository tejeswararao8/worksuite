import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { toastError } from '../../utils/errorHandler';
import { Button, Input } from '../../components/ui';
import LanguageSwitcher from '../../components/ui/LanguageSwitcher';
import { ROUTES } from '../../constants/routes';

export default function LoginPage() {
  const { t } = useTranslation();
  const { login } = useAuth();
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();

  const onSubmit = async (data) => {
    try {
      await login(data);
      navigate(ROUTES.DASHBOARD, { replace: true });
    } catch (err) {
      toastError(err);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 to-slate-100 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-8">
        <div className="flex justify-end mb-2">
          <LanguageSwitcher />
        </div>
        <div className="text-center mb-8">
          <div className="w-14 h-14 bg-indigo-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <span className="text-white text-2xl font-bold">H</span>
          </div>
          <h1 className="text-2xl font-bold text-slate-800">{t('auth.title')}</h1>
          <p className="text-slate-500 text-sm mt-1">{t('auth.subtitle')}</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label={t('auth.email')}
            type="email"
            placeholder={t('auth.emailPlaceholder')}
            error={errors.email?.message}
            {...register('email', { required: t('common.required') })}
          />
          <Input
            label={t('auth.password')}
            type="password"
            placeholder="••••••••"
            error={errors.password?.message}
            {...register('password', { required: t('common.required') })}
          />
          <Button type="submit" className="w-full justify-center" disabled={isSubmitting}>
            {isSubmitting ? t('auth.signingIn') : t('auth.signIn')}
          </Button>
        </form>
      </div>
    </div>
  );
}
