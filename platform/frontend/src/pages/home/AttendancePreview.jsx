export default function AttendancePreview() {
  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  const bars = [92, 88, 95, 91, 97, 60, 20];

  const records = [
    { name: 'Priya Sharma', checkIn: '09:02 AM', checkOut: '06:15 PM', hours: '9h 13m', status: 'Present' },
    { name: 'Rahul Mehta', checkIn: '09:30 AM', checkOut: '06:00 PM', hours: '8h 30m', status: 'Present' },
    { name: 'Anita Patel', checkIn: '—', checkOut: '—', hours: '—', status: 'On Leave' },
    { name: 'Vikram Singh', checkIn: '10:15 AM', checkOut: '07:00 PM', hours: '8h 45m', status: 'Late' },
  ];

  return (
    <div className="rounded-2xl border border-slate-200 bg-white shadow-xl shadow-slate-100/80 overflow-hidden">
      {/* Header stats */}
      <div className="px-5 py-4 border-b border-slate-100">
        <p className="text-xs text-slate-400 font-medium mb-3">Today's Overview</p>
        <div className="grid grid-cols-3 gap-3">
          {[
            { label: 'Present', value: '1,102', color: 'text-emerald-600', bg: 'bg-emerald-50' },
            { label: 'Absent', value: '108', color: 'text-red-500', bg: 'bg-red-50' },
            { label: 'On Leave', value: '38', color: 'text-amber-600', bg: 'bg-amber-50' },
          ].map((s) => (
            <div key={s.label} className={`${s.bg} rounded-xl p-3 text-center`}>
              <p className={`text-lg font-black ${s.color}`}>{s.value}</p>
              <p className="text-[10px] text-slate-500 mt-0.5">{s.label}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Weekly chart */}
      <div className="px-5 py-4 border-b border-slate-100">
        <p className="text-xs font-semibold text-slate-700 mb-3">Weekly Attendance Rate</p>
        <div className="flex items-end gap-2 h-16">
          {bars.map((v, i) => (
            <div key={i} className="flex-1 flex flex-col items-center gap-1">
              <div
                className="w-full rounded-t-md transition-all"
                style={{ height: `${v}%`, background: v > 80 ? '#4F46E5' : v > 50 ? '#818CF8' : '#E0E7FF' }}
              />
              <span className="text-[9px] text-slate-400">{days[i]}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Records */}
      <div className="px-5 py-3">
        <p className="text-xs font-semibold text-slate-700 mb-2">Today's Records</p>
        <div className="flex flex-col gap-2">
          {records.map((r) => (
            <div key={r.name} className="flex items-center gap-3 py-1.5 border-b border-slate-50 last:border-0">
              <div className="w-6 h-6 rounded-full bg-indigo-100 flex items-center justify-center text-[9px] font-bold text-indigo-600 flex-shrink-0">
                {r.name.split(' ').map(n => n[0]).join('')}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-[10px] font-semibold text-slate-800">{r.name}</p>
                <p className="text-[9px] text-slate-400">{r.checkIn} → {r.checkOut}</p>
              </div>
              <span className={`text-[9px] px-1.5 py-0.5 rounded-full font-semibold flex-shrink-0 ${
                r.status === 'Present' ? 'bg-emerald-50 text-emerald-600' :
                r.status === 'Late' ? 'bg-amber-50 text-amber-600' :
                'bg-slate-100 text-slate-500'
              }`}>
                {r.status}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
