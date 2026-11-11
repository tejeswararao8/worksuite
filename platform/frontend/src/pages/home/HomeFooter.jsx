import { Link } from 'react-router-dom';

const footerLinks = {
  Product: ['Employee Management', 'Attendance', 'Leave Management', 'Payroll', 'Recruitment', 'Performance'],
  Solutions: ['For HR Teams', 'For Managers', 'For Employees', 'Enterprise', 'Startups'],
  Resources: ['Documentation', 'API Reference', 'Blog', 'Case Studies', 'Changelog'],
  Company: ['About Us', 'Careers', 'Press', 'Partners', 'Security'],
  Contact: ['support@worksuite.io', 'sales@worksuite.io', '+91 98765 43210', 'Mumbai, India'],
};

export default function HomeFooter() {
  return (
    <footer className="bg-[#14213D] text-slate-300">
      <div className="mx-auto max-w-7xl px-6 py-16">
        <div className="grid grid-cols-2 md:grid-cols-6 gap-8 mb-12">
          {/* Brand */}
          <div className="col-span-2 md:col-span-1">
            <div className="flex items-center gap-2.5 mb-4">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-600 text-base font-black text-white">
                W
              </div>
              <span className="text-base font-extrabold tracking-tight text-white">
                WorkSuite
              </span>
            </div>
            <p className="text-sm text-slate-400 leading-relaxed mb-4">
              Modern HRMS for people-first organizations.
            </p>
            {/* Social links */}
            <div className="flex gap-3">
              {[
                { label: 'LinkedIn', icon: 'in' },
                { label: 'Twitter', icon: '𝕏' },
                { label: 'GitHub', icon: '⌥' },
              ].map((s) => (
                <a
                  key={s.label}
                  href="#"
                  aria-label={s.label}
                  className="w-8 h-8 rounded-lg bg-white/10 flex items-center justify-center text-xs font-bold text-slate-300 hover:bg-indigo-600 hover:text-white transition-all"
                >
                  {s.icon}
                </a>
              ))}
            </div>
          </div>

          {/* Link columns */}
          {Object.entries(footerLinks).map(([col, links]) => (
            <div key={col}>
              <p className="text-xs font-bold text-white uppercase tracking-widest mb-4">{col}</p>
              <ul className="flex flex-col gap-2.5">
                {links.map((l) => (
                  <li key={l}>
                    <a
                      href="#"
                      className="text-sm text-slate-400 hover:text-white transition-colors"
                    >
                      {l}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Bottom bar */}
        <div className="pt-8 border-t border-white/10 flex flex-col sm:flex-row items-center justify-between gap-4">
          <p className="text-xs text-slate-500">
            © {new Date().getFullYear()} WorkSuite HRMS. All rights reserved.
          </p>
          <div className="flex items-center gap-6">
            <a href="#" className="text-xs text-slate-500 hover:text-white transition-colors">Privacy Policy</a>
            <a href="#" className="text-xs text-slate-500 hover:text-white transition-colors">Terms of Service</a>
            <a href="#" className="text-xs text-slate-500 hover:text-white transition-colors">Cookie Policy</a>
          </div>
        </div>
      </div>
    </footer>
  );
}
