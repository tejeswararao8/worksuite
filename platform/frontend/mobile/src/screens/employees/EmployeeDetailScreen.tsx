import { ScrollView, View, Text, StyleSheet } from 'react-native';
import { useRoute } from '@react-navigation/native';
import { useEmployee } from '../../../../shared/hooks';
import { EMPLOYMENT_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate } from '../../../../shared/utils';
import { MBadge, MCard, MSpinner, COLORS } from '../../components';

export default function EmployeeDetailScreen() {
  const route = useRoute<{ key: string; name: string; params: { id: string } }>();
  const { id } = route.params;
  const { employee, isLoading } = useEmployee(id);

  if (isLoading) return <MSpinner />;
  if (!employee) return (
    <View style={styles.center}>
      <Text style={styles.notFound}>Employee not found</Text>
    </View>
  );

  const fields = [
    { label: 'Employee Code',    value: employee.employeeCode },
    { label: 'Email',            value: employee.email },
    { label: 'Mobile',           value: employee.mobile },
    { label: 'Gender',           value: employee.gender },
    { label: 'Date of Birth',    value: formatDate(employee.dateOfBirth) },
    { label: 'Nationality',      value: employee.nationality },
    { label: 'Marital Status',   value: employee.maritalStatus },
    { label: 'Blood Group',      value: employee.bloodGroup },
    { label: 'Joining Date',     value: formatDate(employee.joiningDate) },
    { label: 'Employment Type',  value: employee.employmentType },
    { label: 'Work Location',    value: employee.workLocation },
  ];

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Header Card */}
      <MCard style={styles.headerCard}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{employee.firstName[0]}{employee.lastName[0]}</Text>
        </View>
        <Text style={styles.name}>{employee.firstName} {employee.lastName}</Text>
        <View style={styles.badges}>
          <MBadge label={employee.employeeCode} color="indigo" />
          <MBadge label={employee.employmentStatus} color={EMPLOYMENT_STATUS_COLOR[employee.employmentStatus] as never ?? 'gray'} />
        </View>
      </MCard>

      {/* Details */}
      <MCard>
        <Text style={styles.sectionTitle}>Personal & Employment Info</Text>
        {fields.map((f) => f.value ? (
          <View key={f.label} style={styles.field}>
            <Text style={styles.fieldLabel}>{f.label}</Text>
            <Text style={styles.fieldValue}>{f.value}</Text>
          </View>
        ) : null)}
      </MCard>

      {/* Address */}
      {employee.currentAddress && (
        <MCard>
          <Text style={styles.sectionTitle}>Address</Text>
          <Text style={styles.fieldValue}>{employee.currentAddress}</Text>
        </MCard>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container:   { flex: 1, backgroundColor: COLORS.slate50 },
  content:     { padding: 16, gap: 12, paddingBottom: 32 },
  center:      { flex: 1, alignItems: 'center', justifyContent: 'center' },
  notFound:    { fontSize: 16, color: COLORS.slate400 },
  headerCard:  { alignItems: 'center', gap: 8 },
  avatar:      { width: 72, height: 72, borderRadius: 36, backgroundColor: COLORS.primary + '20', alignItems: 'center', justifyContent: 'center' },
  avatarText:  { fontSize: 26, fontWeight: '700', color: COLORS.primary },
  name:        { fontSize: 20, fontWeight: '700', color: COLORS.slate800 },
  badges:      { flexDirection: 'row', gap: 8 },
  sectionTitle:{ fontSize: 13, fontWeight: '700', color: COLORS.slate500, textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 12 },
  field:       { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 8, borderBottomWidth: 1, borderBottomColor: COLORS.slate100 },
  fieldLabel:  { fontSize: 13, color: COLORS.slate500 },
  fieldValue:  { fontSize: 13, fontWeight: '500', color: COLORS.slate800, maxWidth: '60%', textAlign: 'right' },
});
