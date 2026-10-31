import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/services';
import { AUTH_LOGOUT_EVENT } from '../api/axios';
import storageService from '../services/storageService';
import { ROUTES } from '../constants/routes';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const navigate = useNavigate();

  const [user, setUser] = useState(() => storageService.getUser());

  const logout = useCallback(() => {
    storageService.clear();
    setUser(null);
    navigate(ROUTES.LOGIN, { replace: true });
  }, [navigate]);

  // Listen for forced logout triggered by the axios interceptor (token refresh failure)
  useEffect(() => {
    window.addEventListener(AUTH_LOGOUT_EVENT, logout);
    return () => window.removeEventListener(AUTH_LOGOUT_EVENT, logout);
  }, [logout]);

  const login = useCallback(async (credentials) => {
    const { data } = await authApi.login(credentials);

    storageService.setAccessToken(data.access_token);
    storageService.setRefreshToken(data.refresh_token);

    // Decode JWT to extract user info
    const payload = JSON.parse(atob(data.access_token.split('.')[1]));
    const userData = {
      userId:    payload.sub,
      companyId: payload.companyId,
      email:     payload.email,
      role:      payload.realm_access?.roles?.[0],
    };
    storageService.setUser(userData);
    setUser(userData);
    return userData;
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
