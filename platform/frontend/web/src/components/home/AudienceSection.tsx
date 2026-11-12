import React from 'react';
import { useTranslation } from 'react-i18next';
import { Building2, Settings } from 'lucide-react';

const AudienceSection: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';

  const orgsItems:     string[] = t('homepage.forOrgsItems',     { returnObjects: true }) as string[];
  const platformItems: string[] = t('homepage.forPlatformItems', { returnObjects: true }) as string[];

  return (
    <section id="audience" className="py-24 bg-slate-50">
      <div className="container mx-auto px-6">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
            {t('homepage.audienceTitle')}
          </h2>
          <p className="text-xl text-slate-600">{t('homepage.audienceDesc')}</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* For Organizations */}
          <div className="bg-white rounded-3xl border border-slate-200 p-8 hover:shadow-xl hover:shadow-indigo-50 transition-all">
            <div className={`flex items-center gap-4 mb-6 ${isRtl ? 'flex-row-reverse' : ''}`}>
              <div className="w-14 h-14 bg-indigo-100 rounded-2xl flex items-center justify-center shrink-0">
                <Building2 size={28} className="text-indigo-600" />
              </div>
              <div className={isRtl ? 'text-right' : ''}>
                <h3 className="text-2xl font-extrabold text-slate-900">{t('homepage.forOrgs')}</h3>
                <p className="text-slate-500 text-sm mt-1">{t('homepage.forOrgsDesc')}</p>
              </div>
            </div>
            <ul className="space-y-3">
              {orgsItems.map((item, i) => (
                <li key={i} className={`flex items-start gap-3 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <div className="w-5 h-5 bg-green-100 text-green-600 rounded-full flex items-center justify-center shrink-0 mt-0.5 text-xs font-bold">✓</div>
                  <span className="text-slate-700">{item}</span>
                </li>
              ))}
            </ul>
          </div>

          {/* For Platform Service Teams */}
          <div className="bg-indigo-600 rounded-3xl p-8 hover:shadow-xl hover:shadow-indigo-200 transition-all text-white">
            <div className={`flex items-center gap-4 mb-6 ${isRtl ? 'flex-row-reverse' : ''}`}>
              <div className="w-14 h-14 bg-white/20 rounded-2xl flex items-center justify-center shrink-0">
                <Settings size={28} className="text-white" />
              </div>
              <div className={isRtl ? 'text-right' : ''}>
                <h3 className="text-2xl font-extrabold">{t('homepage.forPlatform')}</h3>
                <p className="text-indigo-200 text-sm mt-1">{t('homepage.forPlatformDesc')}</p>
              </div>
            </div>
            <ul className="space-y-3">
              {platformItems.map((item, i) => (
                <li key={i} className={`flex items-start gap-3 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <div className="w-5 h-5 bg-white/30 text-white rounded-full flex items-center justify-center shrink-0 mt-0.5 text-xs font-bold">✓</div>
                  <span className="text-indigo-100">{item}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
};

export default AudienceSection;
