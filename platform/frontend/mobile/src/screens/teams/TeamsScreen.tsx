import { useState } from 'react';
import { View, Text, FlatList, StyleSheet, TextInput, TouchableOpacity } from 'react-native';
import { useTeams, useTeamMembers } from '../../../../shared/hooks';
import type { Team } from '../../../../shared/types';
import { MCard, MSpinner, COLORS } from '../../components';

export default function TeamsScreen() {
  const [search, setSearch]         = useState('');
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
  const { teams, isLoading }        = useTeams();
  const { members, isLoading: membersLoading } = useTeamMembers(selectedTeam?.id ?? '');

  const filtered = teams.filter((t) =>
    t.name.toLowerCase().includes(search.toLowerCase())
  );

  if (selectedTeam) {
    return (
      <View style={styles.container}>
        <TouchableOpacity style={styles.backBtn} onPress={() => setSelectedTeam(null)}>
          <Text style={styles.backBtnText}>← Back to Teams</Text>
        </TouchableOpacity>

        <View style={styles.teamDetailHeader}>
          <Text style={styles.teamDetailName}>{selectedTeam.name}</Text>
          <Text style={styles.teamDetailDept}>{selectedTeam.departmentName}</Text>
          {selectedTeam.leadEmployeeName && (
            <Text style={styles.teamDetailLead}>Lead: {selectedTeam.leadEmployeeName}</Text>
          )}
        </View>

        <Text style={styles.membersTitle}>Members ({selectedTeam.memberCount})</Text>

        {membersLoading ? <MSpinner /> : (
          <FlatList
            data={members}
            keyExtractor={(item) => item.employeeId}
            contentContainerStyle={styles.list}
            renderItem={({ item }) => (
              <View style={styles.memberItem}>
                <View style={styles.memberAvatar}>
                  <Text style={styles.memberAvatarText}>{item.firstName[0]}{item.lastName[0]}</Text>
                </View>
                <View style={styles.memberInfo}>
                  <Text style={styles.memberName}>{item.firstName} {item.lastName}</Text>
                  <Text style={styles.memberCode}>{item.employeeCode} · {item.designation || '—'}</Text>
                </View>
              </View>
            )}
            ListEmptyComponent={<View style={styles.empty}><Text style={styles.emptyText}>No members</Text></View>}
          />
        )}
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.searchBar}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search teams..."
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
          renderItem={({ item }: { item: Team }) => (
            <TouchableOpacity onPress={() => setSelectedTeam(item)} activeOpacity={0.8}>
              <MCard style={styles.teamCard}>
                <View style={styles.teamHeader}>
                  <View style={styles.teamHeaderLeft}>
                    <Text style={styles.teamName}>{item.name}</Text>
                    <Text style={styles.teamDept}>{item.departmentName || '—'}</Text>
                  </View>
                  <View style={styles.teamHeaderRight}>
                    <Text style={styles.memberCount}>{item.memberCount} members</Text>
                    <Text style={styles.chevron}>›</Text>
                  </View>
                </View>
                {item.leadEmployeeName && (
                  <Text style={styles.teamLead}>Lead: {item.leadEmployeeName}</Text>
                )}
              </MCard>
            </TouchableOpacity>
          )}
          ListEmptyComponent={<View style={styles.empty}><Text style={styles.emptyText}>No teams found</Text></View>}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container:         { flex: 1, backgroundColor: COLORS.slate50 },
  searchBar:         { padding: 12, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200 },
  searchInput:       { backgroundColor: COLORS.slate100, borderRadius: 8, paddingHorizontal: 12, paddingVertical: 8, fontSize: 14, color: COLORS.slate800 },
  list:              { padding: 16, gap: 10, paddingBottom: 32 },
  teamCard:          { gap: 6 },
  teamHeader:        { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  teamHeaderLeft:    { flex: 1 },
  teamName:          { fontSize: 15, fontWeight: '700', color: COLORS.slate800 },
  teamDept:          { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  teamHeaderRight:   { alignItems: 'flex-end', flexDirection: 'row', gap: 8 },
  memberCount:       { fontSize: 12, color: COLORS.slate500 },
  chevron:           { fontSize: 20, color: COLORS.slate300 },
  teamLead:          { fontSize: 12, color: COLORS.slate400 },
  backBtn:           { padding: 16, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200 },
  backBtnText:       { fontSize: 14, color: COLORS.primary, fontWeight: '600' },
  teamDetailHeader:  { backgroundColor: COLORS.white, padding: 20, borderBottomWidth: 1, borderBottomColor: COLORS.slate200, gap: 4 },
  teamDetailName:    { fontSize: 20, fontWeight: '800', color: COLORS.slate800 },
  teamDetailDept:    { fontSize: 14, color: COLORS.slate500 },
  teamDetailLead:    { fontSize: 13, color: COLORS.primary, fontWeight: '600' },
  membersTitle:      { fontSize: 13, fontWeight: '700', color: COLORS.slate500, textTransform: 'uppercase', letterSpacing: 0.5, paddingHorizontal: 16, paddingVertical: 10, backgroundColor: COLORS.slate50 },
  memberItem:        { flexDirection: 'row', alignItems: 'center', padding: 16, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate100, gap: 12 },
  memberAvatar:      { width: 40, height: 40, borderRadius: 20, backgroundColor: COLORS.primary + '20', alignItems: 'center', justifyContent: 'center' },
  memberAvatarText:  { fontSize: 14, fontWeight: '700', color: COLORS.primary },
  memberInfo:        { flex: 1 },
  memberName:        { fontSize: 14, fontWeight: '600', color: COLORS.slate800 },
  memberCode:        { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  empty:             { padding: 48, alignItems: 'center' },
  emptyText:         { fontSize: 14, color: COLORS.slate400 },
});
