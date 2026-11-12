import { useTranslation } from 'react-i18next';
import { BarChart2, Lock, Globe, Zap } from 'lucide-react';

const BENEFIT_ICONS = [BarChart2, Lock, Globe, Zap];

export default function DarkFeatureSection() {
  const { t } = useTranslation();

  const benefits = [
    { icon: BENEFIT_ICONS[0], key: 'analytics' },
    { icon: BENEFIT_ICONS[1], key: 'security' },
    { icon: BENEFIT_ICONS[2], key: 'multiLocation' },
    { icon: BENEFIT_ICONS[3], key: 'automation' },
  ];

  return (
    <section id="features" className="py-24 bg-[#14213D]">
      <div className="mx-auto max-w-7xl px-6">
        <div className="grid md:grid-cols-2 gap-12 lg:gap-20 items-center">
          {/* Text */}
          <div>
            <p className="text-sm font-semibold text-indigo-300 uppercase tracking-widest mb-3">
              {t('home.platform.eyebrow')}
            </p>
            <h2 className="text-3xl md:text-4xl font-black tracking-tight text-white mb-5">
              {t('home.platform.heading')}
            </h2>
            <p className="text-base text-slate-300 leading-relaxed mb-10">
              {t('home.platform.description')}
            </p>
            <div className="grid sm:grid-cols-2 gap-5">
              {benefits.map(({ icon: Icon, key }) => (
                <div key={key} className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl bg-indigo-900/60 flex items-center justify-center flex-shrink-0">
                    <Icon size={18} className="text-indigo-300" />
                  </div>
                  <div>
                    <p className="text-sm font-bold text-white mb-1">{t(`home.platform.${key}.title`)}</p>
                    <p className="text-xs text-slate-400 leading-relaxed">{t(`home.platform.${key}.desc`)}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Analytics preview — illustrative demo data */}
          <div className="relative">
            <div className="absolute -top-8 -right-8 w-48 h-48 bg-indigo-600/20 rounded-full blur-3xl pointer-events-none" />
            <div className="relative rounded-2xl bg-[#1E2D4A] border border-white/10 shadow-2xl overflow-hidden">
              <div className="px-5 py-4 border-b border-white/10">
                <p className="text-xs text-slate-400 font-medium">{t('home.platform.previewLabel')}</p>
                <p className="text-sm font-bold text-white">{t('home.platform.previewTitle')}</p>
              </div>
              <div className="px-5 py-4 grid grid-cols-2 gap-3 border-b border-white/10">
                {[
                  { labelKey: 'home.platform.kpi1Label', value: '+18.4%', subKey: 'home.platform.kpi1Sub' },
                  { labelKey: 'home.platform.kpi2Label', value: '94.2%',  subKey: 'home.platform.kpi2Sub' },
                  { labelKey: 'home.platform.kpi3Label', value: '18d',    subKey: 'home.platform.kpi3Sub' },
                  { labelKey: 'home.platform.kpi4Label', value: '72',     subKey: 'home.platform.kpi4Sub' },
                ].map((k) => (
                  <div key={k.labelKey} className="bg-white/5 rounded-xl p-3">
                    <p className="text-[10px] text-slate-400 mb-1">{t(k.labelKey)}</p>
                    <p className="text-base font-black text-indigo-300">{k.value}</p>
                    <p className="text-[9px] text-slate-500 mt-0.5">{t(k.subKey)}</p>
                  </div>
                ))}
              </div>
              <div className="px-5 py-4">
                <p className="text-xs font-semibold text-slate-300 mb-1">{t('home.platform.trendLabel')}</p>
                <p className="text-[9px] text-slate-500 mb-3 italic">{t('home.platform.demoNote')}</p>
                <div className="flex items-end gap-1.5 h-16">
                  {[820,870,910,950,980,1020,1080,1120,1160,1200,1230,1248].map((v, i) => (
                    <div key={i} className="flex-1 rounded-t-sm" style={{ height: `${((v-800)/500)*100}%`, background: i === 11 ? '#818CF8' : 'rgba(129,140,248,0.3)' }} />
                  ))}
                </div>
                <div className="flex justify-between mt-1">
                  {['J','F','M','A','M','J','J','A','S','O','N','D'].map((m, i) => (
                    <span key={i} className="text-[8px] text-slate-500">{m}</span>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
