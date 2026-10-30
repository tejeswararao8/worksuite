import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// Import translation files from the old frontend (reused)
const en = require('./locales/en.json');
const ar = require('./locales/ar.json');
const hi = require('./locales/hi.json');

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
  });

export default i18n;
