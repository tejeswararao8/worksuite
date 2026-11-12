import { useTranslation } from 'react-i18next';
import { Rocket, Wrench, Users, HeadphonesIcon, Database, ShieldCheck } from 'lucide-react';

const SERVICE_ICONS = [Rocket, Wrench, Users, HeadphonesIcon, Database, ShieldCheck];
const SERVICE_KEYS  = ['implementation', 'configuration', 'empOnboarding', 'support', 'dataMigration', 'accessControl'];

export default function ServicesSection() {
  const { t } = useTranslation();

  return (
    <section id="services" className="py-24 bg-white">
      <div className="mx-auto max-w-7xl px-6">
        <div className="text-center mb-16">
          <p className="text-sm font-semibold text-indigo-600 uppercase tracking-widest mb-3">
            {t('home.services.eyebrow')}
          </p>
          <h2 className="text-4xl md:text-5xl font-black text-[#172033] tracking-tight mb-4">
            {t('home.services.heading')}
          </h2>
          <p className="text-lg text-[#64748B] max-w-2xl mx-auto">
            {t('home.services.description')}
          </p>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {SERVICE_KEYS.map((key, i) => {
            const Icon = SERVICE_ICONS[i];
            return (
              <div
                key={key}
                className="group bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-md hover:border-indigo-100 transition-all"
              >
                <div className="w-11 h-11 rounded-xl bg-indigo-50 flex items-center justify-center mb-4 group-hover:bg-indigo-100 transition-colors">
                  <Icon size={20} className="text-indigo-600" />
                </div>
                <h3 className="text-base font-bold text-[#172033] mb-2">
                  {t(`home.services.${key}.title`)}
                </h3>
                <p className="text-sm text-[#64748B] leading-relaxed">
                  {t(`home.services.${key}.desc`)}
                </p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
