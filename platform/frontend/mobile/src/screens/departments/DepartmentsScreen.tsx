import { useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, TextInput } from 'react-native';
import { useDepartments } from '../../../../shared/hooks';
import type { Department, OrgNode } from '../../../../shared/types';
import { MCard, MSpinner, COLORS } from '../../components';

type View_ = 'list' | 'org';

function OrgNodeItem({ node, depth = 0 }: { node: OrgNode; depth?: number }) {
  const [expanded, setExpanded] = useState(true);
  return (
    <View style={{ marginLeft: depth * 16 }}>
      <TouchableOpacity
        style={[styles.orgNode, { borderLeftColor: depth === 0 ? COLORS.primary : COLORS.slate300 }]}
        onPress={() => setExpanded(!expanded)}
        activeOpacity={0.8}
      >
        <View style={styles.orgNodeContent}>
          <Text style={styles.orgNodeName}>{node.name}</Text>
          <Text style={styles.orgNodeCode}>{node.code}</Text>
        </View>
        <View style={styles.orgNodeRight}>
          <Text style={styles.orgNodeCount}>{node.employeeCount} emp</Text>
          {node.children?.length > 0 && (
            <Text style={styles.orgNodeChevron}>{expanded ? '▼' : '▶'}</Text>
          )}
        </View>
      </TouchableOpacity>
      {expanded && node.children?.map((child) => (
        <OrgNodeItem key={child.id} node={child} depth={depth + 1} />
      ))}
    </View>
  );
}

export default function DepartmentsScreen() {
  const [view, setView]   = useState<View_>('list');
  const [search, setSearch] = useState('');
  const { departments, isLoading, orgChart, setSearch: setDeptSearch } = useDepartments();

  const filtered = departments.filter((d) =>
    d.name.toLowerCase().includes(search.toLowerCase()) ||
    d.code.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <View style={styles.container}>
      {/* View Toggle */}
      <View style={styles.header}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search departments..."
          placeholderTextColor={COLORS.slate400}
          value={search}
          onChangeText={(v) => { setSearch(v); setDeptSearch(v); }}
        />
        <View style={styles.viewToggle}>
          <TouchableOpacity
            style={[styles.toggleBtn, view === 'list' && styles.toggleBtnActive]}
            onPress={() => setView('list')}
          >
            <Text style={[styles.toggleBtnText, view === 'list' && styles.toggleBtnTextActive]}>List</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.toggleBtn, view === 'org' && styles.toggleBtnActive]}
            onPress={() => setView('org')}
          >
            <Text style={[styles.toggleBtnText, view === 'org' && styles.toggleBtnTextActive]}>Org</Text>
          </TouchableOpacity>
        </View>
      </View>

      {isLoading ? <MSpinner /> : view === 'list' ? (
        <FlatList
          data={filtered}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          renderItem={({ item }: { item: Department }) => (
            <MCard style={styles.deptCard}>
              <View style={styles.deptHeader}>
                <View>
                  <Text style={styles.deptName}>{item.name}</Text>
                  <Text style={styles.deptCode}>{item.code}</Text>
                </View>
                <View style={styles.deptStats}>
                  <Text style={styles.deptStat}>{item.employeeCount} emp</Text>
                  <Text style={styles.deptStat}>{item.teamCount} teams</Text>
                </View>
              </View>
              {item.headEmployeeName && (
                <Text style={styles.deptHead}>Head: {item.headEmployeeName}</Text>
              )}
              {item.parentDepartmentName && (
                <Text style={styles.deptParent}>Parent: {item.parentDepartmentName}</Text>
              )}
            </MCard>
          )}
          ListEmptyComponent={<View style={styles.empty}><Text style={styles.emptyText}>No departments found</Text></View>}
        />
      ) : (
        <FlatList
          data={orgChart}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.orgList}
          renderItem={({ item }) => <OrgNodeItem node={item} />}
          ListEmptyComponent={<View style={styles.empty}><Text style={styles.emptyText}>No org chart data</Text></View>}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container:          { flex: 1, backgroundColor: COLORS.slate50 },
  header:             { flexDirection: 'row', gap: 8, padding: 12, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200 },
  searchInput:        { flex: 1, backgroundColor: COLORS.slate100, borderRadius: 8, paddingHorizontal: 12, paddingVertical: 8, fontSize: 14, color: COLORS.slate800 },
  viewToggle:         { flexDirection: 'row', borderWidth: 1, borderColor: COLORS.slate200, borderRadius: 8, overflow: 'hidden' },
  toggleBtn:          { paddingHorizontal: 12, paddingVertical: 8 },
  toggleBtnActive:    { backgroundColor: COLORS.primary },
  toggleBtnText:      { fontSize: 13, fontWeight: '600', color: COLORS.slate500 },
  toggleBtnTextActive:{ color: COLORS.white },
  list:               { padding: 16, gap: 10, paddingBottom: 32 },
  orgList:            { padding: 16, paddingBottom: 32 },
  deptCard:           { gap: 6 },
  deptHeader:         { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  deptName:           { fontSize: 15, fontWeight: '700', color: COLORS.slate800 },
  deptCode:           { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  deptStats:          { alignItems: 'flex-end', gap: 2 },
  deptStat:           { fontSize: 12, color: COLORS.slate500 },
  deptHead:           { fontSize: 12, color: COLORS.slate500 },
  deptParent:         { fontSize: 12, color: COLORS.slate400 },
  orgNode:            { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', backgroundColor: COLORS.white, borderRadius: 8, padding: 12, marginBottom: 4, borderLeftWidth: 3 },
  orgNodeContent:     { flex: 1 },
  orgNodeName:        { fontSize: 14, fontWeight: '600', color: COLORS.slate800 },
  orgNodeCode:        { fontSize: 11, color: COLORS.slate400, marginTop: 1 },
  orgNodeRight:       { flexDirection: 'row', gap: 8, alignItems: 'center' },
  orgNodeCount:       { fontSize: 12, color: COLORS.slate500 },
  orgNodeChevron:     { fontSize: 10, color: COLORS.slate400 },
  empty:              { padding: 48, alignItems: 'center' },
  emptyText:          { fontSize: 14, color: COLORS.slate400 },
});
