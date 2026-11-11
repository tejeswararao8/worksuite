import React from 'react';
import { Navbar } from '../../components/home/Navbar';
import HeroSection from '../../components/home/HeroSection';
import TrustSection from '../../components/home/TrustSection';
import AnalyticsSection from '../../components/home/AnalyticsSection';
import TestimonialsSection from '../../components/home/TestimonialsSection';
import CTASection from '../../components/home/CTASection';
import Footer from '../../components/home/Footer';

const HomePage: React.FC = () => {
  return (
    <div className="min-h-screen bg-white">
      <Navbar />
      <main>
        <HeroSection />
        <TrustSection />
        {/* Placeholder for feature sections */}
        <div id="features" className="py-20 bg-gray-50">
          <div className="max-w-7xl mx-auto px-4 text-center">
            <h2 className="text-3xl font-bold text-gray-900">Core Features</h2>
            <p className="mt-4 text-gray-600">Explore everything WorkSuite has to offer.</p>
          </div>
        </div>
        <AnalyticsSection />
        <TestimonialsSection />
        <CTASection />
      </main>
      <Footer />
    </div>
  );
};

export default HomePage;
