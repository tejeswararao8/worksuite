import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { roleApi } from '../../api/services';
import { Table, Badge } from '../../components/ui';
import { QUERY_KEYS } from '../../constants/queryKeys';

export default function RolesPage() {
  const { t } = useTranslation();

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.ROLES(0),
    queryFn: roleApi.getAll,
  });

  const roles = data?.data?.data?.content || data?.data?.data || [];

  const columns = [
    { key: 'name',        label: t('role.name') },
    { key: 'description', label: t('role.description') },
    {
      key: 'permissions', label: t('role.permissions'),
      render: (r) => (
        <div className="flex flex-wrap gap-1">
          {(r.permissions || []).slice(0, 4).map((p) => <Badge key={p} label={p} color="indigo" />)}
          {(r.permissions || []).length > 4 && (
            <Badge label={`+${r.permissions.length - 4} more`} color="gray" />
          )}
        </div>
      ),
    },
  ];

  return <Table columns={columns} data={roles} loading={isLoading} />;
}
