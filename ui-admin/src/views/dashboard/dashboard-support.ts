export interface DashboardMetric {
  title: string;
  value: number;
  note: string;
}

export interface DashboardQuickEntry {
  title: string;
  description: string;
  category: string;
  path: string;
  permission: string;
  badge: string;
  tone: 'blue' | 'teal' | 'amber' | 'slate';
}

export interface DashboardReminder {
  title: string;
  description: string;
}

interface DashboardVisibilityMenu {
  path: string;
  children?: DashboardVisibilityMenu[];
}

export const dashboardQuickEntries: DashboardQuickEntry[] = [
  {
    title: '用户管理',
    description: '维护账号、昵称、状态和所属部门。',
    category: '账号',
    path: '/system/users',
    permission: 'system:user:list',
    badge: 'USR',
    tone: 'blue',
  },
  {
    title: '角色管理',
    description: '梳理角色编码与数据权限范围。',
    category: '权限',
    path: '/system/roles',
    permission: 'system:role:list',
    badge: 'RLS',
    tone: 'teal',
  },
  {
    title: '菜单管理',
    description: '维护路由结构、组件映射和权限标识。',
    category: '导航',
    path: '/system/access/menus',
    permission: 'system:menu:list',
    badge: 'MNU',
    tone: 'amber',
  },
  {
    title: '日志管理',
    description: '查看关键操作留痕和审计结果。',
    category: '审计',
    path: '/logs/op',
    permission: 'system:log:view',
    badge: 'LOG',
    tone: 'slate',
  },
];

export const dashboardReminders: DashboardReminder[] = [
  {
    title: '权限能力已接入',
    description: '当前会话已经接入菜单、权限识别和基础导航能力。',
  },
  {
    title: '技术栈已对齐',
    description: '当前首页基于 Vue 3、Pinia 与 Arco Design Vue 组织。',
  },
  {
    title: '高频入口已收敛',
    description: '首页优先承接系统管理场景中的高频维护入口。',
  },
];

export function buildDashboardMetrics(input: {
  menuCount: number;
  permissionCount: number;
  roleCount: number;
  quickEntryCount: number;
}): DashboardMetric[] {
  return [
    { title: '菜单总数', value: input.menuCount, note: '已挂载的功能入口' },
    { title: '权限点', value: input.permissionCount, note: '当前前端识别权限编码' },
    { title: '角色数', value: input.roleCount, note: '当前身份关联角色数量' },
    { title: '常用入口', value: input.quickEntryCount, note: '当前会话可见快捷入口' },
  ];
}

export function filterQuickEntriesByPermission(
  entries: DashboardQuickEntry[],
  permissions: string[],
  menus: DashboardVisibilityMenu[],
) {
  const visiblePaths = new Set<string>();

  const walkMenus = (items: DashboardVisibilityMenu[]) => {
    for (const item of items) {
      if (item.path) {
        visiblePaths.add(item.path);
      }
      if (item.children?.length) {
        walkMenus(item.children);
      }
    }
  };

  walkMenus(menus);

  return entries.filter((item) => permissions.includes(item.permission) || visiblePaths.has(item.path));
}

export function formatDashboardRoleCodes(roleCodes?: string[]) {
  return roleCodes?.length ? roleCodes.join(', ') : '--';
}
