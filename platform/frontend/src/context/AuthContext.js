import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { initiateLogin, logout as keycloakLogout, refreshTokens, getAccessToken } from '../api/keycloakAuth';
import { AUTH_LOGOUT_EVENT } from '../api/axios';

const AuthContext = createContext(null);

/**
 * AuthProvider — Authorization Code + PKCE flow.
 *
 * login()           → redirects to Keycloak (no credentials handled in browser)
 * onTokensReceived() → called by AuthCallback after code exchange
 * logout()          → clears session and redirects to Keycloak logout
 */
export function AuthProvider({ children }) {

  // Restore user from sessionStorage on page reload (tokens stored by keycloakAuth.js)
  const [user, setUser] = useState(() => {
    const token = getAccessToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return {
        userId:    payload.sub,
        companyId: payload.companyId,
        email:     payload.email,
        roles:     payload.realm_access?.roles ?? [],
      };
    } catch {
      return null;
    }
  });

  const logout = useCallback(() => {
    keycloakLogout(); // clears sessionStorage and redirects to Keycloak logout
    setUser(null);
  }, []);

  // Listen for forced logout triggered by the axios interceptor (token refresh failure)
  useEffect(() => {
    window.addEventListener(AUTH_LOGOUT_EVENT, logout);
    return () => window.removeEventListener(AUTH_LOGOUT_EVENT, logout);
  }, [logout]);

  // Redirect to Keycloak — no credentials in the browser
  const login = useCallback(() => initiateLogin(), []);

  // Called by AuthCallback after successful code exchange
  const onTokensReceived = useCallback(({ userInfo }) => {
    setUser(userInfo);
  }, []);

  // Silent refresh — called by axios interceptor on 401
  const refresh = useCallback(async () => {
    const result = await refreshTokens();
    if (result) {
      setUser(result.userInfo);
      return result.accessToken;
    }
    logout();
    return null;
  }, [logout]);

  return (
    <AuthContext.Provider value={{ user, login, logout, onTokensReceived, refresh, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
