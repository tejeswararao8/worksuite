/**
 * Reusable two-column feature section.
 * Props:
 *  - id: string
 *  - eyebrow: string
 *  - heading: string
 *  - description: string
 *  - bullets: string[]
 *  - preview: ReactNode  (the UI mock)
 *  - reverse: bool       (text on right, preview on left)
 *  - dark: bool          (dark navy background)
 */
export default function FeatureSection({
  id,
  eyebrow,
  heading,
  description,
  bullets = [],
  preview,
  reverse = false,
  dark = false,
}) {
  const bg = dark ? 'bg-[#14213D]' : 'bg-white';
  const textColor = dark ? 'text-white' : 'text-[#172033]';
  const subColor = dark ? 'text-slate-300' : 'text-[#64748B]';
  const eyebrowColor = dark ? 'text-indigo-300' : 'text-indigo-600';
  const bulletBg = dark ? 'bg-indigo-900/50 text-indigo-300' : 'bg-indigo-50 text-indigo-600';

  return (
    <section id={id} className={`py-24 ${bg}`}>
      <div className="mx-auto max-w-7xl px-6">
        <div
          className={`grid md:grid-cols-2 gap-12 lg:gap-20 items-center ${
            reverse ? 'md:[&>*:first-child]:order-2 md:[&>*:last-child]:order-1' : ''
          }`}
        >
          {/* Text */}
          <div>
            <p className={`text-sm font-semibold uppercase tracking-widest mb-3 ${eyebrowColor}`}>
              {eyebrow}
            </p>
            <h2 className={`text-3xl md:text-4xl font-black tracking-tight mb-5 ${textColor}`}>
              {heading}
            </h2>
            <p className={`text-base leading-relaxed mb-8 ${subColor}`}>{description}</p>
            {bullets.length > 0 && (
              <ul className="flex flex-col gap-3">
                {bullets.map((b) => (
                  <li key={b} className="flex items-start gap-3">
                    <span className={`mt-0.5 flex-shrink-0 w-5 h-5 rounded-full flex items-center justify-center text-xs font-bold ${bulletBg}`}>
                      ✓
                    </span>
                    <span className={`text-sm ${subColor}`}>{b}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Preview */}
          <div className="relative">
            {!dark && (
              <div className="absolute -top-6 -right-6 w-48 h-48 bg-indigo-50 rounded-full blur-3xl opacity-60 pointer-events-none" />
            )}
            <div className="relative">{preview}</div>
          </div>
        </div>
      </div>
    </section>
  );
}
