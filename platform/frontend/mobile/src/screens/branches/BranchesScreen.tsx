import { useState } from 'react';
import { View, Text, FlatList, StyleSheet, TextInput } from 'react-native';
import { useBranches } from '../../../../shared/hooks';
import type { Branch } from '../../../../shared/types';
import { MBadge, MCard, MSpinner, COLORS } from '../../components';

export default function BranchesScreen() {
  const [search, setSearch] = useState('');
  const { branches, isLoading, setSearch: setBranchSearch } = useBranches();

  const filtered = branches.filter((b) =>
    b.name.toLowerCase().includes(search.toLowerCase()) ||
    b.code.toLowerCase().includes(search.toLowerCase()) ||
    (b.city || '').toLowerCase().includes(search.toLowerCase())
  );

  return (
    <View style={styles.container}>
      <View style={styles.searchBar}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search branches..."
          placeholderTextColor={COLORS.slate400}
          value={search}
          onChangeText={(v) => { setSearch(v); setBranchSearch(v); }}
        />
      </View>

      {isLoading ? <MSpinner /> : (
        <FlatList
          data={filtered}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          renderItem={({ item }: { item: Branch }) => (
            <MCard style={styles.branchCard}>
              <View style={styles.branchHeader}>
                <View style={styles.branchHeaderLeft}>
                  <Text style={styles.branchName}>{item.name}</Text>
                  <Text style={styles.branchCode}>{item.code}</Text>
                </View>
                <View style={styles.branchHeaderRight}>
                  {item.headOffice && <MBadge label="HQ" color="indigo" />}
                  <Text style={styles.empCount}>{item.employeeCount} emp</Text>
                </View>
              </View>

              <View style={styles.branchDetails}>
                {(item.city || item.country) && (
                  <View style={styles.detailRow}>
                    <Text style={styles.detailIcon}>📍</Text>
                    <Text style={styles.detailText}>
                      {[item.city, item.country].filter(Boolean).join(', ')}
                    </Text>
                  </View>
                )}
                {item.phone && (
                  <View style={styles.detailRow}>
                    <Text style={styles.detailIcon}>📞</Text>
                    <Text style={styles.detailText}>{item.phone}</Text>
                  </View>
                )}
                {item.email && (
                  <View style={styles.detailRow}>
                    <Text style={styles.detailIcon}>📧</Text>
                    <Text style={styles.detailText}>{item.email}</Text>
                  </View>
                )}
              </View>
            </MCard>
          )}
          ListEmptyComponent={
            <View style={styles.empty}>
              <Text style={styles.emptyText}>No branches found</Text>
            </View>
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container:       { flex: 1, backgroundColor: COLORS.slate50 },
  searchBar:       { padding: 12, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200 },
  searchInput:     { backgroundColor: COLORS.slate100, borderRadius: 8, paddingHorizontal: 12, paddingVertical: 8, fontSize: 14, color: COLORS.slate800 },
  list:            { padding: 16, gap: 10, paddingBottom: 32 },
  branchCard:      { gap: 10 },
  branchHeader:    { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  branchHeaderLeft:{ flex: 1 },
  branchName:      { fontSize: 15, fontWeight: '700', color: COLORS.slate800 },
  branchCode:      { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  branchHeaderRight:{ alignItems: 'flex-end', gap: 4 },
  empCount:        { fontSize: 12, color: COLORS.slate500 },
  branchDetails:   { gap: 4, borderTopWidth: 1, borderTopColor: COLORS.slate100, paddingTop: 8 },
  detailRow:       { flexDirection: 'row', gap: 6, alignItems: 'center' },
  detailIcon:      { fontSize: 12 },
  detailText:      { fontSize: 13, color: COLORS.slate600 },
  empty:           { padding: 48, alignItems: 'center' },
  emptyText:       { fontSize: 14, color: COLORS.slate400 },
});
