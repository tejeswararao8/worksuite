import React from 'react';
import { useTranslation } from 'react-i18next';

const PayrollSection: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';
  const items: string[] = t('homepage.payrollItems', { returnObjects: true }) as string[];

  return (
    <section className="py-20 bg-slate-50">
      <div className="container mx-auto px-6">
        <div className={`flex flex-col ${isRtl ? 'lg:flex-row' : 'lg:flex-row-reverse'} items-center gap-16`}>
          <div className="lg:w-1/2">
            <span className="inline-block bg-amber-100 text-amber-700 text-xs font-bold px-3 py-1 rounded-full mb-4 uppercase tracking-wider">
              {t('homepage.planned')}
            </span>
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              {t('homepage.payrollTitle')}<br />
              <span className="text-indigo-600">{t('homepage.payrollHighlight')}</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8 leading-relaxed">{t('homepage.payrollDesc')}</p>
            <ul className="space-y-3 mb-6">
              {items.map((item, i) => (
                <li key={i} className={`flex items-center gap-3 text-slate-700 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <div className="w-6 h-6 bg-green-100 text-green-600 rounded-full flex items-center justify-center shrink-0 text-xs font-bold">✓</div>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
            <p className="text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2">
              ⚠ {t('homepage.payrollPlanned')}
            </p>
          </div>
          <div className="lg:w-1/2 bg-white p-6 rounded-3xl border border-slate-100 shadow-lg w-full">
            <div className="bg-indigo-900 p-6 rounded-2xl text-white">
              <h4 className="font-bold mb-4 opacity-80 text-sm">{t('homepage.payrollSummaryTitle')}</h4>
              <p className="text-xs opacity-60 mb-1">{t('homepage.payrollNetPay')}</p>
              <p className="text-3xl font-bold mb-6">$42,500.00</p>
              <div className={`grid grid-cols-2 gap-4 ${isRtl ? 'text-right' : ''}`}>
                <div>
                  <p className="text-xs opacity-60">{t('homepage.payrollGross')}</p>
                  <p className="font-semibold">$50,000</p>
                </div>
                <div>
                  <p className="text-xs opacity-60">{t('homepage.payrollDeductions')}</p>
                  <p className="font-semibold">$7,500</p>
                </div>
              </div>
              <div className={`mt-6 pt-4 border-t border-white/10 flex justify-between items-center text-xs ${isRtl ? 'flex-row-reverse' : ''}`}>
                <span>{t('homepage.payrollStatus')}</span>
                <span className="bg-green-500 text-white px-2 py-0.5 rounded font-medium">{t('homepage.payrollPaid')}</span>
              </div>
              <p className="text-[10px] mt-4 opacity-40 text-center uppercase tracking-widest italic">{t('common.demoData')}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default PayrollSection;
