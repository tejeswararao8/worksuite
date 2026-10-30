# Frontend — 3-Layer Architecture

```
frontend/
├── shared/     ← Written ONCE — used by BOTH web and mobile
├── web/        ← React.js browser app (Tailwind CSS)
└── mobile/     ← React Native iOS + Android app (NativeWind)
```

## shared/ — The Brain (80% of all code)

Everything that is NOT UI rendering lives here:

| Folder | Contents |
|---|---|
| `api/axios.ts` | Axios instance with JWT interceptors + token refresh |
| `api/services.ts` | Every API call for every module |
| `store/` | Redux slices: auth, notification |
| `hooks/index.ts` | All business logic hooks: useEmployees, useDashboard, useTransfers, etc. |
| `types/index.ts` | All TypeScript interfaces for every entity |
| `constants/index.ts` | Enums, query keys, API route constants |
| `utils/index.ts` | formatDate, getErrorMessage, storageService, downloadBlob |

## web/ — React.js Browser App

```
web/src/
├── App.tsx              ← Root with all providers + lazy routes
├── contexts/AuthContext.tsx
├── layouts/             ← Sidebar, Navbar, AppLayout
├── components/ui/       ← Button, Input, Table, Modal, Badge, etc.
└── pages/               ← One folder per page
    ├── auth/LoginPage.tsx
    ├── dashboard/DashboardPage.tsx
    ├── employees/EmployeesPage.tsx
    ├── employees/EmployeeDetailPage.tsx  ← tabbed detail view
    ├── departments/DepartmentsPage.tsx   ← with org chart
    ├── branches/BranchesPage.tsx
    ├── designations/DesignationsPage.tsx
    ├── teams/TeamsPage.tsx               ← with member panel
    ├── transfers/TransfersPage.tsx       ← approve/reject
    ├── promotions/PromotionsPage.tsx     ← approve/reject
    ├── probation/ProbationPage.tsx       ← confirm/extend/reject
    ├── onboarding/OnboardingPage.tsx     ← my tasks + by employee
    ├── offboarding/OffboardingPage.tsx   ← initiate + tasks
    ├── assets/AssetsPage.tsx             ← assign/return/retire
    ├── skills/SkillsPage.tsx             ← skills + certifications
    ├── reports/ReportsPage.tsx           ← 6 reports, 3 formats
    ├── notifications/NotificationsPage.tsx
    ├── audit/AuditPage.tsx               ← with JSON diff expand
    ├── profile/ProfilePage.tsx
    ├── users/UsersPage.tsx
    └── roles/RolesPage.tsx
```

## mobile/ — React Native App

```
mobile/src/
├── App.tsx
├── navigation/AppNavigator.tsx   ← Bottom tabs + Stack
├── components/index.tsx          ← Native UI components
└── screens/
    ├── auth/LoginScreen.tsx
    ├── dashboard/DashboardScreen.tsx
    ├── employees/EmployeesScreen.tsx
    ├── employees/EmployeeDetailScreen.tsx
    ├── notifications/NotificationsScreen.tsx
    └── profile/ProfileScreen.tsx
```

## How to Run

### Web
```bash
cd web
npm install
npm start
# Opens at http://localhost:3000
```

### Mobile
```bash
cd mobile
npm install
npx react-native run-android
# or
npx react-native run-ios
```

## Key Design Decisions

- **shared/** imports from nowhere inside frontend — it is the foundation
- **web/** and **mobile/** import from `shared/` — never from each other
- All hooks accept an optional `ToastService` — web passes `react-hot-toast`, mobile passes `Alert`
- Redux store is in `shared/` — both platforms use the same store
- All TypeScript types are in `shared/types/` — single source of truth
- All API calls are in `shared/api/services.ts` — no duplication
