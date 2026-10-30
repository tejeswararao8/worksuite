import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { Bell, CheckCheck } from 'lucide-react';
import { useNotifications } from '../../../../shared/hooks';
import { NOTIFICATION_TYPE_COLOR } from '../../../../shared/constants';
import { formatDateTime } from '../../../../shared/utils';
import type { Notification } from '../../../../shared/types';
import { Button, Badge, Spinner, PageHeader, EmptyState } from '../../components/ui';

const toastService = { success: toast.success, error: toast.error };

export default function NotificationsPage() {
  const { t } = useTranslation();
  const { notifications, isLoading, unreadCount, markReadMutation, markAllReadMutation } = useNotifications(toastService);
  const [showUnread, setShowUnread] = useState(false);

  const filtered = showUnread ? notifications.filter((n: Notification) => !n.isRead) : notifications;

  if (isLoading) return <div className="flex justify-center items-center h-64"><Spinner size="lg" /></div>;

  return (
    <div className="space-y-4">
      <PageHeader
        title={t('nav.notifications')}
        subtitle={unreadCount > 0 ? `${unreadCount} unread` : 'All caught up'}
        action={
          <div className="flex gap-2">
            <Button variant="secondary" size="sm" onClick={() => setShowUnread(!showUnread)}>
              {showUnread ? 'Show All' : 'Unread Only'}
            </Button>
            {unreadCount > 0 && (
              <Button variant="secondary" size="sm" onClick={() => markAllReadMutation.mutate()}>
                <CheckCheck size={14} /> Mark All Read
              </Button>
            )}
          </div>
        }
      />

      <div className="bg-white rounded-xl border border-slate-200 divide-y divide-slate-100">
        {filtered.length === 0 ? (
          <EmptyState icon={Bell} title="No notifications" description={showUnread ? 'No unread notifications' : 'You have no notifications yet'} />
        ) : (
          filtered.map((n: Notification) => (
            <div
              key={n.id}
              className={`flex items-start gap-4 px-6 py-4 hover:bg-slate-50 transition-colors ${!n.isRead ? 'bg-indigo-50/30' : ''}`}
            >
              <div className={`w-2 h-2 rounded-full mt-2 shrink-0 ${!n.isRead ? 'bg-indigo-500' : 'bg-transparent'}`} />
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <p className={`text-sm font-medium ${!n.isRead ? 'text-slate-800' : 'text-slate-600'}`}>{n.title}</p>
                  <Badge label={n.type.replace('_', ' ')} color={NOTIFICATION_TYPE_COLOR[n.type] as never ?? 'gray'} />
                </div>
                <p className="text-sm text-slate-500">{n.message}</p>
                <p className="text-xs text-slate-400 mt-1">{formatDateTime(n.createdAt)}</p>
              </div>
              {!n.isRead && (
                <button
                  onClick={() => markReadMutation.mutate(n.id)}
                  className="text-xs text-indigo-600 hover:text-indigo-800 shrink-0 mt-1"
                >
                  Mark read
                </button>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
