import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { useCrud } from '../../hooks/useCrud';
import { Button, Modal, Table, Pagination, ConfirmDialog } from './index';
import { Plus, Pencil, Trash2 } from 'lucide-react';

/**
 * Generic CRUD page — zero duplication across entity pages.
 * @param {string[]} queryKey
 * @param {object}   api            - { getAll, create, update, delete }
 * @param {Array}    columns        - table columns (without actions)
 * @param {Function} FormFields     - ({ register, errors }) => JSX
 * @param {string}   entityI18nKey  - e.g. 'department.entity'
 */
export default function CrudPage({ queryKey, api, columns, FormFields, entityI18nKey }) {
  const { t } = useTranslation();
  const entityName = t(entityI18nKey);

  const { rows, totalPages, isLoading, page, setPage, createMutation, updateMutation, deleteMutation } =
    useCrud(queryKey, api, entityI18nKey);

  const [modalOpen, setModalOpen]     = useState(false);
  const [editing, setEditing]         = useState(null);
  const [confirmId, setConfirmId]     = useState(null);

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm();

  const openCreate = () => { setEditing(null); reset({}); setModalOpen(true); };
  const openEdit   = (row) => { setEditing(row); reset(row); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditing(null); reset({}); };

  const onSubmit = (formData) => {
    const mutation = editing ? updateMutation : createMutation;
    const payload  = editing ? { id: editing.id, data: formData } : formData;
    mutation.mutate(payload, { onSuccess: closeModal });
  };

  const allColumns = [
    ...columns,
    {
      key: 'actions',
      label: t('common.actions'),
      render: (r) => (
        <div className="flex gap-2">
          <button onClick={() => openEdit(r)} className="p-1.5 rounded hover:bg-slate-100 text-slate-500">
            <Pencil size={14} />
          </button>
          <button onClick={() => setConfirmId(r.id)} className="p-1.5 rounded hover:bg-red-50 text-red-400">
            <Trash2 size={14} />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button onClick={openCreate}><Plus size={16} /> {t('common.add')} {entityName}</Button>
      </div>

      <Table columns={allColumns} data={rows} loading={isLoading} />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal open={modalOpen} onClose={closeModal} title={editing ? `${t('common.edit')} ${entityName}` : `${t('common.add')} ${entityName}`}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <FormFields register={register} errors={errors} />
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={closeModal}>{t('common.cancel')}</Button>
            <Button type="submit" disabled={isSubmitting}>{editing ? t('common.update') : t('common.create')}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!confirmId}
        message={t('common.confirmDelete', { entity: entityName })}
        onConfirm={() => { deleteMutation.mutate(confirmId); setConfirmId(null); }}
        onCancel={() => setConfirmId(null)}
      />
    </div>
  );
}
