import { useLanguage } from '../../context/LanguageContext';
import { useTranslation } from 'react-i18next';
import { Globe } from 'lucide-react';

export default function LanguageSwitcher() {
  const { t } = useTranslation();
  const { currentLang, changeLanguage, supportedLanguages } = useLanguage();

  return (
    <div className="flex items-center gap-1">
      <Globe size={16} className="text-slate-400" />
      <select
        value={currentLang}
        onChange={(e) => changeLanguage(e.target.value)}
        className="text-sm border-none bg-transparent text-slate-600 focus:outline-none cursor-pointer"
        aria-label={t('language.select')}
      >
        {supportedLanguages.map((lang) => (
          <option key={lang.code} value={lang.code}>{lang.label}</option>
        ))}
      </select>
    </div>
  );
}
