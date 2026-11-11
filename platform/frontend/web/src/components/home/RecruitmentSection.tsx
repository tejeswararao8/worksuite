import React from 'react';

const RecruitmentSection: React.FC = () => {
  return (
    <section className="py-20 bg-white">
      <div className="container mx-auto px-6">
        <div className="flex flex-col lg:flex-row items-center gap-16">
          <div className="lg:w-1/2">
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              Great teams start <br />
              <span className="text-indigo-600">with a great hello.</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8">
              Organize your hiring process, manage applications, and track candidates from application to onboarding.
            </p>
            <ul className="space-y-4">
              {['Job posting management', 'Candidate management', 'Application tracking', 'Interview scheduling', 'Recruitment pipeline'].map((item, i) => (
                <li key={i} className="flex items-center space-x-3 text-slate-700">
                  <div className="w-6 h-6 bg-green-100 text-green-600 rounded-full flex items-center justify-center">✓</div>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
          <div className="lg:w-1/2 bg-slate-50 p-6 rounded-3xl border border-slate-100 shadow-inner">
             <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-100">
                <div className="flex justify-between items-center mb-4">
                  <h4 className="font-bold text-slate-800">Recent Applications</h4>
                  <span className="bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-full text-xs font-bold">12 New</span>
                </div>
                <div className="space-y-3">
                  {[1, 2, 3].map((i) => (
                    <div key={i} className="flex items-center space-x-3 border-b border-slate-100 pb-3 last:border-0 last:pb-0">
                      <div className="w-8 h-8 bg-slate-200 rounded-full"></div>
                      <div className="flex-1">
                        <p className="text-sm font-bold text-slate-800">Candidate {i}</p>
                        <p className="text-[10px] text-slate-500 uppercase tracking-tighter">UI/UX Designer • New</p>
                      </div>
                      <div className="w-2 h-2 bg-indigo-600 rounded-full"></div>
                    </div>
                  ))}
                </div>
             </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default RecruitmentSection;
