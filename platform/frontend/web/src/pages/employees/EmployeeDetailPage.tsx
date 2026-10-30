import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, User, Briefcase, Phone, CreditCard, Award, Package, Clock, ArrowLeftRight, TrendingUp, UserCheck, UserX } from 'lucide-react';
import { employeeApi, onboardingApi, offboardingApi } from '../../../../shared/api/services';
import { QUERY_KEYS, EMPLOYMENT_STATUS_COLOR } from '../../../../shared/constants';
import { formatDate, extractData, extractPagedData } from '../../../../shared/utils';
import type { Employee, EmergencyContact, BankDetail, EmployeeSkill, EmployeeCertification, Asset, Probation, Transfer, Promotion, OnboardingTask, OffboardingTask } from '../../../../shared/types';
import { Spinner, Badge, Tabs, ProgressBar, PageHeader } from '../../components/ui';

const TABS = [
  { key: 'personal',    label: 'Personal',    icon: User },
  { key: 'employment',  label: 'Employment',  icon: Briefcase },
  { key: 'emergency',   label: 'Emergency',   icon: Phone },
  { key: 'bank',        label: 'Bank Details',icon: CreditCard },
  { key: 'skills',      label: 'Skills',      icon: Award },
  { key: 'assets',      label: 'Assets',      icon: Package },
  { key: 'probation',   label: 'Probation',   icon: Clock },
  { key: 'transfers',   label: 'Transfers',   icon: ArrowLeftRight },
  { key: 'promotions',  label: 'Promotions',  icon: TrendingUp },
  { key: 'onboarding',  label: 'Onboarding',  icon: UserCheck },
  { key: 'offboarding', label: 'Offboarding', icon: UserX },
];

export default function EmployeeDetailPage() {
  const { id }     = useParams<{ id: string }>();
  const navigate   = useNavigate();
  const { t }      = useTranslation();
  const [activeTab, setActiveTab] = useState('personal');

  const { data: empData, isLoading } = useQuery({
    queryKey: QUERY_KEYS.EMPLOYEE_BY_ID(id!),
    queryFn:  () => employeeApi.getById(id!),
    enabled:  !!id,
  });

  const { data: emergencyData } = useQuery({ queryKey: QUERY_KEYS.EMERGENCY_CONTACTS(id!), queryFn: () => employeeApi.getEmergencyContacts(id!), enabled: !!id });
  const { data: bankData }      = useQuery({ queryKey: QUERY_KEYS.BANK_DETAILS(id!),       queryFn: () => employeeApi.getBankDetails(id!),       enabled: !!id });
  const { data: skillsData }    = useQuery({ queryKey: QUERY_KEYS.SKILLS_BY_EMPLOYEE(id!), queryFn: () => employeeApi.getSkills(id!),            enabled: !!id });
  const { data: certsData }     = useQuery({ queryKey: QUERY_KEYS.CERTS_BY_EMPLOYEE(id!),  queryFn: () => employeeApi.getCertifications(id!),    enabled: !!id });
  const { data: assetsData }    = useQuery({ queryKey: QUERY_KEYS.ASSETS_BY_EMPLOYEE(id!), queryFn: () => employeeApi.getById(id!),              enabled: !!id });
  const { data: onboardData }   = useQuery({ queryKey: QUERY_KEYS.ONBOARDING_BY_EMPLOYEE(id!), queryFn: () => onboardingApi.getByEmployee(id!),  enabled: !!id });
  const { data: offboardData }  = useQuery({ queryKey: QUERY_KEYS.OFFBOARDING_BY_EMPLOYEE(id!), queryFn: () => offboardingApi.getByEmployee(id!), enabled: !!id });

  const employee      = extractData<Employee>(empData);
  const emergencyList = extractData<EmergencyContact[]>(emergencyData) ?? [];
  const bankList      = extractData<BankDetail[]>(bankData) ?? [];
  const skills        = extractData<EmployeeSkill[]>(skillsData) ?? [];
  const certs         = extractData<EmployeeCertification[]>(certsData) ?? [];
  const onboardTasks  = extractData<OnboardingTask[]>(onboardData) ?? [];
  const offboardTasks = extractData<OffboardingTask[]>(offboardData) ?? [];

  if (isLoading) return <div className="flex justify-center items-center h-64"><Spinner size="lg" /></div>;
  if (!employee) return <div className="text-center py-16 text-slate-400">Employee not found</div>;

  const completedOnboard = onboardTasks.filter((t) => t.status === 'COMPLETED').length;
  const completedOffboard = offboardTasks.filter((t) => t.status === 'COMPLETED').length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <button onClick={() => navigate('/employees')} className="flex items-center gap-2 text-sm text-slate-500 hover:text-indigo-600 mb-4">
          <ArrowLeft size={16} /> Back to Employees
        </button>
        <div className="flex items-start gap-6">
          {/* Photo */}
          <div className="w-20 h-20 rounded-full bg-indigo-100 flex items-center justify-center shrink-0 overflow-hidden">
            {employee.photoUrl
              ? <img src={employee.photoUrl} alt="photo" className="w-full h-full object-cover" />
              : <span className="text-2xl font-bold text-indigo-600">{employee.firstName[0]}{employee.lastName[0]}</span>
            }
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-3 flex-wrap">
              <h2 className="text-xl font-bold text-slate-800">{employee.firstName} {employee.lastName}</h2>
              <Badge label={employee.employeeCode} color="indigo" />
              <Badge label={employee.employmentStatus} color={EMPLOYMENT_STATUS_COLOR[employee.employmentStatus] as never ?? 'gray'} />
            </div>
            <div className="flex flex-wrap gap-4 mt-2 text-sm text-slate-500">
              <span>📧 {employee.email}</span>
              {employee.mobile && <span>📱 {employee.mobile}</span>}
              <span>📅 Joined {formatDate(employee.joiningDate)}</span>
              {employee.employmentType && <span>💼 {employee.employmentType.replace('_', ' ')}</span>}
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <Tabs tabs={TABS} active={activeTab} onChange={setActiveTab} />

        {/* Personal Tab */}
        {activeTab === 'personal' && (
          <div className="grid grid-cols-2 md:grid-cols-3 gap-6">
            {[
              { label: 'First Name',      value: employee.firstName },
              { label: 'Last Name',       value: employee.lastName },
              { label: 'Email',           value: employee.email },
              { label: 'Mobile',          value: employee.mobile },
              { label: 'Alternate Mobile',value: employee.alternateMobile },
              { label: 'Gender',          value: employee.gender },
              { label: 'Date of Birth',   value: formatDate(employee.dateOfBirth) },
              { label: 'Nationality',     value: employee.nationality },
              { label: 'Marital Status',  value: employee.maritalStatus },
              { label: 'Blood Group',     value: employee.bloodGroup },
            ].map((field) => (
              <div key={field.label}>
                <p className="text-xs text-slate-400 font-medium uppercase tracking-wide">{field.label}</p>
                <p className="text-sm text-slate-700 mt-0.5">{field.value || '—'}</p>
              </div>
            ))}
            <div className="col-span-2 md:col-span-3">
              <p className="text-xs text-slate-400 font-medium uppercase tracking-wide">Current Address</p>
              <p className="text-sm text-slate-700 mt-0.5">{employee.currentAddress || '—'}</p>
            </div>
            <div className="col-span-2 md:col-span-3">
              <p className="text-xs text-slate-400 font-medium uppercase tracking-wide">Permanent Address</p>
              <p className="text-sm text-slate-700 mt-0.5">{employee.permanentAddress || '—'}</p>
            </div>
          </div>
        )}

        {/* Employment Tab */}
        {activeTab === 'employment' && (
          <div className="grid grid-cols-2 md:grid-cols-3 gap-6">
            {[
              { label: 'Employee Code',    value: employee.employeeCode },
              { label: 'Joining Date',     value: formatDate(employee.joiningDate) },
              { label: 'Employment Status',value: employee.employmentStatus },
              { label: 'Employment Type',  value: employee.employmentType },
              { label: 'Work Location',    value: employee.workLocation },
              { label: 'Department ID',    value: employee.departmentId },
              { label: 'Designation ID',   value: employee.designationId },
              { label: 'Branch ID',        value: employee.branchId },
              { label: 'Team ID',          value: employee.teamId },
              { label: 'Manager ID',       value: employee.managerId },
            ].map((field) => (
              <div key={field.label}>
                <p className="text-xs text-slate-400 font-medium uppercase tracking-wide">{field.label}</p>
                <p className="text-sm text-slate-700 mt-0.5">{field.value || '—'}</p>
              </div>
            ))}
          </div>
        )}

        {/* Emergency Contacts Tab */}
        {activeTab === 'emergency' && (
          <div className="space-y-4">
            {emergencyList.length === 0
              ? <p className="text-slate-400 text-sm">No emergency contacts added</p>
              : emergencyList.map((c) => (
                <div key={c.id} className="border border-slate-200 rounded-lg p-4">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="font-medium text-slate-800">{c.name}</span>
                    <Badge label={c.relationship} color="blue" />
                    {c.isPrimary && <Badge label="Primary" color="green" />}
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-sm text-slate-600">
                    <span>📱 {c.phone}</span>
                    {c.alternatePhone && <span>📱 {c.alternatePhone}</span>}
                    {c.email && <span>📧 {c.email}</span>}
                  </div>
                </div>
              ))
            }
          </div>
        )}

        {/* Bank Details Tab */}
        {activeTab === 'bank' && (
          <div className="space-y-4">
            {bankList.length === 0
              ? <p className="text-slate-400 text-sm">No bank details added</p>
              : bankList.map((b) => (
                <div key={b.id} className="border border-slate-200 rounded-lg p-4">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="font-medium text-slate-800">{b.bankName}</span>
                    {b.isPrimary && <Badge label="Primary" color="green" />}
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-sm text-slate-600">
                    <span>Account: {b.accountNumber}</span>
                    {b.iban && <span>IBAN: {b.iban}</span>}
                    <span>Currency: {b.currency}</span>
                    {b.swiftCode && <span>SWIFT: {b.swiftCode}</span>}
                  </div>
                </div>
              ))
            }
          </div>
        )}

        {/* Skills Tab */}
        {activeTab === 'skills' && (
          <div className="space-y-6">
            <div>
              <h4 className="text-sm font-semibold text-slate-700 mb-3">Skills</h4>
              {skills.length === 0
                ? <p className="text-slate-400 text-sm">No skills added</p>
                : <div className="flex flex-wrap gap-2">
                    {skills.map((s) => (
                      <div key={s.id} className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-lg px-3 py-2">
                        <span className="text-sm font-medium text-slate-700">{s.skillName}</span>
                        {s.proficiency && <Badge label={s.proficiency} color="indigo" />}
                        {s.yearsOfExperience && <span className="text-xs text-slate-400">{s.yearsOfExperience}y</span>}
                      </div>
                    ))}
                  </div>
              }
            </div>
            <div>
              <h4 className="text-sm font-semibold text-slate-700 mb-3">Certifications</h4>
              {certs.length === 0
                ? <p className="text-slate-400 text-sm">No certifications added</p>
                : <div className="space-y-2">
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
                        </div>
                      </div>
                    ))}
                  </div>
              }
            </div>
          </div>
        )}

        {/* Onboarding Tab */}
        {activeTab === 'onboarding' && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-sm text-slate-600">Progress: {completedOnboard}/{onboardTasks.length} tasks</span>
              <ProgressBar value={completedOnboard} max={onboardTasks.length || 1} />
            </div>
            {onboardTasks.map((task) => (
              <div key={task.id} className="flex items-center justify-between border border-slate-200 rounded-lg px-4 py-3">
                <div>
                  <p className="text-sm font-medium text-slate-700">{task.taskName}</p>
                  {task.dueDate && <p className="text-xs text-slate-400">Due: {formatDate(task.dueDate)}</p>}
                </div>
                <div className="flex items-center gap-2">
                  {task.overdue && <Badge label="Overdue" color="red" />}
                  <Badge label={task.status} color={task.status === 'COMPLETED' ? 'green' : task.status === 'IN_PROGRESS' ? 'blue' : 'yellow'} />
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Offboarding Tab */}
        {activeTab === 'offboarding' && (
          <div className="space-y-4">
            {offboardTasks.length === 0
              ? <p className="text-slate-400 text-sm">No offboarding initiated</p>
              : <>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-slate-600">Progress: {completedOffboard}/{offboardTasks.length} tasks</span>
                    <ProgressBar value={completedOffboard} max={offboardTasks.length || 1} />
                  </div>
                  {offboardTasks.map((task) => (
                    <div key={task.id} className="flex items-center justify-between border border-slate-200 rounded-lg px-4 py-3">
                      <div>
                        <p className="text-sm font-medium text-slate-700">{task.taskName}</p>
                        {task.dueDate && <p className="text-xs text-slate-400">Due: {formatDate(task.dueDate)}</p>}
                      </div>
                      <Badge label={task.status} color={task.status === 'COMPLETED' ? 'green' : task.status === 'IN_PROGRESS' ? 'blue' : 'yellow'} />
                    </div>
                  ))}
                </>
            }
          </div>
        )}

        {/* Placeholder tabs */}
        {['assets', 'probation', 'transfers', 'promotions'].includes(activeTab) && (
          <p className="text-slate-400 text-sm text-center py-8">
            {activeTab.charAt(0).toUpperCase() + activeTab.slice(1)} history — navigate to the {activeTab} page to manage
          </p>
        )}
      </div>
    </div>
  );
}
