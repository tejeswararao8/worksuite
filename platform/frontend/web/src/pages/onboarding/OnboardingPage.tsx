import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { CheckCircle } from 'lucide-react';
import { onboardingApi } from '../../../../shared/api/services';
import { QUERY_KEYS, TASK_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate, extractData } from '../../../../shared/utils';
import type { OnboardingTask, OnboardingProgress } from '../../../../shared/types';
import { Badge, Spinner, Tabs, ProgressBar, PageHeader, Table, Button } from '../../components/ui';
import type { Column } from '../../components/ui';

export default function OnboardingPage() {
  const { t }  = useTranslation();
  const qc     = useQueryClient();
  const [tab, setTab] = useState('my-tasks');
  const [selectedEmpId, setSelectedEmpId] = useState('');

  const { data: myTasksData, isLoading: myLoading } = useQuery({
    queryKey: QUERY_KEYS.MY_ONBOARDING_TASKS,
    queryFn:  onboardingApi.getMyTasks,
  });

  const { data: empTasksData, isLoading: empLoading } = useQuery({
    queryKey: QUERY_KEYS.ONBOARDING_BY_EMPLOYEE(selectedEmpId),
    queryFn:  () => onboardingApi.getByEmployee(selectedEmpId),
    enabled:  !!selectedEmpId,
  });

  const { data: progressData } = useQuery({
    queryKey: QUERY_KEYS.ONBOARDING_PROGRESS(selectedEmpId),
    queryFn:  () => onboardingApi.getProgress(selectedEmpId),
    enabled:  !!selectedEmpId,
  });

  const completeMutation = useMutation({
    mutationFn: onboardingApi.completeTask,
    onSuccess:  () => { qc.invalidateQueries({ queryKey: ['onboarding'] }); toast.success('Task completed'); },
    onError:    () => toast.error('Failed to complete task'),
  });

  const myTasks  = extractData<OnboardingTask[]>(myTasksData) ?? [];
  const empTasks = extractData<OnboardingTask[]>(empTasksData) ?? [];
  const progress = extractData<OnboardingProgress>(progressData);

  const taskColumns: Column<OnboardingTask>[] = [
    { key: 'taskName',      label: 'Task' },
    { key: 'employeeName',  label: 'Employee', render: (r) => r.employeeName || '—' },
    { key: 'assignedToName',label: 'Assigned To', render: (r) => r.assignedToName || '—' },
    { key: 'dueDate',       label: 'Due Date', render: (r) => formatDate(r.dueDate) },
    {
      key: 'status', label: 'Status',
      render: (r) => (
        <div className="flex items-center gap-2">
          {r.overdue && <Badge label="Overdue" color="red" />}
          <Badge label={r.status} color={TASK_STATUS_COLOR[r.status] as never ?? 'gray'} />
        </div>
      ),
    },
    {
      key: 'actions', label: 'Action',
      render: (r) => r.status !== 'COMPLETED' ? (
        <button onClick={() => completeMutation.mutate(r.id)} className="p-1.5 rounded hover:bg-green-50 text-green-500" title="Mark Complete">
          <CheckCircle size={14} />
        </button>
      ) : null,
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader title={t('nav.onboarding')} subtitle="Track new employee onboarding tasks" />

      <Tabs
        tabs={[{ key: 'my-tasks', label: 'My Tasks' }, { key: 'by-employee', label: 'By Employee (Admin)' }]}
        active={tab}
        onChange={setTab}
      />

      {tab === 'my-tasks' && (
        myLoading ? <Spinner /> : <Table columns={taskColumns} data={myTasks} loading={myLoading} />
      )}

      {tab === 'by-employee' && (
        <div className="space-y-4">
          <div className="flex gap-3 bg-white p-4 rounded-xl border border-slate-200">
            <input
              className="border border-slate-300 rounded-lg px-3 py-2 text-sm w-64 focus:ring-2 focus:ring-indigo-500"
              placeholder="Enter Employee ID..."
              value={selectedEmpId}
              onChange={(e) => setSelectedEmpId(e.target.value)}
            />
          </div>

          {selectedEmpId && progress && (
            <div className="bg-white rounded-xl border border-slate-200 p-5">
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-sm font-semibold text-slate-700">{progress.employeeName} — Onboarding Progress</h3>
                <span className="text-sm text-slate-500">{progress.completedTasks}/{progress.totalTasks} tasks</span>
              </div>
              <ProgressBar value={progress.completedTasks} max={progress.totalTasks || 1} />
              <div className="flex gap-4 mt-3 text-xs text-slate-500">
                <span className="text-green-600">✓ {progress.completedTasks} completed</span>
                <span className="text-yellow-600">⏳ {progress.pendingTasks} pending</span>
                {progress.overdueTasks > 0 && <span className="text-red-600">⚠ {progress.overdueTasks} overdue</span>}
              </div>
            </div>
          )}

          {selectedEmpId && (
            empLoading ? <Spinner /> : <Table columns={taskColumns} data={empTasks} loading={empLoading} />
          )}
        </div>
      )}
    </div>
  );
}
