import React from 'react';

const PayrollSection: React.FC = () => {
  return (
    <section className="py-20 bg-slate-50">
      <div className="container mx-auto px-6">
        <div className="flex flex-col-reverse lg:flex-row items-center gap-16">
          <div className="lg:w-1/2 bg-white p-6 rounded-3xl border border-slate-100 shadow-lg">
             <div className="bg-indigo-900 p-6 rounded-2xl text-white">
               <h4 className="font-bold mb-4 opacity-80">Payroll Summary</h4>
               <p className="text-sm opacity-60 mb-1">Total Net Pay</p>
               <p className="text-3xl font-bold mb-6">$42,500.00</p>
               <div className="grid grid-cols-2 gap-4">
                 <div>
                   <p className="text-xs opacity-60">Gross Pay</p>
                   <p className="font-semibold">$50,000</p>
                 </div>
                 <div>
                   <p className="text-xs opacity-60">Deductions</p>
                   <p className="font-semibold">$7,500</p>
                 </div>
               </div>
               <div className="mt-6 pt-4 border-t border-white/10 flex justify-between items-center text-xs">
                 <span>Status: Processed</span>
                 <span className="bg-green-500 text-white px-2 py-0.5 rounded">Paid</span>
               </div>
               <p className="text-[10px] mt-4 opacity-40 text-center uppercase tracking-widest italic">Demo Data</p>
             </div>
          </div>
          <div className="lg:w-1/2">
            <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
              From people data <br />
              <span className="text-indigo-600">to payday.</span>
            </h2>
            <p className="text-lg text-slate-600 mb-8">
              Manage payroll information, salary structures, deductions, and payment records with greater visibility.
            </p>
            <ul className="space-y-4">
              {['Salary structure management', 'Payroll processing', 'Salary slips', 'Deductions and allowances', 'Payroll reports'].map((item, i) => (
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

export default PayrollSection;
