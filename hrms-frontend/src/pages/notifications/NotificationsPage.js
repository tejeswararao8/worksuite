import { useTranslation } from 'react-i18next';
import { useNotifications } from '../../hooks/useNotifications';
import { Button, Badge } from '../../components/ui';
import { formatDateTime } from '../../utils/dateUtils';
import { CheckCheck } from 'lucide-react';
import { NOTIFICATION_TYPE_COLOR } from '../../constants/enums';

export default function NotificationsPage() {
  const { t } = useTranslation();
  const { notifications, isLoading, markReadMutation, markAllReadMutation } = useNotifications();

  return (
    <div className="space-y-4 max-w-3xl">
      <div className="flex justify-end">
        <Button variant="secondary" onClick={() => markAllReadMutation.mutate()} disabled={markAllReadMutation.isPending}>
          <CheckCheck size={16} /> {t('common.markAllRead')}
        </Button>
      </div>

      {isLoading ? (
        <p className="text-slate-400 text-sm">{t('common.loading')}</p>
      ) : notifications.length === 0 ? (
        <div className="bg-white rounded-xl border border-slate-200 p-10 text-center text-slate-400">
          {t('common.noNotifications')}
        </div>
      ) : (
        <div className="space-y-2">
          {notifications.map((n) => (
            <div
              key={n.id}
              onClick={() => !n.read && markReadMutation.mutate(n.id)}
              className={`bg-white rounded-xl border p-4 cursor-pointer transition-colors ${
                n.read ? 'border-slate-200 opacity-70' : 'border-indigo-200 bg-indigo-50/30'
              }`}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <Badge label={n.type} color={NOTIFICATION_TYPE_COLOR[n.type] || 'gray'} />
                    {!n.read && <span className="w-2 h-2 bg-indigo-500 rounded-full" />}
                  </div>
                  <p className="text-sm font-medium text-slate-800">{n.title}</p>
                  <p className="text-sm text-slate-500 mt-0.5">{n.message}</p>
                </div>
                <p className="text-xs text-slate-400 whitespace-nowrap">{formatDateTime(n.createdAt)}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
