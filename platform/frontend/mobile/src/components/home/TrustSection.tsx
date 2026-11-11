import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { COLORS } from '../../../../shared/constants';

const TrustSection: React.FC = () => {
  const items = [
    { title: 'One connected HR platform', desc: 'Sync all your HR operations seamlessly.' },
    { title: 'Less admin. More focus.', desc: 'Automate repetitive tasks and focus on people.' },
    { title: 'Everyone seen, every action connected', desc: 'Foster engagement across your organization.' },
  ];

  return (
    <View style={styles.container}>
      {items.map((item, index) => (
        <View key={index} style={styles.item}>
          <View style={styles.iconPlaceholder}>
             <Text style={styles.iconText}>✓</Text>
          </View>
          <Text style={styles.itemTitle}>{item.title}</Text>
          <Text style={styles.itemDesc}>{item.desc}</Text>
        </View>
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { padding: 20, backgroundColor: COLORS.white },
  item: { alignItems: 'center', marginBottom: 30 },
  iconPlaceholder: { width: 50, height: 50, borderRadius: 15, backgroundColor: COLORS.indigoLight, alignItems: 'center', justifyContent: 'center', marginBottom: 15 },
  iconText: { color: COLORS.primary, fontSize: 20, fontWeight: 'bold' },
  itemTitle: { fontSize: 18, fontWeight: 'bold', color: COLORS.textPrimary, marginBottom: 8, textAlign: 'center' },
  itemDesc: { fontSize: 14, color: COLORS.textSecondary, textAlign: 'center', lineHeight: 20 },
});

export default TrustSection;
