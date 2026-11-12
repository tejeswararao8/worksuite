import { useTranslation } from 'react-i18next';
import { Building2, Settings } from 'lucide-react';

export default function BenefitsSection() {
  const { t } = useTranslation();

  const audiences = [
    {
      key: 'org',
      Icon: Building2,
      accent: 'bg-indigo-600',
      light: 'bg-indigo-50',
      text: 'text-indigo-600',
      border: 'border-indigo-100',
      points: ['emp', 'attendance', 'payroll', 'onboarding', 'reports'],
    },
    {
      key: 'platform',
      Icon: Settings,
      accent: 'bg-emerald-600',
      light: 'bg-emerald-50',
      text: 'text-emerald-600',
      border: 'border-emerald-100',
      points: ['onboarding', 'tenant', 'subscription', 'implementation', 'support'],
    },
  ];

  return (
    <section id="audiences" className="py-24 bg-[#F8FAFC]">
      <div className="mx-auto max-w-7xl px-6">
        <div className="text-center mb-16">
          <p className="text-sm font-semibold text-indigo-600 uppercase tracking-widest mb-3">
            {t('home.audiences.eyebrow')}
          </p>
          <h2 className="text-4xl md:text-5xl font-black text-[#172033] tracking-tight mb-4">
            {t('home.audiences.heading')}
          </h2>
          <p className="text-lg text-[#64748B] max-w-2xl mx-auto">
            {t('home.audiences.description')}
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-8">
          {audiences.map(({ key, Icon, accent, light, text, border, points }) => (
            <div
              key={key}
              className={`bg-white rounded-2xl border ${border} shadow-sm hover:shadow-lg transition-all overflow-hidden`}
            >
              <div className={`${light} px-6 py-6 border-b ${border}`}>
                <div className={`w-10 h-10 rounded-xl ${accent} flex items-center justify-center mb-4`}>
                  <Icon size={20} className="text-white" />
                </div>
                <p className={`text-xs font-bold uppercase tracking-widest ${text} mb-2`}>
                  {t(`home.audiences.${key}.label`)}
                </p>
                <h3 className="text-lg font-bold text-[#172033] leading-snug">
                  {t(`home.audiences.${key}.heading`)}
                </h3>
                <p className="text-sm text-[#64748B] mt-2 leading-relaxed">
                  {t(`home.audiences.${key}.description`)}
                </p>
              </div>
              <div className="px-6 py-5">
                <ul className="flex flex-col gap-3">
                  {points.map((p) => (
                    <li key={p} className="flex items-start gap-2.5">
                      <span className={`mt-0.5 flex-shrink-0 w-4 h-4 rounded-full ${accent} flex items-center justify-center text-[9px] font-bold text-white`}>
                        ✓
                      </span>
                      <span className="text-sm text-[#64748B]">{t(`home.audiences.${key}.${p}`)}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
