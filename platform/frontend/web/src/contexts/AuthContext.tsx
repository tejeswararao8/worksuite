import axios from 'axios';
import { createContext, useContext, useCallback, useEffect, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { AUTH_LOGOUT_EVENT } from 'shared/api/axios';
import { setUser, clearUser } from 'shared/store/authSlice';
import { storageService } from 'shared/utils';
import type { RootState, AppDispatch } from 'shared/store';
import type { AuthUser, LoginRequest } from 'shared/types';

// ─── Keycloak ROPC (Resource Owner Password Credentials) ─────────────────────
// SECURITY STATUS: transitional — local development only.
//
// REACT_APP_* variables are embedded in the JavaScript bundle at build time
// and are fully visible to any browser user. A client_secret in a browser
// bundle is not a secret.
//
// Required migration (Phase 1): Set the Keycloak client access type to
// "public" in Keycloak Admin Console. Public clients do not require a
// client_secret. The ROPC flow continues to work with client_id only.
//
// Future migration (Phase 2): Replace ROPC with Authorization Code + PKCE.
//
// Until the Keycloak client is switched to public, the client_secret is read
// from the local .env file (gitignored) only — never hardcoded in source.
const KC_TOKEN_URL = `${process.env.REACT_APP_KC_URL}/realms/${process.env.REACT_APP_KC_REALM}/protocol/openid-connect/token`;
const KC_CLIENT_SECRET = process.env.REACT_APP_KC_CLIENT_SECRET;

async function keycloakLogin(email: string, password: string) {
  const params = new URLSearchParams();
  params.append('grant_type', 'password');
  params.append('client_id',  process.env.REACT_APP_KC_CLIENT_ID ?? '');
  // Only append client_secret if the Keycloak client is still "confidential".
  // Remove this block once the client is switched to "public" access type.
  if (KC_CLIENT_SECRET) {
    params.append('client_secret', KC_CLIENT_SECRET);
  }
  params.append('username', email);
  params.append('password', password);

  const { data } = await axios.post<{
    access_token:  string;
    refresh_token: string;
  }>(KC_TOKEN_URL, params, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  });
  return data;
}

function decodeJwtPayload(token: string): Record<string, unknown> {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch {
    return {};
  }
}

// ─── Context ──────────────────────────────────────────────────────────────────
interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<AuthUser>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate        = useNavigate();
  const dispatch        = useDispatch<AppDispatch>();
  const user            = useSelector((s: RootState) => s.auth.user);
  const isAuthenticated = useSelector((s: RootState) => s.auth.isAuthenticated);

  const logout = useCallback(() => {
    storageService.clear();
    dispatch(clearUser());
    navigate('/login', { replace: true });
  }, [dispatch, navigate]);

  // Listen for forced logout fired by the shared axios interceptor on 401
  useEffect(() => {
    window.addEventListener(AUTH_LOGOUT_EVENT, logout);
    return () => window.removeEventListener(AUTH_LOGOUT_EVENT, logout);
  }, [logout]);

  const login = useCallback(async ({ email, password }: LoginRequest): Promise<AuthUser> => {
    const tokens = await keycloakLogin(email, password);

    storageService.setAccessToken(tokens.access_token);
    storageService.setRefreshToken(tokens.refresh_token);

    // Decode JWT to extract user claims — same as src/context/AuthContext.js
    const payload = decodeJwtPayload(tokens.access_token);
    const userData: AuthUser = {
      userId:    payload.sub            as string ?? '',
      companyId: payload.companyId      as string ?? '',
      email:     payload.email          as string ?? email,
      role:      (payload.realm_access  as { roles?: string[] })?.roles?.[0] ?? '',
    };

    storageService.setUser(userData);
    dispatch(setUser(userData));
    return userData;
  }, [dispatch]);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
