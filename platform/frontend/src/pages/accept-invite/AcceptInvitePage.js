import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { CheckCircle, XCircle, AlertCircle, LogIn } from 'lucide-react';
import { tenantInvitationApi } from '../../api/services';
import { useAuth } from '../../context/AuthContext';
import { getErrorMessage } from '../../utils/errorHandler';
import { Button, Spinner } from '../../components/ui';
import { ROUTES } from '../../constants/routes';

/**
 * Invitation acceptance page.
 *
 * URL format: /accept-invite?token=<raw-token>
 *
 * Security notes:
 * - Token is read from the query string only to pre-populate the form.
 *   It is submitted in the POST body — never in a URL or log.
 * - The page does not expose the token in any rendered output.
 * - If the user is not authenticated, they are redirected to login first.
 *   After login, Keycloak redirects back to this page with the token still
 *   in the query string (standard PKCE redirect_uri pattern).
 */
export default function AcceptInvitePage() {
  const { t } = useTranslation();
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  // Read token from query string once — do not log or display it.
  const token = searchParams.get('token') ?? '';

  const [state, setState] = useState('idle'); // idle | loading | success | error
  const [errorKey, setErrorKey] = useState('');
  const [errorDetail, setErrorDetail] = useState('');

  const acceptMutation = useMutation({
    mutationFn: tenantInvitationApi.accept,
    onSuccess: () => {
      setState('success');
      // Redirect to dashboard after a short delay so the user sees the success message.
      setTimeout(() => navigate(ROUTES.DASHBOARD, { replace: true }), 2500);
    },
    onError: (err) => {
      setState('error');
      const msg = getErrorMessage(err);
      // Map known backend messages to i18n keys for clean UX.
      if (msg.toLowerCase().includes('expired'))          setErrorKey('acceptInvite.errorExpired');
      else if (msg.toLowerCase().includes('revoked'))     setErrorKey('acceptInvite.errorRevoked');
      else if (msg.toLowerCase().includes('already been accepted')) setErrorKey('acceptInvite.errorAlreadyAccepted');
      else if (msg.toLowerCase().includes('email does not match')) setErrorKey('acceptInvite.errorEmailMismatch');
      else if (msg.toLowerCase().includes('email address must be verified')) setErrorKey('acceptInvite.errorEmailUnverified');
      else if (msg.toLowerCase().includes('invalid or unknown')) setErrorKey('acceptInvite.errorInvalid');
      else { setErrorKey('acceptInvite.errorGeneric'); setErrorDetail(msg); }
    },
  });

  const handleAccept = useCallback(() => {
    if (!token) { setState('error'); setErrorKey('acceptInvite.errorNoToken'); return; }
    setState('loading');
    acceptMutation.mutate(token);
  }, [token, acceptMutation]);

  // Auto-submit once authenticated and token is present.
  useEffect(() => {
    if (isAuthenticated && token && state === 'idle') {
      handleAccept();
    }
  }, [isAuthenticated, token, state, handleAccept]);

  // ── Not authenticated ──────────────────────────────────────────────────────
  if (!isAuthenticated) {
    return (
      <CenteredCard>
        <LogIn size={40} className="text-indigo-500 mx-auto" />
        <h1 className="text-lg font-semibold text-slate-800 text-center">{t('acceptInvite.loginRequired')}</h1>
        <p className="text-sm text-slate-500 text-center">{t('acceptInvite.loginRequiredDesc')}</p>
        <Button className="w-full" onClick={login}>{t('acceptInvite.loginBtn')}</Button>
      </CenteredCard>
    );
  }

  // ── No token ───────────────────────────────────────────────────────────────
  if (!token) {
    return (
      <CenteredCard>
        <AlertCircle size={40} className="text-red-400 mx-auto" />
        <h1 className="text-lg font-semibold text-slate-800 text-center">{t('acceptInvite.noToken')}</h1>
        <p className="text-sm text-slate-500 text-center">{t('acceptInvite.noTokenDesc')}</p>
      </CenteredCard>
    );
  }

  // ── Loading / submitting ───────────────────────────────────────────────────
  if (state === 'idle' || state === 'loading') {
    return (
      <CenteredCard>
        <Spinner className="mx-auto" />
        <p className="text-sm text-slate-500 text-center">{t('acceptInvite.processing')}</p>
      </CenteredCard>
    );
  }

  // ── Success ────────────────────────────────────────────────────────────────
  if (state === 'success') {
    return (
      <CenteredCard>
        <CheckCircle size={40} className="text-green-500 mx-auto" />
        <h1 className="text-lg font-semibold text-slate-800 text-center">{t('acceptInvite.successTitle')}</h1>
        <p className="text-sm text-slate-500 text-center">{t('acceptInvite.successDesc')}</p>
        <p className="text-xs text-slate-400 text-center">{t('acceptInvite.redirecting')}</p>
      </CenteredCard>
    );
  }

  // ── Error ──────────────────────────────────────────────────────────────────
  return (
    <CenteredCard>
      <XCircle size={40} className="text-red-400 mx-auto" />
      <h1 className="text-lg font-semibold text-slate-800 text-center">{t('acceptInvite.errorTitle')}</h1>
      <p className="text-sm text-slate-600 text-center">
        {t(errorKey, { defaultValue: errorDetail || t('acceptInvite.errorGeneric') })}
      </p>
      <Button variant="secondary" className="w-full" onClick={() => navigate(ROUTES.DASHBOARD)}>
        {t('acceptInvite.goToDashboard')}
      </Button>
    </CenteredCard>
  );
}

function CenteredCard({ children }) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
      <div className="bg-white rounded-xl shadow-lg border border-slate-200 p-8 w-full max-w-sm flex flex-col gap-4">
        {children}
      </div>
    </div>
  );
}
