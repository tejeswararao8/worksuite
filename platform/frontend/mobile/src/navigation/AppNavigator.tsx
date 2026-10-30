import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useSelector } from 'react-redux';
import { Text, View } from 'react-native';
import type { RootState } from '../../../shared/store';
import { COLORS } from '../components';

// ─── Auth ─────────────────────────────────────────────────────────────────────
import LoginScreen from '../screens/auth/LoginScreen';

// ─── Main Tab Screens ─────────────────────────────────────────────────────────
import DashboardScreen      from '../screens/dashboard/DashboardScreen';
import EmployeesScreen      from '../screens/employees/EmployeesScreen';
import NotificationsScreen  from '../screens/notifications/NotificationsScreen';
import ProfileScreen        from '../screens/profile/ProfileScreen';

// ─── Stack Screens (pushed from tabs) ─────────────────────────────────────────
import EmployeeDetailScreen from '../screens/employees/EmployeeDetailScreen';
import TransfersScreen      from '../screens/transfers/TransfersScreen';
import PromotionsScreen     from '../screens/promotions/PromotionsScreen';
import ProbationScreen      from '../screens/probation/ProbationScreen';
import OnboardingScreen     from '../screens/onboarding/OnboardingScreen';
import OffboardingScreen    from '../screens/offboarding/OffboardingScreen';
import AssetsScreen         from '../screens/assets/AssetsScreen';
import SkillsScreen         from '../screens/skills/SkillsScreen';
import ReportsScreen        from '../screens/reports/ReportsScreen';
import DepartmentsScreen    from '../screens/departments/DepartmentsScreen';
import BranchesScreen       from '../screens/branches/BranchesScreen';
import DesignationsScreen   from '../screens/designations/DesignationsScreen';
import TeamsScreen          from '../screens/teams/TeamsScreen';
import AuditScreen          from '../screens/audit/AuditScreen';

const Tab   = createBottomTabNavigator();
const Stack = createNativeStackNavigator();

// ─── Tab icon mapping ─────────────────────────────────────────────────────────
const TAB_ICONS: Record<string, string> = {
  Dashboard:     '🏠',
  Employees:     '👥',
  Notifications: '🔔',
  Profile:       '👤',
};

function TabIcon({ name, focused }: { name: string; focused: boolean }) {
  return (
    <Text style={{ fontSize: 20, opacity: focused ? 1 : 0.45 }}>
      {TAB_ICONS[name] ?? '•'}
    </Text>
  );
}

// ─── Bottom Tab Navigator ─────────────────────────────────────────────────────
function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarIcon: ({ focused }) => <TabIcon name={route.name} focused={focused} />,
        tabBarActiveTintColor:   COLORS.primary,
        tabBarInactiveTintColor: COLORS.slate400,
        tabBarStyle: {
          borderTopColor:  COLORS.slate200,
          paddingBottom:   6,
          paddingTop:      4,
          height:          62,
        },
        tabBarLabelStyle: { fontSize: 11, fontWeight: '600' },
      })}
    >
      <Tab.Screen name="Dashboard"     component={DashboardScreen} />
      <Tab.Screen name="Employees"     component={EmployeesScreen} />
      <Tab.Screen
        name="Notifications"
        component={NotificationsScreen}
        // Badge will be added via Redux unreadCount in a real implementation
      />
      <Tab.Screen name="Profile"       component={ProfileScreen} />
    </Tab.Navigator>
  );
}

// ─── Stack Navigator (wraps tabs + all detail screens) ────────────────────────
const STACK_SCREENS: Array<{ name: string; component: React.ComponentType<never>; title: string }> = [
  { name: 'EmployeeDetail', component: EmployeeDetailScreen, title: 'Employee Details' },
  { name: 'Transfers',      component: TransfersScreen,      title: 'Transfers' },
  { name: 'Promotions',     component: PromotionsScreen,     title: 'Promotions' },
  { name: 'Probation',      component: ProbationScreen,      title: 'Probation' },
  { name: 'Onboarding',     component: OnboardingScreen,     title: 'Onboarding' },
  { name: 'Offboarding',    component: OffboardingScreen,    title: 'Offboarding' },
  { name: 'Assets',         component: AssetsScreen,         title: 'Assets' },
  { name: 'Skills',         component: SkillsScreen,         title: 'Skills & Certifications' },
  { name: 'Reports',        component: ReportsScreen,        title: 'Reports' },
  { name: 'Departments',    component: DepartmentsScreen,    title: 'Departments' },
  { name: 'Branches',       component: BranchesScreen,       title: 'Branches' },
  { name: 'Designations',   component: DesignationsScreen,   title: 'Designations' },
  { name: 'Teams',          component: TeamsScreen,          title: 'Teams' },
  { name: 'Audit',          component: AuditScreen,          title: 'Audit Logs' },
];

export default function AppNavigator() {
  const isAuthenticated = useSelector((s: RootState) => s.auth.isAuthenticated);

  return (
    <NavigationContainer>
      <Stack.Navigator
        screenOptions={{
          headerStyle:      { backgroundColor: COLORS.white },
          headerTintColor:  COLORS.primary,
          headerTitleStyle: { fontWeight: '700', fontSize: 16, color: COLORS.slate800 },
          headerShadowVisible: false,
          headerBackTitleVisible: false,
        }}
      >
        {!isAuthenticated ? (
          <Stack.Screen
            name="Login"
            component={LoginScreen}
            options={{ headerShown: false }}
          />
        ) : (
          <>
            {/* Main tabs — no header (tabs have their own) */}
            <Stack.Screen
              name="Main"
              component={MainTabs}
              options={{ headerShown: false }}
            />

            {/* All detail / secondary screens */}
            {STACK_SCREENS.map((screen) => (
              <Stack.Screen
                key={screen.name}
                name={screen.name}
                component={screen.component}
                options={{ title: screen.title }}
              />
            ))}
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}
