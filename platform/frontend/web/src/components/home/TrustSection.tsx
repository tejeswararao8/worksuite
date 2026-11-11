import React from 'react';
import { Users, Zap, Link2 } from 'lucide-react';

const TrustSection: React.FC = () => {
  const values = [
    { icon: Link2, title: 'One connected HR platform', desc: 'Sync all your HR operations seamlessly.' },
    { icon: Zap, title: 'Less admin. More focus.', desc: 'Automate repetitive tasks and focus on people.' },
    { icon: Users, title: 'Everyone seen, every action connected', desc: 'Foster engagement across your organization.' },
  ];

  return (
    <section className="py-20 bg-white">
      <div className="container mx-auto px-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-12">
          {values.map((item, index) => (
            <div key={index} className="flex flex-col items-center text-center p-8 rounded-3xl hover:bg-slate-50 transition-colors">
              <div className="w-16 h-16 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center mb-6">
                <item.icon size={32} />
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-4">{item.title}</h3>
              <p className="text-slate-600">{item.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default TrustSection;
