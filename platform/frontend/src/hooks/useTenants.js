import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { platformApi } from '../api/services';
import { QUERY_KEYS } from '../constants/queryKeys';

export function useTenants({ page = 0, size = 20, search = '', status = '' } = {}) {
  const params = { page, size };
  if (search) params.search = search;
  if (status) params.status = status;

  const { data, isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.PLATFORM_TENANTS(page, search, status),
    queryFn: () => platformApi.getTenants(params),
    keepPreviousData: true,
  });

  const pageData = data?.data?.data ?? null;
  return {
    tenants:    pageData?.content ?? [],
    totalPages: pageData?.totalPages ?? 0,
    totalElements: pageData?.totalElements ?? 0,
    isLoading,
    isError,
  };
}

export function useTenantById(id) {
  const { data, isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.PLATFORM_TENANT_BY_ID(id),
    queryFn: () => platformApi.getTenantById(id),
    enabled: !!id,
  });
  return { tenant: data?.data?.data ?? null, isLoading, isError };
}

export function useTenantHistory(id) {
  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.PLATFORM_TENANT_HISTORY(id),
    queryFn: () => platformApi.getTenantHistory(id),
    enabled: !!id,
  });
  return { history: data?.data?.data ?? [], isLoading };
}

export function useTenantMutations() {
  const qc = useQueryClient();

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['platform-tenants'] });
    qc.invalidateQueries({ queryKey: ['platform-dashboard'] });
  };

  const approve = useMutation({
    mutationFn: ({ id, reason }) => platformApi.approveTenant(id, reason ? { reason } : {}),
    onSuccess: (_, { id }) => {
      invalidate();
      qc.invalidateQueries({ queryKey: QUERY_KEYS.PLATFORM_TENANT_BY_ID(id) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.PLATFORM_TENANT_HISTORY(id) });
    },
  });

  const reject = useMutation({
    mutationFn: ({ id, reason }) => platformApi.rejectTenant(id, { reason }),
    onSuccess: (_, { id }) => {
      invalidate();
      qc.invalidateQueries({ queryKey: QUERY_KEYS.PLATFORM_TENANT_BY_ID(id) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.PLATFORM_TENANT_HISTORY(id) });
    },
  });

  const suspend = useMutation({
    mutationFn: ({ id, reason }) => platformApi.suspendTenant(id, { reason }),
    onSuccess: (_, { id }) => {
      invalidate();
      qc.invalidateQueries({ queryKey: QUERY_KEYS.PLATFORM_TENANT_BY_ID(id) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.PLATFORM_TENANT_HISTORY(id) });
    },
  });

  const reactivate = useMutation({
    mutationFn: ({ id, reason }) => platformApi.reactivateTenant(id, reason ? { reason } : {}),
    onSuccess: (_, { id }) => {
      invalidate();
      qc.invalidateQueries({ queryKey: QUERY_KEYS.PLATFORM_TENANT_BY_ID(id) });
      qc.invalidateQueries({ queryKey: QUERY_KEYS.PLATFORM_TENANT_HISTORY(id) });
    },
  });

  return { approve, reject, suspend, reactivate };
}
