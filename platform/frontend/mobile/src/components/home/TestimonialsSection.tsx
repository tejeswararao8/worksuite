import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { COLORS } from '../../../../shared/constants';

const TestimonialsSection: React.FC = () => {
  const testimonials = [
    { name: "Sarah J.", title: "HR Director", text: "WorkSuite has completely transformed how we manage our people." },
    { name: "David C.", title: "Ops Manager", text: "The payroll tracking has saved our team hundreds of hours." }
  ];

  return (
    <View style={styles.container}>
      <Text style={styles.title}>
        A better everyday.{"\n"}
        <Text style={{color: COLORS.primary}}>For everyone.</Text>
      </Text>

      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.scroll}>
        {testimonials.map((t, i) => (
          <View key={i} style={styles.card}>
            <View style={styles.stars}>
               <Text style={styles.starText}>★★★★★</Text>
            </View>
            <Text style={styles.text}>"{t.text}"</Text>
            <View style={styles.footer}>
               <View style={styles.avatar}>
                  <Text style={styles.avatarText}>{t.name[0]}</Text>
               </View>
               <View>
                  <Text style={styles.name}>{t.name}</Text>
                  <Text style={styles.role}>{t.title}</Text>
               </View>
            </View>
          </View>
        ))}
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { paddingVertical: 40, backgroundColor: COLORS.white },
  title: { fontSize: 26, fontWeight: '800', color: COLORS.textPrimary, textAlign: 'center', marginBottom: 30 },
  scroll: { paddingHorizontal: 20 },
  card: { width: 280, backgroundColor: COLORS.lightBackground, borderRadius: 25, padding: 25, marginRight: 15, borderWhidth: 1, borderColor: COLORS.border },
  stars: { marginBottom: 15 },
  starText: { color: '#F59E0B', fontSize: 16 },
  text: { fontSize: 15, color: COLORS.textSecondary, fontStyle: 'italic', marginBottom: 20, lineHeight: 22 },
  footer: { flexDirection: 'row', alignItems: 'center' },
  avatar: { width: 40, height: 40, borderRadius: 20, backgroundColor: COLORS.primary, alignItems: 'center', justifyContent: 'center', marginRight: 12 },
  avatarText: { color: COLORS.white, fontWeight: 'bold' },
  name: { fontSize: 14, fontWeight: 'bold', color: COLORS.textPrimary },
  role: { fontSize: 12, color: COLORS.textSecondary }
});

export default TestimonialsSection;
