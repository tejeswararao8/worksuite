export default function LeavePreview() {
  const requests = [
    { name: 'Priya Sharma', type: 'Annual Leave', from: 'Dec 23', to: 'Dec 27', days: 5, status: 'Approved' },
    { name: 'Rahul Mehta', type: 'Sick Leave', from: 'Dec 18', to: 'Dec 19', days: 2, status: 'Pending' },
    { name: 'Vikram Singh', type: 'Casual Leave', from: 'Dec 30', to: 'Dec 30', days: 1, status: 'Approved' },
  ];

  const balances = [
    { type: 'Annual', used: 8, total: 21, color: '#4F46E5' },
    { type: 'Sick', used: 3, total: 10, color: '#10B981' },
    { type: 'Casual', used: 2, total: 7, color: '#F59E0B' },
  ];

  return (
    <div className="rounded-2xl border border-slate-200 bg-white shadow-xl shadow-slate-100/80 overflow-hidden">
      {/* Leave balances */}
      <div className="px-5 py-4 border-b border-slate-100">
        <p className="text-xs font-semibold text-slate-700 mb-3">Leave Balances</p>
        <div className="flex flex-col gap-3">
          {balances.map((b) => (
            <div key={b.type}>
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs text-slate-600 font-medium">{b.type} Leave</span>
                <span className="text-xs text-slate-400">{b.used}/{b.total} days used</span>
              </div>
              <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                <div
                  className="h-full rounded-full transition-all"
                  style={{ width: `${(b.used / b.total) * 100}%`, background: b.color }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Leave requests */}
      <div className="px-5 py-4">
        <div className="flex items-center justify-between mb-3">
          <p className="text-xs font-semibold text-slate-700">Recent Requests</p>
          <div className="h-6 px-2.5 rounded-lg bg-indigo-600 text-white text-[10px] font-semibold flex items-center cursor-pointer">
            + Apply Leave
          </div>
        </div>
        <div className="flex flex-col gap-2">
          {requests.map((r) => (
            <div key={r.name} className="flex items-center gap-3 p-2.5 rounded-xl bg-slate-50 border border-slate-100">
              <div className="w-7 h-7 rounded-full bg-indigo-100 flex items-center justify-center text-[9px] font-bold text-indigo-600 flex-shrink-0">
                {r.name.split(' ').map(n => n[0]).join('')}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-[10px] font-semibold text-slate-800">{r.name}</p>
                <p className="text-[9px] text-slate-400">{r.type} · {r.from} – {r.to} ({r.days}d)</p>
              </div>
              <span className={`text-[9px] px-2 py-0.5 rounded-full font-semibold flex-shrink-0 ${
                r.status === 'Approved' ? 'bg-emerald-50 text-emerald-600' : 'bg-amber-50 text-amber-600'
              }`}>
                {r.status}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Mini calendar hint */}
      <div className="px-5 pb-4">
        <div className="rounded-xl bg-indigo-50 border border-indigo-100 p-3 flex items-center gap-3">
          <div className="text-2xl">📅</div>
          <div>
            <p className="text-xs font-semibold text-indigo-700">Leave Calendar</p>
            <p className="text-[10px] text-indigo-500">View team availability at a glance</p>
          </div>
        </div>
      </div>
    </div>
  );
}
