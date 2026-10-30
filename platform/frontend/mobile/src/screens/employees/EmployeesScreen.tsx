import { useState } from 'react';
import { View, Text, FlatList, TextInput, StyleSheet, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useEmployees } from '../../../../shared/hooks';
import { EMPLOYMENT_STATUS_COLOR } from '../../../../shared/constants';
import type { Employee } from '../../../../shared/types';
import { MBadge, MSpinner, COLORS } from '../../components';

export default function EmployeesScreen() {
  const navigation = useNavigation<never>();
  const { employees, isLoading, search, setSearch } = useEmployees();

  const renderItem = ({ item }: { item: Employee }) => (
    <TouchableOpacity
      style={styles.item}
      onPress={() => navigation.navigate('EmployeeDetail', { id: item.id })}
      activeOpacity={0.7}
    >
      <View style={styles.avatar}>
        <Text style={styles.avatarText}>{item.firstName[0]}{item.lastName[0]}</Text>
      </View>
      <View style={styles.itemContent}>
        <Text style={styles.itemName}>{item.firstName} {item.lastName}</Text>
        <Text style={styles.itemCode}>{item.employeeCode} · {item.email}</Text>
      </View>
      <MBadge label={item.employmentStatus} color={EMPLOYMENT_STATUS_COLOR[item.employmentStatus] as never ?? 'gray'} />
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      {/* Search */}
      <View style={styles.searchContainer}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search employees..."
          placeholderTextColor={COLORS.slate400}
          value={search}
          onChangeText={setSearch}
        />
      </View>

      {isLoading ? (
        <MSpinner />
      ) : (
        <FlatList
          data={employees}
          keyExtractor={(item) => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.list}
          ListEmptyComponent={
            <View style={styles.empty}>
              <Text style={styles.emptyText}>No employees found</Text>
            </View>
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container:       { flex: 1, backgroundColor: COLORS.slate50 },
  searchContainer: { padding: 16, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate200 },
  searchInput:     { backgroundColor: COLORS.slate100, borderRadius: 10, paddingHorizontal: 14, paddingVertical: 10, fontSize: 14, color: COLORS.slate800 },
  list:            { paddingBottom: 32 },
  item:            { flexDirection: 'row', alignItems: 'center', padding: 16, backgroundColor: COLORS.white, borderBottomWidth: 1, borderBottomColor: COLORS.slate100, gap: 12 },
  avatar:          { width: 44, height: 44, borderRadius: 22, backgroundColor: COLORS.primary + '20', alignItems: 'center', justifyContent: 'center' },
  avatarText:      { fontSize: 15, fontWeight: '700', color: COLORS.primary },
  itemContent:     { flex: 1 },
  itemName:        { fontSize: 14, fontWeight: '600', color: COLORS.slate800 },
  itemCode:        { fontSize: 12, color: COLORS.slate500, marginTop: 2 },
  empty:           { padding: 32, alignItems: 'center' },
  emptyText:       { fontSize: 14, color: COLORS.slate400 },
});
