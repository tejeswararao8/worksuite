import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

const en = require('./locales/en.json');
const ar = require('./locales/ar.json');
const hi = require('./locales/hi.json');

const RTL_LANGS = ['ar'];

export function applyDirection(lang: string) {
  const isRtl = RTL_LANGS.includes(lang);
  document.documentElement.dir  = isRtl ? 'rtl' : 'ltr';
  document.documentElement.lang = lang;
}

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      ar: { translation: ar },
      hi: { translation: hi },
    },
    fallbackLng: 'en',
    interpolation: { escapeValue: false },
    detection: {
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],
      lookupLocalStorage: 'i18nextLng',
    },
  });

// Apply direction on initial load
applyDirection(i18n.language);

// Apply direction whenever language changes
i18n.on('languageChanged', applyDirection);

export default i18n;
