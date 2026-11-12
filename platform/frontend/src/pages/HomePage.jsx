import { useTranslation } from 'react-i18next';
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
import ServicesSection from './home/ServicesSection';
import MultiTenantSection from './home/MultiTenantSection';
import CtaSection from './home/CtaSection';
import HomeFooter from './home/HomeFooter';

export default function HomePage() {
  const { t } = useTranslation();

  return (
    <div className="min-h-screen bg-white text-[#172033]">
      <HomeNavbar />
      <HeroSection />
      <JourneySection />

      {/* Employee Management */}
      <FeatureSection
        id="employee-management"
        eyebrow={t('home.feature.emp.eyebrow')}
        heading={t('home.feature.emp.heading')}
        description={t('home.feature.emp.description')}
        bullets={[
          t('home.feature.emp.b1'),
          t('home.feature.emp.b2'),
          t('home.feature.emp.b3'),
          t('home.feature.emp.b4'),
        ]}
        preview={<EmployeeManagementPreview />}
      />

      {/* Attendance */}
      <FeatureSection
        id="attendance"
        eyebrow={t('home.feature.attendance.eyebrow')}
        heading={t('home.feature.attendance.heading')}
        description={t('home.feature.attendance.description')}
        bullets={[
          t('home.feature.attendance.b1'),
          t('home.feature.attendance.b2'),
          t('home.feature.attendance.b3'),
          t('home.feature.attendance.b4'),
        ]}
        preview={<AttendancePreview />}
        reverse
      />

      {/* Leave */}
      <FeatureSection
        id="leave"
        eyebrow={t('home.feature.leave.eyebrow')}
        heading={t('home.feature.leave.heading')}
        description={t('home.feature.leave.description')}
        bullets={[
          t('home.feature.leave.b1'),
          t('home.feature.leave.b2'),
          t('home.feature.leave.b3'),
          t('home.feature.leave.b4'),
        ]}
        preview={<LeavePreview />}
      />

      {/* Payroll */}
      <FeatureSection
        id="payroll"
        eyebrow={t('home.feature.payroll.eyebrow')}
        heading={t('home.feature.payroll.heading')}
        description={t('home.feature.payroll.description')}
        bullets={[
          t('home.feature.payroll.b1'),
          t('home.feature.payroll.b2'),
          t('home.feature.payroll.b3'),
          t('home.feature.payroll.b4'),
        ]}
        preview={<PayrollPreview />}
        reverse
      />

      {/* Recruitment */}
      <FeatureSection
        id="recruitment"
        eyebrow={t('home.feature.recruitment.eyebrow')}
        heading={t('home.feature.recruitment.heading')}
        description={t('home.feature.recruitment.description')}
        bullets={[
          t('home.feature.recruitment.b1'),
          t('home.feature.recruitment.b2'),
          t('home.feature.recruitment.b3'),
          t('home.feature.recruitment.b4'),
        ]}
        preview={<RecruitmentPreview />}
      />

      {/* Performance */}
      <FeatureSection
        id="performance"
        eyebrow={t('home.feature.performance.eyebrow')}
        heading={t('home.feature.performance.heading')}
        description={t('home.feature.performance.description')}
        bullets={[
          t('home.feature.performance.b1'),
          t('home.feature.performance.b2'),
          t('home.feature.performance.b3'),
          t('home.feature.performance.b4'),
        ]}
        preview={<PerformancePreview />}
        reverse
      />

      <DarkFeatureSection />
      <BenefitsSection />
      <ServicesSection />
      <MultiTenantSection />
      <CtaSection />
      <HomeFooter />
    </div>
  );
}
