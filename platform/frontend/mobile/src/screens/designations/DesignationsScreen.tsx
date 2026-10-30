import { useState } from 'react';
import { View, Text, FlatList, StyleSheet, TextInput } from 'react-native';
import { useDesignations } from '../../../../shared/hooks';
import type { Designation } from '../../../../shared/types';
import { MCard, MSpinner, COLORS } from '../../components';

export default function DesignationsScreen() {
  const [search, setSearch] = useState('');
  const { designations, isLoading } = useDesignations();

  const filtered = designations.filter((d) =>
    d.title.toLowerCase().includes(search.toLowerCase()) ||
    (d.grade || '').toLowerCase().includes(search.toLowerCase())
  );

  const getLevelColor = (level?: number): string => {
    if (!level) return COLORS.slate400;
    if (level >= 8) return '#7c3aed';
    if (level >= 5) return COLORS.primary;
    if (level >= 3) return '#0891b2';
    return '#059669';
  };

  return (
    <View style={styles.container}>
      <View style={styles.searchBar}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search designations..."
          placeholderTextColor={COLORS.slate400}
          value={search}
          onChangeText={setSearch}
        />
      </View>

      {isLoading ? <MSpinner /> : (
        <FlatList
          data={filtered}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          renderItem={({ item }: { item: Designation }) => (
            <MCard style={styles.desigCard}>
              <View style={styles.desigHeader}>
                <View style={styles.desigHeaderLeft}>
                  <Text style={styles.desigTitle}>{item.title}</Text>
                  {item.description && (
                    <Text style={styles.desigDesc} numberOfLines={1}>{item.description}</Text>
                  )}
                </View>
                <View style={styles.desigHeaderRight}>
                  {item.level && (
                    <View style={[styles.levelBadge, { backgroundColor: getLevelColor(item.level) + '20' }]}>
                      <Text style={[styles.levelText, { color: getLevelColor(item.level) }]}>
                        L{item.level}
                      </Text>
                    </View>
                  )}
                  {item.grade && (
                    <View style={styles.gradeBadge}>
                      <Text style={styles.gradeText}>Grade {item.grade}</Text>
                    </View>
                  )}
                </View>
              </View>
              <Text style={styles.empCount}>{item.employeeCount} employees</Text>
            </MCard>
          )}
          ListEmptyComponent={
            <View style={styles.empty}>
              <Text style={styles.emptyText}>No designations found</Text>
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
  desigCard:       { gap: 6 },
  desigHeader:     { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  desigHeaderLeft: { flex: 1 },
  desigTitle:      { fontSize: 15, fontWeight: '700', color: COLORS.slate800 },
  desigDesc:       { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  desigHeaderRight:{ alignItems: 'flex-end', gap: 4 },
  levelBadge:      { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 8 },
  levelText:       { fontSize: 13, fontWeight: '800' },
  gradeBadge:      { backgroundColor: COLORS.slate100, paddingHorizontal: 8, paddingVertical: 3, borderRadius: 6 },
  gradeText:       { fontSize: 11, fontWeight: '600', color: COLORS.slate600 },
  empCount:        { fontSize: 12, color: COLORS.slate400 },
  empty:           { padding: 48, alignItems: 'center' },
  emptyText:       { fontSize: 14, color: COLORS.slate400 },
});
