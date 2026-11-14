import { useState, useRef, useCallback } from 'react';
import { useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Upload, Download, CheckCircle, XCircle, AlertCircle, Clock } from 'lucide-react';
import { bulkOnboardingApi } from '../../api/services';
import { BULK_ROW_STATUS, BULK_ROW_STATUS_COLOR } from '../../constants/enums';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { downloadBlob } from '../../utils/download';
import { Button, Badge } from '../../components/ui';

const MAX_SIZE_MB = 10;

const ROW_STATUS_ICON = {
  [BULK_ROW_STATUS.SUCCESS]:               <CheckCircle size={14} className="text-green-500" />,
  [BULK_ROW_STATUS.INVITATION_PENDING]:    <Clock size={14} className="text-yellow-500" />,
  [BULK_ROW_STATUS.FAILED]:               <XCircle size={14} className="text-red-500" />,
  [BULK_ROW_STATUS.EMAIL_DELIVERY_FAILED]: <AlertCircle size={14} className="text-orange-500" />,
};

export default function BulkImportPage() {
  const { t } = useTranslation();
  const fileInputRef = useRef(null);

  const [selectedFile, setSelectedFile]   = useState(null);
  const [fileError, setFileError]         = useState('');
  const [importResult, setImportResult]   = useState(null);

  // ── Download template ──────────────────────────────────────────────────────
  const templateMutation = useMutation({
    mutationFn: bulkOnboardingApi.downloadTemplate,
    onSuccess: (res) => {
      downloadBlob(new Blob([res.data], { type: 'text/csv' }), 'employee_import_template.csv');
      toastSuccess(t('bulk.templateDownloaded'));
    },
    onError: toastError,
  });

  // ── Import file ────────────────────────────────────────────────────────────
  const importMutation = useMutation({
    mutationFn: bulkOnboardingApi.importFile,
    onSuccess: (res) => {
      setImportResult(res?.data?.data ?? null);
      toastSuccess(t('bulk.importComplete'));
    },
    onError: (err) => {
      toastError(err);
      setImportResult(null);
    },
  });

  const validateFile = useCallback((file) => {
    if (!file) return t('bulk.selectFile');
    const ext = file.name.split('.').pop().toLowerCase();
    if (!['csv', 'xlsx', 'xls'].includes(ext)) return t('bulk.invalidType');
    if (file.size > MAX_SIZE_MB * 1024 * 1024) return t('bulk.fileTooLarge', { mb: MAX_SIZE_MB });
    return '';
  }, [t]);

  const handleFileChange = useCallback((e) => {
    const file = e.target.files?.[0] ?? null;
    setSelectedFile(file);
    setFileError(validateFile(file));
    setImportResult(null);
  }, [validateFile]);

  const handleImport = useCallback(() => {
    const err = validateFile(selectedFile);
    if (err) { setFileError(err); return; }
    importMutation.mutate(selectedFile);
  }, [selectedFile, validateFile, importMutation]);

  const isImporting = importMutation.isPending;

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header actions */}
      <div className="flex flex-wrap gap-3 items-center justify-between">
        <p className="text-sm text-slate-500">{t('bulk.description')}</p>
        <Button
          variant="outline"
          loading={templateMutation.isPending}
          onClick={() => templateMutation.mutate()}
        >
          <Download size={16} /> {t('bulk.downloadTemplate')}
        </Button>
      </div>

      {/* Upload card */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 space-y-4">
        <h3 className="text-sm font-semibold text-slate-700">{t('bulk.uploadTitle')}</h3>

        <div
          className="border-2 border-dashed border-slate-300 rounded-lg p-8 flex flex-col items-center gap-3 cursor-pointer hover:border-indigo-400 transition-colors"
          onClick={() => fileInputRef.current?.click()}
        >
          <Upload size={28} className="text-slate-400" />
          <p className="text-sm text-slate-500">
            {selectedFile ? selectedFile.name : t('bulk.dropOrClick')}
          </p>
          <p className="text-xs text-slate-400">{t('bulk.acceptedFormats')}</p>
          <input
            ref={fileInputRef}
            type="file"
            accept=".csv,.xlsx,.xls"
            className="hidden"
            onChange={handleFileChange}
          />
        </div>

        {fileError && (
          <p className="text-xs text-red-500">{fileError}</p>
        )}

        <div className="flex justify-end">
          <Button
            onClick={handleImport}
            disabled={!selectedFile || !!fileError || isImporting}
            loading={isImporting}
          >
            <Upload size={16} /> {t('bulk.import')}
          </Button>
        </div>
      </div>

      {/* Results */}
      {importResult && <ImportResults result={importResult} />}
    </div>
  );
}

function ImportResults({ result }) {
  const { t } = useTranslation();

  const summaryItems = [
    { label: t('bulk.totalRows'),              value: result.totalRows,              color: 'text-slate-700' },
    { label: t('bulk.successCount'),           value: result.successCount,           color: 'text-green-600' },
    { label: t('bulk.invitationPendingCount'), value: result.invitationPendingCount, color: 'text-yellow-600' },
    { label: t('bulk.emailFailureCount'),      value: result.emailDeliveryFailureCount, color: 'text-orange-600' },
    { label: t('bulk.failedCount'),            value: result.failedCount,            color: 'text-red-600' },
  ];

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-6 space-y-4">
      <h3 className="text-sm font-semibold text-slate-700">{t('bulk.resultsTitle')}</h3>

      {/* Summary */}
      <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
        {summaryItems.map((item) => (
          <div key={item.label} className="bg-slate-50 rounded-lg p-3 text-center">
            <p className={`text-xl font-bold ${item.color}`}>{item.value}</p>
            <p className="text-xs text-slate-500 mt-0.5">{item.label}</p>
          </div>
        ))}
      </div>

      {/* Row details */}
      {result.results?.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="text-left py-2 px-3 text-xs font-medium text-slate-500">{t('bulk.row')}</th>
                <th className="text-left py-2 px-3 text-xs font-medium text-slate-500">{t('invitation.email')}</th>
                <th className="text-left py-2 px-3 text-xs font-medium text-slate-500">{t('employee.code')}</th>
                <th className="text-left py-2 px-3 text-xs font-medium text-slate-500">{t('common.status')}</th>
                <th className="text-left py-2 px-3 text-xs font-medium text-slate-500">{t('bulk.errorField')}</th>
                <th className="text-left py-2 px-3 text-xs font-medium text-slate-500">{t('bulk.errorMessage')}</th>
              </tr>
            </thead>
            <tbody>
              {result.results.map((row) => (
                <tr key={row.rowNumber} className="border-b border-slate-100 hover:bg-slate-50">
                  <td className="py-2 px-3 text-slate-600">{row.rowNumber}</td>
                  <td className="py-2 px-3 text-slate-600">{row.email ?? '—'}</td>
                  <td className="py-2 px-3 text-slate-600">{row.employeeCode ?? '—'}</td>
                  <td className="py-2 px-3">
                    <div className="flex items-center gap-1.5">
                      {ROW_STATUS_ICON[row.status]}
                      <Badge
                        label={t(`bulk.rowStatus.${row.status}`, { defaultValue: row.status })}
                        color={BULK_ROW_STATUS_COLOR[row.status] ?? 'gray'}
                      />
                    </div>
                  </td>
                  <td className="py-2 px-3 text-slate-500 text-xs">{row.errorField ?? '—'}</td>
                  <td className="py-2 px-3 text-slate-500 text-xs">{row.errorMessage ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
