import React from 'react';
import { useTranslation } from 'react-i18next';

const AnalyticsSection: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';

  const stats = [
    { labelKey: 'homepage.statEmployees',   val: '248', inc: '+12' },
    { labelKey: 'homepage.statAttendance',  val: '94%', inc: '+2.1%' },
    { labelKey: 'homepage.statJobs',        val: '14',  inc: '+3' },
    { labelKey: 'homepage.statLeave',       val: '8',   inc: '-2' },
    { labelKey: 'homepage.statPerformance', val: '4.2', inc: '+0.4' },
    { labelKey: 'homepage.statRetention',   val: '98%', inc: '+1%' },
  ];

  const insights = [
    t('homepage.insight1'),
    t('homepage.insight2'),
    t('homepage.insight3'),
  ];

  return (
    <section className="py-24 bg-[#172554] text-white overflow-hidden relative">
      <div className="absolute top-0 right-0 w-96 h-96 bg-indigo-500/10 rounded-full -translate-y-1/2 translate-x-1/2 blur-3xl pointer-events-none" />
      <div className="absolute bottom-0 left-0 w-96 h-96 bg-indigo-500/10 rounded-full translate-y-1/2 -translate-x-1/2 blur-3xl pointer-events-none" />

      <div className="container mx-auto px-6 relative z-10">
        <div className={`text-center max-w-3xl mx-auto mb-16 ${isRtl ? 'text-right md:text-center' : ''}`}>
          <h2 className="text-4xl md:text-5xl font-extrabold mb-6 leading-tight">
            {t('homepage.analyticsTitle')}<br />
            {t('homepage.analyticsHighlight')}
          </h2>
          <p className="text-xl text-indigo-200/80">{t('homepage.analyticsDesc')}</p>
          <p className="text-xs text-indigo-300/60 mt-3 italic">{t('homepage.analyticsNote')}</p>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-12">
          {stats.map((s, i) => (
            <div key={i} className={`bg-white/5 backdrop-blur-sm border border-white/10 p-5 rounded-2xl hover:bg-white/10 transition-colors ${isRtl ? 'text-right' : ''}`}>
              <p className="text-xs text-indigo-300 uppercase tracking-wider mb-2 leading-tight">{t(s.labelKey)}</p>
              <p className="text-2xl font-bold mb-1">{s.val}</p>
              <p className="text-xs text-green-400 font-medium">{s.inc} {t('homepage.statThisMonth')}</p>
            </div>
          ))}
        </div>

        <div className="bg-white/5 backdrop-blur-md border border-white/10 rounded-3xl p-8 lg:p-12">
          <div className={`flex flex-col lg:flex-row justify-between items-end gap-8 ${isRtl ? 'lg:flex-row-reverse' : ''}`}>
            <div className="flex-1 w-full">
              <div className={`flex justify-between items-center mb-8 ${isRtl ? 'flex-row-reverse' : ''}`}>
                <h4 className="font-bold text-lg">{t('homepage.chartTitle')}</h4>
                <div className={`flex gap-4 text-sm text-indigo-300 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <span className={`flex items-center gap-2 ${isRtl ? 'flex-row-reverse' : ''}`}>
                    <span className="w-2 h-2 bg-indigo-500 rounded-full" /> {t('homepage.chartGrowth')}
                  </span>
                  <span className={`flex items-center gap-2 ${isRtl ? 'flex-row-reverse' : ''}`}>
                    <span className="w-2 h-2 bg-green-500 rounded-full" /> {t('homepage.chartRetention')}
                  </span>
                </div>
              </div>
              <div className="flex items-end justify-between h-48 w-full gap-1.5 lg:gap-3">
                {[45,60,55,80,70,90,85,75,95,80,85,90].map((h, i) => (
                  <div key={i} className="flex-1 bg-indigo-500/20 rounded-t-lg group relative cursor-pointer hover:bg-indigo-500/40 transition-colors h-full flex items-end">
                    <div className="w-full bg-indigo-500 rounded-t-lg" style={{ height: `${h}%` }} />
                    <div className="absolute -top-8 left-1/2 -translate-x-1/2 bg-white text-slate-900 text-[10px] font-bold py-1 px-2 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
                      {h}%
                    </div>
                  </div>
                ))}
              </div>
              <div className={`flex justify-between mt-4 text-[10px] text-indigo-300/60 uppercase tracking-widest font-bold ${isRtl ? 'flex-row-reverse' : ''}`}>
                <span>Jan</span><span>Mar</span><span>May</span><span>Jul</span><span>Sep</span><span>Dec</span>
              </div>
            </div>

            <div className={`w-full lg:w-72 bg-indigo-500 rounded-2xl p-6 shadow-2xl shadow-indigo-900/50 ${isRtl ? 'text-right' : ''}`}>
              <h5 className="font-bold mb-4">{t('homepage.insightsTitle')}</h5>
              <ul className="space-y-4 text-sm">
                {insights.map((ins, i) => (
                  <li key={i} className={`flex items-start gap-2 ${isRtl ? 'flex-row-reverse' : ''}`}>
                    <span className="text-indigo-200 mt-1 shrink-0">•</span>
                    <span>{ins}</span>
                  </li>
                ))}
              </ul>
              <p className="text-[10px] mt-6 opacity-40 text-center uppercase tracking-widest italic">{t('common.demoData')}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default AnalyticsSection;
