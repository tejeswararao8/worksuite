import { useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { usePromotions } from '../../../../shared/hooks';
import { APPROVAL_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import type { Promotion } from '../../../../shared/types';
import { MBadge, MCard, MSpinner, COLORS } from '../../components';

export default function PromotionsScreen() {
  const [statusFilter, setStatusFilter] = useState('');
  const { promotions, isLoading, setStatus, approveMutation, rejectMutation } = usePromotions();

  const handleApprove = (id: string) => {
    Alert.alert('Approve Promotion', 'Confirm this promotion?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Approve', style: 'default',
        onPress: () => approveMutation.mutate({ id, data: { comments: '' } }),
      },
    ]);
  };

  const handleReject = (id: string) => {
    Alert.prompt('Reject Promotion', 'Enter reason:', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Reject', style: 'destructive',
        onPress: (reason) => rejectMutation.mutate({ id, data: { reason: reason || 'Rejected' } }),
      },
    ], 'plain-text');
  };

  const STATUS_OPTIONS = ['', 'PENDING', 'APPROVED', 'REJECTED'];

  const renderItem = ({ item }: { item: Promotion }) => (
    <MCard style={styles.card}>
      <View style={styles.cardHeader}>
        <View style={styles.cardHeaderLeft}>
          <Text style={styles.employeeName}>{item.employeeName || 'Employee'}</Text>
          <Text style={styles.employeeCode}>{item.employeeCode}</Text>
        </View>
        <MBadge label={item.status} color={APPROVAL_STATUS_COLOR[item.status] as never ?? 'gray'} />
      </View>

      <View style={styles.promotionFlow}>
        <View style={styles.designationBox}>
          <Text style={styles.designationLabel}>From</Text>
          <Text style={styles.designationTitle}>{item.fromDesignationTitle || '—'}</Text>
        </View>
        <Text style={styles.arrow}>→</Text>
        <View style={styles.designationBox}>
          <Text style={styles.designationLabel}>To</Text>
          <Text style={[styles.designationTitle, styles.toDesignation]}>{item.toDesignationTitle || '—'}</Text>
        </View>
      </View>

      <View style={styles.infoRow}>
        <Text style={styles.infoLabel}>Effective Date</Text>
        <Text style={styles.infoValue}>{formatDate(item.effectiveDate)}</Text>
      </View>
      {item.reason && (
        <View style={styles.infoRow}>
          <Text style={styles.infoLabel}>Reason</Text>
          <Text style={styles.infoValue} numberOfLines={2}>{item.reason}</Text>
        </View>
      )}

      {item.status === 'PENDING' && (
        <View style={styles.actions}>
          <TouchableOpacity style={[styles.actionBtn, styles.approveBtn]} onPress={() => handleApprove(item.id)}>
            <Text style={styles.approveBtnText}>✓ Approve</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.actionBtn, styles.rejectBtn]} onPress={() => handleReject(item.id)}>
            <Text style={styles.rejectBtnText}>✗ Reject</Text>
          </TouchableOpacity>
        </View>
      )}
    </MCard>
  );

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
          data={promotions}
          keyExtractor={(item) => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.list}
          ListEmptyComponent={<View style={styles.empty}><Text style={styles.emptyText}>No promotions found</Text></View>}
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
  promotionFlow:       { flexDirection: 'row', alignItems: 'center', gap: 8, backgroundColor: COLORS.slate50, borderRadius: 10, padding: 12 },
  designationBox:      { flex: 1, alignItems: 'center' },
  designationLabel:    { fontSize: 10, color: COLORS.slate400, textTransform: 'uppercase', letterSpacing: 0.5 },
  designationTitle:    { fontSize: 13, fontWeight: '600', color: COLORS.slate700, textAlign: 'center', marginTop: 2 },
  toDesignation:       { color: COLORS.primary },
  arrow:               { fontSize: 20, color: COLORS.slate400 },
  infoRow:             { flexDirection: 'row', justifyContent: 'space-between' },
  infoLabel:           { fontSize: 12, color: COLORS.slate500 },
  infoValue:           { fontSize: 13, fontWeight: '500', color: COLORS.slate700, flex: 1, textAlign: 'right' },
  actions:             { flexDirection: 'row', gap: 8, borderTopWidth: 1, borderTopColor: COLORS.slate100, paddingTop: 12 },
  actionBtn:           { flex: 1, paddingVertical: 10, borderRadius: 8, alignItems: 'center' },
  approveBtn:          { backgroundColor: '#dcfce7' },
  approveBtnText:      { fontSize: 13, fontWeight: '700', color: '#15803d' },
  rejectBtn:           { backgroundColor: '#fee2e2' },
  rejectBtnText:       { fontSize: 13, fontWeight: '700', color: '#dc2626' },
  empty:               { padding: 48, alignItems: 'center' },
  emptyText:           { fontSize: 14, color: COLORS.slate400 },
});
