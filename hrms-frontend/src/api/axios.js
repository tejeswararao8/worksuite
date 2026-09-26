import axios from 'axios';
import storageService from '../services/storageService';

// Custom event — AuthContext listens to this to trigger logout without coupling to router
export const AUTH_LOGOUT_EVENT = 'hrms:auth:logout';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

// ─── Request interceptor — attach JWT ────────────────────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = storageService.getAccessToken();
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Response interceptor — handle 401 with token refresh ────────────────────
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;

    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      const refreshToken = storageService.getRefreshToken();

      if (refreshToken) {
        try {
          const { data } = await axios.post(
            `${process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1'}/auth/refresh`,
            { refreshToken }
          );
          const newToken = data.data.accessToken;
          storageService.setAccessToken(newToken);
          original.headers.Authorization = `Bearer ${newToken}`;
          return api(original);
        } catch {
          // Refresh failed — fire event so AuthContext can handle logout + redirect
          storageService.clear();
          window.dispatchEvent(new Event(AUTH_LOGOUT_EVENT));
        }
      } else {
        window.dispatchEvent(new Event(AUTH_LOGOUT_EVENT));
      }
    }

    return Promise.reject(error);
  }
);

export default api;
