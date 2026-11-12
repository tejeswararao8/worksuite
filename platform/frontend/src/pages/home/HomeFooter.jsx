import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function HomeFooter() {
  const { t } = useTranslation();
  const year = new Date().getFullYear();

  const productLinks = [
    { labelKey: 'home.footer.employees',    href: '#modules' },
    { labelKey: 'home.footer.departments',  href: '#modules' },
    { labelKey: 'home.footer.assets',       href: '#modules' },
    { labelKey: 'home.footer.documents',    href: '#modules' },
    { labelKey: 'home.footer.reports',      href: '#modules' },
  ];

  const platformLinks = [
    { labelKey: 'home.footer.multiTenant',  href: '#about' },
    { labelKey: 'home.footer.rbac',         href: '#about' },
    { labelKey: 'home.footer.audit',        href: '#modules' },
  ];

  const serviceLinks = [
    { labelKey: 'home.footer.implementation', href: '#services' },
    { labelKey: 'home.footer.configuration',  href: '#services' },
    { labelKey: 'home.footer.techSupport',    href: '#services' },
  ];

  return (
    <footer className="bg-[#14213D] text-slate-300">
      <div className="mx-auto max-w-7xl px-6 py-16">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-10 mb-12">
          {/* Brand */}
          <div className="lg:col-span-2">
            <Link to="/" className="flex items-center gap-2.5 mb-4">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-600 text-base font-black text-white">
                W
              </div>
              <span className="text-base font-extrabold tracking-tight text-white">WorkSuite HRMS</span>
            </Link>
            <p className="text-sm text-slate-400 leading-relaxed mb-5 max-w-xs">
              {t('home.footer.brandDesc')}
            </p>
            <div className="flex gap-3">
              <Link
                to="/login"
                className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-500 transition-all"
              >
                {t('home.nav.login')}
              </Link>
              <a
                href="#hero"
                className="px-4 py-2 text-xs font-semibold text-slate-300 border border-white/20 rounded-lg hover:border-indigo-400 hover:text-white transition-all"
              >
                {t('home.footer.learnMore')}
              </a>
            </div>
          </div>

          {/* Product */}
          <div>
            <p className="text-xs font-bold text-white uppercase tracking-widest mb-4">{t('home.footer.colProduct')}</p>
            <ul className="flex flex-col gap-2.5">
              {productLinks.map((l) => (
                <li key={l.labelKey}>
                  <a href={l.href} className="text-sm text-slate-400 hover:text-white transition-colors">
                    {t(l.labelKey)}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* Platform */}
          <div>
            <p className="text-xs font-bold text-white uppercase tracking-widest mb-4">{t('home.footer.colPlatform')}</p>
            <ul className="flex flex-col gap-2.5">
              {platformLinks.map((l) => (
                <li key={l.labelKey}>
                  <a href={l.href} className="text-sm text-slate-400 hover:text-white transition-colors">
                    {t(l.labelKey)}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* Services */}
          <div>
            <p className="text-xs font-bold text-white uppercase tracking-widest mb-4">{t('home.footer.colServices')}</p>
            <ul className="flex flex-col gap-2.5">
              {serviceLinks.map((l) => (
                <li key={l.labelKey}>
                  <a href={l.href} className="text-sm text-slate-400 hover:text-white transition-colors">
                    {t(l.labelKey)}
                  </a>
                </li>
              ))}
            </ul>
            <div className="mt-6">
              <p className="text-xs font-bold text-white uppercase tracking-widest mb-3">{t('home.footer.colLogin')}</p>
              <Link to="/login" className="text-sm text-slate-400 hover:text-white transition-colors block mb-2">
                {t('home.nav.login')}
              </Link>
              <Link to="/login" className="text-sm text-slate-400 hover:text-white transition-colors block">
                {t('home.nav.getStarted')}
              </Link>
            </div>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="pt-8 border-t border-white/10 flex flex-col sm:flex-row items-center justify-between gap-4">
          <p className="text-xs text-slate-500">
            © {year} WorkSuite HRMS. {t('home.footer.rights')}
          </p>
          <div className="flex items-center gap-6">
            <Link to="/login" className="text-xs text-slate-500 hover:text-white transition-colors">
              {t('home.nav.login')}
            </Link>
            <a href="#hero" className="text-xs text-slate-500 hover:text-white transition-colors">
              {t('home.footer.backToTop')}
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
