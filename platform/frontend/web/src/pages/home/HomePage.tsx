import React, { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import HomeNavbar              from '../../components/common/Navbar';
import HeroSection             from '../../components/home/HeroSection';
import TrustSection            from '../../components/home/TrustSection';
import AudienceSection         from '../../components/home/AudienceSection';
import FeatureSectionHeader    from '../../components/home/FeatureSectionHeader';
import EmployeeManagementSection from '../../components/home/EmployeeManagementSection';
import AttendanceSection       from '../../components/home/AttendanceSection';
import LeaveManagementSection  from '../../components/home/LeaveManagementSection';
import PayrollSection          from '../../components/home/PayrollSection';
import RecruitmentSection      from '../../components/home/RecruitmentSection';
import PerformanceSection      from '../../components/home/PerformanceSection';
import MultiTenantSection      from '../../components/home/MultiTenantSection';
import ServicesSection         from '../../components/home/ServicesSection';
import AnalyticsSection        from '../../components/home/AnalyticsSection';
import TestimonialsSection     from '../../components/home/TestimonialsSection';
import CTASection              from '../../components/home/CTASection';
import Footer                  from '../../components/common/Footer';

const HomePage: React.FC = () => {
  const { t, i18n } = useTranslation();

  // Keep document title in sync with language
  useEffect(() => {
    document.title = `WorkSuite HRMS — ${t('homepage.heroHeadline1')} ${t('homepage.heroHeadline2')}`;
  }, [t, i18n.language]);

  return (
    <div className="min-h-screen bg-white">
      <HomeNavbar />
      <main>
        <HeroSection />
        <TrustSection />
        <AudienceSection />
        <FeatureSectionHeader />
        <EmployeeManagementSection />
        <AttendanceSection />
        <LeaveManagementSection />
        <PayrollSection />
        <RecruitmentSection />
        <PerformanceSection />
        <MultiTenantSection />
        <ServicesSection />
        <AnalyticsSection />
        <TestimonialsSection />
        <CTASection />
      </main>
      <Footer />
    </div>
  );
};

export default HomePage;
