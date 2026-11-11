import HomeNavbar from './home/HomeNavbar';
import HeroSection from './home/HeroSection';
import JourneySection from './home/JourneySection';
import FeatureSection from './home/FeatureSection';
import EmployeeManagementPreview from './home/EmployeeManagementPreview';
import AttendancePreview from './home/AttendancePreview';
import LeavePreview from './home/LeavePreview';
import PayrollPreview from './home/PayrollPreview';
import RecruitmentPreview from './home/RecruitmentPreview';
import PerformancePreview from './home/PerformancePreview';
import DarkFeatureSection from './home/DarkFeatureSection';
import BenefitsSection from './home/BenefitsSection';
import CtaSection from './home/CtaSection';
import HomeFooter from './home/HomeFooter';

export default function HomePage() {
  return (
    <div className="min-h-screen bg-white text-[#172033]">
      <HomeNavbar />
      <HeroSection />
      <JourneySection />

      {/* Employee Management */}
      <FeatureSection
        id="employee-management"
        eyebrow="Employee Management"
        heading="Every person. One place."
        description="Maintain rich employee profiles, org charts, and a searchable directory. Everything from personal details to employment history — organized and accessible."
        bullets={[
          'Centralized employee profiles with full history',
          'Org chart and department hierarchy',
          'Searchable employee directory',
          'Document storage and management',
        ]}
        preview={<EmployeeManagementPreview />}
      />

      {/* Attendance */}
      <FeatureSection
        id="attendance"
        eyebrow="Attendance Management"
        heading="Time well tracked. Days well spent."
        description="Real-time attendance monitoring with check-in/check-out, shift scheduling, and detailed analytics to keep your workforce on track."
        bullets={[
          'Real-time check-in and check-out tracking',
          'Shift management and scheduling',
          'Weekly and monthly attendance reports',
          'Late arrival and overtime alerts',
        ]}
        preview={<AttendancePreview />}
        reverse
      />

      {/* Leave */}
      <FeatureSection
        id="leave"
        eyebrow="Leave Management"
        heading="Less paperwork. More peace of mind."
        description="Simplify leave requests, approvals, and balance tracking. Give employees visibility into their entitlements and managers a clear view of team availability."
        bullets={[
          'One-click leave applications',
          'Configurable leave types and policies',
          'Manager approval workflows',
          'Team leave calendar view',
        ]}
        preview={<LeavePreview />}
      />

      {/* Payroll */}
      <FeatureSection
        id="payroll"
        eyebrow="Payroll Management"
        heading="From people data to payday."
        description="Automate salary calculations, manage deductions, and generate payslips — all from the same platform where your employee data lives."
        bullets={[
          'Automated salary and deduction calculations',
          'Payslip generation and distribution',
          'Tax and compliance management',
          'Payroll summary and audit reports',
        ]}
        preview={<PayrollPreview />}
        reverse
      />

      {/* Recruitment */}
      <FeatureSection
        id="recruitment"
        eyebrow="Recruitment"
        heading="Great teams start with a great hello."
        description="Manage job postings, track candidates through the pipeline, and coordinate interviews — all in one place so you never lose a great hire."
        bullets={[
          'Job posting and applicant tracking',
          'Visual candidate pipeline',
          'Interview scheduling and feedback',
          'Offer letter management',
        ]}
        preview={<RecruitmentPreview />}
      />

      {/* Performance */}
      <FeatureSection
        id="performance"
        eyebrow="Performance Management"
        heading="Make growth part of the everyday."
        description="Set goals, run review cycles, and track employee growth with structured performance management that keeps everyone aligned and motivated."
        bullets={[
          'Goal setting and OKR tracking',
          '360° feedback and peer reviews',
          'Performance score analytics',
          'Growth plans and development tracking',
        ]}
        preview={<PerformancePreview />}
        reverse
      />

      <DarkFeatureSection />
      <BenefitsSection />
      <CtaSection />
      <HomeFooter />
    </div>
  );
}
