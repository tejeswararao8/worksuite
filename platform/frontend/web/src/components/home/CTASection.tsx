import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const CTASection: React.FC = () => {
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';

  return (
    <section className="py-20 px-6">
      <div className="container mx-auto max-w-6xl">
        <div className="bg-indigo-50 rounded-[3rem] p-12 lg:p-20 text-center relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-full opacity-10 pointer-events-none">
            <div className="absolute top-10 left-10 w-40 h-40 bg-indigo-600 rounded-full blur-3xl" />
            <div className="absolute bottom-10 right-10 w-40 h-40 bg-indigo-600 rounded-full blur-3xl" />
          </div>
          <div className="relative z-10">
            <h2 className="text-4xl md:text-6xl font-extrabold text-slate-900 mb-8 leading-tight">
              {t('homepage.ctaTitle')}
            </h2>
            <p className="text-xl text-slate-600 mb-12 max-w-2xl mx-auto leading-relaxed">
              {t('homepage.ctaDesc')}
            </p>
            <div className={`flex flex-col sm:flex-row items-center justify-center gap-4 ${isRtl ? 'sm:flex-row-reverse' : ''}`}>
              <button onClick={() => navigate('/login')}
                className="bg-indigo-600 text-white px-10 py-5 rounded-full font-bold text-lg hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-200 w-full sm:w-auto">
                {t('homepage.ctaBtn1')}
              </button>
              <button className="bg-white text-slate-700 border border-slate-200 px-10 py-5 rounded-full font-bold text-lg hover:bg-slate-50 transition-all w-full sm:w-auto">
                {t('homepage.ctaBtn2')}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default CTASection;
