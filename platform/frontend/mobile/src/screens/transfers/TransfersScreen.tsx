import { useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity,
  StyleSheet, Alert, TextInput,
} from 'react-native';
import { useTransfers } from '../../../../shared/hooks';
import { APPROVAL_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import type { Transfer } from '../../../../shared/types';
import { MBadge, MCard, MSpinner, COLORS } from '../../components';

export default function TransfersScreen() {
  const [statusFilter, setStatusFilter] = useState('');
  const { transfers, isLoading, setStatus, approveMutation, rejectMutation } = useTransfers();

  const handleApprove = (id: string) => {
    Alert.alert('Approve Transfer', 'Are you sure you want to approve this transfer?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Approve', style: 'default',
        onPress: () => approveMutation.mutate({ id, data: { comments: '' } }),
      },
    ]);
  };

  const handleReject = (id: string) => {
    Alert.prompt(
      'Reject Transfer',
      'Enter reason for rejection:',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Reject', style: 'destructive',
          onPress: (reason) => rejectMutation.mutate({ id, data: { reason: reason || 'Rejected' } }),
        },
      ],
      'plain-text'
    );
  };

  const STATUS_OPTIONS = ['', 'PENDING', 'APPROVED', 'REJECTED', 'COMPLETED'];

  const renderItem = ({ item }: { item: Transfer }) => (
    <MCard style={styles.card}>
      <View style={styles.cardHeader}>
        <View style={styles.cardHeaderLeft}>
          <Text style={styles.employeeName}>{item.employeeName || 'Employee'}</Text>
          <Text style={styles.employeeCode}>{item.employeeCode}</Text>
        </View>
        <MBadge
          label={item.status}
          color={APPROVAL_STATUS_COLOR[item.status] as never ?? 'gray'}
        />
      </View>

      <View style={styles.transferInfo}>
        <View style={styles.transferRow}>
          <Text style={styles.transferLabel}>Type</Text>
          <MBadge label={item.transferType} color="blue" />
        </View>
        <View style={styles.transferRow}>
          <Text style={styles.transferLabel}>From → To</Text>
          <Text style={styles.transferValue} numberOfLines={1}>
            {item.fromDepartmentName || item.fromBranchName || '—'}
            {' → '}
            {item.toDepartmentName || item.toBranchName || '—'}
          </Text>
        </View>
        <View style={styles.transferRow}>
          <Text style={styles.transferLabel}>Effective Date</Text>
          <Text style={styles.transferValue}>{formatDate(item.effectiveDate)}</Text>
        </View>
        {item.reason && (
          <View style={styles.transferRow}>
            <Text style={styles.transferLabel}>Reason</Text>
            <Text style={styles.transferValue} numberOfLines={2}>{item.reason}</Text>
          </View>
        )}
      </View>

      {item.status === 'PENDING' && (
        <View style={styles.actions}>
          <TouchableOpacity
            style={[styles.actionBtn, styles.approveBtn]}
            onPress={() => handleApprove(item.id)}
          >
            <Text style={styles.approveBtnText}>✓ Approve</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.actionBtn, styles.rejectBtn]}
            onPress={() => handleReject(item.id)}
          >
            <Text style={styles.rejectBtnText}>✗ Reject</Text>
          </TouchableOpacity>
        </View>
      )}
    </MCard>
  );

  return (
    <View style={styles.container}>
      {/* Status Filter */}
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

      {isLoading ? (
        <MSpinner />
      ) : (
        <FlatList
          data={transfers}
          keyExtractor={(item) => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.list}
          ListEmptyComponent={
            <View style={styles.empty}>
              <Text style={styles.emptyText}>No transfers found</Text>
            </View>
          }
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
  transferInfo:        { gap: 8, borderTopWidth: 1, borderTopColor: COLORS.slate100, paddingTop: 12 },
  transferRow:         { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  transferLabel:       { fontSize: 12, color: COLORS.slate500, flex: 1 },
  transferValue:       { fontSize: 13, fontWeight: '500', color: COLORS.slate700, flex: 2, textAlign: 'right' },
  actions:             { flexDirection: 'row', gap: 8, borderTopWidth: 1, borderTopColor: COLORS.slate100, paddingTop: 12 },
  actionBtn:           { flex: 1, paddingVertical: 10, borderRadius: 8, alignItems: 'center' },
  approveBtn:          { backgroundColor: '#dcfce7' },
  approveBtnText:      { fontSize: 13, fontWeight: '700', color: '#15803d' },
  rejectBtn:           { backgroundColor: '#fee2e2' },
  rejectBtnText:       { fontSize: 13, fontWeight: '700', color: '#dc2626' },
  empty:               { padding: 48, alignItems: 'center' },
  emptyText:           { fontSize: 14, color: COLORS.slate400 },
});
