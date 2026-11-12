import { useTranslation } from 'react-i18next';
import {
  Users, Building2, GitBranch, Briefcase, Package,
  FileText, ArrowLeftRight, TrendingUp, UserCheck, UserX,
  ClipboardList, ScrollText, BarChart2,
} from 'lucide-react';

const MODULES = [
  { icon: Users,          key: 'employees' },
  { icon: Building2,      key: 'departments' },
  { icon: GitBranch,      key: 'branches' },
  { icon: Briefcase,      key: 'designations' },
  { icon: Package,        key: 'assets' },
  { icon: FileText,       key: 'documents' },
  { icon: ArrowLeftRight, key: 'transfers' },
  { icon: TrendingUp,     key: 'promotions' },
  { icon: UserCheck,      key: 'onboarding' },
  { icon: UserX,          key: 'offboarding' },
  { icon: ClipboardList,  key: 'skills' },
  { icon: ScrollText,     key: 'audit' },
  { icon: BarChart2,      key: 'reports' },
];

export default function JourneySection() {
  const { t } = useTranslation();

  return (
    <section id="modules" className="py-24 bg-[#F8FAFC]">
      <div className="mx-auto max-w-7xl px-6">
        <div className="text-center mb-16">
          <p className="text-sm font-semibold text-indigo-600 uppercase tracking-widest mb-3">
            {t('home.modules.eyebrow')}
          </p>
          <h2 className="text-4xl md:text-5xl font-black text-[#172033] tracking-tight mb-4">
            {t('home.modules.heading')}
          </h2>
          <p className="text-lg text-[#64748B] max-w-2xl mx-auto">
            {t('home.modules.description')}
          </p>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
          {MODULES.map(({ icon: Icon, key }) => (
            <div
              key={key}
              className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm hover:shadow-md hover:border-indigo-100 transition-all group"
            >
              <div className="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center mb-3 group-hover:bg-indigo-100 transition-colors">
                <Icon size={20} className="text-indigo-600" />
              </div>
              <h3 className="text-sm font-bold text-[#172033] mb-1">{t(`home.modules.${key}.title`)}</h3>
              <p className="text-xs text-[#64748B] leading-relaxed">{t(`home.modules.${key}.desc`)}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
