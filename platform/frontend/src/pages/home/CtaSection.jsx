import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';

export default function CtaSection() {
  return (
    <section className="py-24 bg-indigo-50">
      <div className="mx-auto max-w-4xl px-6 text-center">
        <div className="inline-flex items-center gap-2 rounded-full bg-indigo-100 border border-indigo-200 px-3 py-1.5 text-xs font-semibold text-indigo-700 mb-6">
          <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 animate-pulse" />
          No credit card required
        </div>
        <h2 className="text-4xl md:text-5xl font-black text-[#172033] tracking-tight mb-5">
          Ready to simplify your<br className="hidden md:block" /> HR operations?
        </h2>
        <p className="text-lg text-[#64748B] mb-10 max-w-xl mx-auto">
          Join hundreds of companies already using WorkSuite HRMS to manage their people smarter.
        </p>
        <div className="flex flex-wrap items-center justify-center gap-4">
          <Link
            to="/login"
            className="inline-flex items-center gap-2 px-8 py-4 bg-indigo-600 text-white text-base font-semibold rounded-xl hover:bg-indigo-500 transition-all shadow-lg shadow-indigo-600/25"
          >
            Get Started Free <ArrowRight size={18} />
          </Link>
          <a
            href="#features"
            className="inline-flex items-center gap-2 px-8 py-4 bg-white text-slate-700 text-base font-semibold rounded-xl border border-slate-200 hover:border-indigo-300 hover:text-indigo-600 transition-all"
          >
            See all features
          </a>
        </div>
        <p className="mt-6 text-sm text-[#64748B]">
          Free 14-day trial · No setup fees · Cancel anytime
        </p>
      </div>
    </section>
  );
}
