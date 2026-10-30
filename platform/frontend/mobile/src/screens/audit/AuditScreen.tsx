import { useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../../../../shared/api/services';
import { QUERY_KEYS } from '../../../../shared/constants';
import { formatDateTime, extractPagedData } from '../../../../shared/utils';
import type { AuditLog } from '../../../../shared/types';
import { MBadge, MCard, MSpinner, COLORS } from '../../components';

const ACTION_COLOR: Record<string, string> = {
  CREATE: 'green', UPDATE: 'blue', DELETE: 'red',
  LOGIN: 'indigo', LOGOUT: 'gray', EXPORT: 'orange',
};

const ENTITIES = ['', 'Employee', 'Department', 'Branch', 'Designation', 'Team', 'Transfer', 'Promotion', 'Probation', 'Asset'];

export default function AuditScreen() {
  const [page, setPage]         = useState(0);
  const [entity, setEntity]     = useState('');
  const [expanded, setExpanded] = useState<string | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.AUDIT_LOGS(page, entity),
    queryFn:  () => auditApi.getAll({ page, size: 20, entityName: entity || undefined }),
  });

  const { content: logs, totalPages } = extractPagedData<AuditLog>(data);

  return (
    <View style={styles.container}>
      {/* Entity Filter */}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        style={styles.filterScroll}
        contentContainerStyle={styles.filterRow}
      >
        {ENTITIES.map((e) => (
          <TouchableOpacity
            key={e || 'all'}
            style={[styles.filterChip, entity === e && styles.filterChipActive]}
            onPress={() => { setEntity(e); setPage(0); }}
          >
            <Text style={[styles.filterChipText, entity === e && styles.filterChipTextActive]}>
              {e || 'All'}
            </Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      {isLoading ? <MSpinner /> : (
        <FlatList
          data={logs}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          renderItem={({ item }: { item: AuditLog }) => (
            <TouchableOpacity
              onPress={() => setExpanded(expanded === item.id ? null : item.id)}
              activeOpacity={0.8}
            >
              <MCard style={styles.logCard}>
                <View style={styles.logHeader}>
                  <View style={styles.logHeaderLeft}>
                    <Text style={styles.entityName}>{item.entityName}</Text>
                    <Text style={styles.performedBy}>{item.performedBy || 'System'}</Text>
                  </View>
                  <View style={styles.logHeaderRight}>
                    <MBadge label={item.action} color={ACTION_COLOR[item.action] as never ?? 'gray'} />
                    <Text style={styles.logTime}>{formatDateTime(item.createdAt)}</Text>
                  </View>
                </View>

                {item.ipAddress && (
                  <Text style={styles.ipAddress}>IP: {item.ipAddress}</Text>
                )}

                {expanded === item.id && (item.oldValue || item.newValue) && (
                  <View style={styles.diffSection}>
                    {item.oldValue && (
                      <View style={styles.diffBlock}>
                        <Text style={styles.diffLabel}>Old Value</Text>
                        <ScrollView horizontal>
                          <Text style={[styles.diffCode, styles.oldCode]}>
                            {JSON.stringify(JSON.parse(item.oldValue), null, 2)}
                          </Text>
                        </ScrollView>
                      </View>
                    )}
                    {item.newValue && (
                      <View style={styles.diffBlock}>
                        <Text style={styles.diffLabel}>New Value</Text>
                        <ScrollView horizontal>
                          <Text style={[styles.diffCode, styles.newCode]}>
                            {JSON.stringify(JSON.parse(item.newValue), null, 2)}
                          </Text>
                        </ScrollView>
                      </View>
                    )}
                  </View>
                )}

                {(item.oldValue || item.newValue) && (
                  <Text style={styles.expandHint}>
                    {expanded === item.id ? '▲ Hide details' : '▼ Show details'}
                  </Text>
                )}
              </MCard>
            </TouchableOpacity>
          )}
          ListEmptyComponent={<View style={styles.empty}><Text style={styles.emptyText}>No audit logs found</Text></View>}
          ListFooterComponent={
            totalPages > 1 ? (
              <View style={styles.pagination}>
                <TouchableOpacity
                  style={[styles.pageBtn, page === 0 && styles.pageBtnDisabled]}
                  onPress={() => setPage(Math.max(0, page - 1))}
                  disabled={page === 0}
                >
                  <Text style={styles.pageBtnText}>← Prev</Text>
                </TouchableOpacity>
                <Text style={styles.pageInfo}>Page {page + 1} of {totalPages}</Text>
                <TouchableOpacity
                  style={[styles.pageBtn, page + 1 >= totalPages && styles.pageBtnDisabled]}
                  onPress={() => setPage(page + 1)}
                  disabled={page + 1 >= totalPages}
                >
                  <Text style={styles.pageBtnText}>Next →</Text>
                </TouchableOpacity>
              </View>
            ) : null
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container:           { flex: 1, backgroundColor: COLORS.slate50 },
  filterScroll:        { backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200, maxHeight: 52 },
  filterRow:           { flexDirection: 'row', padding: 10, gap: 8 },
  filterChip:          { paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999, backgroundColor: COLORS.slate100, borderWidth: 1, borderColor: COLORS.slate200 },
  filterChipActive:    { backgroundColor: COLORS.primary, borderColor: COLORS.primary },
  filterChipText:      { fontSize: 12, fontWeight: '600', color: COLORS.slate600 },
  filterChipTextActive:{ color: COLORS.white },
  list:                { padding: 16, gap: 10, paddingBottom: 32 },
  logCard:             { gap: 8 },
  logHeader:           { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8 },
  logHeaderLeft:       { flex: 1 },
  entityName:          { fontSize: 14, fontWeight: '700', color: COLORS.slate800 },
  performedBy:         { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  logHeaderRight:      { alignItems: 'flex-end', gap: 4 },
  logTime:             { fontSize: 11, color: COLORS.slate400 },
  ipAddress:           { fontSize: 11, color: COLORS.slate400 },
  diffSection:         { gap: 8, borderTopWidth: 1, borderTopColor: COLORS.slate100, paddingTop: 8 },
  diffBlock:           { gap: 4 },
  diffLabel:           { fontSize: 11, fontWeight: '700', color: COLORS.slate500, textTransform: 'uppercase' },
  diffCode:            { fontSize: 11, fontFamily: 'monospace', padding: 8, borderRadius: 6 },
  oldCode:             { backgroundColor: '#fee2e2', color: '#991b1b' },
  newCode:             { backgroundColor: '#dcfce7', color: '#166534' },
  expandHint:          { fontSize: 11, color: COLORS.slate400, textAlign: 'center' },
  pagination:          { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: 16 },
  pageBtn:             { paddingHorizontal: 16, paddingVertical: 8, backgroundColor: COLORS.white, borderRadius: 8, borderWidth: 1, borderColor: COLORS.slate200 },
  pageBtnDisabled:     { opacity: 0.4 },
  pageBtnText:         { fontSize: 13, fontWeight: '600', color: COLORS.slate700 },
  pageInfo:            { fontSize: 13, color: COLORS.slate500 },
  empty:               { padding: 48, alignItems: 'center' },
  emptyText:           { fontSize: 14, color: COLORS.slate400 },
});
