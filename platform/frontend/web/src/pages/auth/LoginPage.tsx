import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../contexts/AuthContext';
import { Button, Input } from '../../components/ui';
import { getErrorMessage } from '../../../../shared/utils';
import type { LoginRequest } from '../../../../shared/types';

export default function LoginPage() {
  const { t }    = useTranslation();
  const { login } = useAuth();
  const navigate  = useNavigate();

  const { register, handleSubmit, setError, formState: { errors, isSubmitting } } = useForm<LoginRequest>();

  const onSubmit = async (data: LoginRequest) => {
    try {
      await login(data);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError('root', { message: getErrorMessage(err) });
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 to-indigo-900 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-indigo-600 rounded-2xl mb-4">
            <span className="text-white text-2xl font-bold">W</span>
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

          {errors.root && (
            <p className="text-sm text-red-500 bg-red-50 px-3 py-2 rounded-lg">{errors.root.message}</p>
          )}

          <Button type="submit" className="w-full justify-center" size="lg" loading={isSubmitting}>
            {isSubmitting ? t('auth.signingIn') : t('auth.signIn')}
          </Button>
        </form>

        {/* Language switcher */}
        <div className="flex justify-center gap-3 mt-6">
          {['en', 'ar', 'hi'].map((lang) => (
            <button
              key={lang}
              onClick={() => { /* handled by i18n detector */ }}
              className="text-xs text-slate-400 hover:text-indigo-600 uppercase font-medium"
            >
              {lang}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
