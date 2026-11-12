import React from 'react';
import { useTranslation } from 'react-i18next';
import { Link2, Zap, Users } from 'lucide-react';

const TrustSection: React.FC = () => {
  const { t } = useTranslation();

  const items = [
    { icon: Link2, titleKey: 'homepage.trustItem1Title', descKey: 'homepage.trustItem1Desc' },
    { icon: Zap,   titleKey: 'homepage.trustItem2Title', descKey: 'homepage.trustItem2Desc' },
    { icon: Users, titleKey: 'homepage.trustItem3Title', descKey: 'homepage.trustItem3Desc' },
  ];

  return (
    <section className="py-20 bg-white">
      <div className="container mx-auto px-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {items.map((item, i) => (
            <div key={i} className="flex flex-col items-center text-center p-8 rounded-3xl hover:bg-slate-50 transition-colors">
              <div className="w-16 h-16 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center mb-6">
                <item.icon size={32} />
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-4">{t(item.titleKey)}</h3>
              <p className="text-slate-600 leading-relaxed">{t(item.descKey)}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default TrustSection;
