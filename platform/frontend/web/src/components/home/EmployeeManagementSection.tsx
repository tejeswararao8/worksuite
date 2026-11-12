import React from 'react';
import { useTranslation } from 'react-i18next';

const EmployeeManagementSection: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';
  const items: string[] = t('homepage.empMgmtItems', { returnObjects: true }) as string[];

  return (
    <section className="py-20 bg-white">
      <div className="container mx-auto px-6">
        <div className={`flex flex-col ${isRtl ? 'lg:flex-row-reverse' : 'lg:flex-row'} items-center gap-16`}>
          <div className="lg:w-1/2">
            <span className="inline-block bg-green-100 text-green-700 text-xs font-bold px-3 py-1 rounded-full mb-4 uppercase tracking-wider">
              {t('homepage.available')}
            </span>
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              {t('homepage.empMgmtTitle').replace(t('homepage.empMgmtHighlight'), '')}<br />
              <span className="text-indigo-600">{t('homepage.empMgmtHighlight')}</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8 leading-relaxed">{t('homepage.empMgmtDesc')}</p>
            <ul className="space-y-3">
              {items.map((item, i) => (
                <li key={i} className={`flex items-center gap-3 text-slate-700 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <div className="w-6 h-6 bg-green-100 text-green-600 rounded-full flex items-center justify-center shrink-0 text-xs font-bold">✓</div>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
          <div className="lg:w-1/2 bg-slate-50 p-6 rounded-3xl border border-slate-100 shadow-inner w-full">
            <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-100">
              <div className={`flex justify-between items-center mb-4 ${isRtl ? 'flex-row-reverse' : ''}`}>
                <h4 className="font-bold text-slate-800">{t('homepage.empDirTitle')}</h4>
                <button className="text-indigo-600 text-sm font-semibold">{t('homepage.empDirViewAll')}</button>
              </div>
              <div className="space-y-3">
                {[['AK','Arjun Kumar','Engineering | Senior Developer'],['PS','Priya Sharma','HR | HR Manager'],['MR','Mohammed Raza','Finance | Analyst']].map(([initials, name, role], i) => (
                  <div key={i} className={`flex items-center gap-4 border-b border-slate-100 pb-3 last:border-0 last:pb-0 ${isRtl ? 'flex-row-reverse' : ''}`}>
                    <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center font-bold text-indigo-700 shrink-0 text-sm">{initials}</div>
                    <div className={isRtl ? 'text-right' : ''}>
                      <p className="font-bold text-slate-800 text-sm">{name}</p>
                      <p className="text-xs text-slate-500">{role}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default EmployeeManagementSection;
