import { ScrollView, View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import { clearUser } from '../../../../shared/store/authSlice';
import { storageService } from '../../../../shared/utils';
import type { RootState, AppDispatch } from '../../../../shared/store';
import { MCard, MButton, COLORS } from '../../components';

export default function ProfileScreen() {
  const dispatch = useDispatch<AppDispatch>();
  const user     = useSelector((s: RootState) => s.auth.user);

  const handleLogout = () => {
    storageService.clear();
    dispatch(clearUser());
  };

  const fields = [
    { label: 'Email',      value: user?.email },
    { label: 'Role',       value: user?.role },
    { label: 'User ID',    value: user?.userId },
    { label: 'Company ID', value: user?.companyId },
  ];

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Avatar */}
      <View style={styles.avatarContainer}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{user?.email?.[0]?.toUpperCase() ?? 'U'}</Text>
        </View>
        <Text style={styles.email}>{user?.email}</Text>
        <Text style={styles.role}>{user?.role}</Text>
      </View>

      {/* Info */}
      <MCard>
        <Text style={styles.sectionTitle}>Account Information</Text>
        {fields.map((f) => (
          <View key={f.label} style={styles.field}>
            <Text style={styles.fieldLabel}>{f.label}</Text>
            <Text style={styles.fieldValue} numberOfLines={1}>{f.value || '—'}</Text>
          </View>
        ))}
      </MCard>

      {/* Logout */}
      <MButton onPress={handleLogout} variant="danger">
        Sign Out
      </MButton>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container:       { flex: 1, backgroundColor: COLORS.slate50 },
  content:         { padding: 16, gap: 16, paddingBottom: 32 },
  avatarContainer: { alignItems: 'center', gap: 8, paddingVertical: 24 },
  avatar:          { width: 80, height: 80, borderRadius: 40, backgroundColor: COLORS.primary + '20', alignItems: 'center', justifyContent: 'center' },
  avatarText:      { fontSize: 32, fontWeight: '700', color: COLORS.primary },
  email:           { fontSize: 16, fontWeight: '600', color: COLORS.slate800 },
  role:            { fontSize: 13, color: COLORS.primary, fontWeight: '600' },
  sectionTitle:    { fontSize: 12, fontWeight: '700', color: COLORS.slate500, textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 12 },
  field:           { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: COLORS.slate100 },
  fieldLabel:      { fontSize: 13, color: COLORS.slate500 },
  fieldValue:      { fontSize: 13, fontWeight: '500', color: COLORS.slate800, maxWidth: '60%' },
});
