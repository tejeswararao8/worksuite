import { useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity,
  StyleSheet, SectionList,
} from 'react-native';
import { useMyOnboardingTasks, useOnboarding } from '../../../../shared/hooks';
import { TASK_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import type { OnboardingTask } from '../../../../shared/types';
import { MBadge, MCard, MSpinner, COLORS } from '../../components';

type Tab = 'my-tasks' | 'by-employee';

export default function OnboardingScreen() {
  const [tab, setTab]         = useState<Tab>('my-tasks');
  const [empId, setEmpId]     = useState('');
  const [inputEmpId, setInputEmpId] = useState('');

  const { tasks: myTasks, isLoading: myLoading } = useMyOnboardingTasks();
  const { tasks: empTasks, progress, isLoading: empLoading, completeMutation } = useOnboarding(empId);

  const handleComplete = (taskId: string) => {
    completeMutation.mutate(taskId);
  };

  const renderTask = ({ item }: { item: OnboardingTask }) => (
    <MCard style={styles.taskCard}>
      <View style={styles.taskHeader}>
        <View style={styles.taskHeaderLeft}>
          <Text style={styles.taskName}>{item.taskName}</Text>
          {item.employeeName && (
            <Text style={styles.taskEmployee}>For: {item.employeeName}</Text>
          )}
        </View>
        <MBadge
          label={item.status}
          color={TASK_STATUS_COLOR[item.status] as never ?? 'gray'}
        />
      </View>

      {item.description && (
        <Text style={styles.taskDescription}>{item.description}</Text>
      )}

      <View style={styles.taskMeta}>
        {item.dueDate && (
          <Text style={[styles.taskDue, item.overdue && styles.taskOverdue]}>
            {item.overdue ? '⚠ Overdue: ' : 'Due: '}{formatDate(item.dueDate)}
          </Text>
        )}
        {item.assignedToName && (
          <Text style={styles.taskAssigned}>Assigned to: {item.assignedToName}</Text>
        )}
      </View>

      {item.status !== 'COMPLETED' && (
        <TouchableOpacity
          style={styles.completeBtn}
          onPress={() => handleComplete(item.id)}
          activeOpacity={0.8}
        >
          <Text style={styles.completeBtnText}>✓ Mark Complete</Text>
        </TouchableOpacity>
      )}
    </MCard>
  );

  return (
    <View style={styles.container}>
      {/* Tab Switcher */}
      <View style={styles.tabs}>
        <TouchableOpacity
          style={[styles.tab, tab === 'my-tasks' && styles.tabActive]}
          onPress={() => setTab('my-tasks')}
        >
          <Text style={[styles.tabText, tab === 'my-tasks' && styles.tabTextActive]}>My Tasks</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, tab === 'by-employee' && styles.tabActive]}
          onPress={() => setTab('by-employee')}
        >
          <Text style={[styles.tabText, tab === 'by-employee' && styles.tabTextActive]}>By Employee</Text>
        </TouchableOpacity>
      </View>

      {tab === 'my-tasks' && (
        myLoading ? <MSpinner /> : (
          <FlatList
            data={myTasks}
            keyExtractor={(item) => item.id}
            renderItem={renderTask}
            contentContainerStyle={styles.list}
            ListEmptyComponent={
              <View style={styles.empty}>
                <Text style={styles.emptyText}>🎉 No tasks assigned to you</Text>
              </View>
            }
          />
        )
      )}

      {tab === 'by-employee' && (
        <View style={styles.flex}>
          {/* Employee ID Input */}
          <View style={styles.searchBar}>
            <View style={styles.searchInputRow}>
              <Text style={styles.searchLabel}>Employee ID</Text>
              <TouchableOpacity
                style={styles.searchBtn}
                onPress={() => setEmpId(inputEmpId)}
              >
                <Text style={styles.searchBtnText}>Search</Text>
              </TouchableOpacity>
            </View>
            <View style={styles.inputBox}>
              <Text
                style={styles.inputPlaceholder}
                onPress={() => {}}
              >
                {inputEmpId || 'Enter employee ID...'}
              </Text>
            </View>
          </View>

          {/* Progress */}
          {empId && progress && (
            <MCard style={styles.progressCard}>
              <Text style={styles.progressTitle}>{progress.employeeName} — Onboarding</Text>
              <View style={styles.progressTrack}>
                <View style={[styles.progressFill, { width: `${progress.completionPercentage}%` as never }]} />
              </View>
              <View style={styles.progressStats}>
                <Text style={styles.progressStat}>✓ {progress.completedTasks} done</Text>
                <Text style={styles.progressStat}>⏳ {progress.pendingTasks} pending</Text>
                {progress.overdueTasks > 0 && (
                  <Text style={[styles.progressStat, { color: COLORS.danger }]}>⚠ {progress.overdueTasks} overdue</Text>
                )}
              </View>
            </MCard>
          )}

          {empId && (
            empLoading ? <MSpinner /> : (
              <FlatList
                data={empTasks}
                keyExtractor={(item) => item.id}
                renderItem={renderTask}
                contentContainerStyle={styles.list}
                ListEmptyComponent={
                  <View style={styles.empty}>
                    <Text style={styles.emptyText}>No tasks found for this employee</Text>
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
  taskAssigned:     { fontSize: 12, color: COLORS.slate400 },
  completeBtn:      { backgroundColor: '#dcfce7', borderRadius: 8, paddingVertical: 10, alignItems: 'center' },
  completeBtnText:  { fontSize: 13, fontWeight: '700', color: '#15803d' },
  searchBar:        { backgroundColor: COLORS.white, padding: 16, borderBottomWidth: 1, borderBottomColor: COLORS.slate200, gap: 8 },
  searchInputRow:   { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  searchLabel:      { fontSize: 13, fontWeight: '600', color: COLORS.slate700 },
  searchBtn:        { backgroundColor: COLORS.primary, paddingHorizontal: 16, paddingVertical: 8, borderRadius: 8 },
  searchBtnText:    { fontSize: 13, fontWeight: '600', color: COLORS.white },
  inputBox:         { borderWidth: 1, borderColor: COLORS.slate200, borderRadius: 8, paddingHorizontal: 12, paddingVertical: 10 },
  inputPlaceholder: { fontSize: 14, color: COLORS.slate400 },
  progressCard:     { margin: 16, marginBottom: 0, gap: 8 },
  progressTitle:    { fontSize: 14, fontWeight: '700', color: COLORS.slate800 },
  progressTrack:    { height: 8, backgroundColor: COLORS.slate200, borderRadius: 4, overflow: 'hidden' },
  progressFill:     { height: 8, backgroundColor: COLORS.primary, borderRadius: 4 },
  progressStats:    { flexDirection: 'row', gap: 12 },
  progressStat:     { fontSize: 12, color: COLORS.slate500 },
  empty:            { padding: 48, alignItems: 'center' },
  emptyText:        { fontSize: 14, color: COLORS.slate400 },
});
