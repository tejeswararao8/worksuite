import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { platformApi } from '../api/services';
import { QUERY_KEYS } from '../constants/queryKeys';

export function usePlatformEmployees({ page = 0, size = 20, status = '' } = {}) {
  const params = { page, size };
  if (status) params.status = status;

  const { data, isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.PLATFORM_EMPLOYEES(page, status),
    queryFn: () => platformApi.getPlatformEmployees(params),
    keepPreviousData: true,
  });

  const pageData = data?.data?.data ?? null;
  return {
    employees:    pageData?.content ?? [],
    totalPages:   pageData?.totalPages ?? 0,
    totalElements: pageData?.totalElements ?? 0,
    isLoading,
    isError,
  };
}

export function usePlatformInvitations({ page = 0, size = 20, status = '' } = {}) {
  const params = { page, size };
  if (status) params.status = status;

  const { data, isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.PLATFORM_INVITATIONS(page, status),
    queryFn: () => platformApi.getInvitations(params),
    keepPreviousData: true,
  });

  const pageData = data?.data?.data ?? null;
  return {
    invitations:  pageData?.content ?? [],
    totalPages:   pageData?.totalPages ?? 0,
    isLoading,
    isError,
  };
}

export function usePlatformEmployeeMutations() {
  const qc = useQueryClient();

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['platform-employees'] });
    qc.invalidateQueries({ queryKey: ['platform-dashboard'] });
  };

  const invite = useMutation({
    mutationFn: (data) => platformApi.inviteEmployee(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['platform-invitations'] }),
  });

  const resendInvitation = useMutation({
    mutationFn: (id) => platformApi.resendInvitation(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['platform-invitations'] }),
  });

  const revokeInvitation = useMutation({
    mutationFn: (id) => platformApi.revokeInvitation(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['platform-invitations'] }),
  });

  const changeRole = useMutation({
    mutationFn: ({ id, roleName }) => platformApi.changeEmployeeRole(id, { roleName }),
    onSuccess: () => invalidate(),
  });

  const suspend = useMutation({
    mutationFn: (id) => platformApi.suspendEmployee(id),
    onSuccess: () => invalidate(),
  });

  const reactivate = useMutation({
    mutationFn: (id) => platformApi.reactivateEmployee(id),
    onSuccess: () => invalidate(),
  });

  const revoke = useMutation({
    mutationFn: (id) => platformApi.revokeEmployee(id),
    onSuccess: () => invalidate(),
  });

  return { invite, resendInvitation, revokeInvitation, changeRole, suspend, reactivate, revoke };
}
