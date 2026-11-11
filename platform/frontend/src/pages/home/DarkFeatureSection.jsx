export default function DarkFeatureSection() {
  const benefits = [
    { icon: '📊', title: 'Real-time Analytics', desc: 'Live dashboards with HR metrics, trends, and actionable insights.' },
    { icon: '🔒', title: 'Enterprise Security', desc: 'Role-based access, audit logs, and SOC 2 compliant data handling.' },
    { icon: '🌐', title: 'Multi-location Support', desc: 'Manage teams across branches, cities, and time zones seamlessly.' },
    { icon: '⚡', title: 'Workflow Automation', desc: 'Automate approvals, reminders, and repetitive HR tasks.' },
  ];

  return (
    <section id="solutions" className="py-24 bg-[#14213D]">
      <div className="mx-auto max-w-7xl px-6">
        <div className="grid md:grid-cols-2 gap-12 lg:gap-20 items-center">
          {/* Text */}
          <div>
            <p className="text-sm font-semibold text-indigo-300 uppercase tracking-widest mb-3">
              Unified Platform
            </p>
            <h2 className="text-3xl md:text-4xl font-black tracking-tight text-white mb-5">
              Everything in sync.<br />Everyone empowered.
            </h2>
            <p className="text-base text-slate-300 leading-relaxed mb-10">
              WorkSuite connects every HR function into one intelligent platform — so your data, your team, and your decisions are always aligned.
            </p>
            <div className="grid sm:grid-cols-2 gap-5">
              {benefits.map((b) => (
                <div key={b.title} className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl bg-indigo-900/60 flex items-center justify-center text-xl flex-shrink-0">
                    {b.icon}
                  </div>
                  <div>
                    <p className="text-sm font-bold text-white mb-1">{b.title}</p>
                    <p className="text-xs text-slate-400 leading-relaxed">{b.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Analytics preview */}
          <div className="relative">
            <div className="absolute -top-8 -right-8 w-48 h-48 bg-indigo-600/20 rounded-full blur-3xl pointer-events-none" />
            <div className="relative rounded-2xl bg-[#1E2D4A] border border-white/10 shadow-2xl overflow-hidden">
              {/* Top bar */}
              <div className="px-5 py-4 border-b border-white/10">
                <p className="text-xs text-slate-400 font-medium">HR Analytics</p>
                <p className="text-sm font-bold text-white">Company Overview</p>
              </div>

              {/* KPI row */}
              <div className="px-5 py-4 grid grid-cols-2 gap-3 border-b border-white/10">
                {[
                  { label: 'Headcount Growth', value: '+18.4%', sub: 'vs last year' },
                  { label: 'Retention Rate', value: '94.2%', sub: 'rolling 12 months' },
                  { label: 'Avg. Time to Hire', value: '18 days', sub: '-3 days vs target' },
                  { label: 'eNPS Score', value: '72', sub: 'Excellent' },
                ].map((k) => (
                  <div key={k.label} className="bg-white/5 rounded-xl p-3">
                    <p className="text-[10px] text-slate-400 mb-1">{k.label}</p>
                    <p className="text-base font-black text-indigo-300">{k.value}</p>
                    <p className="text-[9px] text-slate-500 mt-0.5">{k.sub}</p>
                  </div>
                ))}
              </div>

              {/* Trend chart */}
              <div className="px-5 py-4">
                <p className="text-xs font-semibold text-slate-300 mb-3">Headcount Trend</p>
                <div className="flex items-end gap-1.5 h-16">
                  {[820, 870, 910, 950, 980, 1020, 1080, 1120, 1160, 1200, 1230, 1248].map((v, i) => (
                    <div
                      key={i}
                      className="flex-1 rounded-t-sm"
                      style={{
                        height: `${((v - 800) / 500) * 100}%`,
                        background: i === 11 ? '#818CF8' : 'rgba(129,140,248,0.3)',
                      }}
                    />
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
