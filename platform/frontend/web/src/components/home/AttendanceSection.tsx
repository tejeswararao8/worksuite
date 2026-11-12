import React from 'react';
import { useTranslation } from 'react-i18next';

const AttendanceSection: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';
  const items: string[] = t('homepage.attendanceItems', { returnObjects: true }) as string[];

  return (
    <section className="py-20 bg-slate-50">
      <div className="container mx-auto px-6">
        <div className={`flex flex-col ${isRtl ? 'lg:flex-row' : 'lg:flex-row-reverse'} items-center gap-16`}>
          <div className="lg:w-1/2">
            <span className="inline-block bg-amber-100 text-amber-700 text-xs font-bold px-3 py-1 rounded-full mb-4 uppercase tracking-wider">
              {t('homepage.planned')}
            </span>
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              {t('homepage.attendanceTitle')}<br />
              <span className="text-indigo-600">{t('homepage.attendanceHighlight')}</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8 leading-relaxed">{t('homepage.attendanceDesc')}</p>
            <ul className="space-y-3 mb-6">
              {items.map((item, i) => (
                <li key={i} className={`flex items-center gap-3 text-slate-700 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <div className="w-6 h-6 bg-green-100 text-green-600 rounded-full flex items-center justify-center shrink-0 text-xs font-bold">✓</div>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
            <p className="text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2">
              ⚠ {t('homepage.attendancePlanned')}
            </p>
          </div>
          <div className="lg:w-1/2 bg-white p-6 rounded-3xl border border-slate-100 shadow-lg w-full">
            <div className="bg-slate-50 p-4 rounded-xl">
              <h4 className="font-bold text-slate-800 mb-4">{t('homepage.attendancePreviewTitle')}</h4>
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-white p-4 rounded-lg shadow-sm border border-slate-100 text-center">
                  <p className="text-3xl font-bold text-green-600">85</p>
                  <p className="text-sm text-slate-500">{t('homepage.attendancePresent')}</p>
                </div>
                <div className="bg-white p-4 rounded-lg shadow-sm border border-slate-100 text-center">
                  <p className="text-3xl font-bold text-red-500">5</p>
                  <p className="text-sm text-slate-500">{t('homepage.attendanceAbsent')}</p>
                </div>
              </div>
              <p className="text-[10px] mt-4 text-slate-400 text-center italic">{t('common.demoData')}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default AttendanceSection;
