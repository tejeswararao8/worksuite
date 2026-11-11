export default function PerformancePreview() {
  const employees = [
    { name: 'Priya Sharma', score: 92, trend: '+4%', goals: 8, completed: 7 },
    { name: 'Rahul Mehta', score: 85, trend: '+2%', goals: 6, completed: 5 },
    { name: 'Vikram Singh', score: 78, trend: '-1%', goals: 5, completed: 3 },
  ];

  return (
    <div className="rounded-2xl border border-slate-200 bg-white shadow-xl shadow-slate-100/80 overflow-hidden">
      {/* Header */}
      <div className="px-5 py-4 border-b border-slate-100">
        <p className="text-xs text-slate-400 font-medium">Q4 2024</p>
        <p className="text-sm font-bold text-slate-800">Performance Reviews</p>
      </div>

      {/* Team average */}
      <div className="px-5 py-4 border-b border-slate-100">
        <div className="flex items-center gap-4">
          <div className="relative w-16 h-16 flex-shrink-0">
            <svg viewBox="0 0 64 64" className="w-full h-full -rotate-90">
              <circle cx="32" cy="32" r="26" fill="none" stroke="#E0E7FF" strokeWidth="8" />
              <circle cx="32" cy="32" r="26" fill="none" stroke="#4F46E5" strokeWidth="8"
                strokeDasharray={`${(85 / 100) * 163} 163`} strokeLinecap="round" />
            </svg>
            <div className="absolute inset-0 flex items-center justify-center">
              <span className="text-sm font-black text-indigo-600">85</span>
            </div>
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-700">Team Average Score</p>
            <p className="text-[10px] text-slate-400 mt-0.5">Based on 1,248 reviews</p>
            <span className="text-[10px] bg-emerald-50 text-emerald-600 px-2 py-0.5 rounded-full font-semibold mt-1 inline-block">
              +3.2% vs last quarter
            </span>
          </div>
        </div>
      </div>

      {/* Employee scores */}
      <div className="px-5 py-3">
        <p className="text-xs font-semibold text-slate-700 mb-2">Top Performers</p>
        <div className="flex flex-col gap-3">
          {employees.map((e) => (
            <div key={e.name}>
              <div className="flex items-center justify-between mb-1">
                <div className="flex items-center gap-2">
                  <div className="w-5 h-5 rounded-full bg-indigo-100 flex items-center justify-center text-[8px] font-bold text-indigo-600">
                    {e.name.split(' ').map(n => n[0]).join('')}
                  </div>
                  <span className="text-[10px] font-semibold text-slate-700">{e.name}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`text-[9px] font-semibold ${e.trend.startsWith('+') ? 'text-emerald-600' : 'text-red-500'}`}>
                    {e.trend}
                  </span>
                  <span className="text-[10px] font-bold text-slate-800">{e.score}/100</span>
                </div>
              </div>
              <div className="h-1.5 bg-slate-100 rounded-full overflow-hidden">
                <div
                  className="h-full rounded-full bg-indigo-500 transition-all"
                  style={{ width: `${e.score}%` }}
                />
              </div>
              <p className="text-[9px] text-slate-400 mt-0.5">{e.completed}/{e.goals} goals completed</p>
            </div>
          ))}
        </div>
      </div>

      {/* Review cycle */}
      <div className="px-5 pb-4">
        <div className="rounded-xl bg-indigo-50 border border-indigo-100 p-3 flex items-center gap-3">
          <div className="text-xl">🎯</div>
          <div>
            <p className="text-xs font-semibold text-indigo-700">Annual Review Cycle Active</p>
            <p className="text-[10px] text-indigo-500">Deadline: Dec 31, 2024 · 847 pending</p>
          </div>
        </div>
      </div>
    </div>
  );
}
