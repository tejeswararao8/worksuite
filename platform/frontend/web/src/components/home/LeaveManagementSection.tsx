import React from 'react';
import { useTranslation } from 'react-i18next';

const LeaveManagementSection: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';
  const items: string[] = t('homepage.leaveItems', { returnObjects: true }) as string[];

  return (
    <section className="py-20 bg-white">
      <div className="container mx-auto px-6">
        <div className={`flex flex-col ${isRtl ? 'lg:flex-row-reverse' : 'lg:flex-row'} items-center gap-16`}>
          <div className="lg:w-1/2">
            <span className="inline-block bg-amber-100 text-amber-700 text-xs font-bold px-3 py-1 rounded-full mb-4 uppercase tracking-wider">
              {t('homepage.planned')}
            </span>
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              {t('homepage.leaveTitle')}<br />
              <span className="text-indigo-600">{t('homepage.leaveHighlight')}</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8 leading-relaxed">{t('homepage.leaveDesc')}</p>
            <ul className="space-y-3 mb-6">
              {items.map((item, i) => (
                <li key={i} className={`flex items-center gap-3 text-slate-700 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <div className="w-6 h-6 bg-green-100 text-green-600 rounded-full flex items-center justify-center shrink-0 text-xs font-bold">✓</div>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
            <p className="text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2">
              ⚠ {t('homepage.leavePlanned')}
            </p>
          </div>
          <div className="lg:w-1/2 bg-slate-50 p-6 rounded-3xl border border-slate-100 shadow-inner w-full">
            <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-100">
              <h4 className="font-bold text-slate-800 mb-4">{t('homepage.leavePreviewTitle')}</h4>
              <div className="space-y-3">
                {[1, 2].map(i => (
                  <div key={i} className={`flex justify-between items-center border-b border-slate-100 pb-3 last:border-0 last:pb-0 ${isRtl ? 'flex-row-reverse' : ''}`}>
                    <div className={isRtl ? 'text-right' : ''}>
                      <p className="font-bold text-slate-800 text-sm">Jane Doe</p>
                      <p className="text-xs text-slate-500">{t('homepage.leaveCasual')}</p>
                    </div>
                    <div className={`flex gap-2 ${isRtl ? 'flex-row-reverse' : ''}`}>
                      <button className="text-xs bg-green-100 text-green-700 px-3 py-1 rounded-full font-medium">{t('homepage.leaveApprove')}</button>
                      <button className="text-xs bg-red-100 text-red-700 px-3 py-1 rounded-full font-medium">{t('homepage.leaveReject')}</button>
                    </div>
                  </div>
                ))}
              </div>
              <p className="text-[10px] mt-3 text-slate-400 text-center italic">{t('common.demoData')}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default LeaveManagementSection;
