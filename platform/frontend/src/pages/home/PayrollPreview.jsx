export default function PayrollPreview() {
  const employees = [
    { name: 'Priya Sharma', role: 'Sr. Engineer', gross: '₹1,20,000', deductions: '₹18,400', net: '₹1,01,600' },
    { name: 'Rahul Mehta', role: 'HR Manager', gross: '₹85,000', deductions: '₹12,750', net: '₹72,250' },
    { name: 'Anita Patel', role: 'Sales Lead', gross: '₹95,000', deductions: '₹14,250', net: '₹80,750' },
  ];

  return (
    <div className="rounded-2xl border border-slate-200 bg-white shadow-xl shadow-slate-100/80 overflow-hidden">
      {/* Summary cards */}
      <div className="px-5 py-4 border-b border-slate-100">
        <div className="flex items-center justify-between mb-3">
          <p className="text-xs font-semibold text-slate-700">December 2024 Payroll</p>
          <span className="text-[10px] bg-emerald-50 text-emerald-600 px-2 py-0.5 rounded-full font-semibold">Processed</span>
        </div>
        <div className="grid grid-cols-3 gap-3">
          {[
            { label: 'Total Gross', value: '₹48.2L', color: 'text-slate-800' },
            { label: 'Deductions', value: '₹7.1L', color: 'text-red-500' },
            { label: 'Net Payout', value: '₹41.1L', color: 'text-indigo-600' },
          ].map((s) => (
            <div key={s.label} className="bg-slate-50 rounded-xl p-3">
              <p className="text-[10px] text-slate-400 mb-1">{s.label}</p>
              <p className={`text-sm font-black ${s.color}`}>{s.value}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Payslip rows */}
      <div className="px-5 py-3">
        <p className="text-xs font-semibold text-slate-700 mb-2">Payslip Summary</p>
        <div className="flex flex-col gap-2">
          {employees.map((e) => (
            <div key={e.name} className="flex items-center gap-3 p-2.5 rounded-xl border border-slate-100 hover:bg-slate-50 transition-colors">
              <div className="w-7 h-7 rounded-full bg-indigo-100 flex items-center justify-center text-[9px] font-bold text-indigo-600 flex-shrink-0">
                {e.name.split(' ').map(n => n[0]).join('')}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-[10px] font-semibold text-slate-800">{e.name}</p>
                <p className="text-[9px] text-slate-400">{e.role}</p>
              </div>
              <div className="text-right flex-shrink-0">
                <p className="text-[10px] font-bold text-indigo-600">{e.net}</p>
                <p className="text-[9px] text-slate-400">Net pay</p>
              </div>
              <div className="w-6 h-6 rounded-lg bg-indigo-50 flex items-center justify-center cursor-pointer hover:bg-indigo-100 transition-colors">
                <svg className="w-3 h-3 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                </svg>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Deduction breakdown */}
      <div className="px-5 pb-4">
        <div className="rounded-xl bg-slate-50 border border-slate-100 p-3">
          <p className="text-[10px] font-semibold text-slate-600 mb-2">Deduction Breakdown</p>
          <div className="flex gap-2">
            {[['PF', 40, '#4F46E5'], ['Tax', 35, '#818CF8'], ['ESI', 15, '#C7D2FE'], ['Other', 10, '#E0E7FF']].map(([l, w, c]) => (
              <div key={l} className="flex-1">
                <div className="h-1.5 rounded-full mb-1" style={{ background: c }} />
                <p className="text-[9px] text-slate-400 text-center">{l}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
