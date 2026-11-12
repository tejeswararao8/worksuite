import { useTranslation } from 'react-i18next';
import { Building2, Lock, UserCog, LayoutDashboard } from 'lucide-react';

const ARCH_ICONS = [Building2, Lock, UserCog, LayoutDashboard];
const ARCH_KEYS  = ['isolation', 'rbac', 'workspaces', 'admin'];

export default function MultiTenantSection() {
  const { t } = useTranslation();

  return (
    <section id="about" className="py-24 bg-[#F8FAFC]">
      <div className="mx-auto max-w-7xl px-6">
        <div className="grid md:grid-cols-2 gap-12 lg:gap-20 items-center">
          {/* Text */}
          <div>
            <p className="text-sm font-semibold text-indigo-600 uppercase tracking-widest mb-3">
              {t('home.multiTenant.eyebrow')}
            </p>
            <h2 className="text-3xl md:text-4xl font-black text-[#172033] tracking-tight mb-5">
              {t('home.multiTenant.heading')}
            </h2>
            <p className="text-base text-[#64748B] leading-relaxed mb-8">
              {t('home.multiTenant.description')}
            </p>
            <div className="grid sm:grid-cols-2 gap-5">
              {ARCH_KEYS.map((key, i) => {
                const Icon = ARCH_ICONS[i];
                return (
                  <div key={key} className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center flex-shrink-0">
                      <Icon size={18} className="text-indigo-600" />
                    </div>
                    <div>
                      <p className="text-sm font-bold text-[#172033] mb-1">{t(`home.multiTenant.${key}.title`)}</p>
                      <p className="text-xs text-[#64748B] leading-relaxed">{t(`home.multiTenant.${key}.desc`)}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Architecture diagram */}
          <div className="relative">
            <div className="absolute -top-6 -right-6 w-48 h-48 bg-indigo-50 rounded-full blur-3xl opacity-60 pointer-events-none" />
            <div className="relative rounded-2xl bg-white border border-slate-200 shadow-xl overflow-hidden p-6">
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-widest mb-5">{t('home.multiTenant.diagramTitle')}</p>
              {/* Platform layer */}
              <div className="rounded-xl bg-indigo-600 p-4 mb-4">
                <p className="text-xs font-bold text-white text-center">{t('home.multiTenant.platformLayer')}</p>
              </div>
              {/* Tenant row */}
              <div className="grid grid-cols-3 gap-3 mb-4">
                {['A', 'B', 'C'].map((org) => (
                  <div key={org} className="rounded-xl bg-indigo-50 border border-indigo-100 p-3 text-center">
                    <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white text-xs font-bold mx-auto mb-2">
                      {org}
                    </div>
                    <p className="text-[10px] font-semibold text-indigo-700">{t('home.multiTenant.orgLabel')} {org}</p>
                    <p className="text-[9px] text-slate-400 mt-0.5">{t('home.multiTenant.isolatedData')}</p>
                  </div>
                ))}
              </div>
              {/* Data layer */}
              <div className="rounded-xl bg-slate-50 border border-slate-200 p-3 text-center">
                <p className="text-xs font-semibold text-slate-600">{t('home.multiTenant.dataLayer')}</p>
                <p className="text-[10px] text-slate-400 mt-0.5">{t('home.multiTenant.dataLayerDesc')}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
