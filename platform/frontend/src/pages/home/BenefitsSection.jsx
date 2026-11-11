const cards = [
  {
    emoji: '🏢',
    audience: 'HR Teams',
    heading: 'Automate the routine. Focus on people.',
    points: [
      'Centralized employee records and documents',
      'Automated payroll and compliance workflows',
      'Real-time HR analytics and custom reports',
      'Streamlined onboarding and offboarding',
    ],
    accent: 'bg-indigo-600',
    light: 'bg-indigo-50',
    text: 'text-indigo-600',
  },
  {
    emoji: '👔',
    audience: 'Managers',
    heading: 'Lead your team with clarity and confidence.',
    points: [
      'Team attendance and leave visibility',
      'Performance goals and review tracking',
      'Approval workflows at your fingertips',
      'Instant notifications and alerts',
    ],
    accent: 'bg-emerald-600',
    light: 'bg-emerald-50',
    text: 'text-emerald-600',
  },
  {
    emoji: '🙋',
    audience: 'Employees',
    heading: 'Everything you need, right when you need it.',
    points: [
      'Self-service leave applications and tracking',
      'Digital payslips and salary history',
      'Personal profile and document management',
      'Performance goals and feedback access',
    ],
    accent: 'bg-sky-600',
    light: 'bg-sky-50',
    text: 'text-sky-600',
  },
];

export default function BenefitsSection() {
  return (
    <section id="benefits" className="py-24 bg-[#F8FAFC]">
      <div className="mx-auto max-w-7xl px-6">
        <div className="text-center mb-16">
          <p className="text-sm font-semibold text-indigo-600 uppercase tracking-widest mb-3">
            For Everyone
          </p>
          <h2 className="text-4xl md:text-5xl font-black text-[#172033] tracking-tight mb-4">
            A better everyday.<br className="hidden md:block" /> For everyone.
          </h2>
          <p className="text-lg text-[#64748B] max-w-xl mx-auto">
            WorkSuite is designed to make work better for every person in your organization.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-6">
          {cards.map((c) => (
            <div
              key={c.audience}
              className="bg-white rounded-2xl border border-slate-100 shadow-sm hover:shadow-lg hover:border-slate-200 transition-all overflow-hidden group"
            >
              <div className={`${c.light} px-6 py-5 border-b border-slate-100`}>
                <div className="text-3xl mb-3">{c.emoji}</div>
                <p className={`text-xs font-bold uppercase tracking-widest ${c.text} mb-1`}>{c.audience}</p>
                <h3 className="text-base font-bold text-[#172033] leading-snug">{c.heading}</h3>
              </div>
              <div className="px-6 py-5">
                <ul className="flex flex-col gap-3">
                  {c.points.map((p) => (
                    <li key={p} className="flex items-start gap-2.5">
                      <span className={`mt-0.5 flex-shrink-0 w-4 h-4 rounded-full ${c.accent} flex items-center justify-center text-[9px] font-bold text-white`}>
                        ✓
                      </span>
                      <span className="text-sm text-[#64748B]">{p}</span>
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
