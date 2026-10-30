import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { auditApi } from '../../../../shared/api/services';
import { QUERY_KEYS } from '../../../../shared/constants';
import { formatDateTime, extractPagedData } from '../../../../shared/utils';
import type { AuditLog } from '../../../../shared/types';
import { Badge, Pagination, PageHeader, Spinner } from '../../components/ui';

const ACTION_COLOR: Record<string, string> = {
  CREATE: 'green', UPDATE: 'blue', DELETE: 'red',
  LOGIN: 'indigo', LOGOUT: 'gray', EXPORT: 'orange',
};

export default function AuditPage() {
  const { t }  = useTranslation();
  const [page, setPage]         = useState(0);
  const [entity, setEntity]     = useState('');
  const [expanded, setExpanded] = useState<string | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.AUDIT_LOGS(page, entity),
    queryFn:  () => auditApi.getAll({ page, size: 20, entityName: entity || undefined }),
  });

  const { content: logs, totalPages } = extractPagedData<AuditLog>(data);

  const ENTITIES = ['Employee', 'Department', 'Branch', 'Designation', 'Team', 'Transfer', 'Promotion', 'Probation', 'Asset', 'BankDetail', 'EmergencyContact'];

  return (
    <div className="space-y-4">
      <PageHeader title={t('nav.audit')} subtitle="Track all changes made in the system" />

      <div className="flex gap-3 bg-white p-4 rounded-xl border border-slate-200">
        <select className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white" value={entity} onChange={(e) => { setEntity(e.target.value); setPage(0); }}>
          <option value="">All Entities</option>
          {ENTITIES.map((e) => <option key={e} value={e}>{e}</option>)}
        </select>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : (
        <div className="bg-white rounded-xl border border-slate-200 divide-y divide-slate-100">
          {logs.map((log) => (
            <div key={log.id}>
              <div
                className="flex items-center gap-4 px-6 py-4 hover:bg-slate-50 cursor-pointer"
                onClick={() => setExpanded(expanded === log.id ? null : log.id)}
              >
                <div className="shrink-0">
                  {expanded === log.id ? <ChevronDown size={16} className="text-slate-400" /> : <ChevronRight size={16} className="text-slate-400" />}
                </div>
                <div className="flex-1 grid grid-cols-4 gap-4 text-sm">
                  <span className="font-medium text-slate-700">{log.entityName}</span>
                  <Badge label={log.action} color={ACTION_COLOR[log.action] as never ?? 'gray'} />
                  <span className="text-slate-500 truncate">{log.performedBy || '—'}</span>
                  <span className="text-slate-400 text-xs">{formatDateTime(log.createdAt)}</span>
                </div>
                <span className="text-xs text-slate-400 shrink-0">{log.ipAddress || '—'}</span>
              </div>

              {expanded === log.id && (log.oldValue || log.newValue) && (
                <div className="px-6 pb-4 grid grid-cols-2 gap-4">
                  {log.oldValue && (
                    <div>
                      <p className="text-xs font-semibold text-red-500 mb-1">Old Value</p>
                      <pre className="text-xs bg-red-50 border border-red-100 rounded-lg p-3 overflow-auto max-h-40 text-slate-600">
                        {JSON.stringify(JSON.parse(log.oldValue), null, 2)}
                      </pre>
                    </div>
                  )}
                  {log.newValue && (
                    <div>
                      <p className="text-xs font-semibold text-green-500 mb-1">New Value</p>
                      <pre className="text-xs bg-green-50 border border-green-100 rounded-lg p-3 overflow-auto max-h-40 text-slate-600">
                        {JSON.stringify(JSON.parse(log.newValue), null, 2)}
                      </pre>
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
