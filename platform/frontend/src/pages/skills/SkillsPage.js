import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { skillsApi } from '../../api/services';
import { Button, Input, Select, Modal, Table, Badge, ConfirmDialog } from '../../components/ui';
import { Plus, Trash2, Search } from 'lucide-react';
import { toastSuccess, toastError } from '../../utils/errorHandler';
import { formatDate } from '../../utils/dateUtils';
import { QUERY_KEYS } from '../../constants/queryKeys';

export default function SkillsPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [empId, setEmpId] = useState('');
  const [activeEmpId, setActiveEmpId] = useState('');
  const [skillModal, setSkillModal] = useState(false);
  const [certModal, setCertModal] = useState(false);
  const [confirmSkill, setConfirmSkill] = useState(null);
  const [confirmCert, setConfirmCert] = useState(null);

  const { data: skillsData, isLoading: loadingSkills } = useQuery({
    queryKey: QUERY_KEYS.SKILLS_BY_EMPLOYEE(activeEmpId),
    queryFn: () => skillsApi.getSkills(activeEmpId),
    enabled: !!activeEmpId,
  });

  const { data: certsData, isLoading: loadingCerts } = useQuery({
    queryKey: QUERY_KEYS.CERTS_BY_EMPLOYEE(activeEmpId),
    queryFn: () => skillsApi.getCertifications(activeEmpId),
    enabled: !!activeEmpId,
  });

  const skills = skillsData?.data?.data || [];
  const certs  = certsData?.data?.data  || [];

  const { register: regSkill, handleSubmit: handleSkill, reset: resetSkill, formState: { isSubmitting: skillSubmitting } } = useForm();
  const { register: regCert,  handleSubmit: handleCert,  reset: resetCert,  formState: { isSubmitting: certSubmitting  } } = useForm();

  const invalidateSkills = () => { qc.invalidateQueries({ queryKey: QUERY_KEYS.SKILLS_BY_EMPLOYEE(activeEmpId) }); };
  const invalidateCerts  = () => { qc.invalidateQueries({ queryKey: QUERY_KEYS.CERTS_BY_EMPLOYEE(activeEmpId) }); };

  const addSkillMutation    = useMutation({ mutationFn: (d) => skillsApi.addSkill(activeEmpId, d),           onSuccess: () => { invalidateSkills(); toastSuccess(t('common.createSuccess', { entity: t('skills.entity') })); setSkillModal(false); resetSkill(); }, onError: toastError });
  const removeSkillMutation = useMutation({ mutationFn: (skillId) => skillsApi.removeSkill(activeEmpId, skillId), onSuccess: () => { invalidateSkills(); toastSuccess(t('common.deleteSuccess', { entity: t('skills.entity') })); setConfirmSkill(null); }, onError: toastError });
  const addCertMutation     = useMutation({ mutationFn: (d) => skillsApi.addCertification(activeEmpId, d),   onSuccess: () => { invalidateCerts();  toastSuccess(t('common.createSuccess', { entity: t('skills.certEntity') })); setCertModal(false); resetCert(); }, onError: toastError });
  const removeCertMutation  = useMutation({ mutationFn: (certId) => skillsApi.removeCertification(activeEmpId, certId), onSuccess: () => { invalidateCerts();  toastSuccess(t('common.deleteSuccess', { entity: t('skills.certEntity') })); setConfirmCert(null); }, onError: toastError });

  const skillColumns = [
    { key: 'skillName',        label: t('skills.skillName') },
    { key: 'skillType',        label: t('skills.skillType'),        render: (r) => <Badge label={r.skillType} color={r.skillType === 'PRIMARY' ? 'indigo' : 'gray'} /> },
    { key: 'proficiencyLevel', label: t('skills.proficiencyLevel'), render: (r) => <Badge label={r.proficiencyLevel} color="blue" /> },
    { key: 'yearsOfExperience',label: t('skills.yearsOfExperience') },
    { key: 'actions', label: t('common.actions'), render: (r) => <button onClick={() => setConfirmSkill(r.id)} className="p-1.5 rounded hover:bg-red-50 text-red-400"><Trash2 size={14} /></button> },
  ];

  const certColumns = [
    { key: 'certificationName',   label: t('skills.certName') },
    { key: 'issuingOrganization', label: t('skills.issuingOrg') },
    { key: 'issueDate',           label: t('skills.issueDate'),   render: (r) => formatDate(r.issueDate) },
    { key: 'expiryDate',          label: t('skills.expiryDate'),  render: (r) => formatDate(r.expiryDate) },
    { key: 'credentialId',        label: t('skills.credentialId') },
    { key: 'actions', label: t('common.actions'), render: (r) => <button onClick={() => setConfirmCert(r.id)} className="p-1.5 rounded hover:bg-red-50 text-red-400"><Trash2 size={14} /></button> },
  ];

  return (
    <div className="space-y-6">
      {/* Employee search */}
      <div className="bg-white rounded-xl border border-slate-200 p-4 flex gap-3 items-end">
        <div className="flex-1">
          <label className="text-sm font-medium text-slate-700 block mb-1">Employee ID</label>
          <input className="border border-slate-300 rounded-lg px-3 py-2 text-sm w-full focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={empId} onChange={(e) => setEmpId(e.target.value)} placeholder="Enter employee UUID..." />
        </div>
        <Button onClick={() => setActiveEmpId(empId)} disabled={!empId}><Search size={16} /> Load</Button>
      </div>

      {activeEmpId && (
        <>
          {/* Skills */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-slate-700">{t('skills.entity')}s</h3>
              <Button size="sm" onClick={() => setSkillModal(true)}><Plus size={14} /> {t('common.add')}</Button>
            </div>
            <Table columns={skillColumns} data={skills} loading={loadingSkills} />
          </div>

          {/* Certifications */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-slate-700">{t('skills.certEntity')}s</h3>
              <Button size="sm" onClick={() => setCertModal(true)}><Plus size={14} /> {t('common.add')}</Button>
            </div>
            <Table columns={certColumns} data={certs} loading={loadingCerts} />
          </div>
        </>
      )}

      {/* Add Skill Modal */}
      <Modal open={skillModal} onClose={() => { setSkillModal(false); resetSkill(); }} title={`${t('common.add')} ${t('skills.entity')}`}>
        <form onSubmit={handleSkill((d) => addSkillMutation.mutate(d))} className="space-y-4">
          <Input label={t('skills.skillName')} {...regSkill('skillName', { required: t('common.required') })} />
          <Select label={t('skills.skillType')} {...regSkill('skillType')}>
            <option value="PRIMARY">{t('skills.primary')}</option>
            <option value="SECONDARY">{t('skills.secondary')}</option>
          </Select>
          <Select label={t('skills.proficiencyLevel')} {...regSkill('proficiencyLevel')}>
            <option value="BEGINNER">{t('skills.beginner')}</option>
            <option value="INTERMEDIATE">{t('skills.intermediate')}</option>
            <option value="ADVANCED">{t('skills.advanced')}</option>
            <option value="EXPERT">{t('skills.expert')}</option>
          </Select>
          <Input label={t('skills.yearsOfExperience')} type="number" {...regSkill('yearsOfExperience')} />
          <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={() => setSkillModal(false)}>{t('common.cancel')}</Button><Button type="submit" disabled={skillSubmitting}>{t('common.create')}</Button></div>
        </form>
      </Modal>

      {/* Add Cert Modal */}
      <Modal open={certModal} onClose={() => { setCertModal(false); resetCert(); }} title={`${t('common.add')} ${t('skills.certEntity')}`}>
        <form onSubmit={handleCert((d) => addCertMutation.mutate(d))} className="space-y-4">
          <Input label={t('skills.certName')}    {...regCert('certificationName',   { required: t('common.required') })} />
          <Input label={t('skills.issuingOrg')}  {...regCert('issuingOrganization', { required: t('common.required') })} />
          <Input label={t('skills.issueDate')}   type="date" {...regCert('issueDate')} />
          <Input label={t('skills.expiryDate')}  type="date" {...regCert('expiryDate')} />
          <Input label={t('skills.credentialId')} {...regCert('credentialId')} />
          <Input label={t('skills.credentialUrl')} {...regCert('credentialUrl')} />
          <div className="flex justify-end gap-3"><Button type="button" variant="secondary" onClick={() => setCertModal(false)}>{t('common.cancel')}</Button><Button type="submit" disabled={certSubmitting}>{t('common.create')}</Button></div>
        </form>
      </Modal>

      <ConfirmDialog open={!!confirmSkill} message={t('common.confirmDelete', { entity: t('skills.entity') })} onConfirm={() => removeSkillMutation.mutate(confirmSkill)} onCancel={() => setConfirmSkill(null)} />
      <ConfirmDialog open={!!confirmCert}  message={t('common.confirmDelete', { entity: t('skills.certEntity') })} onConfirm={() => removeCertMutation.mutate(confirmCert)} onCancel={() => setConfirmCert(null)} />
    </div>
  );
}
