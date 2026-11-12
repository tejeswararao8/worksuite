import React from 'react';
import { useTranslation } from 'react-i18next';
import { Layers, Settings2, Package, Users, HeadphonesIcon } from 'lucide-react';

const ICONS = [Layers, Settings2, Package, Users, HeadphonesIcon];

interface ServiceItem { title: string; desc: string; }

const ServicesSection: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl  = i18n.language === 'ar';
  const items: ServiceItem[] = t('homepage.servicesItems', { returnObjects: true }) as ServiceItem[];

  return (
    <section id="services" className="py-24 bg-slate-50">
      <div className="container mx-auto px-6">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
            {t('homepage.servicesTitle')}
          </h2>
          <p className="text-xl text-slate-600 leading-relaxed">{t('homepage.servicesDesc')}</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {items.map((item, i) => {
            const Icon = ICONS[i] ?? Layers;
            return (
              <div key={i} className={`bg-white rounded-2xl p-6 border border-slate-200 hover:border-indigo-300 hover:shadow-xl hover:shadow-indigo-50 transition-all group ${isRtl ? 'text-right' : ''}`}>
                <div className={`flex items-center gap-4 mb-4 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <div className="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center shrink-0 group-hover:bg-indigo-100 transition-colors">
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

export default ServicesSection;
