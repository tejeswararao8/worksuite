import React from 'react';
import Navbar from '../components/common/Navbar';
import HeroSection from '../components/home/HeroSection';
import TrustSection from '../components/home/TrustSection';
import EmployeeManagementSection from '../components/home/EmployeeManagementSection';
import AttendanceSection from '../components/home/AttendanceSection';
import LeaveManagementSection from '../components/home/LeaveManagementSection';
import PayrollSection from '../components/home/PayrollSection';
import RecruitmentSection from '../components/home/RecruitmentSection';
import PerformanceSection from '../components/home/PerformanceSection';
import AnalyticsSection from '../components/home/AnalyticsSection';
import TestimonialsSection from '../components/home/TestimonialsSection';
import CTASection from '../components/home/CTASection';
import Footer from '../components/common/Footer';

const HomePage: React.FC = () => {
  return (
    <div className="bg-white">
      <Navbar />
      <HeroSection />
      <TrustSection />
      <EmployeeManagementSection />
      <AttendanceSection />
      <LeaveManagementSection />
      <PayrollSection />
      <RecruitmentSection />
      <PerformanceSection />
      <AnalyticsSection />
      <TestimonialsSection />
      <CTASection />
      <Footer />
    </div>
  );
};

export default HomePage;
