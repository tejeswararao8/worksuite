import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { reportApi } from '../../api/services';
import { Button } from '../../components/ui';
import { FileSpreadsheet, FileText, Download } from 'lucide-react';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { downloadBlob, getFileExtension } from '../../utils/download';

const REPORT_TYPES = [
  { key: 'employees',      labelKey: 'report.employeeReports',  apiFn: (f) => reportApi.employees({ format: f }) },
  { key: 'departments',    labelKey: 'report.departments',      apiFn: (f) => reportApi.departments({ format: f }) },
  { key: 'branches',       labelKey: 'report.branches',         apiFn: (f) => reportApi.branches({ format: f }) },
  { key: 'designations',   labelKey: 'report.designations',     apiFn: (f) => reportApi.designations({ format: f }) },
  { key: 'promotions',     labelKey: 'report.promotions',       apiFn: (f) => reportApi.promotions({ format: f }) },
  { key: 'transfers',      labelKey: 'report.transfers',        apiFn: (f) => reportApi.transfers({ format: f }) },
  { key: 'documentExpiry', labelKey: 'report.docExpiry',        apiFn: (f) => reportApi.documentExpiry({ format: f, daysAhead: 90 }) },
  { key: 'assets',         labelKey: 'report.assets',           apiFn: (f) => reportApi.assets({ format: f }) },
];

const FORMAT_ICONS = { excel: FileSpreadsheet, csv: Download, pdf: FileText };
const FORMATS = ['excel', 'csv', 'pdf'];

export default function ReportsPage() {
  const { t } = useTranslation();
  const [loading, setLoading] = useState({});

  const handleExport = async (reportKey, apiFn, format) => {
    const key = `${reportKey}-${format}`;
    setLoading((prev) => ({ ...prev, [key]: true }));
    try {
      const { data } = await apiFn(format.toUpperCase());
      downloadBlob(data, `${reportKey}_report.${getFileExtension(format)}`);
      toastSuccess(t('report.downloadSuccess'));
    } catch (err) {
      toastError(err);
    } finally {
      setLoading((prev) => ({ ...prev, [key]: false }));
    }
  };

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {REPORT_TYPES.map(({ key, labelKey, apiFn }) => (
          <div key={key} className="bg-white rounded-xl border border-slate-200 p-5">
            <h3 className="text-sm font-semibold text-slate-800 mb-3">{t(labelKey)}</h3>
            <div className="flex gap-2 flex-wrap">
              {FORMATS.map((fmt) => {
                const Icon = FORMAT_ICONS[fmt];
                const loadKey = `${key}-${fmt}`;
                return (
                  <Button
                    key={fmt}
                    size="sm"
                    variant={fmt === 'excel' ? 'primary' : 'secondary'}
                    onClick={() => handleExport(key, apiFn, fmt)}
                    disabled={loading[loadKey]}
                  >
                    <Icon size={14} /> {fmt.toUpperCase()}
                  </Button>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
