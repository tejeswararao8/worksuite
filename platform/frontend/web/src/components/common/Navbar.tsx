import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Menu, X, ChevronDown, Globe } from 'lucide-react';

const LANGS = [
  { code: 'en', label: 'English', dir: 'ltr' },
  { code: 'ar', label: 'العربية', dir: 'rtl' },
  { code: 'hi', label: 'हिंदी',   dir: 'ltr' },
];

const HomeNavbar: React.FC = () => {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [scrolled, setScrolled]     = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [langOpen, setLangOpen]     = useState(false);
  const langRef = useRef<HTMLDivElement>(null);
  const isRtl   = i18n.language === 'ar';

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (langRef.current && !langRef.current.contains(e.target as Node)) {
        setLangOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const changeLanguage = (code: string) => {
    i18n.changeLanguage(code);
    setLangOpen(false);
    setMobileOpen(false);
  };

  const currentLang = LANGS.find(l => l.code === i18n.language) ?? LANGS[0];

  const navLinks = [
    { key: 'nav.home',      href: '#hero' },
    { key: 'nav.product',   href: '#features' },
    { key: 'nav.solutions', href: '#audience' },
    { key: 'nav.services',  href: '#services' },
    { key: 'nav.pricing',   href: '#pricing' },
    { key: 'nav.about',     href: '#about' },
  ];

  const gap = isRtl ? 'space-x-reverse space-x-8' : 'space-x-8';

  return (
    <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${scrolled ? 'bg-white shadow-md py-3' : 'bg-transparent py-5'}`}>
      <div className={`container mx-auto px-6 flex justify-between items-center`}>

        {/* Logo */}
        <Link to="/" className={`flex items-center ${isRtl ? 'space-x-reverse space-x-2' : 'space-x-2'}`}>
          <div className="w-10 h-10 bg-indigo-600 rounded-lg flex items-center justify-center shrink-0">
            <span className="text-white font-bold text-xl">W</span>
          </div>
          <span className="text-2xl font-bold text-slate-800">WorkSuite</span>
        </Link>

        {/* Desktop links */}
        <div className={`hidden lg:flex items-center ${gap}`}>
          {navLinks.map(link => (
            <a key={link.key} href={link.href}
              className="text-slate-600 hover:text-indigo-600 font-medium transition-colors text-sm whitespace-nowrap">
              {t(link.key)}
            </a>
          ))}
        </div>

        {/* Desktop right actions */}
        <div className={`hidden lg:flex items-center ${isRtl ? 'space-x-reverse space-x-3' : 'space-x-3'}`}>

          {/* Language switcher */}
          <div className="relative" ref={langRef}>
            <button
              onClick={() => setLangOpen(!langOpen)}
              className={`flex items-center ${isRtl ? 'space-x-reverse space-x-1' : 'space-x-1'} text-slate-600 hover:text-indigo-600 font-medium text-sm px-3 py-2 rounded-lg hover:bg-slate-50 transition-colors`}
              aria-label={t('language.select')}
            >
              <Globe size={16} />
              <span>{currentLang.label}</span>
              <ChevronDown size={14} className={`transition-transform ${langOpen ? 'rotate-180' : ''}`} />
            </button>
            {langOpen && (
              <div className={`absolute top-full mt-1 bg-white border border-slate-200 rounded-xl shadow-xl py-1 min-w-[140px] z-50 ${isRtl ? 'left-0' : 'right-0'}`}>
                {LANGS.map(lang => (
                  <button key={lang.code} onClick={() => changeLanguage(lang.code)}
                    className={`w-full text-start px-4 py-2.5 text-sm hover:bg-indigo-50 hover:text-indigo-600 transition-colors ${i18n.language === lang.code ? 'text-indigo-600 font-semibold bg-indigo-50/50' : 'text-slate-700'}`}>
                    {lang.label}
                  </button>
                ))}
              </div>
            )}
          </div>

          <Link to="/login" className="text-slate-600 hover:text-indigo-600 font-medium text-sm transition-colors">
            {t('nav.login')}
          </Link>
          <button onClick={() => navigate('/login')}
            className="bg-indigo-600 text-white px-5 py-2.5 rounded-full font-semibold text-sm hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 whitespace-nowrap">
            {t('nav.getStarted')}
          </button>
        </div>

        {/* Mobile toggle */}
        <button className="lg:hidden text-slate-800 p-1" onClick={() => setMobileOpen(!mobileOpen)} aria-label="Toggle menu">
          {mobileOpen ? <X size={26} /> : <Menu size={26} />}
        </button>
      </div>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="lg:hidden bg-white border-t border-slate-100 absolute top-full left-0 right-0 shadow-xl z-40">
          <div className="px-6 py-4 flex flex-col space-y-1">
            {navLinks.map(link => (
              <a key={link.key} href={link.href}
                className="text-slate-600 hover:text-indigo-600 font-medium py-2.5 border-b border-slate-50 text-sm"
                onClick={() => setMobileOpen(false)}>
                {t(link.key)}
              </a>
            ))}
            <Link to="/login" className="text-slate-600 hover:text-indigo-600 font-medium py-2.5 border-b border-slate-50 text-sm"
              onClick={() => setMobileOpen(false)}>
              {t('nav.login')}
            </Link>

            {/* Mobile language selector */}
            <div className="pt-2 pb-1">
              <p className="text-xs text-slate-400 uppercase tracking-wider mb-2 font-semibold">{t('language.select')}</p>
              <div className="flex gap-2 flex-wrap">
                {LANGS.map(lang => (
                  <button key={lang.code} onClick={() => changeLanguage(lang.code)}
                    className={`px-3 py-1.5 rounded-full text-sm font-medium border transition-colors ${i18n.language === lang.code ? 'bg-indigo-600 text-white border-indigo-600' : 'border-slate-200 text-slate-600 hover:border-indigo-300'}`}>
                    {lang.label}
                  </button>
                ))}
              </div>
            </div>

            <button onClick={() => { navigate('/login'); setMobileOpen(false); }}
              className="bg-indigo-600 text-white px-6 py-3 rounded-xl font-semibold w-full mt-2 text-sm">
              {t('nav.getStarted')}
            </button>
          </div>
        </div>
      )}
    </nav>
  );
};

export default HomeNavbar;
