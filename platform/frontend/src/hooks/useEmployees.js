import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { employeeApi } from '../api/services';
import { QUERY_KEYS } from '../constants/queryKeys';
import { DEFAULT_PAGE_SIZE } from '../constants/enums';
import { toastSuccess, toastError } from '../utils/errorHandler';

export function useEmployees() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [deptFilter, setDeptFilter] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.EMPLOYEES(page, search, deptFilter),
    queryFn: () => employeeApi.search({
      q: search || undefined,
      departmentId: deptFilter || undefined,
      page,
      size: DEFAULT_PAGE_SIZE,
    }),
  });

  const employees  = data?.data?.data?.content || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  const invalidate = () => qc.invalidateQueries({ queryKey: ['employees'] });

  const createMutation = useMutation({
    mutationFn: employeeApi.create,
    onSuccess: () => { invalidate(); toastSuccess(t('common.createSuccess', { entity: t('employee.entity') })); },
    onError: toastError,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => employeeApi.update(id, data),
    onSuccess: () => { invalidate(); toastSuccess(t('common.updateSuccess', { entity: t('employee.entity') })); },
    onError: toastError,
  });

  const deleteMutation = useMutation({
    mutationFn: employeeApi.delete,
    onSuccess: () => { invalidate(); toastSuccess(t('common.deleteSuccess', { entity: t('employee.entity') })); },
    onError: toastError,
  });

  const handleSearch = (value) => { setSearch(value); setPage(0); };
  const handleDeptFilter = (value) => { setDeptFilter(value); setPage(0); };

  return {
    employees, totalPages, isLoading,
    page, setPage,
    search, handleSearch,
    deptFilter, handleDeptFilter,
    createMutation, updateMutation, deleteMutation,
  };
}
