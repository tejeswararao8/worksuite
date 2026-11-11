import React from 'react';
import { User, Layers, Briefcase, Calendar } from 'lucide-react';

const EmployeeManagementSection: React.FC = () => {
  return (
    <section className="py-20 bg-white">
      <div className="container mx-auto px-6">
        <div className="flex flex-col lg:flex-row items-center gap-16">
          <div className="lg:w-1/2">
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              Every person. <br />
              <span className="text-indigo-600">One place.</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8">
              Bring employee information, records, roles, and organizational details together 
              in one secure place.
            </p>
            <ul className="space-y-4">
              {['Centralized employee profiles', 'Employee information management', 'Department and designation management', 'Employee lifecycle tracking'].map((item, i) => (
                <li key={i} className="flex items-center space-x-3 text-slate-700">
                  <div className="w-6 h-6 bg-green-100 text-green-600 rounded-full flex items-center justify-center">✓</div>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
          <div className="lg:w-1/2 bg-slate-50 p-6 rounded-3xl border border-slate-100 shadow-inner">
             {/* Employee directory dashboard preview */}
             <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-100">
                <div className="flex justify-between items-center mb-4">
                  <h4 className="font-bold text-slate-800">Employee Directory</h4>
                  <button className="text-indigo-600 text-sm font-semibold">View All</button>
                </div>
                <div className="space-y-3">
                  {[1, 2, 3].map((i) => (
                    <div key={i} className="flex items-center space-x-4 border-b border-slate-100 pb-3">
                      <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center font-bold text-indigo-700">JS</div>
                      <div>
                        <p className="font-bold text-slate-800">John Smith</p>
                        <p className="text-xs text-slate-500">Engineering | Senior Developer</p>
                      </div>
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

export default EmployeeManagementSection;
