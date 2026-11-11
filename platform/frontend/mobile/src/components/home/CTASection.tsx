import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { COLORS } from '../../../../shared/constants';
import { useNavigation } from '@react-navigation/native';

const CTASection: React.FC = () => {
  const navigation = useNavigation<any>();

  return (
    <View style={styles.container}>
      <View style={styles.card}>
        <Text style={styles.title}>Ready to simplify{"\n"}your HR operations?</Text>
        <Text style={styles.subtitle}>Bring your people and processes together with WorkSuite.</Text>
        
        <TouchableOpacity 
          style={styles.button}
          onPress={() => navigation.navigate('Login')}
        >
          <Text style={styles.buttonText}>Get Started</Text>
        </TouchableOpacity>
        
        <TouchableOpacity style={styles.outlineButton}>
          <Text style={styles.outlineButtonText}>Talk to Our Team</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { padding: 20, paddingBottom: 60, backgroundColor: COLORS.white },
  card: { backgroundColor: COLORS.indigoLight, borderRadius: 35, padding: 35, alignItems: 'center' },
  title: { fontSize: 26, fontWeight: '900', color: COLORS.textPrimary, textAlign: 'center', marginBottom: 15, lineHeight: 32 },
  subtitle: { fontSize: 15, color: COLORS.textSecondary, textAlign: 'center', marginBottom: 30, lineHeight: 22 },
  button: { backgroundColor: COLORS.primary, width: '100%', paddingVertical: 18, borderRadius: 15, alignItems: 'center', marginBottom: 12 },
  buttonText: { color: COLORS.white, fontWeight: 'bold', fontSize: 16 },
  outlineButton: { width: '100%', paddingVertical: 18, borderRadius: 15, alignItems: 'center', borderWidth: 1, borderColor: 'rgba(0,0,0,0.1)' },
  outlineButtonText: { color: COLORS.textPrimary, fontWeight: 'bold', fontSize: 16 }
});

export default CTASection;
