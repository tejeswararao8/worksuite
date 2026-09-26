import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import CrudPage from '../../components/ui/CrudPage';
import { assetApi } from '../../api/services';
import { Input, Select, Badge, Modal, Button, ConfirmDialog } from '../../components/ui';
import { QUERY_KEYS } from '../../constants/queryKeys';
import { ASSET_STATUS_COLOR } from '../../constants/enums';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { UserPlus, RotateCcw } from 'lucide-react';

function FormFields({ register, errors }) {
  const { t } = useTranslation();
  return (
    <>
      <Input  label={t('asset.name')}         error={errors.name?.message}      {...register('name',      { required: t('common.required') })} />
      <Input  label={t('asset.code')}         error={errors.assetCode?.message} {...register('assetCode', { required: t('common.required') })} />
      <Input  label={t('asset.serialNumber')} {...register('serialNumber')} />
      <Select label={t('asset.type')}         {...register('assetType')}>
        <option value="">Select</option>
        <option value="LAPTOP">Laptop</option>
        <option value="DESKTOP">Desktop</option>
        <option value="MOBILE">Mobile</option>
        <option value="MONITOR">Monitor</option>
        <option value="SIM">SIM</option>
        <option value="ACCESS_CARD">Access Card</option>
        <option value="OTHER">Other</option>
      </Select>
      <Select label={t('asset.status')} {...register('status')}>
        <option value="AVAILABLE">Available</option>
        <option value="ASSIGNED">Assigned</option>
        <option value="IN_REPAIR">In Repair</option>
        <option value="RETIRED">Retired</option>
      </Select>
      <Input label={t('asset.purchaseDate')} type="date" {...register('purchaseDate')} />
    </>
  );
}

// Wrapper that adds assign/return columns on top of CrudPage
export default function AssetsPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [assignId, setAssignId] = useState(null);
  const [returnConfirmId, setReturnConfirmId] = useState(null);

  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm();

  const assignMutation = useMutation({
    mutationFn: ({ id, data }) => assetApi.assign(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['assets'] }); toastSuccess('Asset assigned'); setAssignId(null); reset(); },
    onError: toastError,
  });

  const returnMutation = useMutation({
    mutationFn: assetApi.returnAsset,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['assets'] }); toastSuccess('Asset returned'); setReturnConfirmId(null); },
    onError: toastError,
  });

  const extraColumns = [
    { key: 'assetCode',    label: t('asset.code') },
    { key: 'name',         label: t('asset.name') },
    { key: 'assetType',    label: t('asset.type') },
    { key: 'serialNumber', label: t('asset.serialNumber') },
    { key: 'status',       label: t('asset.status'), render: (r) => <Badge label={r.status} color={ASSET_STATUS_COLOR[r.status] || 'gray'} /> },
    {
      key: 'assetActions', label: 'Assign / Return', render: (r) => (
        <div className="flex gap-1">
          {r.status === 'AVAILABLE' && (
            <button onClick={() => setAssignId(r.id)} className="p-1.5 rounded hover:bg-indigo-50 text-indigo-500" title="Assign"><UserPlus size={14} /></button>
          )}
          {r.status === 'ASSIGNED' && (
            <button onClick={() => setReturnConfirmId(r.id)} className="p-1.5 rounded hover:bg-yellow-50 text-yellow-500" title="Return"><RotateCcw size={14} /></button>
          )}
        </div>
      ),
    },
  ];

  return (
    <>
      <CrudPage
        queryKey={QUERY_KEYS.ASSETS(0).slice(0, 1)}
        api={assetApi}
        columns={extraColumns}
        FormFields={FormFields}
        entityI18nKey="asset.entity"
      />

      {/* Assign Modal */}
      <Modal open={!!assignId} onClose={() => { setAssignId(null); reset(); }} title="Assign Asset">
        <form onSubmit={handleSubmit((d) => assignMutation.mutate({ id: assignId, data: d }))} className="space-y-4">
          <Input label="Employee ID" {...register('employeeId', { required: t('common.required') })} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setAssignId(null)}>{t('common.cancel')}</Button>
            <Button type="submit" disabled={isSubmitting}>Assign</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog open={!!returnConfirmId} message="Return this asset?" onConfirm={() => returnMutation.mutate(returnConfirmId)} onCancel={() => setReturnConfirmId(null)} />
    </>
  );
}
