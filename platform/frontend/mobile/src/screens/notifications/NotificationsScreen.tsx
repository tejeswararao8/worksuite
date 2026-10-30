import { View, Text, FlatList, TouchableOpacity, StyleSheet } from 'react-native';
import { useNotifications } from '../../../../shared/hooks';
import { NOTIFICATION_TYPE_COLOR } from '../../../../shared/constants';
import { formatDateTime } from '../../../../shared/utils';
import type { Notification } from '../../../../shared/types';
import { MBadge, MSpinner, COLORS } from '../../components';

export default function NotificationsScreen() {
  const { notifications, isLoading, unreadCount, markReadMutation, markAllReadMutation } = useNotifications();

  const renderItem = ({ item }: { item: Notification }) => (
    <TouchableOpacity
      style={[styles.item, !item.isRead && styles.unread]}
      onPress={() => !item.isRead && markReadMutation.mutate(item.id)}
      activeOpacity={0.7}
    >
      <View style={[styles.dot, { backgroundColor: item.isRead ? 'transparent' : COLORS.primary }]} />
      <View style={styles.content}>
        <View style={styles.titleRow}>
          <Text style={styles.title}>{item.title}</Text>
          <MBadge label={item.type.replace('_', ' ')} color={NOTIFICATION_TYPE_COLOR[item.type] as never ?? 'gray'} />
        </View>
        <Text style={styles.message}>{item.message}</Text>
        <Text style={styles.time}>{formatDateTime(item.createdAt)}</Text>
      </View>
    </TouchableOpacity>
  );

  if (isLoading) return <MSpinner />;

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Notifications {unreadCount > 0 ? `(${unreadCount})` : ''}</Text>
        {unreadCount > 0 && (
          <TouchableOpacity onPress={() => markAllReadMutation.mutate()}>
            <Text style={styles.markAll}>Mark all read</Text>
          </TouchableOpacity>
        )}
      </View>

      <FlatList
        data={notifications}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={styles.list}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Text style={styles.emptyText}>🔔 No notifications</Text>
          </View>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container:  { flex: 1, backgroundColor: COLORS.slate50 },
  header:     { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: 16, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200 },
  headerTitle:{ fontSize: 16, fontWeight: '700', color: COLORS.slate800 },
  markAll:    { fontSize: 13, color: COLORS.primary, fontWeight: '600' },
  list:       { paddingBottom: 32 },
  item:       { flexDirection: 'row', padding: 16, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate100, gap: 12 },
  unread:     { backgroundColor: '#eef2ff' },
  dot:        { width: 8, height: 8, borderRadius: 4, marginTop: 6, shrink: 0 } as never,
  content:    { flex: 1, gap: 4 },
  titleRow:   { flexDirection: 'row', alignItems: 'center', gap: 8, flexWrap: 'wrap' },
  title:      { fontSize: 14, fontWeight: '600', color: COLORS.slate800, flex: 1 },
  message:    { fontSize: 13, color: COLORS.slate500 },
  time:       { fontSize: 11, color: COLORS.slate400 },
  empty:      { padding: 48, alignItems: 'center' },
  emptyText:  { fontSize: 15, color: COLORS.slate400 },
});
