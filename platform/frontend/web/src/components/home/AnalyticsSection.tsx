import React from 'react';

const AnalyticsSection: React.FC = () => {
  return (
    <section className="py-24 bg-[#172554] text-white overflow-hidden relative">
      {/* Decorative patterns */}
      <div className="absolute top-0 right-0 w-96 h-96 bg-indigo-500/10 rounded-full -translate-y-1/2 translate-x-1/2 blur-3xl"></div>
      <div className="absolute bottom-0 left-0 w-96 h-96 bg-indigo-500/10 rounded-full translate-y-1/2 -translate-x-1/2 blur-3xl"></div>

      <div className="container mx-auto px-6 relative z-10">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h2 className="text-4xl md:text-5xl font-extrabold mb-6 leading-tight">
            Everything in sync. <br />
            Everyone empowered.
          </h2>
          <p className="text-xl text-indigo-200/80">
            Get a complete picture of your workforce with meaningful insights, reports, and analytics.
          </p>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6 mb-16">
          {[
            { label: 'Total Employees', val: '248', inc: '+12' },
            { label: 'Attendance Rate', val: '94%', inc: '+2.1%' },
            { label: 'Active Jobs', val: '14', inc: '+3' },
            { label: 'Pending Leave', val: '8', inc: '-2' },
            { label: 'Avg Performance', val: '4.2', inc: '+0.4' },
            { label: 'Retention Rate', val: '98%', inc: '+1%' }
          ].map((stat, i) => (
            <div key={i} className="bg-white/5 backdrop-blur-sm border border-white/10 p-6 rounded-2xl hover:bg-white/10 transition-colors">
              <p className="text-xs text-indigo-300 uppercase tracking-wider mb-2">{stat.label}</p>
              <p className="text-2xl font-bold mb-1">{stat.val}</p>
              <p className="text-xs text-green-400 font-medium">{stat.inc} this month</p>
            </div>
          ))}
        </div>

        <div className="bg-white/5 backdrop-blur-md border border-white/10 rounded-3xl p-8 lg:p-12">
          <div className="flex flex-col lg:flex-row justify-between items-end gap-8">
             <div className="flex-1 w-full">
               <div className="flex justify-between items-center mb-8">
                  <h4 className="font-bold text-lg">Workforce Trends</h4>
                  <div className="flex space-x-4 text-sm text-indigo-300">
                    <span className="flex items-center"><span className="w-2 h-2 bg-indigo-500 rounded-full mr-2"></span> Growth</span>
                    <span className="flex items-center"><span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span> Retention</span>
                  </div>
               </div>
               {/* Simple CSS-based bar chart mockup */}
               <div className="flex items-end justify-between h-48 w-full gap-2 lg:gap-4">
                  {[45, 60, 55, 80, 70, 90, 85, 75, 95, 80, 85, 90].map((h, i) => (
                    <div key={i} className="flex-1 bg-indigo-500/20 rounded-t-lg group relative cursor-pointer hover:bg-indigo-500/40 transition-colors h-full flex items-end">
                      <div className="w-full bg-indigo-500 rounded-t-lg transition-all duration-1000" style={{ height: `${h}%` }}></div>
                      <div className="absolute -top-8 left-1/2 -translate-x-1/2 bg-white text-slate-900 text-[10px] font-bold py-1 px-2 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
                        {h}%
                      </div>
                    </div>
                  ))}
               </div>
               <div className="flex justify-between mt-4 text-[10px] text-indigo-300/60 uppercase tracking-widest font-bold">
                  <span>Jan</span>
                  <span>Mar</span>
                  <span>May</span>
                  <span>Jul</span>
                  <span>Sep</span>
                  <span>Dec</span>
               </div>
             </div>
             <div className="w-full lg:w-72 bg-indigo-500 rounded-2xl p-6 shadow-2xl shadow-indigo-900/50">
               <h5 className="font-bold mb-4">Quick Insights</h5>
               <ul className="space-y-4 text-sm">
                 <li className="flex items-start space-x-2">
                   <span className="text-indigo-200 mt-1">•</span>
                   <span>Hiring is 20% faster this quarter</span>
                 </li>
                 <li className="flex items-start space-x-2">
                   <span className="text-indigo-200 mt-1">•</span>
                   <span>Attendance increased by 5% in Sales</span>
                 </li>
                 <li className="flex items-start space-x-2">
                   <span className="text-indigo-200 mt-1">•</span>
                   <span>Top performers have 100% goal completion</span>
                 </li>
               </ul>
               <p className="text-[10px] mt-6 opacity-40 text-center uppercase tracking-widest italic">Demo Analytics</p>
             </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default AnalyticsSection;
