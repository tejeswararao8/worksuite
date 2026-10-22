import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import enTranslation from './locales/en/translation.json';
import arTranslation from './locales/ar/translation.json';
import hiTranslation from './locales/hi/translation.json';

export const SUPPORTED_LANGUAGES = [
  { code: 'en', label: 'English', dir: 'ltr' },
  { code: 'ar', label: 'العربية', dir: 'rtl' },
  { code: 'hi', label: 'हिंदी',   dir: 'ltr' },
];

export const RTL_LANGUAGES = ['ar'];

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: enTranslation },
      ar: { translation: arTranslation },
      hi: { translation: hiTranslation },
    },
    fallbackLng: 'en',
    interpolation: { escapeValue: false },
    detection: {
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],
    },
  });

/** Apply RTL/LTR direction to the document root. */
export function applyLanguageDirection(lang) {
  const isRtl = RTL_LANGUAGES.includes(lang);
  document.documentElement.dir = isRtl ? 'rtl' : 'ltr';
  document.documentElement.lang = lang;
}

export default i18n;
