import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { notificationApi } from '../api/services';
import { QUERY_KEYS } from '../constants/queryKeys';
import { toastSuccess, toastError } from '../utils/errorHandler';

export function useNotifications() {
  const { t } = useTranslation();
  const qc = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.NOTIFICATIONS,
    queryFn: () => notificationApi.getAll({ page: 0, size: 50 }),
  });

  const { data: countData } = useQuery({
    queryKey: QUERY_KEYS.UNREAD_COUNT,
    queryFn: notificationApi.getUnreadCount,
    refetchInterval: 30000,
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: QUERY_KEYS.NOTIFICATIONS });
    qc.invalidateQueries({ queryKey: QUERY_KEYS.UNREAD_COUNT });
  };

  const markReadMutation = useMutation({
    mutationFn: notificationApi.markRead,
    onSuccess: invalidate,
    onError: toastError,
  });

  const markAllReadMutation = useMutation({
    mutationFn: notificationApi.markAllRead,
    onSuccess: () => { invalidate(); toastSuccess(t('common.markAllRead')); },
    onError: toastError,
  });

  const notifications = data?.data?.data?.content || data?.data?.data || [];
  const unreadCount   = countData?.data?.data?.count || 0;

  return {
    notifications, isLoading,
    unreadCount,
    markReadMutation, markAllReadMutation,
  };
}
