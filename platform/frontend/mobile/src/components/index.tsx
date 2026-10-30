import {
  View, Text, TouchableOpacity, TextInput, ActivityIndicator,
  StyleSheet, ViewStyle, TextStyle,
} from 'react-native';
import { ReactNode } from 'react';

// ─── Colors ───────────────────────────────────────────────────────────────────
export const COLORS = {
  primary:   '#6366f1',
  success:   '#22c55e',
  warning:   '#f59e0b',
  danger:    '#ef4444',
  info:      '#3b82f6',
  slate50:   '#f8fafc',
  slate100:  '#f1f5f9',
  slate200:  '#e2e8f0',
  slate400:  '#94a3b8',
  slate500:  '#64748b',
  slate700:  '#334155',
  slate800:  '#1e293b',
  slate900:  '#0f172a',
  white:     '#ffffff',
};

// ─── Button ───────────────────────────────────────────────────────────────────
type BtnVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'ghost';

interface MButtonProps {
  onPress: () => void;
  children: ReactNode;
  variant?: BtnVariant;
  loading?: boolean;
  disabled?: boolean;
  style?: ViewStyle;
}

export function MButton({ onPress, children, variant = 'primary', loading, disabled, style }: MButtonProps) {
  const bg: Record<BtnVariant, string> = {
    primary:   COLORS.primary,
    secondary: COLORS.slate100,
    danger:    COLORS.danger,
    success:   COLORS.success,
    ghost:     'transparent',
  };
  const textColor: Record<BtnVariant, string> = {
    primary:   COLORS.white,
    secondary: COLORS.slate700,
    danger:    COLORS.white,
    success:   COLORS.white,
    ghost:     COLORS.slate700,
  };

  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={disabled || loading}
      style={[styles.btn, { backgroundColor: bg[variant], opacity: (disabled || loading) ? 0.5 : 1 }, style]}
      activeOpacity={0.8}
    >
      {loading
        ? <ActivityIndicator size="small" color={textColor[variant]} />
        : <Text style={[styles.btnText, { color: textColor[variant] }]}>{children}</Text>
      }
    </TouchableOpacity>
  );
}

// ─── Input ────────────────────────────────────────────────────────────────────
interface MInputProps {
  label?: string;
  error?: string;
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
  secureTextEntry?: boolean;
  keyboardType?: 'default' | 'email-address' | 'numeric' | 'phone-pad';
  style?: ViewStyle;
}

export function MInput({ label, error, value, onChangeText, placeholder, secureTextEntry, keyboardType, style }: MInputProps) {
  return (
    <View style={[styles.inputContainer, style]}>
      {label && <Text style={styles.label}>{label}</Text>}
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        secureTextEntry={secureTextEntry}
        keyboardType={keyboardType}
        style={[styles.input, error ? styles.inputError : null]}
        placeholderTextColor={COLORS.slate400}
      />
      {error && <Text style={styles.errorText}>{error}</Text>}
    </View>
  );
}

// ─── Card ─────────────────────────────────────────────────────────────────────
interface MCardProps { children: ReactNode; style?: ViewStyle; onPress?: () => void; }

export function MCard({ children, style, onPress }: MCardProps) {
  if (onPress) {
    return (
      <TouchableOpacity onPress={onPress} style={[styles.card, style]} activeOpacity={0.9}>
        {children}
      </TouchableOpacity>
    );
  }
  return <View style={[styles.card, style]}>{children}</View>;
}

// ─── Badge ────────────────────────────────────────────────────────────────────
type BadgeColor = 'green' | 'red' | 'yellow' | 'blue' | 'indigo' | 'gray' | 'orange';

interface MBadgeProps { label: string; color?: BadgeColor; }

export function MBadge({ label, color = 'gray' }: MBadgeProps) {
  const colors: Record<BadgeColor, { bg: string; text: string }> = {
    green:  { bg: '#dcfce7', text: '#15803d' },
    red:    { bg: '#fee2e2', text: '#dc2626' },
    yellow: { bg: '#fef9c3', text: '#a16207' },
    blue:   { bg: '#dbeafe', text: '#1d4ed8' },
    indigo: { bg: '#e0e7ff', text: '#4338ca' },
    gray:   { bg: '#f1f5f9', text: '#475569' },
    orange: { bg: '#ffedd5', text: '#c2410c' },
  };
  const c = colors[color];
  return (
    <View style={[styles.badge, { backgroundColor: c.bg }]}>
      <Text style={[styles.badgeText, { color: c.text }]}>{label}</Text>
    </View>
  );
}

// ─── StatCard ─────────────────────────────────────────────────────────────────
interface MStatCardProps {
  title: string;
  value: number | string;
  color?: 'indigo' | 'green' | 'yellow' | 'red' | 'blue' | 'orange';
  onPress?: () => void;
}

export function MStatCard({ title, value, color = 'indigo', onPress }: MStatCardProps) {
  const colors = {
    indigo: { bg: '#e0e7ff', text: '#4338ca' },
    green:  { bg: '#dcfce7', text: '#15803d' },
    yellow: { bg: '#fef9c3', text: '#a16207' },
    red:    { bg: '#fee2e2', text: '#dc2626' },
    blue:   { bg: '#dbeafe', text: '#1d4ed8' },
    orange: { bg: '#ffedd5', text: '#c2410c' },
  };
  const c = colors[color];
  return (
    <TouchableOpacity onPress={onPress} style={styles.statCard} activeOpacity={onPress ? 0.8 : 1}>
      <View style={[styles.statIcon, { backgroundColor: c.bg }]}>
        <Text style={[styles.statIconText, { color: c.text }]}>{String(value ?? '—')}</Text>
      </View>
      <Text style={styles.statTitle}>{title}</Text>
    </TouchableOpacity>
  );
}

// ─── ListItem ─────────────────────────────────────────────────────────────────
interface MListItemProps {
  title: string;
  subtitle?: string;
  right?: ReactNode;
  onPress?: () => void;
  style?: ViewStyle;
}

export function MListItem({ title, subtitle, right, onPress, style }: MListItemProps) {
  return (
    <TouchableOpacity onPress={onPress} style={[styles.listItem, style]} activeOpacity={onPress ? 0.7 : 1}>
      <View style={styles.listItemContent}>
        <Text style={styles.listItemTitle}>{title}</Text>
        {subtitle && <Text style={styles.listItemSubtitle}>{subtitle}</Text>}
      </View>
      {right && <View>{right}</View>}
    </TouchableOpacity>
  );
}

// ─── Spinner ──────────────────────────────────────────────────────────────────
export function MSpinner({ size = 'large' }: { size?: 'small' | 'large' }) {
  return (
    <View style={styles.spinnerContainer}>
      <ActivityIndicator size={size} color={COLORS.primary} />
    </View>
  );
}

// ─── SectionHeader ────────────────────────────────────────────────────────────
export function MSectionHeader({ title }: { title: string }) {
  return <Text style={styles.sectionHeader}>{title}</Text>;
}

// ─── Styles ───────────────────────────────────────────────────────────────────
const styles = StyleSheet.create({
  btn:              { paddingHorizontal: 16, paddingVertical: 12, borderRadius: 10, alignItems: 'center', justifyContent: 'center', flexDirection: 'row', gap: 8 },
  btnText:          { fontSize: 14, fontWeight: '600' },
  inputContainer:   { gap: 4 },
  label:            { fontSize: 13, fontWeight: '500', color: COLORS.slate700 },
  input:            { borderWidth: 1, borderColor: COLORS.slate200, borderRadius: 10, paddingHorizontal: 12, paddingVertical: 10, fontSize: 14, color: COLORS.slate800, backgroundColor: COLORS.white },
  inputError:       { borderColor: COLORS.danger },
  errorText:        { fontSize: 11, color: COLORS.danger },
  card:             { backgroundColor: COLORS.white, borderRadius: 12, padding: 16, borderWidth: 1, borderColor: COLORS.slate200 },
  badge:            { paddingHorizontal: 8, paddingVertical: 2, borderRadius: 999, alignSelf: 'flex-start' },
  badgeText:        { fontSize: 11, fontWeight: '600' },
  statCard:         { backgroundColor: COLORS.white, borderRadius: 12, padding: 16, borderWidth: 1, borderColor: COLORS.slate200, alignItems: 'center', gap: 8 },
  statIcon:         { width: 48, height: 48, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  statIconText:     { fontSize: 18, fontWeight: '700' },
  statTitle:        { fontSize: 12, color: COLORS.slate500, textAlign: 'center' },
  listItem:         { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, paddingHorizontal: 16, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate100 },
  listItemContent:  { flex: 1 },
  listItemTitle:    { fontSize: 14, fontWeight: '500', color: COLORS.slate800 },
  listItemSubtitle: { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  spinnerContainer: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 32 },
  sectionHeader:    { fontSize: 12, fontWeight: '700', color: COLORS.slate500, textTransform: 'uppercase', letterSpacing: 0.5, paddingHorizontal: 16, paddingVertical: 8, backgroundColor: COLORS.slate50 },
});
