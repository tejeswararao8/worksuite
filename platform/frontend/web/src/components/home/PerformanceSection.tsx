import React from 'react';

const PerformanceSection: React.FC = () => {
  return (
    <section className="py-20 bg-slate-50">
      <div className="container mx-auto px-6">
        <div className="flex flex-col-reverse lg:flex-row items-center gap-16">
          <div className="lg:w-1/2 bg-white p-6 rounded-3xl border border-slate-100 shadow-lg">
             <div className="space-y-6">
               <h4 className="font-bold text-slate-800">Q3 Performance Goals</h4>
               {[
                 { label: 'Project Delivery', val: 85, color: 'bg-indigo-600' },
                 { label: 'Client Satisfaction', val: 92, color: 'bg-green-500' },
                 { label: 'Learning & Development', val: 64, color: 'bg-amber-500' }
               ].map((goal, i) => (
                 <div key={i}>
                   <div className="flex justify-between text-xs mb-1">
                     <span className="font-semibold text-slate-700">{goal.label}</span>
                     <span className="text-slate-500">{goal.val}%</span>
                   </div>
                   <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                     <div className={`h-full ${goal.color}`} style={{ width: `${goal.val}%` }}></div>
                   </div>
                 </div>
               ))}
             </div>
          </div>
          <div className="lg:w-1/2">
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              Make growth <br />
              <span className="text-indigo-600">part of the everyday.</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8">
              Set meaningful goals, measure progress, and support employee development.
            </p>
            <ul className="space-y-4">
              {['Employee performance reviews', 'Goal management', 'KPI tracking', 'Performance analytics', 'Feedback management'].map((item, i) => (
                <li key={i} className="flex items-center space-x-3 text-slate-700">
                  <div className="w-6 h-6 bg-green-100 text-green-600 rounded-full flex items-center justify-center">✓</div>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
};

export default PerformanceSection;
