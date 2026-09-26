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
    const tokenData = data.data;

    storageService.setAccessToken(tokenData.accessToken);
    storageService.setRefreshToken(tokenData.refreshToken);

    const userData = {
      userId:    tokenData.userId,
      companyId: tokenData.companyId,
      email:     tokenData.email,
      role:      tokenData.role,
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
