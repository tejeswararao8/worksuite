export default function EmployeeManagementPreview() {
  const employees = [
    { name: 'Priya Sharma', role: 'Senior Engineer', dept: 'Engineering', status: 'Active', avatar: 'PS', color: 'bg-indigo-100 text-indigo-600' },
    { name: 'Rahul Mehta', role: 'HR Manager', dept: 'Human Resources', status: 'Active', avatar: 'RM', color: 'bg-emerald-100 text-emerald-600' },
    { name: 'Anita Patel', role: 'Sales Lead', dept: 'Sales', status: 'On Leave', avatar: 'AP', color: 'bg-amber-100 text-amber-600' },
    { name: 'Vikram Singh', role: 'Product Designer', dept: 'Design', status: 'Active', avatar: 'VS', color: 'bg-sky-100 text-sky-600' },
  ];

  return (
    <div className="rounded-2xl border border-slate-200 bg-white shadow-xl shadow-slate-100/80 overflow-hidden">
      {/* Header */}
      <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
        <div>
          <p className="text-xs text-slate-400 font-medium">Employee Directory</p>
          <p className="text-sm font-bold text-slate-800">All Employees</p>
        </div>
        <div className="flex items-center gap-2">
          <div className="h-7 px-3 rounded-lg bg-indigo-600 text-white text-xs font-semibold flex items-center">+ Add Employee</div>
        </div>
      </div>

      {/* Search bar */}
      <div className="px-5 py-3 border-b border-slate-100">
        <div className="flex items-center gap-2 bg-slate-50 rounded-lg px-3 py-2">
          <svg className="w-3.5 h-3.5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <span className="text-xs text-slate-400">Search employees...</span>
        </div>
      </div>

      {/* Table header */}
      <div className="px-5 py-2 grid grid-cols-4 gap-2 bg-slate-50 border-b border-slate-100">
        {['Name', 'Role', 'Department', 'Status'].map((h) => (
          <p key={h} className="text-[10px] font-semibold text-slate-400 uppercase tracking-wide">{h}</p>
        ))}
      </div>

      {/* Rows */}
      <div className="divide-y divide-slate-50">
        {employees.map((e) => (
          <div key={e.name} className="px-5 py-3 grid grid-cols-4 gap-2 items-center hover:bg-slate-50 transition-colors">
            <div className="flex items-center gap-2">
              <div className={`w-7 h-7 rounded-full flex items-center justify-center text-[10px] font-bold flex-shrink-0 ${e.color}`}>
                {e.avatar}
              </div>
              <span className="text-xs font-semibold text-slate-800 truncate">{e.name}</span>
            </div>
            <span className="text-xs text-slate-500 truncate">{e.role}</span>
            <span className="text-xs text-slate-500 truncate">{e.dept}</span>
            <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full w-fit ${
              e.status === 'Active' ? 'bg-emerald-50 text-emerald-600' : 'bg-amber-50 text-amber-600'
            }`}>
              {e.status}
            </span>
          </div>
        ))}
      </div>

      {/* Footer */}
      <div className="px-5 py-3 border-t border-slate-100 flex items-center justify-between">
        <span className="text-xs text-slate-400">Showing 4 of 1,248 employees</span>
        <div className="flex gap-1">
          {[1,2,3].map((p) => (
            <div key={p} className={`w-6 h-6 rounded flex items-center justify-center text-[10px] font-semibold ${p === 1 ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:bg-slate-100'}`}>{p}</div>
          ))}
        </div>
      </div>
    </div>
  );
}
