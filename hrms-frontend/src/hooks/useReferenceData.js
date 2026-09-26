import { useQuery } from '@tanstack/react-query';
import { departmentApi, branchApi, designationApi } from '../api/services';
import { QUERY_KEYS } from '../constants/queryKeys';

/** Fetches all departments, branches, and designations for use in dropdowns. */
export function useReferenceData() {
  const { data: deptData } = useQuery({
    queryKey: QUERY_KEYS.DEPARTMENTS_ALL,
    queryFn: () => departmentApi.getAll(),
    staleTime: 5 * 60 * 1000, // 5 min — reference data changes rarely
  });

  const { data: branchData } = useQuery({
    queryKey: QUERY_KEYS.BRANCHES_ALL,
    queryFn: () => branchApi.getAll(),
    staleTime: 5 * 60 * 1000,
  });

  const { data: designationData } = useQuery({
    queryKey: QUERY_KEYS.DESIGNATIONS_ALL,
    queryFn: () => designationApi.getAll(),
    staleTime: 5 * 60 * 1000,
  });

  const extract = (d) => d?.data?.data?.content || d?.data?.data || [];

  return {
    departments:  extract(deptData),
    branches:     extract(branchData),
    designations: extract(designationData),
  };
}
