import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { storageService } from '../utils';

export const AUTH_LOGOUT_EVENT = 'platform:auth:logout';

const BASE_URL = process.env.REACT_APP_API_URL
  || process.env.EXPO_PUBLIC_API_URL
  || 'http://localhost:8081/api/v1';

const api: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
});

// ─── Request — attach JWT ─────────────────────────────────────────────────────
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = storageService.getAccessToken();
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Response — handle 401 with token refresh ─────────────────────────────────
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      const refreshToken = storageService.getRefreshToken();

      if (refreshToken) {
        try {
          const { data } = await axios.post(`${BASE_URL}/auth/refresh`, { refreshToken });
          const newToken: string = data.data.accessToken;
          storageService.setAccessToken(newToken);
          original.headers.Authorization = `Bearer ${newToken}`;
          return api(original);
        } catch {
          storageService.clear();
          dispatchLogout();
        }
      } else {
        dispatchLogout();
      }
    }

    return Promise.reject(error);
  }
);

function dispatchLogout(): void {
  // Web: dispatch DOM event — AuthContext listens
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event(AUTH_LOGOUT_EVENT));
  }
}

export default api;
