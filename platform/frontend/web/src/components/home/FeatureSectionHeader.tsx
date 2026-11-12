import React from 'react';
import { useTranslation } from 'react-i18next';

const FeatureSectionHeader: React.FC = () => {
  const { t } = useTranslation();
  return (
    <div id="features" className="py-16 bg-white">
      <div className="container mx-auto px-6 text-center max-w-3xl mx-auto">
        <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
          {t('homepage.featuresTitle')}
        </h2>
        <p className="text-xl text-slate-600">{t('homepage.featuresDesc')}</p>
      </div>
    </div>
  );
};

export default FeatureSectionHeader;
