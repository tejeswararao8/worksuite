import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { documentApi } from '../../api/services';
import { Button, Table, Badge, Modal, Pagination, ConfirmDialog, Input } from '../../components/ui';
import { Plus, Trash2, Download, Search } from 'lucide-react';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { formatDate } from '../../utils/dateUtils';
import { DOCUMENT_TYPE, DOCUMENT_TYPE_COLOR } from '../../constants/enums';

export default function DocumentsPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [employeeId, setEmployeeId]   = useState('');
  const [searchId, setSearchId]       = useState('');
  const [page, setPage]               = useState(0);
  const [modalOpen, setModalOpen]     = useState(false);
  const [confirmId, setConfirmId]     = useState(null);
  const [form, setForm]               = useState({ documentType: DOCUMENT_TYPE.OTHER, expiryDate: '' });
  const [file, setFile]               = useState(null);

  const { data, isLoading } = useQuery({
    queryKey: ['documents', searchId, page],
    queryFn: () => documentApi.getAll(searchId, { page, size: 20 }),
    enabled: !!searchId,
  });

  const docs       = data?.data?.data?.content || data?.data?.data || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  const invalidate = () => qc.invalidateQueries({ queryKey: ['documents', searchId] });

  const uploadMutation = useMutation({
    mutationFn: (fd) => documentApi.upload(searchId, fd),
    onSuccess: () => {
      invalidate();
      toastSuccess(t('common.createSuccess', { entity: t('document.entity') }));
      setModalOpen(false);
      setFile(null);
    },
    onError: toastError,
  });

  const deleteMutation = useMutation({
    mutationFn: (docId) => documentApi.delete(searchId, docId),
    onSuccess: () => {
      invalidate();
      toastSuccess(t('common.deleteSuccess', { entity: t('document.entity') }));
    },
    onError: toastError,
  });

  const handleUpload = () => {
    if (!file) return toastError({ response: { status: 400, data: { message: t('document.selectFile') } } });
    const fd = new FormData();
    fd.append('file', file);
    Object.entries(form).forEach(([k, v]) => v && fd.append(k, v));
    uploadMutation.mutate(fd);
  };

  const columns = [
    { key: 'documentType', label: t('document.type'),      render: (r) => <Badge label={r.documentType} color={DOCUMENT_TYPE_COLOR[r.documentType] || 'gray'} /> },
    { key: 'fileName',     label: t('document.fileName') },
    { key: 'expiryDate',   label: t('document.expiryDate'), render: (r) => formatDate(r.expiryDate) },
    { key: 'uploadedAt',   label: t('document.uploadedAt'), render: (r) => formatDate(r.uploadedAt) },
    {
      key: 'actions', label: t('common.actions'), render: (r) => (
        <div className="flex gap-2">
          {r.fileUrl && (
            <a href={r.fileUrl} target="_blank" rel="noreferrer" className="p-1.5 rounded hover:bg-slate-100 text-slate-500">
              <Download size={14} />
            </a>
          )}
          <button onClick={() => setConfirmId(r.id)} className="p-1.5 rounded hover:bg-red-50 text-red-400">
            <Trash2 size={14} />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Employee ID lookup */}
      <div className="flex gap-3 items-end">
        <div className="flex-1 max-w-sm">
          <Input
            label={t('employee.id')}
            placeholder={t('document.enterEmployeeId')}
            value={employeeId}
            onChange={(e) => setEmployeeId(e.target.value)}
          />
        </div>
        <Button
          onClick={() => { setSearchId(employeeId); setPage(0); }}
          disabled={!employeeId.trim()}
        >
          <Search size={16} /> {t('common.search')}
        </Button>
        {searchId && (
          <Button onClick={() => setModalOpen(true)}>
            <Plus size={16} /> {t('document.uploadTitle')}
          </Button>
        )}
      </div>

      {searchId ? (
        <>
          <Table columns={columns} data={docs} loading={isLoading} />
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      ) : (
        <div className="bg-white rounded-xl border border-slate-200 p-10 text-center text-slate-400 text-sm">
          {t('document.enterEmployeeId')}
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={t('document.uploadTitle')}>
        <div className="space-y-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-slate-700">{t('document.type')}</label>
            <select
              className="border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
              value={form.documentType}
              onChange={(e) => setForm({ ...form, documentType: e.target.value })}
            >
              {Object.values(DOCUMENT_TYPE).map((type) => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-slate-700">{t('document.expiryDate')}</label>
            <input type="date" className="border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={form.expiryDate} onChange={(e) => setForm({ ...form, expiryDate: e.target.value })} />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-slate-700">{t('document.file')}</label>
            <input type="file" className="text-sm" onChange={(e) => setFile(e.target.files[0])} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button variant="secondary" onClick={() => setModalOpen(false)}>{t('common.cancel')}</Button>
            <Button onClick={handleUpload} disabled={uploadMutation.isPending}>{t('common.upload')}</Button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        open={!!confirmId}
        message={t('common.confirmDelete', { entity: t('document.entity') })}
        onConfirm={() => { deleteMutation.mutate(confirmId); setConfirmId(null); }}
        onCancel={() => setConfirmId(null)}
      />
    </div>
  );
}
