import React from 'react';
import { View, Text, StyleSheet, Image, Dimensions } from 'react-native';
import { COLORS } from '../../../../shared/constants';

const { width } = Dimensions.get('window');

interface FeatureSectionProps {
  title: string;
  highlight: string;
  description: string;
  features: string[];
  imageUri?: string;
  isReversed?: boolean;
  bg?: string;
}

const FeatureSection: React.FC<FeatureSectionProps> = ({ 
  title, highlight, description, features, imageUri, bg = COLORS.white 
}) => {
  return (
    <View style={[styles.container, { backgroundColor: bg }]}>
      <Text style={styles.title}>
        {title}{"\n"}
        <Text style={{color: COLORS.primary}}>{highlight}</Text>
      </Text>
      
      <Text style={styles.description}>{description}</Text>
      
      <View style={styles.featureList}>
        {features.map((f, i) => (
          <View key={i} style={styles.featureItem}>
            <View style={styles.check}>
               <Text style={styles.checkText}>✓</Text>
            </View>
            <Text style={styles.featureText}>{f}</Text>
          </View>
        ))}
      </View>

      <View style={styles.preview}>
         <View style={styles.mockup}>
            {imageUri ? (
              <Image source={{ uri: imageUri }} style={styles.image} resizeMode="cover" />
            ) : (
              <View style={styles.skeleton} />
            )}
         </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { padding: 25, paddingVertical: 40 },
  title: { fontSize: 28, fontWeight: '800', color: COLORS.textPrimary, marginBottom: 15 },
  description: { fontSize: 15, color: COLORS.textSecondary, marginBottom: 25, lineHeight: 22 },
  featureList: { marginBottom: 30 },
  featureItem: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },
  check: { width: 20, height: 20, borderRadius: 10, backgroundColor: '#DCFCE7', alignItems: 'center', justifyContent: 'center', marginRight: 10 },
  checkText: { color: '#16A34A', fontSize: 12, fontWeight: 'bold' },
  featureText: { fontSize: 14, color: COLORS.textPrimary, fontWeight: '500' },
  preview: { width: '100%', height: 200, backgroundColor: COLORS.lightBackground, borderRadius: 20, padding: 15 },
  mockup: { flex: 1, backgroundColor: COLORS.white, borderRadius: 12, overflow: 'hidden', elevation: 2, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 5 },
  image: { width: '100%', height: '100%' },
  skeleton: { flex: 1, backgroundColor: '#F1F5F9' }
});

export default FeatureSection;
