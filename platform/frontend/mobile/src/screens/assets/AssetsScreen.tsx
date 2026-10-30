import { useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { useAssets } from '../../../../shared/hooks';
import { ASSET_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import type { Asset } from '../../../../shared/types';
import { MBadge, MCard, MSpinner, COLORS } from '../../components';

export default function AssetsScreen() {
  const [statusFilter, setStatusFilter] = useState('');
  const { assets, isLoading, setStatus, assignMutation, returnMutation, retireMutation } = useAssets();

  const handleAssign = (id: string) => {
    Alert.prompt('Assign Asset', 'Enter Employee ID:', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Assign', style: 'default',
        onPress: (empId) => assignMutation.mutate({ id, data: { employeeId: empId } }),
      },
    ], 'plain-text');
  };

  const handleReturn = (id: string) => {
    Alert.alert('Return Asset', 'Confirm asset return?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Return', style: 'default',
        onPress: () => returnMutation.mutate({ id, data: { newStatus: 'AVAILABLE' } }),
      },
    ]);
  };

  const handleRetire = (id: string) => {
    Alert.alert('Retire Asset', 'This cannot be undone. Retire this asset?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Retire', style: 'destructive', onPress: () => retireMutation.mutate(id) },
    ]);
  };

  const STATUS_OPTIONS = ['', 'AVAILABLE', 'ASSIGNED', 'IN_REPAIR', 'RETIRED'];

  const renderItem = ({ item }: { item: Asset }) => (
    <MCard style={styles.card}>
      <View style={styles.cardHeader}>
        <View style={styles.cardHeaderLeft}>
          <Text style={styles.assetName}>{item.name}</Text>
          <Text style={styles.assetCode}>{item.assetCode} · {item.assetType}</Text>
        </View>
        <MBadge label={item.status} color={ASSET_STATUS_COLOR[item.status] as never ?? 'gray'} />
      </View>

      <View style={styles.assetInfo}>
        {item.serialNumber && (
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Serial</Text>
            <Text style={styles.infoValue}>{item.serialNumber}</Text>
          </View>
        )}
        {item.model && (
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Model</Text>
            <Text style={styles.infoValue}>{item.model}</Text>
          </View>
        )}
        {item.assignedToEmployeeName && (
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Assigned To</Text>
            <Text style={styles.infoValue}>{item.assignedToEmployeeName}</Text>
          </View>
        )}
        {item.warrantyExpiry && (
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Warranty</Text>
            <Text style={[styles.infoValue, item.warrantyExpiringSoon && styles.warningText]}>
              {formatDate(item.warrantyExpiry)}{item.warrantyExpiringSoon ? ' ⚠' : ''}
            </Text>
          </View>
        )}
      </View>

      <View style={styles.actions}>
        {item.status === 'AVAILABLE' && (
          <TouchableOpacity style={[styles.actionBtn, styles.assignBtn]} onPress={() => handleAssign(item.id)}>
            <Text style={styles.assignBtnText}>Assign</Text>
          </TouchableOpacity>
        )}
        {item.status === 'ASSIGNED' && (
          <TouchableOpacity style={[styles.actionBtn, styles.returnBtn]} onPress={() => handleReturn(item.id)}>
            <Text style={styles.returnBtnText}>Return</Text>
          </TouchableOpacity>
        )}
        {item.status !== 'RETIRED' && (
          <TouchableOpacity style={[styles.actionBtn, styles.retireBtn]} onPress={() => handleRetire(item.id)}>
            <Text style={styles.retireBtnText}>Retire</Text>
          </TouchableOpacity>
        )}
      </View>
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
          data={assets}
          keyExtractor={(item) => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.list}
          ListEmptyComponent={<View style={styles.empty}><Text style={styles.emptyText}>No assets found</Text></View>}
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
  assetName:           { fontSize: 15, fontWeight: '700', color: COLORS.slate800 },
  assetCode:           { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  assetInfo:           { gap: 6, borderTopWidth: 1, borderTopColor: COLORS.slate100, paddingTop: 10 },
  infoRow:             { flexDirection: 'row', justifyContent: 'space-between' },
  infoLabel:           { fontSize: 12, color: COLORS.slate500 },
  infoValue:           { fontSize: 13, fontWeight: '500', color: COLORS.slate700 },
  warningText:         { color: '#f59e0b', fontWeight: '700' },
  actions:             { flexDirection: 'row', gap: 8, borderTopWidth: 1, borderTopColor: COLORS.slate100, paddingTop: 10 },
  actionBtn:           { flex: 1, paddingVertical: 9, borderRadius: 8, alignItems: 'center' },
  assignBtn:           { backgroundColor: '#dbeafe' },
  assignBtnText:       { fontSize: 13, fontWeight: '700', color: '#1d4ed8' },
  returnBtn:           { backgroundColor: '#dcfce7' },
  returnBtnText:       { fontSize: 13, fontWeight: '700', color: '#15803d' },
  retireBtn:           { backgroundColor: '#fee2e2' },
  retireBtnText:       { fontSize: 13, fontWeight: '700', color: '#dc2626' },
  empty:               { padding: 48, alignItems: 'center' },
  emptyText:           { fontSize: 14, color: COLORS.slate400 },
});
