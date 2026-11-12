import React from 'react';
import { useTranslation } from 'react-i18next';
import { Building2, SlidersHorizontal, Shield, GitBranch, Package, Lock } from 'lucide-react';

const ICONS = [Building2, SlidersHorizontal, Shield, GitBranch, Package, Lock];

interface TenantItem { title: string; desc: string; }

const MultiTenantSection: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl  = i18n.language === 'ar';
  const items: TenantItem[] = t('homepage.tenantItems', { returnObjects: true }) as TenantItem[];

  return (
    <section className="py-24 bg-white">
      <div className="container mx-auto px-6">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
            {t('homepage.tenantTitle')}
          </h2>
          <p className="text-xl text-slate-600 leading-relaxed">{t('homepage.tenantDesc')}</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {items.map((item, i) => {
            const Icon = ICONS[i] ?? Shield;
            return (
              <div key={i} className={`bg-slate-50 rounded-2xl p-6 border border-slate-100 hover:border-indigo-200 hover:shadow-lg hover:shadow-indigo-50 transition-all ${isRtl ? 'text-right' : ''}`}>
                <div className={`flex items-center gap-4 mb-4 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <div className="w-12 h-12 bg-indigo-100 rounded-xl flex items-center justify-center shrink-0">
                    <Icon size={22} className="text-indigo-600" />
                  </div>
                  <h3 className="font-bold text-slate-900 text-base leading-snug">{item.title}</h3>
                </div>
                <p className="text-slate-600 text-sm leading-relaxed">{item.desc}</p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

export default MultiTenantSection;
