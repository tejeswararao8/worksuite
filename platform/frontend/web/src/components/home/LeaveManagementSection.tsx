import React from 'react';

const LeaveManagementSection: React.FC = () => {
  return (
    <section className="py-20 bg-white">
      <div className="container mx-auto px-6">
        <div className="flex flex-col lg:flex-row items-center gap-16">
          <div className="lg:w-1/2">
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              Less paperwork. <br />
              <span className="text-indigo-600">More peace of mind.</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8">
              Make leave management simple with transparent requests, approvals, and employee leave balances.
            </p>
            <ul className="space-y-4">
              {['Leave applications', 'Leave balance tracking', 'Approval workflows', 'Leave history'].map((item, i) => (
                <li key={i} className="flex items-center space-x-3 text-slate-700">
                  <div className="w-6 h-6 bg-green-100 text-green-600 rounded-full flex items-center justify-center">✓</div>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
          <div className="lg:w-1/2 bg-slate-50 p-6 rounded-3xl border border-slate-100 shadow-inner">
             {/* Leave management dashboard preview */}
             <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-100">
                <h4 className="font-bold text-slate-800 mb-4">Pending Leave Requests</h4>
                <div className="space-y-3">
                  {[1, 2].map((i) => (
                    <div key={i} className="flex justify-between items-center border-b border-slate-100 pb-3">
                      <div>
                        <p className="font-bold text-slate-800">Jane Doe</p>
                        <p className="text-xs text-slate-500">Casual Leave - 2 Days</p>
                      </div>
                      <div className="flex space-x-2">
                        <button className="text-xs bg-green-100 text-green-700 px-3 py-1 rounded-full">Approve</button>
                        <button className="text-xs bg-red-100 text-red-700 px-3 py-1 rounded-full">Reject</button>
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

export default LeaveManagementSection;
