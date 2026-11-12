import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Menu, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import LanguageSwitcher from '../../components/ui/LanguageSwitcher';

export default function HomeNavbar() {
  const { t } = useTranslation();
  const [menuOpen, setMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 10);
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  const navLinks = [
    { labelKey: 'home.nav.features',  href: '#features' },
    { labelKey: 'home.nav.modules',   href: '#modules' },
    { labelKey: 'home.nav.services',  href: '#services' },
    { labelKey: 'home.nav.audiences', href: '#audiences' },
    { labelKey: 'home.nav.about',     href: '#about' },
  ];

  return (
    <header
      className={`fixed top-0 inset-x-0 z-50 transition-all duration-300 ${
        scrolled ? 'bg-white shadow-md' : 'bg-white border-b border-slate-100'
      }`}
    >
      <div className="mx-auto max-w-7xl px-6 py-4 flex items-center justify-between gap-4">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2.5 shrink-0">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-600 text-base font-black text-white">
            W
          </div>
          <span className="text-xl font-extrabold tracking-tight text-slate-900">
            WorkSuite <span className="text-indigo-600">HRMS</span>
          </span>
        </Link>

        {/* Desktop nav */}
        <nav className="hidden md:flex items-center gap-7 text-sm font-medium text-slate-600">
          {navLinks.map((l) => (
            <a key={l.labelKey} href={l.href} className="hover:text-indigo-600 transition-colors">
              {t(l.labelKey)}
            </a>
          ))}
        </nav>

        {/* Desktop right */}
        <div className="hidden md:flex items-center gap-3 shrink-0">
          <LanguageSwitcher />
          <Link
            to="/login"
            className="px-4 py-2 text-sm font-semibold text-slate-700 rounded-lg border border-slate-200 hover:border-indigo-300 hover:text-indigo-600 transition-all"
          >
            {t('home.nav.login')}
          </Link>
          <Link
            to="/login"
            className="px-4 py-2 text-sm font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-500 transition-all shadow-sm"
          >
            {t('home.nav.getStarted')}
          </Link>
        </div>

        {/* Mobile hamburger */}
        <button
          className="md:hidden p-2 rounded-lg text-slate-600 hover:bg-slate-100 transition"
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label={menuOpen ? t('common.close') : t('home.nav.openMenu')}
        >
          {menuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>
      </div>

      {/* Mobile menu */}
      {menuOpen && (
        <div className="md:hidden bg-white border-t border-slate-100 px-6 pb-5 pt-3 flex flex-col gap-4">
          {navLinks.map((l) => (
            <a
              key={l.labelKey}
              href={l.href}
              className="text-sm font-medium text-slate-700 hover:text-indigo-600 transition-colors"
              onClick={() => setMenuOpen(false)}
            >
              {t(l.labelKey)}
            </a>
          ))}
          <div className="pt-2 border-t border-slate-100">
            <LanguageSwitcher />
          </div>
          <div className="flex flex-col gap-2">
            <Link
              to="/login"
              className="text-center px-4 py-2.5 text-sm font-semibold text-slate-700 rounded-lg border border-slate-200"
              onClick={() => setMenuOpen(false)}
            >
              {t('home.nav.login')}
            </Link>
            <Link
              to="/login"
              className="text-center px-4 py-2.5 text-sm font-semibold text-white bg-indigo-600 rounded-lg"
              onClick={() => setMenuOpen(false)}
            >
              {t('home.nav.getStarted')}
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
