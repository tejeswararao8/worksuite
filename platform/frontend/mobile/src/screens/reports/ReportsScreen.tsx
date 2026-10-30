import { useState } from 'react';
import {
  View, Text, TouchableOpacity, StyleSheet,
  ScrollView, Alert, Platform,
} from 'react-native';
import { reportApi } from '../../../../shared/api/services';
import { MCard, MSpinner, COLORS } from '../../components';

type Format = 'EXCEL' | 'CSV' | 'PDF';

interface ReportItem {
  id: string;
  title: string;
  description: string;
  emoji: string;
  hasDateRange: boolean;
  fn: (params: Record<string, unknown>) => Promise<{ data: Blob }>;
}

const REPORTS: ReportItem[] = [
  { id: 'employees',   title: 'Employee Master',   description: 'All employees with full details',       emoji: '👥', hasDateRange: false, fn: reportApi.employees },
  { id: 'new-joiners', title: 'New Joiners',        description: 'Employees who joined in a date range', emoji: '🆕', hasDateRange: true,  fn: reportApi.newJoiners },
  { id: 'probation',   title: 'Probation Status',   description: 'All employees on probation',           emoji: '⏱', hasDateRange: false, fn: reportApi.probation },
  { id: 'transfers',   title: 'Transfer History',   description: 'All transfers in a date range',        emoji: '🔄', hasDateRange: true,  fn: reportApi.transfers },
  { id: 'promotions',  title: 'Promotion History',  description: 'All promotions in a date range',       emoji: '📈', hasDateRange: true,  fn: reportApi.promotions },
  { id: 'assets',      title: 'Asset Assignment',   description: 'All currently assigned assets',        emoji: '💻', hasDateRange: false, fn: reportApi.assets },
];

export default function ReportsScreen() {
  const [loading, setLoading] = useState<string | null>(null);

  const handleDownload = async (report: ReportItem, format: Format) => {
    const key = `${report.id}-${format}`;
    setLoading(key);

    try {
      const params: Record<string, unknown> = { format };

      if (report.hasDateRange) {
        // On mobile, prompt for dates
        await new Promise<void>((resolve) => {
          Alert.prompt('From Date', 'Enter start date (YYYY-MM-DD):', [
            { text: 'Cancel', style: 'cancel', onPress: () => { setLoading(null); resolve(); } },
            {
              text: 'Next', onPress: (from) => {
                params.from = from;
                Alert.prompt('To Date', 'Enter end date (YYYY-MM-DD):', [
                  { text: 'Cancel', style: 'cancel', onPress: () => { setLoading(null); resolve(); } },
                  {
                    text: 'Download', onPress: async (to) => {
                      params.to = to;
                      resolve();
                    },
                  },
                ], 'plain-text');
              },
            },
          ], 'plain-text');
        });
      }

      if (!loading) return; // cancelled

      await report.fn(params);
      Alert.alert('Success', `${report.title} report downloaded`);
    } catch {
      Alert.alert('Error', 'Failed to download report. Please try again.');
    } finally {
      setLoading(null);
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.subtitle}>Download HR reports in your preferred format</Text>

      {REPORTS.map((report) => (
        <MCard key={report.id} style={styles.reportCard}>
          <View style={styles.reportHeader}>
            <Text style={styles.reportEmoji}>{report.emoji}</Text>
            <View style={styles.reportInfo}>
              <Text style={styles.reportTitle}>{report.title}</Text>
              <Text style={styles.reportDescription}>{report.description}</Text>
              {report.hasDateRange && (
                <Text style={styles.dateRangeNote}>📅 Requires date range</Text>
              )}
            </View>
          </View>

          <View style={styles.formatButtons}>
            {(['EXCEL', 'CSV', 'PDF'] as Format[]).map((fmt) => {
              const key = `${report.id}-${fmt}`;
              const isLoading = loading === key;
              const fmtColors: Record<Format, { bg: string; text: string }> = {
                EXCEL: { bg: '#dcfce7', text: '#15803d' },
                CSV:   { bg: '#dbeafe', text: '#1d4ed8' },
                PDF:   { bg: '#fee2e2', text: '#dc2626' },
              };
              return (
                <TouchableOpacity
                  key={fmt}
                  style={[styles.fmtBtn, { backgroundColor: fmtColors[fmt].bg }]}
                  onPress={() => handleDownload(report, fmt)}
                  disabled={!!loading}
                  activeOpacity={0.8}
                >
                  {isLoading
                    ? <MSpinner size="small" />
                    : <Text style={[styles.fmtBtnText, { color: fmtColors[fmt].text }]}>{fmt}</Text>
                  }
                </TouchableOpacity>
              );
            })}
          </View>
        </MCard>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container:         { flex: 1, backgroundColor: COLORS.slate50 },
  content:           { padding: 16, gap: 12, paddingBottom: 32 },
  subtitle:          { fontSize: 13, color: COLORS.slate500, marginBottom: 4 },
  reportCard:        { gap: 14 },
  reportHeader:      { flexDirection: 'row', gap: 12, alignItems: 'flex-start' },
  reportEmoji:       { fontSize: 28 },
  reportInfo:        { flex: 1, gap: 2 },
  reportTitle:       { fontSize: 15, fontWeight: '700', color: COLORS.slate800 },
  reportDescription: { fontSize: 13, color: COLORS.slate500 },
  dateRangeNote:     { fontSize: 11, color: COLORS.slate400, marginTop: 2 },
  formatButtons:     { flexDirection: 'row', gap: 8 },
  fmtBtn:            { flex: 1, paddingVertical: 10, borderRadius: 8, alignItems: 'center', justifyContent: 'center', minHeight: 38 },
  fmtBtnText:        { fontSize: 13, fontWeight: '700' },
});
