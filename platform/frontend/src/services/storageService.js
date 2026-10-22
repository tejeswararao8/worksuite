// All localStorage access is centralized here.
// No component or hook should call localStorage directly.

const KEYS = {
  ACCESS_TOKEN:  'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  USER:          'user',
  LANGUAGE:      'i18nextLng',
};

const storageService = {
  getAccessToken:  () => localStorage.getItem(KEYS.ACCESS_TOKEN),
  setAccessToken:  (token) => localStorage.setItem(KEYS.ACCESS_TOKEN, token),

  getRefreshToken: () => localStorage.getItem(KEYS.REFRESH_TOKEN),
  setRefreshToken: (token) => localStorage.setItem(KEYS.REFRESH_TOKEN, token),

  getUser:  () => {
    const raw = localStorage.getItem(KEYS.USER);
    try { return raw ? JSON.parse(raw) : null; } catch { return null; }
  },
  setUser:  (user) => localStorage.setItem(KEYS.USER, JSON.stringify(user)),

  getLanguage: () => localStorage.getItem(KEYS.LANGUAGE),
  setLanguage: (lang) => localStorage.setItem(KEYS.LANGUAGE, lang),

  clear: () => localStorage.clear(),
};

export default storageService;
