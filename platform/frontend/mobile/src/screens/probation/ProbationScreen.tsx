import { useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { useProbations } from '../../../../shared/hooks';
import { PROBATION_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import type { Probation } from '../../../../shared/types';
import { MBadge, MCard, MSpinner, COLORS } from '../../components';

export default function ProbationScreen() {
  const [statusFilter, setStatusFilter] = useState('');
  const { probations, isLoading, setStatus, confirmMutation, extendMutation, rejectMutation } = useProbations();

  const handleConfirm = (id: string) => {
    Alert.alert('Confirm Probation', 'This will change employee status to ACTIVE.', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Confirm', style: 'default',
        onPress: () => confirmMutation.mutate({ id, data: { remarks: '' } }),
      },
    ]);
  };

  const handleExtend = (id: string) => {
    Alert.prompt('Extend Probation', 'Enter new end date (YYYY-MM-DD):', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Extend', style: 'default',
        onPress: (date) => extendMutation.mutate({ id, data: { newEndDate: date, reason: 'Extended via mobile' } }),
      },
    ], 'plain-text');
  };

  const handleReject = (id: string) => {
    Alert.alert('Reject Probation', 'This will TERMINATE the employee. Are you sure?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Reject', style: 'destructive',
        onPress: () => rejectMutation.mutate({ id, data: { reason: 'Probation not confirmed' } }),
      },
    ]);
  };

  const STATUS_OPTIONS = ['', 'IN_PROGRESS', 'CONFIRMED', 'EXTENDED', 'REJECTED'];

  const renderItem = ({ item }: { item: Probation }) => {
    const effectiveEnd = item.extendedEndDate || item.endDate;
    const pct = Math.min(100, Math.max(0, Math.round((1 - item.daysRemaining / 90) * 100)));
    const barColor = item.daysRemaining <= 7 ? COLORS.danger : item.daysRemaining <= 30 ? '#f59e0b' : '#22c55e';

    return (
      <MCard style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={styles.cardHeaderLeft}>
            <Text style={styles.employeeName}>{item.employeeName || 'Employee'}</Text>
            <Text style={styles.employeeCode}>{item.employeeCode}</Text>
          </View>
          <MBadge label={item.status} color={PROBATION_STATUS_COLOR[item.status] as never ?? 'gray'} />
        </View>

        {/* Progress Bar */}
        <View style={styles.progressSection}>
          <View style={styles.progressHeader}>
            <Text style={styles.progressLabel}>Progress</Text>
            <Text style={[styles.daysLeft, { color: barColor }]}>{item.daysRemaining} days left</Text>
          </View>
          <View style={styles.progressTrack}>
            <View style={[styles.progressFill, { width: `${pct}%` as never, backgroundColor: barColor }]} />
          </View>
        </View>

        <View style={styles.dateRow}>
          <View style={styles.dateItem}>
            <Text style={styles.dateLabel}>Start</Text>
            <Text style={styles.dateValue}>{formatDate(item.startDate)}</Text>
          </View>
          <View style={styles.dateItem}>
            <Text style={styles.dateLabel}>End</Text>
            <Text style={styles.dateValue}>{formatDate(effectiveEnd)}</Text>
          </View>
        </View>

        {(item.status === 'IN_PROGRESS' || item.status === 'EXTENDED') && (
          <View style={styles.actions}>
            <TouchableOpacity style={[styles.actionBtn, styles.confirmBtn]} onPress={() => handleConfirm(item.id)}>
              <Text style={styles.confirmBtnText}>✓ Confirm</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.actionBtn, styles.extendBtn]} onPress={() => handleExtend(item.id)}>
              <Text style={styles.extendBtnText}>⏱ Extend</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.actionBtn, styles.rejectBtn]} onPress={() => handleReject(item.id)}>
              <Text style={styles.rejectBtnText}>✗ Reject</Text>
            </TouchableOpacity>
          </View>
        )}
      </MCard>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.filterRow}>
        {STATUS_OPTIONS.map((s) => (
          <TouchableOpacity
            key={s || 'all'}
            style={[styles.filterChip, statusFilter === s && styles.filterChipActive]}
            onPress={() => { setStatusFilter(s); setStatus(s); }}
          >
            <Text style={[styles.filterChipText, statusFilter === s && styles.filterChipTextActive]}>
              {s || 'All'}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {isLoading ? <MSpinner /> : (
        <FlatList
          data={probations}
          keyExtractor={(item) => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.list}
          ListEmptyComponent={<View style={styles.empty}><Text style={styles.emptyText}>No probations found</Text></View>}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container:           { flex: 1, backgroundColor: COLORS.slate50 },
  filterRow:           { flexDirection: 'row', padding: 12, gap: 8, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200, flexWrap: 'wrap' },
  filterChip:          { paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999, backgroundColor: COLORS.slate100, borderWidth: 1, borderColor: COLORS.slate200 },
  filterChipActive:    { backgroundColor: COLORS.primary, borderColor: COLORS.primary },
  filterChipText:      { fontSize: 12, fontWeight: '600', color: COLORS.slate600 },
  filterChipTextActive:{ color: COLORS.white },
  list:                { padding: 16, gap: 12, paddingBottom: 32 },
  card:                { gap: 12 },
  cardHeader:          { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  cardHeaderLeft:      { flex: 1 },
  employeeName:        { fontSize: 15, fontWeight: '700', color: COLORS.slate800 },
  employeeCode:        { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  progressSection:     { gap: 6 },
  progressHeader:      { flexDirection: 'row', justifyContent: 'space-between' },
  progressLabel:       { fontSize: 12, color: COLORS.slate500 },
  daysLeft:            { fontSize: 12, fontWeight: '700' },
  progressTrack:       { height: 6, backgroundColor: COLORS.slate200, borderRadius: 3, overflow: 'hidden' },
  progressFill:        { height: 6, borderRadius: 3 },
  dateRow:             { flexDirection: 'row', gap: 16 },
  dateItem:            { flex: 1 },
  dateLabel:           { fontSize: 11, color: COLORS.slate400, textTransform: 'uppercase', letterSpacing: 0.5 },
  dateValue:           { fontSize: 13, fontWeight: '600', color: COLORS.slate700, marginTop: 2 },
  actions:             { flexDirection: 'row', gap: 6, borderTopWidth: 1, borderTopColor: COLORS.slate100, paddingTop: 12 },
  actionBtn:           { flex: 1, paddingVertical: 9, borderRadius: 8, alignItems: 'center' },
  confirmBtn:          { backgroundColor: '#dcfce7' },
  confirmBtnText:      { fontSize: 12, fontWeight: '700', color: '#15803d' },
  extendBtn:           { backgroundColor: '#fef9c3' },
  extendBtnText:       { fontSize: 12, fontWeight: '700', color: '#a16207' },
  rejectBtn:           { backgroundColor: '#fee2e2' },
  rejectBtnText:       { fontSize: 12, fontWeight: '700', color: '#dc2626' },
  empty:               { padding: 48, alignItems: 'center' },
  emptyText:           { fontSize: 14, color: COLORS.slate400 },
});
