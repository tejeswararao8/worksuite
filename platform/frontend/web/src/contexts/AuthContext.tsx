import { createContext, useContext, useCallback, useEffect, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { authApi } from '../../../shared/api/services';
import { AUTH_LOGOUT_EVENT } from '../../../shared/api/axios';
import { setUser, clearUser } from '../../../shared/store/authSlice';
import { storageService } from '../../../shared/utils';
import type { RootState, AppDispatch } from '../../../shared/store';
import type { AuthUser, LoginRequest, TokenResponse } from '../../../shared/types';

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<AuthUser>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate   = useNavigate();
  const dispatch   = useDispatch<AppDispatch>();
  const user       = useSelector((s: RootState) => s.auth.user);
  const isAuthenticated = useSelector((s: RootState) => s.auth.isAuthenticated);

  const logout = useCallback(() => {
    storageService.clear();
    dispatch(clearUser());
    navigate('/login', { replace: true });
  }, [dispatch, navigate]);

  useEffect(() => {
    window.addEventListener(AUTH_LOGOUT_EVENT, logout);
    return () => window.removeEventListener(AUTH_LOGOUT_EVENT, logout);
  }, [logout]);

  const login = useCallback(async (credentials: LoginRequest): Promise<AuthUser> => {
    const { data } = await authApi.login(credentials);
    const token = data.data as TokenResponse;

    storageService.setAccessToken(token.accessToken);
    storageService.setRefreshToken(token.refreshToken);

    const userData: AuthUser = {
      userId:    token.userId,
      companyId: token.companyId,
      email:     token.email,
      role:      token.role,
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
