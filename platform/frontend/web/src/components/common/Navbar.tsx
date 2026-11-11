import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Menu, X } from 'lucide-react';
import { COLORS } from '../../../../shared/constants';

const Navbar: React.FC = () => {
  const [isScrolled, setIsScrolled] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navLinks = [
    { name: 'Home', href: '#' },
    { name: 'Product', href: '#' },
    { name: 'Solutions', href: '#' },
    { name: 'Features', href: '#' },
    { name: 'About Us', href: '#' },
  ];

  return (
    <nav
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        isScrolled ? 'bg-white shadow-md py-3' : 'bg-transparent py-5'
      }`}
    >
      <div className="container mx-auto px-6 flex justify-between items-center">
        <Link to="/" className="flex items-center space-x-2">
          <div className="w-10 h-10 bg-indigo-600 rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-xl">W</span>
          </div>
          <span className="text-2xl font-bold text-slate-800">WorkSuite</span>
        </Link>

        {/* Desktop Menu */}
        <div className="hidden md:flex items-center space-x-8">
          {navLinks.map((link) => (
            <a
              key={link.name}
              href={link.href}
              className="text-slate-600 hover:text-indigo-600 font-medium transition-colors"
            >
              {link.name}
            </a>
          ))}
          <Link
            to="/login"
            className="text-slate-600 hover:text-indigo-600 font-medium transition-colors"
          >
            Login
          </Link>
          <button
            onClick={() => navigate('/login')}
            className="bg-indigo-600 text-white px-6 py-2.5 rounded-full font-semibold hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200"
          >
            Get Started
          </button>
        </div>

        {/* Mobile Toggle */}
        <div className="md:hidden flex items-center">
          <button onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)} className="text-slate-800">
            {isMobileMenuOpen ? <X size={28} /> : <Menu size={28} />}
          </button>
        </div>
      </div>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="md:hidden bg-white border-t border-slate-100 absolute top-full left-0 right-0 shadow-xl p-6 flex flex-col space-y-4 animate-fade-in-down">
          {navLinks.map((link) => (
            <a
              key={link.name}
              href={link.href}
              className="text-slate-600 hover:text-indigo-600 font-medium"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              {link.name}
            </a>
          ))}
          <Link
            to="/login"
            className="text-slate-600 hover:text-indigo-600 font-medium"
            onClick={() => setIsMobileMenuOpen(false)}
          >
            Login
          </Link>
          <button
            onClick={() => {
              navigate('/login');
              setIsMobileMenuOpen(false);
            }}
            className="bg-indigo-600 text-white px-6 py-3 rounded-xl font-semibold w-full"
          >
            Get Started
          </button>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
