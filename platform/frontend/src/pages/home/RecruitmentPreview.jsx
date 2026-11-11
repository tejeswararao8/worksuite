export default function RecruitmentPreview() {
  const jobs = [
    { title: 'Senior React Developer', dept: 'Engineering', applicants: 24, stage: 'Interviewing' },
    { title: 'HR Business Partner', dept: 'Human Resources', applicants: 18, stage: 'Screening' },
    { title: 'Product Designer', dept: 'Design', applicants: 31, stage: 'Offer' },
  ];

  const pipeline = [
    { stage: 'Applied', count: 73, color: 'bg-slate-200' },
    { stage: 'Screened', count: 41, color: 'bg-indigo-200' },
    { stage: 'Interview', count: 18, color: 'bg-indigo-400' },
    { stage: 'Offer', count: 6, color: 'bg-indigo-600' },
    { stage: 'Hired', count: 3, color: 'bg-emerald-500' },
  ];

  return (
    <div className="rounded-2xl border border-slate-200 bg-white shadow-xl shadow-slate-100/80 overflow-hidden">
      {/* Header */}
      <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
        <div>
          <p className="text-xs text-slate-400 font-medium">Recruitment</p>
          <p className="text-sm font-bold text-slate-800">Active Job Openings</p>
        </div>
        <div className="h-7 px-3 rounded-lg bg-indigo-600 text-white text-xs font-semibold flex items-center cursor-pointer">
          + Post Job
        </div>
      </div>

      {/* Pipeline */}
      <div className="px-5 py-4 border-b border-slate-100">
        <p className="text-xs font-semibold text-slate-700 mb-3">Candidate Pipeline</p>
        <div className="flex items-end gap-2 h-14">
          {pipeline.map((p) => (
            <div key={p.stage} className="flex-1 flex flex-col items-center gap-1">
              <span className="text-[9px] font-bold text-slate-600">{p.count}</span>
              <div
                className={`w-full rounded-t-md ${p.color}`}
                style={{ height: `${(p.count / 73) * 100}%` }}
              />
              <span className="text-[9px] text-slate-400">{p.stage}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Job listings */}
      <div className="px-5 py-3">
        <div className="flex flex-col gap-2">
          {jobs.map((j) => (
            <div key={j.title} className="flex items-center gap-3 p-2.5 rounded-xl border border-slate-100 hover:bg-slate-50 transition-colors">
              <div className="w-8 h-8 rounded-xl bg-indigo-50 flex items-center justify-center text-base flex-shrink-0">
                💼
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-[10px] font-semibold text-slate-800 truncate">{j.title}</p>
                <p className="text-[9px] text-slate-400">{j.dept} · {j.applicants} applicants</p>
              </div>
              <span className={`text-[9px] px-2 py-0.5 rounded-full font-semibold flex-shrink-0 ${
                j.stage === 'Offer' ? 'bg-emerald-50 text-emerald-600' :
                j.stage === 'Interviewing' ? 'bg-indigo-50 text-indigo-600' :
                'bg-amber-50 text-amber-600'
              }`}>
                {j.stage}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
