import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  employeeApi, branchApi, departmentApi, designationApi, teamApi,
  assetApi, transferApi, promotionApi, probationApi,
  onboardingApi, offboardingApi, dashboardApi, notificationApi,
} from '../api/services';
import { QUERY_KEYS, DEFAULT_PAGE_SIZE } from '../constants';
import { extractPagedData, extractData, getErrorMessage } from '../utils';
import type {
  Employee, Branch, Department, Designation, Team, Asset,
  Transfer, Promotion, Probation, OnboardingTask, OffboardingTask,
  DashboardStats, Notification, BranchSummary, DepartmentSummary,
  DesignationSummary, TeamSummary, OrgNode, TeamMember,
  OnboardingProgress,
} from '../types';

// ─── Toast abstraction — web uses react-hot-toast, mobile uses Alert ──────────
// Each platform provides its own toast implementation via this interface
export interface ToastService {
  success: (msg: string) => void;
  error:   (msg: string) => void;
}

// ─── Dashboard ────────────────────────────────────────────────────────────────
export function useDashboard() {
  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.DASHBOARD_STATS,
    queryFn:  dashboardApi.getStats,
    staleTime: 60_000,
  });
  const stats = extractData<DashboardStats>(data) ?? {} as DashboardStats;
  return { stats, isLoading };
}

// ─── Employees ────────────────────────────────────────────────────────────────
export function useEmployees(toast?: ToastService) {
  const qc = useQueryClient();
  const [page, setPage]           = useState(0);
  const [search, setSearch]       = useState('');
  const [deptFilter, setDeptFilter] = useState('');
  const [branchFilter, setBranchFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.EMPLOYEES(page, search, deptFilter, branchFilter, statusFilter),
    queryFn:  () => employeeApi.search({
      q:            search   || undefined,
      departmentId: deptFilter || undefined,
      branchId:     branchFilter || undefined,
      status:       statusFilter || undefined,
      page,
      size: DEFAULT_PAGE_SIZE,
    }),
  });

  const { content: employees, totalPages } = extractPagedData<Employee>(data);
  const invalidate = () => qc.invalidateQueries({ queryKey: ['employees'] });

  const createMutation = useMutation({
    mutationFn: employeeApi.create,
    onSuccess:  () => { invalidate(); toast?.success('Employee created'); },
    onError:    (e) => toast?.error(getErrorMessage(e)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: unknown }) => employeeApi.update(id, data as never),
    onSuccess:  () => { invalidate(); toast?.success('Employee updated'); },
    onError:    (e) => toast?.error(getErrorMessage(e)),
  });

  const deleteMutation = useMutation({
    mutationFn: employeeApi.delete,
    onSuccess:  () => { invalidate(); toast?.success('Employee deleted'); },
    onError:    (e) => toast?.error(getErrorMessage(e)),
  });

  return {
    employees, totalPages, isLoading,
    page, setPage,
    search, setSearch: (v: string) => { setSearch(v); setPage(0); },
    deptFilter, setDeptFilter: (v: string) => { setDeptFilter(v); setPage(0); },
    branchFilter, setBranchFilter: (v: string) => { setBranchFilter(v); setPage(0); },
    statusFilter, setStatusFilter: (v: string) => { setStatusFilter(v); setPage(0); },
    createMutation, updateMutation, deleteMutation,
  };
}

export function useEmployee(id: string) {
  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.EMPLOYEE_BY_ID(id),
    queryFn:  () => employeeApi.getById(id),
    enabled:  !!id,
  });
  return { employee: extractData<Employee>(data), isLoading };
}

// ─── Reference Data (dropdowns) ───────────────────────────────────────────────
export function useReferenceData() {
  const { data: deptData }  = useQuery({ queryKey: QUERY_KEYS.DEPARTMENTS_ALL, queryFn: departmentApi.getAllList, staleTime: 5 * 60_000 });
  const { data: branchData }= useQuery({ queryKey: QUERY_KEYS.BRANCHES_ALL,    queryFn: branchApi.getAllList,    staleTime: 5 * 60_000 });
  const { data: desigData } = useQuery({ queryKey: QUERY_KEYS.DESIGNATIONS_ALL,queryFn: designationApi.getAllList,staleTime: 5 * 60_000 });
  const { data: teamData }  = useQuery({ queryKey: QUERY_KEYS.TEAMS_ALL,       queryFn: teamApi.getAllList,      staleTime: 5 * 60_000 });

  const extract = <T>(d: unknown): T[] => {
    const r = d as { data?: { data?: T[] } };
    return r?.data?.data || [];
  };

  return {
    departments:  extract<DepartmentSummary>(deptData),
    branches:     extract<BranchSummary>(branchData),
    designations: extract<DesignationSummary>(desigData),
    teams:        extract<TeamSummary>(teamData),
  };
}

// ─── Branches ─────────────────────────────────────────────────────────────────
export function useBranches(toast?: ToastService) {
  const qc = useQueryClient();
  const [page, setPage]   = useState(0);
  const [search, setSearch] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.BRANCHES(page, search),
    queryFn:  () => branchApi.getAll({ q: search || undefined, page, size: DEFAULT_PAGE_SIZE }),
  });

  const { content: branches, totalPages } = extractPagedData<Branch>(data);
  const invalidate = () => qc.invalidateQueries({ queryKey: ['branches'] });

  const createMutation = useMutation({ mutationFn: branchApi.create,                                    onSuccess: () => { invalidate(); toast?.success('Branch created'); },  onError: (e) => toast?.error(getErrorMessage(e)) });
  const updateMutation = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => branchApi.update(id, data), onSuccess: () => { invalidate(); toast?.success('Branch updated'); },  onError: (e) => toast?.error(getErrorMessage(e)) });
  const deleteMutation = useMutation({ mutationFn: branchApi.delete,                                    onSuccess: () => { invalidate(); toast?.success('Branch deactivated'); }, onError: (e) => toast?.error(getErrorMessage(e)) });

  return { branches, totalPages, isLoading, page, setPage, search, setSearch: (v: string) => { setSearch(v); setPage(0); }, createMutation, updateMutation, deleteMutation };
}

// ─── Departments ──────────────────────────────────────────────────────────────
export function useDepartments(toast?: ToastService) {
  const qc = useQueryClient();
  const [page, setPage]   = useState(0);
  const [search, setSearch] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.DEPARTMENTS(page, search),
    queryFn:  () => departmentApi.getAll({ q: search || undefined, page, size: DEFAULT_PAGE_SIZE }),
  });

  const { data: orgData } = useQuery({ queryKey: QUERY_KEYS.ORG_CHART, queryFn: departmentApi.getOrgChart });

  const { content: departments, totalPages } = extractPagedData<Department>(data);
  const orgChart = extractData<OrgNode[]>(orgData) ?? [];
  const invalidate = () => qc.invalidateQueries({ queryKey: ['departments'] });

  const createMutation = useMutation({ mutationFn: departmentApi.create,                                       onSuccess: () => { invalidate(); toast?.success('Department created'); },    onError: (e) => toast?.error(getErrorMessage(e)) });
  const updateMutation = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => departmentApi.update(id, data), onSuccess: () => { invalidate(); toast?.success('Department updated'); },    onError: (e) => toast?.error(getErrorMessage(e)) });
  const deleteMutation = useMutation({ mutationFn: departmentApi.delete,                                       onSuccess: () => { invalidate(); toast?.success('Department deactivated'); }, onError: (e) => toast?.error(getErrorMessage(e)) });

  return { departments, totalPages, isLoading, orgChart, page, setPage, search, setSearch: (v: string) => { setSearch(v); setPage(0); }, createMutation, updateMutation, deleteMutation };
}

// ─── Designations ─────────────────────────────────────────────────────────────
export function useDesignations(toast?: ToastService) {
  const qc = useQueryClient();
  const [page, setPage]   = useState(0);
  const [search, setSearch] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.DESIGNATIONS(page, search),
    queryFn:  () => designationApi.getAll({ q: search || undefined, page, size: DEFAULT_PAGE_SIZE }),
  });

  const { content: designations, totalPages } = extractPagedData<Designation>(data);
  const invalidate = () => qc.invalidateQueries({ queryKey: ['designations'] });

  const createMutation = useMutation({ mutationFn: designationApi.create,                                        onSuccess: () => { invalidate(); toast?.success('Designation created'); },    onError: (e) => toast?.error(getErrorMessage(e)) });
  const updateMutation = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => designationApi.update(id, data), onSuccess: () => { invalidate(); toast?.success('Designation updated'); },    onError: (e) => toast?.error(getErrorMessage(e)) });
  const deleteMutation = useMutation({ mutationFn: designationApi.delete,                                        onSuccess: () => { invalidate(); toast?.success('Designation deactivated'); }, onError: (e) => toast?.error(getErrorMessage(e)) });

  return { designations, totalPages, isLoading, page, setPage, search, setSearch: (v: string) => { setSearch(v); setPage(0); }, createMutation, updateMutation, deleteMutation };
}

// ─── Teams ────────────────────────────────────────────────────────────────────
export function useTeams(toast?: ToastService) {
  const qc = useQueryClient();
  const [page, setPage]         = useState(0);
  const [search, setSearch]     = useState('');
  const [deptFilter, setDeptFilter] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.TEAMS(page, search, deptFilter),
    queryFn:  () => teamApi.getAll({ q: search || undefined, departmentId: deptFilter || undefined, page, size: DEFAULT_PAGE_SIZE }),
  });

  const { content: teams, totalPages } = extractPagedData<Team>(data);
  const invalidate = () => qc.invalidateQueries({ queryKey: ['teams'] });

  const createMutation    = useMutation({ mutationFn: teamApi.create,                                     onSuccess: () => { invalidate(); toast?.success('Team created'); },    onError: (e) => toast?.error(getErrorMessage(e)) });
  const updateMutation    = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => teamApi.update(id, data), onSuccess: () => { invalidate(); toast?.success('Team updated'); },    onError: (e) => toast?.error(getErrorMessage(e)) });
  const deleteMutation    = useMutation({ mutationFn: teamApi.delete,                                     onSuccess: () => { invalidate(); toast?.success('Team deactivated'); }, onError: (e) => toast?.error(getErrorMessage(e)) });
  const addMemberMutation = useMutation({ mutationFn: ({ id, empId }: { id: string; empId: string }) => teamApi.addMember(id, empId), onSuccess: () => { invalidate(); toast?.success('Member added'); }, onError: (e) => toast?.error(getErrorMessage(e)) });
  const removeMemberMutation = useMutation({ mutationFn: ({ id, empId }: { id: string; empId: string }) => teamApi.removeMember(id, empId), onSuccess: () => { invalidate(); toast?.success('Member removed'); }, onError: (e) => toast?.error(getErrorMessage(e)) });

  return { teams, totalPages, isLoading, page, setPage, search, setSearch: (v: string) => { setSearch(v); setPage(0); }, deptFilter, setDeptFilter: (v: string) => { setDeptFilter(v); setPage(0); }, createMutation, updateMutation, deleteMutation, addMemberMutation, removeMemberMutation };
}

export function useTeamMembers(teamId: string) {
  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.TEAM_MEMBERS(teamId),
    queryFn:  () => teamApi.getMembers(teamId),
    enabled:  !!teamId,
  });
  const members = extractData<TeamMember[]>(data) ?? [];
  return { members, isLoading };
}

// ─── Assets ───────────────────────────────────────────────────────────────────
export function useAssets(toast?: ToastService) {
  const qc = useQueryClient();
  const [page, setPage]           = useState(0);
  const [search, setSearch]       = useState('');
  const [statusFilter, setStatus] = useState('');
  const [typeFilter, setType]     = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.ASSETS(page, search, statusFilter, typeFilter),
    queryFn:  () => assetApi.getAll({ q: search || undefined, status: statusFilter || undefined, assetType: typeFilter || undefined, page, size: DEFAULT_PAGE_SIZE }),
  });

  const { content: assets, totalPages } = extractPagedData<Asset>(data);
  const invalidate = () => qc.invalidateQueries({ queryKey: ['assets'] });

  const createMutation = useMutation({ mutationFn: assetApi.create,                                    onSuccess: () => { invalidate(); toast?.success('Asset created'); },   onError: (e) => toast?.error(getErrorMessage(e)) });
  const updateMutation = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => assetApi.update(id, data), onSuccess: () => { invalidate(); toast?.success('Asset updated'); },   onError: (e) => toast?.error(getErrorMessage(e)) });
  const assignMutation = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => assetApi.assign(id, data), onSuccess: () => { invalidate(); toast?.success('Asset assigned'); },  onError: (e) => toast?.error(getErrorMessage(e)) });
  const returnMutation = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => assetApi.return(id, data), onSuccess: () => { invalidate(); toast?.success('Asset returned'); },  onError: (e) => toast?.error(getErrorMessage(e)) });
  const retireMutation = useMutation({ mutationFn: assetApi.retire,                                    onSuccess: () => { invalidate(); toast?.success('Asset retired'); },   onError: (e) => toast?.error(getErrorMessage(e)) });

  return { assets, totalPages, isLoading, page, setPage, search, setSearch: (v: string) => { setSearch(v); setPage(0); }, statusFilter, setStatus: (v: string) => { setStatus(v); setPage(0); }, typeFilter, setType: (v: string) => { setType(v); setPage(0); }, createMutation, updateMutation, assignMutation, returnMutation, retireMutation };
}

// ─── Transfers ────────────────────────────────────────────────────────────────
export function useTransfers(toast?: ToastService) {
  const qc = useQueryClient();
  const [page, setPage]           = useState(0);
  const [statusFilter, setStatus] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.TRANSFERS(page, statusFilter),
    queryFn:  () => transferApi.getAll({ status: statusFilter || undefined, page, size: DEFAULT_PAGE_SIZE }),
  });

  const { content: transfers, totalPages } = extractPagedData<Transfer>(data);
  const invalidate = () => qc.invalidateQueries({ queryKey: ['transfers'] });

  const createMutation  = useMutation({ mutationFn: transferApi.create,                                       onSuccess: () => { invalidate(); toast?.success('Transfer request created'); }, onError: (e) => toast?.error(getErrorMessage(e)) });
  const approveMutation = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => transferApi.approve(id, data), onSuccess: () => { invalidate(); toast?.success('Transfer approved'); },         onError: (e) => toast?.error(getErrorMessage(e)) });
  const rejectMutation  = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => transferApi.reject(id, data),  onSuccess: () => { invalidate(); toast?.success('Transfer rejected'); },         onError: (e) => toast?.error(getErrorMessage(e)) });

  return { transfers, totalPages, isLoading, page, setPage, statusFilter, setStatus: (v: string) => { setStatus(v); setPage(0); }, createMutation, approveMutation, rejectMutation };
}

// ─── Promotions ───────────────────────────────────────────────────────────────
export function usePromotions(toast?: ToastService) {
  const qc = useQueryClient();
  const [page, setPage]           = useState(0);
  const [statusFilter, setStatus] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.PROMOTIONS(page, statusFilter),
    queryFn:  () => promotionApi.getAll({ status: statusFilter || undefined, page, size: DEFAULT_PAGE_SIZE }),
  });

  const { content: promotions, totalPages } = extractPagedData<Promotion>(data);
  const invalidate = () => qc.invalidateQueries({ queryKey: ['promotions'] });

  const createMutation  = useMutation({ mutationFn: promotionApi.create,                                        onSuccess: () => { invalidate(); toast?.success('Promotion request created'); }, onError: (e) => toast?.error(getErrorMessage(e)) });
  const approveMutation = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => promotionApi.approve(id, data), onSuccess: () => { invalidate(); toast?.success('Promotion approved'); },          onError: (e) => toast?.error(getErrorMessage(e)) });
  const rejectMutation  = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => promotionApi.reject(id, data),  onSuccess: () => { invalidate(); toast?.success('Promotion rejected'); },          onError: (e) => toast?.error(getErrorMessage(e)) });

  return { promotions, totalPages, isLoading, page, setPage, statusFilter, setStatus: (v: string) => { setStatus(v); setPage(0); }, createMutation, approveMutation, rejectMutation };
}

// ─── Probation ────────────────────────────────────────────────────────────────
export function useProbations(toast?: ToastService) {
  const qc = useQueryClient();
  const [page, setPage]           = useState(0);
  const [statusFilter, setStatus] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.PROBATIONS(page, statusFilter),
    queryFn:  () => probationApi.getAll({ status: statusFilter || undefined, page, size: DEFAULT_PAGE_SIZE }),
  });

  const { content: probations, totalPages } = extractPagedData<Probation>(data);
  const invalidate = () => qc.invalidateQueries({ queryKey: ['probations'] });

  const confirmMutation = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => probationApi.confirm(id, data), onSuccess: () => { invalidate(); toast?.success('Probation confirmed'); }, onError: (e) => toast?.error(getErrorMessage(e)) });
  const extendMutation  = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => probationApi.extend(id, data),  onSuccess: () => { invalidate(); toast?.success('Probation extended'); },  onError: (e) => toast?.error(getErrorMessage(e)) });
  const rejectMutation  = useMutation({ mutationFn: ({ id, data }: { id: string; data: unknown }) => probationApi.reject(id, data),  onSuccess: () => { invalidate(); toast?.success('Probation rejected'); },  onError: (e) => toast?.error(getErrorMessage(e)) });

  return { probations, totalPages, isLoading, page, setPage, statusFilter, setStatus: (v: string) => { setStatus(v); setPage(0); }, confirmMutation, extendMutation, rejectMutation };
}

// ─── Onboarding ───────────────────────────────────────────────────────────────
export function useOnboarding(employeeId: string) {
  const qc = useQueryClient();
  const invalidate = () => qc.invalidateQueries({ queryKey: ['onboarding'] });

  const { data: tasksData, isLoading } = useQuery({
    queryKey: QUERY_KEYS.ONBOARDING_BY_EMPLOYEE(employeeId),
    queryFn:  () => onboardingApi.getByEmployee(employeeId),
    enabled:  !!employeeId,
  });

  const { data: progressData } = useQuery({
    queryKey: QUERY_KEYS.ONBOARDING_PROGRESS(employeeId),
    queryFn:  () => onboardingApi.getProgress(employeeId),
    enabled:  !!employeeId,
  });

  const tasks    = extractData<OnboardingTask[]>(tasksData) ?? [];
  const progress = extractData<OnboardingProgress>(progressData);

  const completeMutation = useMutation({ mutationFn: onboardingApi.completeTask, onSuccess: invalidate });
  const addTaskMutation  = useMutation({ mutationFn: ({ empId, data }: { empId: string; data: unknown }) => onboardingApi.addTask(empId, data), onSuccess: invalidate });

  return { tasks, progress, isLoading, completeMutation, addTaskMutation };
}

export function useMyOnboardingTasks() {
  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.MY_ONBOARDING_TASKS,
    queryFn:  onboardingApi.getMyTasks,
  });
  const tasks = extractData<OnboardingTask[]>(data) ?? [];
  return { tasks, isLoading };
}

// ─── Offboarding ──────────────────────────────────────────────────────────────
export function useOffboarding(employeeId: string) {
  const qc = useQueryClient();
  const invalidate = () => qc.invalidateQueries({ queryKey: ['offboarding'] });

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.OFFBOARDING_BY_EMPLOYEE(employeeId),
    queryFn:  () => offboardingApi.getByEmployee(employeeId),
    enabled:  !!employeeId,
  });

  const tasks = extractData<OffboardingTask[]>(data) ?? [];
  const completeMutation = useMutation({ mutationFn: offboardingApi.completeTask, onSuccess: invalidate });

  return { tasks, isLoading, completeMutation };
}

// ─── Notifications ────────────────────────────────────────────────────────────
export function useNotifications(toast?: ToastService) {
  const qc = useQueryClient();
  const [page] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.NOTIFICATIONS(page),
    queryFn:  () => notificationApi.getAll({ page, size: 50 }),
  });

  const { data: countData } = useQuery({
    queryKey:       QUERY_KEYS.UNREAD_COUNT,
    queryFn:        notificationApi.getUnreadCount,
    refetchInterval: 30_000,
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['notifications'] });
    qc.invalidateQueries({ queryKey: QUERY_KEYS.UNREAD_COUNT });
  };

  const { content: notifications } = extractPagedData<Notification>(data);
  const unreadCount = (extractData<{ count: number }>(countData))?.count ?? 0;

  const markReadMutation    = useMutation({ mutationFn: notificationApi.markRead,    onSuccess: invalidate, onError: (e) => toast?.error(getErrorMessage(e)) });
  const markAllReadMutation = useMutation({ mutationFn: notificationApi.markAllRead, onSuccess: () => { invalidate(); toast?.success('All notifications marked as read'); }, onError: (e) => toast?.error(getErrorMessage(e)) });

  return { notifications, isLoading, unreadCount, markReadMutation, markAllReadMutation };
}
