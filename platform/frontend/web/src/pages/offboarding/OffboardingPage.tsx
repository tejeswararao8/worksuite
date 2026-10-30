import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Plus, CheckCircle } from 'lucide-react';
import { offboardingApi } from '../../../../shared/api/services';
import { QUERY_KEYS, TASK_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate, extractData } from '../../../../shared/utils';
import type { OffboardingTask } from '../../../../shared/types';
import { Button, Input, Select, Modal, Table, Badge, Spinner, PageHeader, Tabs } from '../../components/ui';
import type { Column } from '../../components/ui';

export default function OffboardingPage() {
  const { t }  = useTranslation();
  const qc     = useQueryClient();
  const [tab, setTab]             = useState('my-tasks');
  const [initiateOpen, setInitiateOpen] = useState(false);
  const [selectedEmpId, setSelectedEmpId] = useState('');

  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm();

  const { data: myTasksData, isLoading: myLoading } = useQuery({
    queryKey: QUERY_KEYS.MY_OFFBOARDING_TASKS,
    queryFn:  offboardingApi.getMyTasks,
  });

  const { data: empTasksData, isLoading: empLoading } = useQuery({
    queryKey: QUERY_KEYS.OFFBOARDING_BY_EMPLOYEE(selectedEmpId),
    queryFn:  () => offboardingApi.getByEmployee(selectedEmpId),
    enabled:  !!selectedEmpId,
  });

  const initiateMutation = useMutation({
    mutationFn: offboardingApi.initiate,
    onSuccess:  () => { qc.invalidateQueries({ queryKey: ['offboarding'] }); toast.success('Offboarding initiated'); setInitiateOpen(false); reset(); },
    onError:    () => toast.error('Failed to initiate offboarding'),
  });

  const completeMutation = useMutation({
    mutationFn: offboardingApi.completeTask,
    onSuccess:  () => { qc.invalidateQueries({ queryKey: ['offboarding'] }); toast.success('Task completed'); },
    onError:    () => toast.error('Failed to complete task'),
  });

  const myTasks  = extractData<OffboardingTask[]>(myTasksData) ?? [];
  const empTasks = extractData<OffboardingTask[]>(empTasksData) ?? [];

  const taskColumns: Column<OffboardingTask>[] = [
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
        <button onClick={() => completeMutation.mutate(r.id)} className="p-1.5 rounded hover:bg-green-50 text-green-500"><CheckCircle size={14} /></button>
      ) : null,
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader
        title={t('nav.offboarding')}
        action={<Button onClick={() => setInitiateOpen(true)}><Plus size={16} />Initiate Offboarding</Button>}
      />

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
            <input className="border border-slate-300 rounded-lg px-3 py-2 text-sm w-64 focus:ring-2 focus:ring-indigo-500" placeholder="Enter Employee ID..." value={selectedEmpId} onChange={(e) => setSelectedEmpId(e.target.value)} />
          </div>
          {selectedEmpId && (empLoading ? <Spinner /> : <Table columns={taskColumns} data={empTasks} loading={empLoading} />)}
        </div>
      )}

      <Modal open={initiateOpen} onClose={() => { setInitiateOpen(false); reset(); }} title="Initiate Offboarding">
        <form onSubmit={handleSubmit((d) => initiateMutation.mutate(d))} className="space-y-4">
          <Input label="Employee ID" {...register('employeeId', { required: true })} />
          <Input label="Last Working Day" type="date" {...register('lastWorkingDay', { required: true })} />
          <Select label="Offboarding Type" {...register('type')}>
            <option value="RESIGNATION">Resignation</option>
            <option value="TERMINATION">Termination</option>
            <option value="RETIREMENT">Retirement</option>
            <option value="CONTRACT_END">Contract End</option>
          </Select>
          <Input label="Reason (optional)" {...register('resignationReason')} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setInitiateOpen(false)}>Cancel</Button>
            <Button type="submit" loading={isSubmitting}>Initiate</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
