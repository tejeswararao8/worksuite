import React from 'react';
import { Star } from 'lucide-react';

const TestimonialsSection: React.FC = () => {
  const testimonials = [
    {
      stars: 5,
      text: "WorkSuite has completely transformed how we manage our people. The interface is intuitive, and the features are robust.",
      name: "Sarah Jenkins",
      title: "HR Director",
      company: "TechFlow Systems"
    },
    {
      stars: 5,
      text: "The payroll and attendance tracking have saved our team hundreds of hours each month. Best HRMS we've ever used.",
      name: "David Chen",
      title: "Operations Manager",
      company: "Innovate Global"
    },
    {
      stars: 5,
      text: "From recruitment to performance reviews, everything is seamless. Our employees love the transparent leave management.",
      name: "Emma Rodriguez",
      title: "CEO",
      company: "Creative Spark"
    }
  ];

  return (
    <section className="py-24 bg-white">
      <div className="container mx-auto px-6">
        <div className="text-center max-w-3xl mx-auto mb-20">
          <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
            A better everyday. <br />
            <span className="text-indigo-600">For everyone.</span>
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {testimonials.map((t, i) => (
            <div key={i} className="bg-slate-50 p-8 rounded-3xl border border-slate-100 hover:border-indigo-200 hover:shadow-xl hover:shadow-indigo-50 transition-all group">
              <div className="flex space-x-1 mb-6 text-amber-400">
                {[...Array(t.stars)].map((_, i) => <Star key={i} size={18} fill="currentColor" />)}
              </div>
              <p className="text-lg text-slate-700 mb-8 italic">"{t.text}"</p>
              <div className="flex items-center space-x-4">
                <div className="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center font-bold text-indigo-700 group-hover:bg-indigo-600 group-hover:text-white transition-colors">
                  {t.name.split(' ').map(n => n[0]).join('')}
                </div>
                <div>
                  <h4 className="font-bold text-slate-900">{t.name}</h4>
                  <p className="text-sm text-slate-500">{t.title}, {t.company}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default TestimonialsSection;
