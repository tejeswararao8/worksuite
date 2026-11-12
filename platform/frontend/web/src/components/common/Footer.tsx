import React from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Globe, Share2, Link2, Rss, Mail } from 'lucide-react';

const Footer: React.FC = () => {
  const { t, i18n } = useTranslation();
  const isRtl = i18n.language === 'ar';
  const year  = new Date().getFullYear();

  const cols = [
    {
      headKey: 'homepage.footerProduct',
      links: [
        { key: 'homepage.footerFeatures',   href: '#features'  },
        { key: 'homepage.footerHRMgmt',     href: '#features'  },
        { key: 'homepage.footerPayroll',     href: '#features'  },
        { key: 'homepage.footerRecruitment', href: '#features'  },
        { key: 'homepage.footerPerformance', href: '#features'  },
        { key: 'homepage.footerSecurity',    href: '#'          },
      ],
    },
    {
      headKey: 'homepage.footerSolutions',
      links: [
        { key: 'homepage.forOrgs',      href: '#audience' },
        { key: 'homepage.forPlatform',  href: '#audience' },
        { key: 'homepage.footerEnterprise', href: '#'     },
      ],
    },
    {
      headKey: 'homepage.footerServices',
      links: [
        { key: 'homepage.footerImpl',    href: '#services' },
        { key: 'homepage.footerSupport', href: '#services' },
        { key: 'homepage.footerManaged', href: '#services' },
      ],
    },
    {
      headKey: 'homepage.footerCompany',
      links: [
        { key: 'homepage.footerAbout',   href: '#' },
        { key: 'homepage.footerCareers', href: '#' },
        { key: 'homepage.footerBlog',    href: '#' },
      ],
    },
    {
      headKey: 'homepage.footerResources',
      links: [
        { key: 'homepage.footerDocs',      href: '#' },
        { key: 'homepage.footerAPI',       href: '#' },
        { key: 'homepage.footerChangelog', href: '#' },
      ],
    },
  ];

  return (
    <footer className="bg-slate-900 text-slate-300 pt-20 pb-10">
      <div className="container mx-auto px-6">
        <div className={`grid grid-cols-1 md:grid-cols-2 lg:grid-cols-7 gap-10 mb-16 ${isRtl ? 'text-right' : ''}`}>

          {/* Brand column — spans 2 */}
          <div className="lg:col-span-2">
            <Link to="/" className={`flex items-center gap-2 mb-6 text-white ${isRtl ? 'flex-row-reverse justify-end' : ''}`}>
              <div className="w-10 h-10 bg-indigo-500 rounded-lg flex items-center justify-center shrink-0">
                <span className="text-white font-bold text-xl">W</span>
              </div>
              <span className="text-2xl font-bold">WorkSuite</span>
            </Link>
            <p className="text-slate-400 mb-8 max-w-xs leading-relaxed text-sm">
              {t('homepage.footerTagline')}
            </p>
            <div className={`flex gap-3 ${isRtl ? 'flex-row-reverse justify-end' : ''}`}>
              {[Globe, Share2, Link2, Rss].map((Icon, i) => (
                <a key={i} href="#"
                  className="w-9 h-9 bg-slate-800 rounded-full flex items-center justify-center hover:bg-indigo-500 transition-colors"
                  aria-label="social">
                  <Icon size={16} />
                </a>
              ))}
            </div>
          </div>

          {/* Link columns */}
          {cols.map((col, ci) => (
            <div key={ci}>
              <h4 className="text-white font-bold mb-5 text-sm uppercase tracking-wider">{t(col.headKey)}</h4>
              <ul className="space-y-3">
                {col.links.map((link, li) => (
                  <li key={li}>
                    <a href={link.href} className="text-sm hover:text-white transition-colors">{t(link.key)}</a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Contact row */}
        <div className={`border-t border-slate-800 pt-8 mb-8 ${isRtl ? 'text-right' : ''}`}>
          <h4 className="text-white font-bold mb-4 text-sm uppercase tracking-wider">{t('homepage.footerContact')}</h4>
          <div className={`flex flex-wrap gap-6 text-sm text-slate-400 ${isRtl ? 'flex-row-reverse' : ''}`}>
            <a href="mailto:hello@worksuite.ai" className={`flex items-center gap-2 hover:text-white transition-colors ${isRtl ? 'flex-row-reverse' : ''}`}>
              <Mail size={15} className="text-indigo-400 shrink-0" />
              hello@worksuite.ai
            </a>
          </div>
        </div>

        {/* Bottom bar */}
        <div className={`border-t border-slate-800 pt-8 flex flex-col md:flex-row justify-between items-center gap-4 ${isRtl ? 'md:flex-row-reverse' : ''}`}>
          <p className="text-sm text-slate-500">
            {t('homepage.footerCopyright', { year })}
          </p>
          <div className={`flex gap-6 text-sm text-slate-500 ${isRtl ? 'flex-row-reverse' : ''}`}>
            <a href="#" className="hover:text-white transition-colors">{t('homepage.footerPrivacy')}</a>
            <a href="#" className="hover:text-white transition-colors">{t('homepage.footerTerms')}</a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
