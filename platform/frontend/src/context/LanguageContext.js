import { createContext, useContext, useCallback, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { SUPPORTED_LANGUAGES, applyLanguageDirection } from '../i18n';

const LanguageContext = createContext(null);

export function LanguageProvider({ children }) {
  const { i18n } = useTranslation();
  const [currentLang, setCurrentLang] = useState(i18n.language?.slice(0, 2) || 'en');

  const changeLanguage = useCallback((lang) => {
    i18n.changeLanguage(lang);
    setCurrentLang(lang);
    applyLanguageDirection(lang);
  }, [i18n]);

  const isRtl = SUPPORTED_LANGUAGES.find((l) => l.code === currentLang)?.dir === 'rtl';

  return (
    <LanguageContext.Provider value={{ currentLang, changeLanguage, isRtl, supportedLanguages: SUPPORTED_LANGUAGES }}>
      {children}
    </LanguageContext.Provider>
  );
}

export const useLanguage = () => useContext(LanguageContext);
