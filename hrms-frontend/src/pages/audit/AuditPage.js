import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { auditApi } from '../../api/services';
import { Table, Badge, Pagination } from '../../components/ui';
import { formatDateTime } from '../../utils/dateUtils';
import { QUERY_KEYS } from '../../constants/queryKeys';

const ENTITY_TYPES = ['Employee', 'Department', 'Branch', 'Designation', 'Asset', 'Document', 'Transfer', 'Promotion', 'User', 'Role'];
const ACTION_COLOR = { CREATE: 'green', UPDATE: 'blue', DELETE: 'red', LOGIN: 'indigo', LOGOUT: 'gray', APPROVE: 'green', REJECT: 'red', EXPORT: 'orange' };

export default function AuditPage() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [entityFilter, setEntityFilter] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.AUDIT_BY_ENTITY(entityFilter, page),
    queryFn: () => entityFilter
      ? auditApi.getByEntity(entityFilter, { page, size: 20 })
      : auditApi.getAll({ page, size: 20 }),
  });

  const logs       = data?.data?.data?.content || data?.data?.data || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  const columns = [
    { key: 'entityName',  label: t('audit.entityName') },
    { key: 'action',      label: t('audit.action'), render: (r) => <Badge label={r.action} color={ACTION_COLOR[r.action] || 'gray'} /> },
    { key: 'performedBy', label: t('audit.performedBy') },
    { key: 'performedAt', label: t('audit.performedAt'), render: (r) => formatDateTime(r.performedAt) },
    { key: 'ipAddress',   label: t('audit.ipAddress') },
  ];

  return (
    <div className="space-y-4">
      <div className="flex gap-3 items-center">
        <label className="text-sm font-medium text-slate-700">{t('audit.filterByEntity')}</label>
        <select
          className="border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
          value={entityFilter}
          onChange={(e) => { setEntityFilter(e.target.value); setPage(0); }}
        >
          <option value="">All Entities</option>
          {ENTITY_TYPES.map((e) => <option key={e} value={e}>{e}</option>)}
        </select>
      </div>
      <Table columns={columns} data={logs} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
