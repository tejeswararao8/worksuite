import React from 'react';
import { useTranslation } from 'react-i18next';
import { Star } from 'lucide-react';

const TestimonialsSection: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';

  const testimonials = [
    { textKey: 'homepage.t1Text', nameKey: 'homepage.t1Name', titleKey: 'homepage.t1Title' },
    { textKey: 'homepage.t2Text', nameKey: 'homepage.t2Name', titleKey: 'homepage.t2Title' },
    { textKey: 'homepage.t3Text', nameKey: 'homepage.t3Name', titleKey: 'homepage.t3Title' },
  ];

  return (
    <section className="py-24 bg-white">
      <div className="container mx-auto px-6">
        <div className={`text-center max-w-3xl mx-auto mb-20 ${isRtl ? 'text-right md:text-center' : ''}`}>
          <h2 className="text-4xl md:text-5xl font-extrabold text-slate-900 mb-6">
            {t('homepage.testimonialsTitle')}<br />
            <span className="text-indigo-600">{t('homepage.testimonialsHighlight')}</span>
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {testimonials.map((item, i) => {
            const name  = t(item.nameKey);
            const initials = name.split(' ').map((n: string) => n[0]).join('').slice(0, 2);
            return (
              <div key={i} className={`bg-slate-50 p-8 rounded-3xl border border-slate-100 hover:border-indigo-200 hover:shadow-xl hover:shadow-indigo-50 transition-all group ${isRtl ? 'text-right' : ''}`}>
                <div className={`flex gap-1 mb-6 text-amber-400 ${isRtl ? 'flex-row-reverse justify-end' : ''}`}>
                  {[...Array(5)].map((_, j) => <Star key={j} size={18} fill="currentColor" />)}
                </div>
                <p className="text-lg text-slate-700 mb-8 italic leading-relaxed">"{t(item.textKey)}"</p>
                <div className={`flex items-center gap-4 ${isRtl ? 'flex-row-reverse' : ''}`}>
                  <div className="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center font-bold text-indigo-700 group-hover:bg-indigo-600 group-hover:text-white transition-colors shrink-0 text-sm">
                    {initials}
                  </div>
                  <div className={isRtl ? 'text-right' : ''}>
                    <h4 className="font-bold text-slate-900">{name}</h4>
                    <p className="text-sm text-slate-500">{t(item.titleKey)}</p>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

export default TestimonialsSection;
