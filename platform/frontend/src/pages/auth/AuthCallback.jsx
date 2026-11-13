import { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { handleCallback } from '../../api/keycloakAuth';
import { useAuth } from '../../context/AuthContext';
import { ROUTES } from '../../constants/routes';
import { PLATFORM_ROUTES } from '../../constants/platformRoutes';

const PLATFORM_ROLES = [
  'PLATFORM_SUPER_ADMIN',
  'PLATFORM_ADMIN',
  'PLATFORM_SUPPORT',
  'PLATFORM_IMPLEMENTATION',
];

/**
 * Handles the Keycloak Authorization Code + PKCE callback.
 *
 * After token exchange, redirects platform users to /platform/dashboard
 * and tenant users to /dashboard.
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
        const isPlatform = result.userInfo?.roles?.some((r) => PLATFORM_ROLES.includes(r));
        navigate(isPlatform ? PLATFORM_ROUTES.DASHBOARD : ROUTES.DASHBOARD, { replace: true });
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
