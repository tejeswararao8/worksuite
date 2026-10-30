import { ScrollView, View, Text, StyleSheet, RefreshControl, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useSelector } from 'react-redux';
import { useDashboard } from '../../../../shared/hooks';
import type { RootState } from '../../../../shared/store';
import { MStatCard, MCard, MSpinner, COLORS } from '../../components';

export default function DashboardScreen() {
  const navigation = useNavigation();
  const user = useSelector((s: RootState) => s.auth.user);
  const { stats: s, isLoading } = useDashboard();

  if (isLoading) return <MSpinner />;

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      refreshControl={<RefreshControl refreshing={isLoading} onRefresh={() => {}} />}
    >
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.greeting}>Good morning 👋</Text>
        <Text style={styles.email}>{user?.email}</Text>
        <Text style={styles.role}>{user?.role}</Text>
      </View>

      {/* Alerts */}
      {(s.probationEndingSoon > 0 || s.pendingTransfers > 0) && (
        <MCard style={styles.alertCard}>
          <Text style={styles.alertTitle}>⚠ Attention Required</Text>
          {s.probationEndingSoon > 0 && (
            <Text style={styles.alertText}>• {s.probationEndingSoon} probation(s) ending soon</Text>
          )}
          {s.pendingTransfers > 0 && (
            <Text style={styles.alertText}>• {s.pendingTransfers} transfer(s) pending approval</Text>
          )}
          {s.pendingPromotions > 0 && (
            <Text style={styles.alertText}>• {s.pendingPromotions} promotion(s) pending approval</Text>
          )}
        </MCard>
      )}

      {/* Stats Grid */}
      <Text style={styles.sectionTitle}>Overview</Text>
      <View style={styles.statsGrid}>
        <MStatCard title="Total Employees"  value={s.totalEmployees  ?? 0} color="indigo" />
        <MStatCard title="Active"           value={s.activeEmployees ?? 0} color="green" />
        <MStatCard title="On Probation"     value={s.onProbation     ?? 0} color="yellow" />
        <MStatCard title="Pending Transfers"value={s.pendingTransfers ?? 0} color="blue" />
        <MStatCard title="In Onboarding"    value={s.employeesInOnboarding ?? 0} color="orange" />
        <MStatCard title="Available Assets" value={s.availableAssets ?? 0} color="indigo" />
      </View>

      {/* Quick Actions */}
      <Text style={styles.sectionTitle}>Quick Actions</Text>
      <MCard>
        {[
          { label: '📋 Employees',    screen: 'Employees' },
          { label: '🔄 Transfers',    screen: 'Transfers' },
          { label: '📈 Promotions',   screen: 'Promotions' },
          { label: '⏱ Probation',    screen: 'Probation' },
          { label: '✅ Onboarding',   screen: 'Onboarding' },
          { label: '💻 Assets',       screen: 'Assets' },
          { label: '📊 Reports',      screen: 'Reports' },
          { label: '🏢 Departments',  screen: 'Departments' },
          { label: '🌿 Branches',     screen: 'Branches' },
          { label: '🎖 Designations', screen: 'Designations' },
          { label: '👥 Teams',        screen: 'Teams' },
          { label: '📜 Audit Logs',   screen: 'Audit' },
        ].map((action) => (
          <TouchableOpacity
            key={action.screen}
            style={styles.quickActionItem}
            onPress={() => navigation.navigate(action.screen as never)}
            activeOpacity={0.7}
          >
            <Text style={styles.quickActionText}>{action.label}</Text>
            <Text style={styles.quickActionChevron}>›</Text>
          </TouchableOpacity>
        ))}
      </MCard>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container:        { flex: 1, backgroundColor: COLORS.slate50 },
  content:          { padding: 16, gap: 16, paddingBottom: 32 },
  header:           { backgroundColor: COLORS.primary, borderRadius: 16, padding: 20 },
  greeting:         { fontSize: 22, fontWeight: '700', color: COLORS.white },
  email:            { fontSize: 13, color: 'rgba(255,255,255,0.8)', marginTop: 4 },
  role:             { fontSize: 12, color: 'rgba(255,255,255,0.6)', marginTop: 2 },
  alertCard:        { borderLeftWidth: 4, borderLeftColor: COLORS.warning },
  alertTitle:       { fontSize: 13, fontWeight: '700', color: COLORS.slate800, marginBottom: 6 },
  alertText:        { fontSize: 12, color: COLORS.slate600, marginBottom: 2 },
  sectionTitle:     { fontSize: 15, fontWeight: '700', color: COLORS.slate800 },
  statsGrid:        { flexDirection: 'row', flexWrap: 'wrap', gap: 12 },
  quickActionItem:    { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: COLORS.slate100 },
  quickActionText:    { fontSize: 14, color: COLORS.slate700 },
  quickActionChevron: { fontSize: 18, color: COLORS.slate300 },
});
