import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowRight, ArrowLeft, Users, BarChart2, Clock, CheckCircle } from 'lucide-react';

const HeroSection: React.FC = () => {
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';
  const Arrow = isRtl ? ArrowLeft : ArrowRight;

  return (
    <section id="hero" className="relative pt-28 pb-20 overflow-hidden bg-[#F5F7FF]">
      <div className="absolute top-0 right-0 w-1/2 h-full bg-indigo-50/50 -skew-x-12 transform translate-x-1/4 pointer-events-none" />

      <div className="container mx-auto px-6 relative z-10">
        <div className="flex flex-col items-center text-center max-w-4xl mx-auto mb-16">
          <div className={`inline-flex items-center ${isRtl ? 'space-x-reverse space-x-2' : 'space-x-2'} bg-indigo-100 text-indigo-700 px-4 py-2 rounded-full text-sm font-bold mb-8`}>
            <span className="flex h-2 w-2 rounded-full bg-indigo-600 animate-pulse shrink-0" />
            <span>{t('homepage.badge')}</span>
          </div>

          <h1 className="text-5xl md:text-7xl font-extrabold text-slate-900 leading-tight mb-8">
            {t('homepage.heroHeadline1')}<br />
            <span className="text-indigo-600">{t('homepage.heroHeadline2')}</span>
          </h1>

          <p className="text-xl text-slate-600 mb-12 max-w-2xl leading-relaxed">
            {t('homepage.heroDesc')}
          </p>

          <div className={`flex flex-col sm:flex-row items-center gap-4 ${isRtl ? 'sm:flex-row-reverse' : ''}`}>
            <button onClick={() => navigate('/login')}
              className={`bg-indigo-600 text-white px-8 py-4 rounded-full font-bold text-lg hover:bg-indigo-700 transition-all flex items-center gap-2 shadow-xl shadow-indigo-200 group ${isRtl ? 'flex-row-reverse' : ''}`}>
              {t('homepage.getStarted')}
              <Arrow size={20} className="group-hover:translate-x-1 transition-transform" />
            </button>
            <button className="bg-white text-slate-700 border border-slate-200 px-8 py-4 rounded-full font-bold text-lg hover:bg-slate-50 transition-all">
              {t('homepage.bookDemo')}
            </button>
          </div>
        </div>

        {/* Coded dashboard preview — no external images */}
        <div className="relative max-w-5xl mx-auto">
          <div className="bg-white rounded-3xl shadow-2xl border border-slate-100 p-3 hover:shadow-indigo-100 transition-shadow duration-700">
            <div className="bg-slate-50 rounded-2xl overflow-hidden border border-slate-100 p-4">
              {/* Mock top bar */}
              <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-200">
                <div className="flex items-center gap-2">
                  <div className="w-7 h-7 bg-indigo-600 rounded-md flex items-center justify-center">
                    <span className="text-white text-xs font-bold">W</span>
                  </div>
                  <span className="text-sm font-bold text-slate-700">WorkSuite</span>
                </div>
                <div className="flex gap-2">
                  <div className="w-2 h-2 rounded-full bg-red-400" />
                  <div className="w-2 h-2 rounded-full bg-yellow-400" />
                  <div className="w-2 h-2 rounded-full bg-green-400" />
                </div>
              </div>
              {/* Stat cards row */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
                {[
                  { icon: Users,      label: 'Employees', val: '248', color: 'bg-indigo-50 text-indigo-600' },
                  { icon: CheckCircle,label: 'Active',    val: '231', color: 'bg-green-50 text-green-600'  },
                  { icon: Clock,      label: 'On Leave',  val: '12',  color: 'bg-amber-50 text-amber-600'  },
                  { icon: BarChart2,  label: 'Reports',   val: '6',   color: 'bg-purple-50 text-purple-600'},
                ].map((s, i) => (
                  <div key={i} className="bg-white rounded-xl p-3 border border-slate-100 flex items-center gap-3">
                    <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${s.color}`}>
                      <s.icon size={16} />
                    </div>
                    <div>
                      <p className="text-xs text-slate-400">{s.label}</p>
                      <p className="text-base font-bold text-slate-800">{s.val}</p>
                    </div>
                  </div>
                ))}
              </div>
              {/* Mini bar chart */}
              <div className="bg-white rounded-xl p-3 border border-slate-100">
                <p className="text-xs font-semibold text-slate-600 mb-3">Workforce Overview</p>
                <div className="flex items-end gap-1.5 h-16">
                  {[40,65,50,80,70,90,75,85,60,95,80,88].map((h, i) => (
                    <div key={i} className="flex-1 bg-indigo-100 rounded-t-sm" style={{ height: `${h}%` }}>
                      <div className="w-full bg-indigo-500 rounded-t-sm h-full opacity-70" />
                    </div>
                  ))}
                </div>
                <p className="text-[10px] text-slate-400 mt-1 text-center italic">{t('common.demoData')}</p>
              </div>
            </div>
          </div>

          {/* Floating cards */}
          <div className={`absolute -left-4 lg:-left-12 top-1/4 bg-white p-3 rounded-2xl shadow-xl border border-slate-100 hidden lg:block`}>
            <div className={`flex items-center gap-3 ${isRtl ? 'flex-row-reverse' : ''}`}>
              <div className="w-9 h-9 bg-green-100 rounded-full flex items-center justify-center shrink-0">
                <CheckCircle size={16} className="text-green-600" />
              </div>
              <div>
                <p className="text-xs text-slate-400">Attendance</p>
                <p className="text-sm font-bold text-slate-800">98% Present</p>
              </div>
            </div>
          </div>
          <div className={`absolute -right-4 lg:-right-12 bottom-1/4 bg-white p-3 rounded-2xl shadow-xl border border-slate-100 hidden lg:block`}>
            <div className={`flex items-center gap-3 ${isRtl ? 'flex-row-reverse' : ''}`}>
              <div className="w-9 h-9 bg-indigo-100 rounded-full flex items-center justify-center shrink-0">
                <Users size={16} className="text-indigo-600" />
              </div>
              <div>
                <p className="text-xs text-slate-400">New Hires</p>
                <p className="text-sm font-bold text-slate-800">+12 This Month</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
