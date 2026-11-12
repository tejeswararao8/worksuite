import { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { handleCallback } from '../../api/keycloakAuth';
import { useAuth } from '../../context/AuthContext';
import { ROUTES } from '../../constants/routes';

/**
 * Handles the Keycloak Authorization Code + PKCE callback.
 *
 * Keycloak redirects to /auth/callback?code=...&state=...
 * This component exchanges the code for tokens and redirects to the dashboard.
 *
 * Mount this component at the /auth/callback route in App.js.
 */
export default function AuthCallback() {
  const [searchParams] = useSearchParams();
  const navigate       = useNavigate();
  const { onTokensReceived } = useAuth();
  const handled = useRef(false);

  useEffect(() => {
    if (handled.current) return;
    handled.current = true;

    handleCallback(searchParams)
      .then((result) => {
        onTokensReceived(result);
        navigate(ROUTES.DASHBOARD, { replace: true });
      })
      .catch((err) => {
        console.error('Auth callback failed:', err.message);
        navigate(ROUTES.LOGIN + '?error=auth_failed', { replace: true });
      });
  }, [searchParams, navigate, onTokensReceived]);

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
      <p>Completing sign-in…</p>
    </div>
  );
}
