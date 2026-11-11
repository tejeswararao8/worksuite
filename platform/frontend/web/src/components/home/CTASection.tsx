import React from 'react';
import { useNavigate } from 'react-router-dom';

const CTASection: React.FC = () => {
  const navigate = useNavigate();

  return (
    <section className="py-20 px-6">
      <div className="container mx-auto max-w-6xl">
        <div className="bg-indigo-50 rounded-[3rem] p-12 lg:p-20 text-center relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-full opacity-10">
            <div className="absolute top-10 left-10 w-40 h-40 bg-indigo-600 rounded-full blur-3xl"></div>
            <div className="absolute bottom-10 right-10 w-40 h-40 bg-indigo-600 rounded-full blur-3xl"></div>
          </div>
          
          <div className="relative z-10">
            <h2 className="text-4xl md:text-6xl font-extrabold text-slate-900 mb-8">
              Ready to simplify <br />
              your HR operations?
            </h2>
            <p className="text-xl text-slate-600 mb-12 max-w-2xl mx-auto">
              Bring your people and processes together with WorkSuite. 
              Start your journey towards a smarter workplace today.
            </p>
            <div className="flex flex-col sm:flex-row items-center justify-center space-y-4 sm:space-y-0 sm:space-x-4">
              <button 
                onClick={() => navigate('/login')}
                className="bg-indigo-600 text-white px-10 py-5 rounded-full font-bold text-lg hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-200"
              >
                Get Started
              </button>
              <button className="bg-white text-slate-700 border border-slate-200 px-10 py-5 rounded-full font-bold text-lg hover:bg-slate-50 transition-all">
                Talk to Our Team
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default CTASection;
