import React from 'react';

const AttendanceSection: React.FC = () => {
  return (
    <section className="py-20 bg-slate-50">
      <div className="container mx-auto px-6">
        <div className="flex flex-col-reverse lg:flex-row items-center gap-16">
          <div className="lg:w-1/2 bg-white p-6 rounded-3xl border border-slate-100 shadow-lg">
             <div className="bg-slate-50 p-4 rounded-xl">
               <h4 className="font-bold text-slate-800 mb-4">Attendance Overview</h4>
               <div className="grid grid-cols-2 gap-4">
                 <div className="bg-white p-4 rounded-lg shadow-sm border border-slate-100 text-center">
                    <p className="text-3xl font-bold text-green-600">85</p>
                    <p className="text-sm text-slate-500">Present</p>
                 </div>
                 <div className="bg-white p-4 rounded-lg shadow-sm border border-slate-100 text-center">
                    <p className="text-3xl font-bold text-red-500">5</p>
                    <p className="text-sm text-slate-500">Absent</p>
                 </div>
               </div>
             </div>
          </div>
          <div className="lg:w-1/2">
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              Time well tracked. <br />
              <span className="text-indigo-600">Days well spent.</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8">
              Manage attendance, monitor working hours, and get clear visibility into employee availability.
            </p>
            <ul className="space-y-4">
              {['Daily attendance tracking', 'Check-in and check-out', 'Attendance reports', 'Leave and absence tracking'].map((item, i) => (
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

export default AttendanceSection;
