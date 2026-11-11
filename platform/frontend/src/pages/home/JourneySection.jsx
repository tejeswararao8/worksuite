const modules = [
  {
    icon: '👥',
    title: 'Employee Management',
    desc: 'Centralized profiles, org charts, and employee directory.',
  },
  {
    icon: '🕐',
    title: 'Attendance Tracking',
    desc: 'Real-time check-in/out, shift management, and analytics.',
  },
  {
    icon: '📅',
    title: 'Leave Management',
    desc: 'Streamlined requests, approvals, balances, and calendars.',
  },
  {
    icon: '💰',
    title: 'Payroll Processing',
    desc: 'Automated salary runs, deductions, and payslip generation.',
  },
  {
    icon: '🎯',
    title: 'Recruitment',
    desc: 'Job postings, candidate pipeline, and interview scheduling.',
  },
  {
    icon: '📈',
    title: 'Performance Reviews',
    desc: 'Goal setting, 360° feedback, and growth analytics.',
  },
];

export default function JourneySection() {
  return (
    <section id="features" className="py-24 bg-[#F8FAFC]">
      <div className="mx-auto max-w-7xl px-6">
        <div className="text-center mb-16">
          <p className="text-sm font-semibold text-indigo-600 uppercase tracking-widest mb-3">
            Complete Lifecycle
          </p>
          <h2 className="text-4xl md:text-5xl font-black text-[#172033] tracking-tight mb-4">
            Built for every part of the<br className="hidden md:block" /> employee journey.
          </h2>
          <p className="text-lg text-[#64748B] max-w-2xl mx-auto">
            From the first offer letter to the final farewell, WorkSuite manages the complete employee lifecycle in one place.
          </p>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {modules.map((m) => (
            <div
              key={m.title}
              className="bg-white rounded-2xl p-6 border border-slate-100 shadow-sm hover:shadow-md hover:border-indigo-100 transition-all group"
            >
              <div className="w-12 h-12 rounded-xl bg-indigo-50 flex items-center justify-center text-2xl mb-4 group-hover:bg-indigo-100 transition-colors">
                {m.icon}
              </div>
              <h3 className="text-base font-bold text-[#172033] mb-2">{m.title}</h3>
              <p className="text-sm text-[#64748B] leading-relaxed">{m.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
