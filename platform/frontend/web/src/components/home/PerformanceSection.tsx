import React from 'react';
import { useTranslation } from 'react-i18next';

const PerformanceSection: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';
  const items: string[] = t('homepage.perfItems', { returnObjects: true }) as string[];

  const goals = [
    { labelKey: 'homepage.perfGoal1', val: 85, color: 'bg-indigo-600' },
    { labelKey: 'homepage.perfGoal2', val: 92, color: 'bg-green-500'  },
    { labelKey: 'homepage.perfGoal3', val: 64, color: 'bg-amber-500'  },
  ];

  return (
    <section className="py-20 bg-slate-50">
      <div className="container mx-auto px-6">
        <div className={`flex flex-col ${isRtl ? 'lg:flex-row' : 'lg:flex-row-reverse'} items-center gap-16`}>
          <div className="lg:w-1/2">
            <span className="inline-block bg-amber-100 text-amber-700 text-xs font-bold px-3 py-1 rounded-full mb-4 uppercase tracking-wider">
              {t('homepage.planned')}
            </span>
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              {t('homepage.perfTitle')}<br />
              <span className="text-indigo-600">{t('homepage.perfHighlight')}</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8 leading-relaxed">{t('homepage.perfDesc')}</p>
            <ul className="space-y-3 mb-6">
              {items.map((item, i) => (
                <li key={i} className={`flex items-center gap-3 text-slate-700 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <div className="w-6 h-6 bg-green-100 text-green-600 rounded-full flex items-center justify-center shrink-0 text-xs font-bold">✓</div>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
            <p className="text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2">
              ⚠ {t('homepage.perfPlanned')}
            </p>
          </div>
          <div className="lg:w-1/2 bg-white p-6 rounded-3xl border border-slate-100 shadow-lg w-full">
            <h4 className="font-bold text-slate-800 mb-6 text-sm">{t('homepage.perfGoalsTitle')}</h4>
            <div className="space-y-5">
              {goals.map((g, i) => (
                <div key={i}>
                  <div className={`flex justify-between text-xs mb-1.5 ${isRtl ? 'flex-row-reverse' : ''}`}>
                    <span className="font-semibold text-slate-700">{t(g.labelKey)}</span>
                    <span className="text-slate-500">{g.val}%</span>
                  </div>
                  <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                    <div className={`h-full ${g.color} rounded-full`} style={{ width: `${g.val}%` }} />
                  </div>
                </div>
              ))}
            </div>
            <p className="text-[10px] mt-6 text-slate-400 text-center italic">{t('common.demoData')}</p>
          </div>
        </div>
      </div>
    </section>
  );
};

export default PerformanceSection;
