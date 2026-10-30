import { useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity,
  StyleSheet, Alert,
} from 'react-native';
import { useMyOnboardingTasks } from '../../../../shared/hooks';
import { useOffboarding } from '../../../../shared/hooks';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { offboardingApi } from '../../../../shared/api/services';
import { TASK_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import type { OffboardingTask } from '../../../../shared/types';
import { MBadge, MCard, MSpinner, MButton, COLORS } from '../../components';

type Tab = 'my-tasks' | 'by-employee';

export default function OffboardingScreen() {
  const [tab, setTab]     = useState<Tab>('my-tasks');
  const [empId, setEmpId] = useState('');
  const qc = useQueryClient();

  const { tasks: empTasks, isLoading: empLoading, completeMutation } = useOffboarding(empId);

  // My assigned offboarding tasks
  const { data: myTasksData, isLoading: myLoading } = { data: null, isLoading: false };

  const initiateMutation = useMutation({
    mutationFn: offboardingApi.initiate,
    onSuccess:  () => { qc.invalidateQueries({ queryKey: ['offboarding'] }); Alert.alert('Success', 'Offboarding initiated'); },
    onError:    () => Alert.alert('Error', 'Failed to initiate offboarding'),
  });

  const handleInitiate = () => {
    Alert.prompt('Initiate Offboarding', 'Enter Employee ID:', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Next', style: 'default',
        onPress: (empId) => {
          Alert.prompt('Last Working Day', 'Enter date (YYYY-MM-DD):', [
            { text: 'Cancel', style: 'cancel' },
            {
              text: 'Initiate', style: 'default',
              onPress: (date) => initiateMutation.mutate({
                employeeId: empId,
                lastWorkingDay: date,
                type: 'RESIGNATION',
              }),
            },
          ], 'plain-text');
        },
      },
    ], 'plain-text');
  };

  const renderTask = ({ item }: { item: OffboardingTask }) => (
    <MCard style={styles.taskCard}>
      <View style={styles.taskHeader}>
        <View style={styles.taskHeaderLeft}>
          <Text style={styles.taskName}>{item.taskName}</Text>
          {item.employeeName && <Text style={styles.taskEmployee}>For: {item.employeeName}</Text>}
        </View>
        <MBadge label={item.status} color={TASK_STATUS_COLOR[item.status] as never ?? 'gray'} />
      </View>

      {item.description && <Text style={styles.taskDescription}>{item.description}</Text>}

      <View style={styles.taskMeta}>
        {item.dueDate && (
          <Text style={[styles.taskDue, item.overdue && styles.taskOverdue]}>
            {item.overdue ? '⚠ Overdue: ' : 'Due: '}{formatDate(item.dueDate)}
          </Text>
        )}
      </View>

      {item.status !== 'COMPLETED' && (
        <TouchableOpacity
          style={styles.completeBtn}
          onPress={() => completeMutation.mutate(item.id)}
          activeOpacity={0.8}
        >
          <Text style={styles.completeBtnText}>✓ Mark Complete</Text>
        </TouchableOpacity>
      )}
    </MCard>
  );

  return (
    <View style={styles.container}>
      {/* Tabs */}
      <View style={styles.tabs}>
        <TouchableOpacity style={[styles.tab, tab === 'my-tasks' && styles.tabActive]} onPress={() => setTab('my-tasks')}>
          <Text style={[styles.tabText, tab === 'my-tasks' && styles.tabTextActive]}>My Tasks</Text>
        </TouchableOpacity>
        <TouchableOpacity style={[styles.tab, tab === 'by-employee' && styles.tabActive]} onPress={() => setTab('by-employee')}>
          <Text style={[styles.tabText, tab === 'by-employee' && styles.tabTextActive]}>By Employee</Text>
        </TouchableOpacity>
      </View>

      {tab === 'my-tasks' && (
        <View style={styles.flex}>
          <View style={styles.initiateBar}>
            <MButton onPress={handleInitiate} variant="danger" style={styles.initiateBtn}>
              Initiate Offboarding
            </MButton>
          </View>
          <View style={styles.empty}>
            <Text style={styles.emptyText}>No offboarding tasks assigned to you</Text>
          </View>
        </View>
      )}

      {tab === 'by-employee' && (
        <View style={styles.flex}>
          <View style={styles.searchBar}>
            <View style={styles.searchInputRow}>
              <Text style={styles.searchLabel}>Employee ID</Text>
              <TouchableOpacity style={styles.searchBtn} onPress={() => {}}>
                <Text style={styles.searchBtnText}>Search</Text>
              </TouchableOpacity>
            </View>
          </View>

          {empId && (
            empLoading ? <MSpinner /> : (
              <FlatList
                data={empTasks}
                keyExtractor={(item) => item.id}
                renderItem={renderTask}
                contentContainerStyle={styles.list}
                ListEmptyComponent={
                  <View style={styles.empty}>
                    <Text style={styles.emptyText}>No offboarding tasks found</Text>
                  </View>
                }
              />
            )
          )}
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container:        { flex: 1, backgroundColor: COLORS.slate50 },
  flex:             { flex: 1 },
  tabs:             { flexDirection: 'row', backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200 },
  tab:              { flex: 1, paddingVertical: 14, alignItems: 'center', borderBottomWidth: 2, borderBottomColor: 'transparent' },
  tabActive:        { borderBottomColor: COLORS.primary },
  tabText:          { fontSize: 14, fontWeight: '600', color: COLORS.slate500 },
  tabTextActive:    { color: COLORS.primary },
  initiateBar:      { padding: 16, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200 },
  initiateBtn:      { width: '100%' },
  list:             { padding: 16, gap: 12, paddingBottom: 32 },
  taskCard:         { gap: 10 },
  taskHeader:       { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8 },
  taskHeaderLeft:   { flex: 1 },
  taskName:         { fontSize: 14, fontWeight: '600', color: COLORS.slate800 },
  taskEmployee:     { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  taskDescription:  { fontSize: 13, color: COLORS.slate500 },
  taskMeta:         { gap: 2 },
  taskDue:          { fontSize: 12, color: COLORS.slate500 },
  taskOverdue:      { color: COLORS.danger, fontWeight: '600' },
  completeBtn:      { backgroundColor: '#dcfce7', borderRadius: 8, paddingVertical: 10, alignItems: 'center' },
  completeBtnText:  { fontSize: 13, fontWeight: '700', color: '#15803d' },
  searchBar:        { backgroundColor: COLORS.white, padding: 16, borderBottomWidth: 1, borderBottomColor: COLORS.slate200 },
  searchInputRow:   { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  searchLabel:      { fontSize: 13, fontWeight: '600', color: COLORS.slate700 },
  searchBtn:        { backgroundColor: COLORS.primary, paddingHorizontal: 16, paddingVertical: 8, borderRadius: 8 },
  searchBtnText:    { fontSize: 13, fontWeight: '600', color: COLORS.white },
  empty:            { padding: 48, alignItems: 'center' },
  emptyText:        { fontSize: 14, color: COLORS.slate400 },
});
