import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Play } from 'lucide-react';

const HeroSection: React.FC = () => {
  const navigate = useNavigate();

  return (
    <section className="relative pt-32 pb-20 overflow-hidden bg-[#F5F7FF]">
      {/* Background Shapes */}
      <div className="absolute top-0 right-0 w-1/2 h-full bg-indigo-50/50 -skew-x-12 transform translate-x-1/4"></div>
      
      <div className="container mx-auto px-6 relative z-10">
        <div className="flex flex-col items-center text-center max-w-4xl mx-auto mb-16">
          <div className="inline-flex items-center space-x-2 bg-indigo-100 text-indigo-700 px-4 py-2 rounded-full text-sm font-bold mb-8 animate-fade-in">
            <span className="flex h-2 w-2 rounded-full bg-indigo-600 animate-pulse"></span>
            <span>New: Version 2.0 is now live</span>
          </div>
          
          <h1 className="text-5xl md:text-7xl font-extrabold text-slate-900 leading-tight mb-8">
            People First. <br />
            <span className="text-indigo-600">Work Smarter.</span>
          </h1>
          
          <p className="text-xl text-slate-600 mb-12 max-w-2xl">
            Bring your people, processes, and possibilities together. 
            WorkSuite simplifies HR operations, helping your organization work smarter and grow faster.
          </p>
          
          <div className="flex flex-col sm:flex-row items-center space-y-4 sm:space-y-0 sm:space-x-4">
            <button 
              onClick={() => navigate('/login')}
              className="bg-indigo-600 text-white px-8 py-4 rounded-full font-bold text-lg hover:bg-indigo-700 transition-all flex items-center group shadow-xl shadow-indigo-200"
            >
              Get Started 
              <ArrowRight className="ml-2 group-hover:translate-x-1 transition-transform" />
            </button>
            <button className="bg-white text-slate-700 border border-slate-200 px-8 py-4 rounded-full font-bold text-lg hover:bg-slate-50 transition-all flex items-center">
              <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center mr-2">
                <Play size={16} className="text-indigo-600 fill-indigo-600" />
              </div>
              Explore Features
            </button>
          </div>
        </div>

        <div className="relative max-w-5xl mx-auto">
          {/* Dashboard Preview Mockup */}
          <div className="bg-white rounded-3xl shadow-2xl border border-slate-100 p-2 transform rotate-1 hover:rotate-0 transition-transform duration-700">
            <div className="bg-slate-50 rounded-2xl overflow-hidden border border-slate-100">
              <img 
                src="https://images.unsplash.com/photo-1460925895917-afdab827c52f?ixlib=rb-4.0.3&auto=format&fit=crop&w=2426&q=80" 
                alt="WorkSuite Dashboard" 
                className="w-full h-auto opacity-90"
              />
            </div>
          </div>
          
          {/* Floating Cards */}
          <div className="absolute -left-12 top-1/4 bg-white p-4 rounded-2xl shadow-xl border border-slate-50 hidden lg:block animate-bounce-slow">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                <span className="text-green-600 font-bold">✓</span>
              </div>
              <div>
                <p className="text-xs text-slate-400">Attendance</p>
                <p className="text-sm font-bold text-slate-800">98% Present Today</p>
              </div>
            </div>
          </div>

          <div className="absolute -right-12 bottom-1/4 bg-white p-4 rounded-2xl shadow-xl border border-slate-50 hidden lg:block animate-bounce-slow-reverse">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                <span className="text-blue-600 font-bold">★</span>
              </div>
              <div>
                <p className="text-xs text-slate-400">Recruitment</p>
                <p className="text-sm font-bold text-slate-800">12 New Candidates</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
