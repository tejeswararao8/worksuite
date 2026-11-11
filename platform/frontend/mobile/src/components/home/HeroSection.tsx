import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image, Dimensions } from 'react-native';
import { COLORS } from '../../../../shared/constants';
import { useNavigation } from '@react-navigation/native';

const { width } = Dimensions.get('window');

const HeroSection: React.FC = () => {
  const navigation = useNavigation<any>();

  return (
    <View style={styles.container}>
      <View style={styles.badge}>
        <View style={styles.dot} />
        <Text style={styles.badgeText}>New: Version 2.0 is now live</Text>
      </View>
      
      <Text style={styles.title}>
        People First.{"\n"}
        <Text style={{color: COLORS.primary}}>Work Smarter.</Text>
      </Text>
      
      <Text style={styles.description}>
        Bring your people, processes, and possibilities together. WorkSuite simplifies HR operations.
      </Text>
      
      <View style={styles.buttonContainer}>
        <TouchableOpacity 
          style={styles.primaryButton}
          onPress={() => navigation.navigate('Login')}
        >
          <Text style={styles.primaryButtonText}>Get Started</Text>
        </TouchableOpacity>
        
        <TouchableOpacity style={styles.secondaryButton}>
          <Text style={styles.secondaryButtonText}>Explore Features</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.previewContainer}>
         <Image 
           source={{ uri: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&q=80' }} 
           style={styles.previewImage}
           resizeMode="cover"
         />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { padding: 20, alignItems: 'center', backgroundColor: COLORS.lightBackground, paddingBottom: 40 },
  badge: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#E0E7FF', paddingVertical: 6, paddingHorizontal: 12, borderRadius: 20, marginBottom: 20 },
  dot: { width: 6, height: 6, borderRadius: 3, backgroundColor: COLORS.primary, marginRight: 8 },
  badgeText: { fontSize: 12, fontWeight: 'bold', color: COLORS.primary },
  title: { fontSize: 32, fontWeight: '900', color: COLORS.textPrimary, textAlign: 'center', lineHeight: 40, marginBottom: 15 },
  description: { fontSize: 16, color: COLORS.textSecondary, textAlign: 'center', marginBottom: 30, paddingHorizontal: 10 },
  buttonContainer: { width: '100%', marginBottom: 40 },
  primaryButton: { backgroundColor: COLORS.primary, paddingVertical: 16, borderRadius: 12, alignItems: 'center', marginBottom: 12, elevation: 4, shadowColor: COLORS.primary, shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.2, shadowRadius: 8 },
  primaryButtonText: { color: COLORS.white, fontSize: 16, fontWeight: 'bold' },
  secondaryButton: { backgroundColor: COLORS.white, paddingVertical: 16, borderRadius: 12, alignItems: 'center', borderWidth: 1, borderColor: COLORS.border },
  secondaryButtonText: { color: COLORS.textPrimary, fontSize: 16, fontWeight: 'bold' },
  previewContainer: { width: width - 40, height: 220, borderRadius: 20, overflow: 'hidden', elevation: 8, shadowColor: '#000', shadowOffset: { width: 0, height: 10 }, shadowOpacity: 0.1, shadowRadius: 20, backgroundColor: COLORS.white },
  previewImage: { width: '100%', height: '100%' }
});

export default HeroSection;
