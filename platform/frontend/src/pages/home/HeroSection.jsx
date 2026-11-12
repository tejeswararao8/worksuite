import { Link } from 'react-router-dom';
import { ArrowRight, ChevronRight } from 'lucide-react';
import { useTranslation } from 'react-i18next';

function DashboardPreview() {
  return (
    <div className="relative rounded-2xl bg-white border border-slate-200 shadow-2xl shadow-indigo-100/50 overflow-hidden">
      <div className="bg-[#14213D] px-5 py-3 flex items-center gap-2">
        <div className="w-3 h-3 rounded-full bg-red-400" />
        <div className="w-3 h-3 rounded-full bg-yellow-400" />
        <div className="w-3 h-3 rounded-full bg-green-400" />
        <span className="ml-3 text-xs text-slate-400 font-medium">WorkSuite HRMS — Dashboard</span>
      </div>
      <div className="bg-slate-50 p-4">
        <div className="grid grid-cols-4 gap-3 mb-4">
          {[
            { label: 'Total Employees', value: '1,248', color: 'text-indigo-600', bg: 'bg-indigo-50' },
            { label: 'Present Today',   value: '1,102', color: 'text-emerald-600', bg: 'bg-emerald-50' },
            { label: 'On Leave',        value: '38',    color: 'text-amber-600',   bg: 'bg-amber-50' },
            { label: 'Open Positions',  value: '14',    color: 'text-sky-600',     bg: 'bg-sky-50' },
          ].map((s) => (
            <div key={s.label} className={`${s.bg} rounded-xl p-3`}>
              <p className="text-[10px] text-slate-500 font-medium leading-tight">{s.label}</p>
              <p className={`text-lg font-black ${s.color} mt-1`}>{s.value}</p>
            </div>
          ))}
        </div>
        <div className="grid grid-cols-3 gap-3 mb-4">
          <div className="col-span-2 bg-white rounded-xl p-3 border border-slate-100">
            <div className="flex items-center justify-between mb-2">
              <p className="text-xs font-semibold text-slate-700">Attendance Overview</p>
              <span className="text-[10px] text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full font-medium">+2.4%</span>
            </div>
            <div className="flex items-end gap-1.5 h-16">
              {[65,80,72,90,85,95,88,92,78,96,84,91].map((v, i) => (
                <div key={i} className="flex-1 rounded-t-sm" style={{ height: `${v}%`, background: i === 11 ? '#4F46E5' : '#E0E7FF' }} />
              ))}
            </div>
            <div className="flex justify-between mt-1">
              {['J','F','M','A','M','J','J','A','S','O','N','D'].map((m, i) => (
                <span key={i} className="text-[8px] text-slate-400">{m}</span>
              ))}
            </div>
          </div>
          <div className="bg-white rounded-xl p-3 border border-slate-100">
            <p className="text-xs font-semibold text-slate-700 mb-2">Dept. Split</p>
            <div className="flex items-center justify-center">
              <svg viewBox="0 0 80 80" className="w-16 h-16">
                <circle cx="40" cy="40" r="28" fill="none" stroke="#E0E7FF" strokeWidth="12" />
                <circle cx="40" cy="40" r="28" fill="none" stroke="#4F46E5" strokeWidth="12" strokeDasharray="70 106" strokeDashoffset="0" strokeLinecap="round" />
                <circle cx="40" cy="40" r="28" fill="none" stroke="#818CF8" strokeWidth="12" strokeDasharray="35 141" strokeDashoffset="-70" strokeLinecap="round" />
                <circle cx="40" cy="40" r="28" fill="none" stroke="#C7D2FE" strokeWidth="12" strokeDasharray="25 151" strokeDashoffset="-105" strokeLinecap="round" />
              </svg>
            </div>
            <div className="flex flex-col gap-1 mt-1">
              {[['Engineering','#4F46E5'],['HR','#818CF8'],['Sales','#C7D2FE']].map(([l,c]) => (
                <div key={l} className="flex items-center gap-1.5">
                  <div className="w-2 h-2 rounded-full" style={{ background: c }} />
                  <span className="text-[9px] text-slate-500">{l}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
        <div className="bg-white rounded-xl p-3 border border-slate-100">
          <div className="flex items-center justify-between mb-2">
            <p className="text-xs font-semibold text-slate-700">Recent Employees</p>
            <span className="text-[10px] text-indigo-600 font-medium">View all →</span>
          </div>
          <div className="flex flex-col gap-2">
            {[
              { name: 'Priya Sharma', role: 'Senior Engineer', dept: 'Engineering', status: 'Active', avatar: 'PS' },
              { name: 'Rahul Mehta',  role: 'HR Manager',      dept: 'Human Resources', status: 'Active', avatar: 'RM' },
              { name: 'Anita Patel',  role: 'Sales Lead',      dept: 'Sales',           status: 'On Leave', avatar: 'AP' },
            ].map((e) => (
              <div key={e.name} className="flex items-center gap-2">
                <div className="w-6 h-6 rounded-full bg-indigo-100 flex items-center justify-center text-[9px] font-bold text-indigo-600 flex-shrink-0">{e.avatar}</div>
                <div className="flex-1 min-w-0">
                  <p className="text-[10px] font-semibold text-slate-800 truncate">{e.name}</p>
                  <p className="text-[9px] text-slate-400 truncate">{e.role} · {e.dept}</p>
                </div>
                <span className={`text-[9px] px-1.5 py-0.5 rounded-full font-medium flex-shrink-0 ${e.status === 'Active' ? 'bg-emerald-50 text-emerald-600' : 'bg-amber-50 text-amber-600'}`}>
                  {e.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function HeroSection() {
  const { t } = useTranslation();

  return (
    <section className="pt-28 pb-20 bg-white" id="hero">
      <div className="mx-auto max-w-7xl px-6">
        <div className="grid md:grid-cols-2 gap-12 items-center">
          {/* Text */}
          <div>
            <div className="inline-flex items-center gap-2 rounded-full bg-indigo-50 border border-indigo-100 px-3 py-1.5 text-xs font-semibold text-indigo-700 mb-6">
              <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 animate-pulse" />
              {t('home.hero.badge')}
            </div>
            <h1 className="text-5xl md:text-6xl font-black tracking-tight text-[#172033] leading-[1.1] mb-6">
              {t('home.hero.headline1')}{' '}
              <span className="text-indigo-600">{t('home.hero.headline2')}</span>
            </h1>
            <p className="text-lg text-[#64748B] leading-relaxed mb-8 max-w-lg">
              {t('home.hero.description')}
            </p>
            <div className="flex flex-wrap gap-4">
              <Link
                to="/login"
                className="inline-flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white text-sm font-semibold rounded-xl hover:bg-indigo-500 transition-all shadow-lg shadow-indigo-600/25"
              >
                {t('home.hero.ctaPrimary')} <ArrowRight size={16} />
              </Link>
              <a
                href="#features"
                className="inline-flex items-center gap-2 px-6 py-3 bg-white text-slate-700 text-sm font-semibold rounded-xl border border-slate-200 hover:border-indigo-300 hover:text-indigo-600 transition-all"
              >
                <ChevronRight size={14} className="text-indigo-500" /> {t('home.hero.ctaSecondary')}
              </a>
            </div>

            {/* Stats — labeled as illustrative */}
            <div className="mt-10 pt-8 border-t border-slate-100">
              <p className="text-xs text-slate-400 mb-4 italic">{t('home.hero.statsNote')}</p>
              <div className="flex items-center gap-6">
                {[
                  { value: t('home.hero.stat1Value'), label: t('home.hero.stat1Label') },
                  { value: t('home.hero.stat2Value'), label: t('home.hero.stat2Label') },
                  { value: t('home.hero.stat3Value'), label: t('home.hero.stat3Label') },
                ].map((s) => (
                  <div key={s.label}>
                    <p className="text-xl font-black text-[#172033]">{s.value}</p>
                    <p className="text-xs text-[#64748B] mt-0.5">{s.label}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Dashboard preview */}
          <div className="relative">
            <div className="absolute -top-8 -right-8 w-64 h-64 bg-indigo-100 rounded-full blur-3xl opacity-50 pointer-events-none" />
            <div className="absolute -bottom-8 -left-8 w-48 h-48 bg-sky-100 rounded-full blur-3xl opacity-50 pointer-events-none" />
            <div className="relative"><DashboardPreview /></div>
          </div>
        </div>
      </div>
    </section>
  );
}
