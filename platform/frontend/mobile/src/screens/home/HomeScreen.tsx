import React from 'react';
import { ScrollView, StyleSheet, View, Text } from 'react-native';
import { COLORS } from '../../../shared/constants';
import { SafeAreaView } from 'react-native-safe-area-context';

import HeroSection from '../../components/home/HeroSection';
import TrustSection from '../../components/home/TrustSection';
import FeatureSection from '../../components/home/FeatureSection';
import AnalyticsSection from '../../components/home/AnalyticsSection';
import TestimonialsSection from '../../components/home/TestimonialsSection';
import CTASection from '../../components/home/CTASection';

const HomeScreen: React.FC = () => {
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}>
        {/* Mobile Header */}
        <View style={styles.header}>
            <View style={styles.logoContainer}>
              <View style={styles.logoIcon}><Text style={styles.logoIconText}>W</Text></View>
              <Text style={styles.logoText}>WorkSuite</Text>
            </View>
            <View style={styles.menuIcon}>
                <View style={styles.bar} />
                <View style={[styles.bar, { width: 15 }]} />
            </View>
        </View>

        <HeroSection />
        <TrustSection />
        
        <FeatureSection 
          title="Every person."
          highlight="One place."
          description="Bring employee information, records, roles, and organizational details together in one secure place."
          features={['Centralized profiles', 'Records management', 'Org structure']}
        />

        <FeatureSection 
          title="Time well tracked."
          highlight="Days well spent."
          description="Manage attendance, monitor working hours, and get clear visibility into employee availability."
          features={['Daily tracking', 'Check-in/out', 'Attendance reports']}
          bg={COLORS.lightBackground}
        />

        <FeatureSection 
          title="Less paperwork."
          highlight="More peace of mind."
          description="Make leave management simple with transparent requests, approvals, and balances."
          features={['Leave applications', 'Balance tracking', 'Workflows']}
        />

        <FeatureSection 
          title="From people data"
          highlight="to payday."
          description="Manage payroll information, salary structures, and payments with greater visibility."
          features={['Payroll processing', 'Salary slips', 'Deductions']}
          bg={COLORS.lightBackground}
        />

        <AnalyticsSection />
        <TestimonialsSection />
        <CTASection />

        {/* Simple Footer */}
        <View style={styles.footer}>
           <Text style={styles.footerText}>© 2026 WorkSuite HRMS</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.white },
  scrollContent: { paddingBottom: 0 },
  header: { flexDirection: 'row', justifyContent: 'space-between', padding: 20, alignItems: 'center', backgroundColor: COLORS.lightBackground },
  logoContainer: { flexDirection: 'row', alignItems: 'center' },
  logoIcon: { width: 32, height: 32, backgroundColor: COLORS.primary, borderRadius: 8, alignItems: 'center', justifyContent: 'center', marginRight: 8 },
  logoIconText: { color: COLORS.white, fontWeight: 'bold', fontSize: 18 },
  logoText: { fontSize: 20, fontWeight: 'bold', color: COLORS.secondary },
  menuIcon: { alignItems: 'flex-end' },
  bar: { height: 2, width: 22, backgroundColor: COLORS.secondary, marginBottom: 4, borderRadius: 2 },
  footer: { padding: 30, backgroundColor: COLORS.secondary, alignItems: 'center' },
  footerText: { color: 'rgba(255,255,255,0.4)', fontSize: 12 }
});

export default HomeScreen;
