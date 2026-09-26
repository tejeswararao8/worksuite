import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { onboardingApi } from '../../api/services';
import api from '../../api/axios';
import { Table, Badge, Button } from '../../components/ui';
import { CheckCircle } from 'lucide-react';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { QUERY_KEYS } from '../../constants/queryKeys';
import { APPROVAL_STATUS_COLOR } from '../../constants/enums';
import { formatDate } from '../../utils/dateUtils';

export default function OnboardingPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.ONBOARDING(0),
    queryFn: () => api.get('/onboarding', { params: { page: 0, size: 50 } }),
  });

  const items = data?.data?.data?.content || data?.data?.data || [];

  const completeTaskMutation = useMutation({
    mutationFn: onboardingApi.completeTask,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['onboarding'] }); toastSuccess('Task completed'); },
    onError: toastError,
  });

  const columns = [
    { key: 'employeeName', label: t('employee.entity') },
    { key: 'taskName',     label: t('onboarding.task') },
    { key: 'assignedTo',   label: t('onboarding.assignedTo') },
    { key: 'dueDate',      label: t('onboarding.dueDate'), render: (r) => formatDate(r.dueDate) },
    { key: 'status',       label: t('common.status'), render: (r) => <Badge label={r.status} color={APPROVAL_STATUS_COLOR[r.status] || 'gray'} /> },
    {
      key: 'actions', label: t('common.actions'), render: (r) => r.status !== 'COMPLETED' ? (
        <Button size="sm" variant="secondary" onClick={() => completeTaskMutation.mutate(r.id)}>
          <CheckCircle size={14} /> {t('offboarding.completeTask')}
        </Button>
      ) : null,
    },
  ];

  return (
    <div className="space-y-4">
      <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-4 text-sm text-indigo-700">
        {t('onboarding.infoMessage')}
      </div>
      <Table columns={columns} data={items} loading={isLoading} />
    </div>
  );
}
