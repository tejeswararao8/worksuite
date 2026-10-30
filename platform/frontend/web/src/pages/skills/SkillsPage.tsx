import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import toast from 'react-hot-toast';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Search, X } from 'lucide-react';
import { employeeApi } from '../../../../shared/api/services';
import { QUERY_KEYS } from '../../../../shared/constants';
import { formatDate, extractData } from '../../../../shared/utils';
import type { EmployeeSkill, EmployeeCertification } from '../../../../shared/types';
import { Button, Input, Select, Modal, Badge, Spinner, PageHeader, Tabs } from '../../components/ui';

export default function SkillsPage() {
  const { t }  = useTranslation();
  const qc     = useQueryClient();
  const [tab, setTab]           = useState('skills');
  const [empId, setEmpId]       = useState('');
  const [addSkillOpen, setAddSkillOpen]   = useState(false);
  const [addCertOpen, setAddCertOpen]     = useState(false);

  const { data: skillsData, isLoading: skillsLoading } = useQuery({
    queryKey: QUERY_KEYS.SKILLS_BY_EMPLOYEE(empId),
    queryFn:  () => employeeApi.getSkills(empId),
    enabled:  !!empId,
  });

  const { data: certsData, isLoading: certsLoading } = useQuery({
    queryKey: QUERY_KEYS.CERTS_BY_EMPLOYEE(empId),
    queryFn:  () => employeeApi.getCertifications(empId),
    enabled:  !!empId,
  });

  const skills = extractData<EmployeeSkill[]>(skillsData) ?? [];
  const certs  = extractData<EmployeeCertification[]>(certsData) ?? [];

  const { register: regSkill, handleSubmit: handleSkill, reset: resetSkill, formState: { isSubmitting: skillSubmitting } } = useForm();
  const { register: regCert,  handleSubmit: handleCert,  reset: resetCert,  formState: { isSubmitting: certSubmitting  } } = useForm();

  const addSkillMutation = useMutation({
    mutationFn: (data: unknown) => employeeApi.addSkill(empId, data),
    onSuccess:  () => { qc.invalidateQueries({ queryKey: QUERY_KEYS.SKILLS_BY_EMPLOYEE(empId) }); toast.success('Skill added'); setAddSkillOpen(false); resetSkill(); },
    onError:    () => toast.error('Failed to add skill'),
  });

  const deleteSkillMutation = useMutation({
    mutationFn: (skillId: string) => employeeApi.deleteSkill(empId, skillId),
    onSuccess:  () => { qc.invalidateQueries({ queryKey: QUERY_KEYS.SKILLS_BY_EMPLOYEE(empId) }); toast.success('Skill removed'); },
  });

  const addCertMutation = useMutation({
    mutationFn: (data: unknown) => employeeApi.addCertification(empId, data),
    onSuccess:  () => { qc.invalidateQueries({ queryKey: QUERY_KEYS.CERTS_BY_EMPLOYEE(empId) }); toast.success('Certification added'); setAddCertOpen(false); resetCert(); },
    onError:    () => toast.error('Failed to add certification'),
  });

  const deleteCertMutation = useMutation({
    mutationFn: (certId: string) => employeeApi.deleteCertification(empId, certId),
    onSuccess:  () => { qc.invalidateQueries({ queryKey: QUERY_KEYS.CERTS_BY_EMPLOYEE(empId) }); toast.success('Certification removed'); },
  });

  const PROFICIENCY_COLOR: Record<string, string> = { BEGINNER: 'gray', INTERMEDIATE: 'blue', ADVANCED: 'indigo', EXPERT: 'purple' };

  return (
    <div className="space-y-4">
      <PageHeader title={t('nav.skills')} subtitle="Employee skills and certifications" />

      {/* Employee Search */}
      <div className="flex gap-3 bg-white p-4 rounded-xl border border-slate-200">
        <div className="relative">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input className="pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 w-64" placeholder="Enter Employee ID..." value={empId} onChange={(e) => setEmpId(e.target.value)} />
        </div>
      </div>

      {empId && (
        <div className="bg-white rounded-xl border border-slate-200 p-6">
          <Tabs
            tabs={[{ key: 'skills', label: 'Skills' }, { key: 'certifications', label: 'Certifications' }]}
            active={tab}
            onChange={setTab}
          />

          {tab === 'skills' && (
            <div className="space-y-4">
              <div className="flex justify-end">
                <Button size="sm" onClick={() => setAddSkillOpen(true)}><Plus size={14} />Add Skill</Button>
              </div>
              {skillsLoading ? <Spinner /> : (
                <div className="flex flex-wrap gap-2">
                  {skills.map((s) => (
                    <div key={s.id} className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-lg px-3 py-2">
                      <span className="text-sm font-medium text-slate-700">{s.skillName}</span>
                      {s.proficiency && <Badge label={s.proficiency} color={PROFICIENCY_COLOR[s.proficiency] as never ?? 'gray'} />}
                      {s.yearsOfExperience && <span className="text-xs text-slate-400">{s.yearsOfExperience}y exp</span>}
                      <button onClick={() => deleteSkillMutation.mutate(s.id)} className="text-slate-300 hover:text-red-400 ml-1"><X size={12} /></button>
                    </div>
                  ))}
                  {skills.length === 0 && <p className="text-slate-400 text-sm">No skills added yet</p>}
                </div>
              )}
            </div>
          )}

          {tab === 'certifications' && (
            <div className="space-y-4">
              <div className="flex justify-end">
                <Button size="sm" onClick={() => setAddCertOpen(true)}><Plus size={14} />Add Certification</Button>
              </div>
              {certsLoading ? <Spinner /> : (
                <div className="space-y-2">
                  {certs.map((c) => (
                    <div key={c.id} className="flex items-center justify-between border border-slate-200 rounded-lg px-4 py-3">
                      <div>
                        <p className="text-sm font-medium text-slate-700">{c.name}</p>
                        {c.issuingOrg && <p className="text-xs text-slate-400">{c.issuingOrg}</p>}
                      </div>
                      <div className="flex items-center gap-2">
                        {c.expiryDate && <span className="text-xs text-slate-500">Expires {formatDate(c.expiryDate)}</span>}
                        {c.expired && <Badge label="Expired" color="red" />}
                        {c.expiringSoon && !c.expired && <Badge label="Expiring Soon" color="yellow" />}
                        <button onClick={() => deleteCertMutation.mutate(c.id)} className="text-slate-300 hover:text-red-400"><X size={14} /></button>
                      </div>
                    </div>
                  ))}
                  {certs.length === 0 && <p className="text-slate-400 text-sm">No certifications added yet</p>}
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Add Skill Modal */}
      <Modal open={addSkillOpen} onClose={() => { setAddSkillOpen(false); resetSkill(); }} title="Add Skill">
        <form onSubmit={handleSkill((d) => addSkillMutation.mutate(d))} className="space-y-4">
          <Input label="Skill Name" {...regSkill('skillName', { required: true })} />
          <Input label="Skill Type (e.g. Technical, Soft)" {...regSkill('skillType')} />
          <Select label="Proficiency" {...regSkill('proficiency')}>
            <option value="">Select</option>
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="ADVANCED">Advanced</option>
            <option value="EXPERT">Expert</option>
          </Select>
          <Input label="Years of Experience" type="number" {...regSkill('yearsOfExperience', { valueAsNumber: true })} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setAddSkillOpen(false)}>Cancel</Button>
            <Button type="submit" loading={skillSubmitting}>Add</Button>
          </div>
        </form>
      </Modal>

      {/* Add Certification Modal */}
      <Modal open={addCertOpen} onClose={() => { setAddCertOpen(false); resetCert(); }} title="Add Certification">
        <form onSubmit={handleCert((d) => addCertMutation.mutate(d))} className="space-y-4">
          <Input label="Certification Name" {...regCert('name', { required: true })} />
          <Input label="Issuing Organization" {...regCert('issuingOrg')} />
          <div className="grid grid-cols-2 gap-4">
            <Input label="Issue Date" type="date" {...regCert('issueDate')} />
            <Input label="Expiry Date" type="date" {...regCert('expiryDate')} />
          </div>
          <Input label="Credential URL" type="url" {...regCert('credentialUrl')} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setAddCertOpen(false)}>Cancel</Button>
            <Button type="submit" loading={certSubmitting}>Add</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
