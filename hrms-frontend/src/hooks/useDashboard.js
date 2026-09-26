import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '../api/services';
import { QUERY_KEYS } from '../constants/queryKeys';

export function useDashboard() {
  const { data: summaryData, isLoading } = useQuery({
    queryKey: QUERY_KEYS.DASHBOARD_SUMMARY,
    queryFn: dashboardApi.getSummary,
  });

  const { data: deptData } = useQuery({
    queryKey: QUERY_KEYS.HEADCOUNT_DEPT,
    queryFn: dashboardApi.headcountByDepartment,
  });

  const { data: branchData } = useQuery({
    queryKey: QUERY_KEYS.HEADCOUNT_BRANCH,
    queryFn: dashboardApi.headcountByBranch,
  });

  const { data: trendData } = useQuery({
    queryKey: QUERY_KEYS.JOINING_TREND,
    queryFn: dashboardApi.joiningTrend,
  });

  const { data: docAlertData } = useQuery({
    queryKey: QUERY_KEYS.DOC_EXPIRY_ALERTS,
    queryFn: dashboardApi.documentExpiryAlerts,
  });

  const { data: assetData } = useQuery({
    queryKey: QUERY_KEYS.ASSET_UTILIZATION,
    queryFn: dashboardApi.assetUtilization,
  });

  const summary       = summaryData?.data?.data  || {};
  const deptChart     = deptData?.data?.data      || [];
  const branchChart   = branchData?.data?.data    || [];
  const trend         = trendData?.data?.data     || [];
  const docAlerts     = docAlertData?.data?.data  || {};
  const assetUtil     = assetData?.data?.data     || [];

  return { summary, deptChart, branchChart, trend, docAlerts, assetUtil, isLoading };
}
