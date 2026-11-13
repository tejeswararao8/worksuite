import { useQuery } from '@tanstack/react-query';
import { platformApi } from '../api/services';
import { QUERY_KEYS } from '../constants/queryKeys';

export function usePlatformDashboard() {
  const { data, isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.PLATFORM_DASHBOARD,
    queryFn: platformApi.getDashboardSummary,
    retry: 1,
  });

  const summary = data?.data?.data ?? null;

  return { summary, isLoading, isError };
}
