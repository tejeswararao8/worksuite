import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DEFAULT_PAGE_SIZE } from '../constants/enums';
import { toastSuccess, toastError } from '../utils/errorHandler';

/**
 * Generic CRUD hook.
 * @param {string[]} queryKey  - base query key array
 * @param {object}   api       - { getAll, create, update, delete }
 * @param {string}   entityI18nKey - i18n key for entity name e.g. 'department.entity'
 */
export function useCrud(queryKey, api, entityI18nKey) {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: [...queryKey, page],
    queryFn: () => api.getAll({ page, size: DEFAULT_PAGE_SIZE }),
  });

  const rows       = data?.data?.data?.content || data?.data?.data || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  const invalidate = () => qc.invalidateQueries({ queryKey });

  const createMutation = useMutation({
    mutationFn: api.create,
    onSuccess: () => { invalidate(); toastSuccess(t('common.createSuccess', { entity: t(entityI18nKey) })); },
    onError: toastError,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => api.update(id, data),
    onSuccess: () => { invalidate(); toastSuccess(t('common.updateSuccess', { entity: t(entityI18nKey) })); },
    onError: toastError,
  });

  const deleteMutation = useMutation({
    mutationFn: api.delete,
    onSuccess: () => { invalidate(); toastSuccess(t('common.deleteSuccess', { entity: t(entityI18nKey) })); },
    onError: toastError,
  });

  return {
    rows, totalPages, isLoading,
    page, setPage,
    createMutation, updateMutation, deleteMutation,
  };
}
