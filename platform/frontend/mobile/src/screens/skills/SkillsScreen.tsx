import { useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity,
  StyleSheet, ScrollView, Alert,
} from 'react-native';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { employeeApi } from '../../../../shared/api/services';
import { QUERY_KEYS } from '../../../../shared/constants';
import { formatDate, extractData } from '../../../../shared/utils';
import type { EmployeeSkill, EmployeeCertification } from '../../../../shared/types';
import { MBadge, MCard, MSpinner, COLORS } from '../../components';

type Tab = 'skills' | 'certifications';

const PROFICIENCY_COLOR: Record<string, string> = {
  BEGINNER: 'gray', INTERMEDIATE: 'blue', ADVANCED: 'indigo', EXPERT: 'purple',
};

export default function SkillsScreen() {
  const [tab, setTab]     = useState<Tab>('skills');
  const [empId, setEmpId] = useState('');
  const qc = useQueryClient();

  const { data: skillsData, isLoading: skillsLoading } = useQuery({
    queryKey: QUERY_KEYS.SKILLS_BY_EMPLOYEE(empId),
    queryFn:  () => employeeApi.getSkills(empId),
    enabled:  !!empId,
  });

  const { data: certsData, isLoading: certsLoading } = useQuery({
    queryKey: QUERY_KEYS.CERTS_BY_EMPLOYEE(empId),
    queryFn:  () => employeeApi.getCertifications(empId),
    enabled:  !!empId,
  });

  const skills = extractData<EmployeeSkill[]>(skillsData) ?? [];
  const certs  = extractData<EmployeeCertification[]>(certsData) ?? [];

  const deleteSkillMutation = useMutation({
    mutationFn: (skillId: string) => employeeApi.deleteSkill(empId, skillId),
    onSuccess:  () => qc.invalidateQueries({ queryKey: QUERY_KEYS.SKILLS_BY_EMPLOYEE(empId) }),
    onError:    () => Alert.alert('Error', 'Failed to delete skill'),
  });

  const deleteCertMutation = useMutation({
    mutationFn: (certId: string) => employeeApi.deleteCertification(empId, certId),
    onSuccess:  () => qc.invalidateQueries({ queryKey: QUERY_KEYS.CERTS_BY_EMPLOYEE(empId) }),
    onError:    () => Alert.alert('Error', 'Failed to delete certification'),
  });

  const handleDeleteSkill = (id: string, name: string) => {
    Alert.alert('Delete Skill', `Remove "${name}"?`, [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => deleteSkillMutation.mutate(id) },
    ]);
  };

  const handleDeleteCert = (id: string, name: string) => {
    Alert.alert('Delete Certification', `Remove "${name}"?`, [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => deleteCertMutation.mutate(id) },
    ]);
  };

  return (
    <View style={styles.container}>
      {/* Employee Search */}
      <View style={styles.searchBar}>
        <Text style={styles.searchLabel}>Enter Employee ID to view skills</Text>
        <TouchableOpacity
          style={styles.searchBtn}
          onPress={() => Alert.prompt('Employee ID', 'Enter ID:', [
            { text: 'Cancel', style: 'cancel' },
            { text: 'Search', onPress: (id) => setEmpId(id || '') },
          ], 'plain-text')}
        >
          <Text style={styles.searchBtnText}>{empId ? `ID: ${empId.slice(0, 8)}...` : 'Select Employee'}</Text>
        </TouchableOpacity>
      </View>

      {empId && (
        <>
          {/* Tabs */}
          <View style={styles.tabs}>
            <TouchableOpacity style={[styles.tab, tab === 'skills' && styles.tabActive]} onPress={() => setTab('skills')}>
              <Text style={[styles.tabText, tab === 'skills' && styles.tabTextActive]}>Skills ({skills.length})</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.tab, tab === 'certifications' && styles.tabActive]} onPress={() => setTab('certifications')}>
              <Text style={[styles.tabText, tab === 'certifications' && styles.tabTextActive]}>Certifications ({certs.length})</Text>
            </TouchableOpacity>
          </View>

          {tab === 'skills' && (
            skillsLoading ? <MSpinner /> : (
              <FlatList
                data={skills}
                keyExtractor={(item) => item.id}
                contentContainerStyle={styles.list}
                renderItem={({ item }) => (
                  <MCard style={styles.skillCard}>
                    <View style={styles.skillHeader}>
                      <Text style={styles.skillName}>{item.skillName}</Text>
                      <TouchableOpacity onPress={() => handleDeleteSkill(item.id, item.skillName)}>
                        <Text style={styles.deleteBtn}>✕</Text>
                      </TouchableOpacity>
                    </View>
                    <View style={styles.skillMeta}>
                      {item.skillType && <Text style={styles.skillType}>{item.skillType}</Text>}
                      {item.proficiency && (
                        <MBadge label={item.proficiency} color={PROFICIENCY_COLOR[item.proficiency] as never ?? 'gray'} />
                      )}
                      {item.yearsOfExperience && (
                        <Text style={styles.yearsExp}>{item.yearsOfExperience} yrs exp</Text>
                      )}
                    </View>
                  </MCard>
                )}
                ListEmptyComponent={<View style={styles.empty}><Text style={styles.emptyText}>No skills added</Text></View>}
              />
            )
          )}

          {tab === 'certifications' && (
            certsLoading ? <MSpinner /> : (
              <FlatList
                data={certs}
                keyExtractor={(item) => item.id}
                contentContainerStyle={styles.list}
                renderItem={({ item }) => (
                  <MCard style={styles.certCard}>
                    <View style={styles.certHeader}>
                      <View style={styles.certHeaderLeft}>
                        <Text style={styles.certName}>{item.name}</Text>
                        {item.issuingOrg && <Text style={styles.certOrg}>{item.issuingOrg}</Text>}
                      </View>
                      <TouchableOpacity onPress={() => handleDeleteCert(item.id, item.name)}>
                        <Text style={styles.deleteBtn}>✕</Text>
                      </TouchableOpacity>
                    </View>
                    <View style={styles.certMeta}>
                      {item.issueDate && <Text style={styles.certDate}>Issued: {formatDate(item.issueDate)}</Text>}
                      {item.expiryDate && (
                        <Text style={[styles.certDate, item.expired && styles.expiredText, item.expiringSoon && !item.expired && styles.expiringSoonText]}>
                          Expires: {formatDate(item.expiryDate)}
                          {item.expired ? ' (Expired)' : item.expiringSoon ? ' (Soon!)' : ''}
                        </Text>
                      )}
                    </View>
                  </MCard>
                )}
                ListEmptyComponent={<View style={styles.empty}><Text style={styles.emptyText}>No certifications added</Text></View>}
              />
            )
          )}
        </>
      )}

      {!empId && (
        <View style={styles.empty}>
          <Text style={styles.emptyText}>Select an employee to view their skills</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container:       { flex: 1, backgroundColor: COLORS.slate50 },
  searchBar:       { backgroundColor: COLORS.white, padding: 16, borderBottomWidth: 1, borderBottomColor: COLORS.slate200, gap: 8 },
  searchLabel:     { fontSize: 13, color: COLORS.slate500 },
  searchBtn:       { backgroundColor: COLORS.primary, paddingVertical: 10, borderRadius: 8, alignItems: 'center' },
  searchBtnText:   { fontSize: 14, fontWeight: '600', color: COLORS.white },
  tabs:            { flexDirection: 'row', backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200 },
  tab:             { flex: 1, paddingVertical: 14, alignItems: 'center', borderBottomWidth: 2, borderBottomColor: 'transparent' },
  tabActive:       { borderBottomColor: COLORS.primary },
  tabText:         { fontSize: 14, fontWeight: '600', color: COLORS.slate500 },
  tabTextActive:   { color: COLORS.primary },
  list:            { padding: 16, gap: 10, paddingBottom: 32 },
  skillCard:       { gap: 8 },
  skillHeader:     { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  skillName:       { fontSize: 15, fontWeight: '700', color: COLORS.slate800, flex: 1 },
  skillMeta:       { flexDirection: 'row', gap: 8, alignItems: 'center', flexWrap: 'wrap' },
  skillType:       { fontSize: 12, color: COLORS.slate500 },
  yearsExp:        { fontSize: 12, color: COLORS.slate400 },
  certCard:        { gap: 8 },
  certHeader:      { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  certHeaderLeft:  { flex: 1 },
  certName:        { fontSize: 15, fontWeight: '700', color: COLORS.slate800 },
  certOrg:         { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  certMeta:        { gap: 4 },
  certDate:        { fontSize: 12, color: COLORS.slate500 },
  expiredText:     { color: COLORS.danger, fontWeight: '600' },
  expiringSoonText:{ color: '#f59e0b', fontWeight: '600' },
  deleteBtn:       { fontSize: 16, color: COLORS.slate300, padding: 4 },
  empty:           { padding: 48, alignItems: 'center' },
  emptyText:       { fontSize: 14, color: COLORS.slate400, textAlign: 'center' },
});
