import axios from 'axios';
import { getAccessToken, refreshTokens } from './keycloakAuth';

// Custom event — AuthContext listens to this to trigger logout without coupling to router
export const AUTH_LOGOUT_EVENT = 'hrms:auth:logout';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

// ─── Request interceptor — attach JWT from sessionStorage ────────────────────
api.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Response interceptor — silent token refresh on 401 ──────────────────────
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;

    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;

      try {
        const result = await refreshTokens();
        if (result) {
          original.headers.Authorization = `Bearer ${result.accessToken}`;
          return api(original);
        }
      } catch {
        // fall through to logout
      }

      // Refresh failed — fire event so AuthContext can handle logout + Keycloak redirect
      window.dispatchEvent(new Event(AUTH_LOGOUT_EVENT));
    }

    return Promise.reject(error);
  }
);

export default api;
