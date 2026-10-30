import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { FileSpreadsheet, FileText, Download } from 'lucide-react';
import { reportApi } from '../../../../shared/api/services';
import { downloadBlob } from '../../../../shared/utils';
import { Button, PageHeader } from '../../components/ui';

type Format = 'EXCEL' | 'CSV' | 'PDF';

interface ReportCard {
  id: string;
  title: string;
  description: string;
  hasDateRange: boolean;
  fn: (params: Record<string, unknown>) => Promise<{ data: Blob }>;
}

const REPORTS: ReportCard[] = [
  { id: 'employees',   title: 'Employee Master',    description: 'All employees with full details',         hasDateRange: false, fn: reportApi.employees },
  { id: 'new-joiners', title: 'New Joiners',         description: 'Employees who joined in a date range',   hasDateRange: true,  fn: reportApi.newJoiners },
  { id: 'probation',   title: 'Probation Status',    description: 'All employees currently on probation',   hasDateRange: false, fn: reportApi.probation },
  { id: 'transfers',   title: 'Transfer History',    description: 'All transfers in a date range',          hasDateRange: true,  fn: reportApi.transfers },
  { id: 'promotions',  title: 'Promotion History',   description: 'All promotions in a date range',         hasDateRange: true,  fn: reportApi.promotions },
  { id: 'assets',      title: 'Asset Assignment',    description: 'All currently assigned assets',          hasDateRange: false, fn: reportApi.assets },
];

export default function ReportsPage() {
  const { t } = useTranslation();
  const [loading, setLoading] = useState<string | null>(null);
  const [dates, setDates]     = useState<Record<string, { from: string; to: string }>>({});

  const download = async (report: ReportCard, format: Format) => {
    const key = `${report.id}-${format}`;
    setLoading(key);
    try {
      const params: Record<string, unknown> = { format };
      if (report.hasDateRange && dates[report.id]) {
        params.from = dates[report.id].from;
        params.to   = dates[report.id].to;
      }
      const { data } = await report.fn(params);
      const ext = format === 'EXCEL' ? 'xlsx' : format === 'CSV' ? 'csv' : 'pdf';
      downloadBlob(data, `${report.id}_report.${ext}`);
      toast.success('Report downloaded');
    } catch {
      toast.error('Export failed');
    } finally {
      setLoading(null);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader title={t('nav.reports')} subtitle="Export HR data in Excel, CSV, or PDF format" />

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        {REPORTS.map((report) => (
          <div key={report.id} className="bg-white rounded-xl border border-slate-200 p-5 space-y-4">
            <div>
              <h3 className="text-sm font-semibold text-slate-800">{report.title}</h3>
              <p className="text-xs text-slate-400 mt-0.5">{report.description}</p>
            </div>

            {report.hasDateRange && (
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="text-xs text-slate-500">From</label>
                  <input
                    type="date"
                    className="w-full border border-slate-300 rounded-lg px-2 py-1.5 text-xs mt-0.5"
                    onChange={(e) => setDates((prev) => ({ ...prev, [report.id]: { ...prev[report.id], from: e.target.value } }))}
                  />
                </div>
                <div>
                  <label className="text-xs text-slate-500">To</label>
                  <input
                    type="date"
                    className="w-full border border-slate-300 rounded-lg px-2 py-1.5 text-xs mt-0.5"
                    onChange={(e) => setDates((prev) => ({ ...prev, [report.id]: { ...prev[report.id], to: e.target.value } }))}
                  />
                </div>
              </div>
            )}

            <div className="flex gap-2">
              {(['EXCEL', 'CSV', 'PDF'] as Format[]).map((fmt) => {
                const key = `${report.id}-${fmt}`;
                return (
                  <button
                    key={fmt}
                    onClick={() => download(report, fmt)}
                    disabled={loading === key}
                    className="flex-1 flex items-center justify-center gap-1 py-2 text-xs font-medium border border-slate-200 rounded-lg hover:bg-slate-50 disabled:opacity-50 transition-colors"
                  >
                    {loading === key
                      ? <span className="w-3 h-3 border border-current border-t-transparent rounded-full animate-spin" />
                      : fmt === 'EXCEL' ? <FileSpreadsheet size={12} className="text-green-600" />
                      : fmt === 'PDF'   ? <FileText size={12} className="text-red-500" />
                      : <Download size={12} className="text-blue-500" />
                    }
                    {fmt}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
