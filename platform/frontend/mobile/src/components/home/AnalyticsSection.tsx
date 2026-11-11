import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { COLORS } from '../../../../shared/constants';

const AnalyticsSection: React.FC = () => {
  const stats = [
    { label: 'Total', val: '248', inc: '+12' },
    { label: 'Present', val: '94%', inc: '+2.1%' },
    { label: 'Retention', val: '98%', inc: '+1%' }
  ];

  return (
    <View style={styles.container}>
      <Text style={styles.title}>
        Everything in sync.{"\n"}
        Everyone empowered.
      </Text>
      <Text style={styles.subtitle}>
        Get a complete picture of your workforce with meaningful insights.
      </Text>

      <View style={styles.statsGrid}>
        {stats.map((s, i) => (
          <View key={i} style={styles.statCard}>
            <Text style={styles.statLabel}>{s.label}</Text>
            <Text style={styles.statVal}>{s.val}</Text>
            <Text style={styles.statInc}>{s.inc}</Text>
          </View>
        ))}
      </View>

      <View style={styles.chartMockup}>
         <View style={styles.chartHeader}>
            <Text style={styles.chartTitle}>Workforce Trends</Text>
         </View>
         <View style={styles.bars}>
            {[30, 50, 40, 70, 60, 90, 75, 55].map((h, i) => (
              <View key={i} style={[styles.bar, { height: h }]} />
            ))}
         </View>
         <Text style={styles.demoTag}>DEMO DATA</Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { padding: 25, backgroundColor: COLORS.secondary, paddingVertical: 50 },
  title: { fontSize: 28, fontWeight: '800', color: COLORS.white, marginBottom: 15, textAlign: 'center' },
  subtitle: { fontSize: 15, color: '#93C5FD', textAlign: 'center', marginBottom: 35, lineHeight: 22 },
  statsGrid: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 30 },
  statCard: { flex: 1, backgroundColor: 'rgba(255,255,255,0.05)', borderRadius: 15, padding: 15, marginHorizontal: 5, alignItems: 'center' },
  statLabel: { fontSize: 10, color: '#93C5FD', textTransform: 'uppercase', marginBottom: 5 },
  statVal: { fontSize: 18, fontWeight: 'bold', color: COLORS.white, marginBottom: 2 },
  statInc: { fontSize: 10, color: '#4ADE80', fontWeight: 'bold' },
  chartMockup: { backgroundColor: 'rgba(255,255,255,0.05)', borderRadius: 20, padding: 20 },
  chartHeader: { marginBottom: 20 },
  chartTitle: { color: COLORS.white, fontWeight: 'bold', fontSize: 16 },
  bars: { flexDirection: 'row', alignItems: 'end', justifyContent: 'space-between', height: 100 },
  bar: { width: 25, backgroundColor: COLORS.primary, borderRadius: 5 },
  demoTag: { fontSize: 8, color: 'rgba(255,255,255,0.2)', textAlign: 'center', marginTop: 15, letterSpacing: 2 }
});

export default AnalyticsSection;
