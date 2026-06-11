import type { MenuItem, UserProfile } from '@/types/auth';
import { formatNow } from '@/utils/time';

type EntityRecord = {
  id: number;
  name: string;
  status: string;
  remark: string;
  updatedAt: string;
  [key: string]: string | number | boolean | unknown[] | undefined;
};

type DepartmentRecord = EntityRecord & {
  parentId: number;
  leader: string;
  orderNum: number;
  children?: DepartmentRecord[];
};

type DictTypeRecord = EntityRecord & {
  dictType: string;
};

type DictItemRecord = EntityRecord & {
  dictType: string;
  dictLabel: string;
  dictValue: string;
};

type PlatformConfigRecord = {
  platformName: string;
  platformSubtitle: string;
  logoUrl: string;
};

type LogRecord = EntityRecord & {
  operator: string;
  ip: string;
};

type MenuRecord = EntityRecord & {
  parentId: number;
  menuType: string;
  routeName: string;
  routePath: string;
  component: string;
  permission: string;
  visible: string;
  keepAlive: number;
};

type MockMqMessageRecord = {
  id: number;
  eventId: string;
  streamKey: string;
  eventType: string;
  bizKey: string | null;
  traceId: string | null;
  status: string;
  retryCount: number;
  source: string | null;
  lastError: string | null;
  processingDeadlineAt: string | null;
  processingTimedOut: boolean;
  publishedAt: string | null;
  consumeStartedAt: string | null;
  consumedAt: string | null;
  createTime: string | null;
  payloadSnapshot: string | null;
};

type MockCacheEntryRecord = {
  key: string;
  type: 'string' | 'hash' | 'list' | 'set' | 'zset';
  ttlSeconds: number | null;
  valuePreview: string;
  value: unknown;
};

const delay = async <T>(data: T, ms = 180): Promise<T> =>
  new Promise((resolve) => {
    globalThis.setTimeout(() => resolve(data), ms);
  });

let mockPlatformConfig: PlatformConfigRecord = {
  platformName: 'manzhushaka 管理台',
  platformSubtitle: 'PLATFORM CONSOLE',
  logoUrl: '',
};

type DbRecord =
  | EntityRecord
  | DepartmentRecord
  | MenuRecord
  | DictTypeRecord
  | DictItemRecord
  | LogRecord;

const createRows = (label: string): EntityRecord[] =>
  Array.from({ length: 4 }, (_, index) => ({
    id: index + 1,
    name: `${label}${index + 1}`,
    status: index % 2 === 0 ? '启用' : '停用',
    remark: `${label}骨架数据，后续可直接替换真实接口`,
    updatedAt: formatNow(),
  }));

type MockMenuItemRecord = {
  id: number;
  parentId: number;
  menuType: 'DIR' | 'MENU' | 'BUTTON';
  menuName: string;
  routeName: string;
  routePath: string;
  component: string;
  icon: string;
  perms: string;
  sort: number;
  visible: number;
  keepAlive: number;
  status: number;
  remark: string;
  createTime: string;
};

let mockMenuStore: MockMenuItemRecord[] = [
  {
    id: 1,
    parentId: 0,
    menuType: 'MENU',
    menuName: '工作台',
    routeName: 'Dashboard',
    routePath: '/dashboard',
    component: 'dashboard/index',
    icon: 'icon-dashboard',
    perms: '',
    sort: 1,
    visible: 1,
    keepAlive: 1,
    status: 1,
    remark: '工作台菜单',
    createTime: formatNow(),
  },
  {
    id: 10,
    parentId: 0,
    menuType: 'DIR',
    menuName: '系统管理',
    routeName: 'System',
    routePath: '/system',
    component: '',
    icon: 'icon-settings',
    perms: '',
    sort: 2,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: '系统管理目录',
    createTime: formatNow(),
  },
  {
    id: 11,
    parentId: 10,
    menuType: 'MENU',
    menuName: '用户管理',
    routeName: 'Users',
    routePath: '/system/users',
    component: 'system/users',
    icon: '',
    perms: 'system:user:list',
    sort: 1,
    visible: 1,
    keepAlive: 1,
    status: 1,
    remark: '用户管理菜单',
    createTime: formatNow(),
  },
  {
    id: 12,
    parentId: 10,
    menuType: 'MENU',
    menuName: '角色管理',
    routeName: 'Roles',
    routePath: '/system/roles',
    component: 'system/roles',
    icon: '',
    perms: 'system:role:list',
    sort: 2,
    visible: 1,
    keepAlive: 1,
    status: 1,
    remark: '角色管理菜单',
    createTime: formatNow(),
  },
  {
    id: 13,
    parentId: 10,
    menuType: 'MENU',
    menuName: '部门管理',
    routeName: 'Depts',
    routePath: '/system/depts',
    component: 'system/depts',
    icon: '',
    perms: 'system:dept:list',
    sort: 3,
    visible: 1,
    keepAlive: 1,
    status: 1,
    remark: '部门管理菜单',
    createTime: formatNow(),
  },
  {
    id: 14,
    parentId: 10,
    menuType: 'DIR',
    menuName: '访问控制',
    routeName: 'MenuRoot',
    routePath: '/system/access',
    component: '',
    icon: '',
    perms: '',
    sort: 4,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: '访问控制目录',
    createTime: formatNow(),
  },
  {
    id: 15,
    parentId: 14,
    menuType: 'MENU',
    menuName: '菜单管理',
    routeName: 'Menus',
    routePath: '/system/access/menus',
    component: 'system/menus',
    icon: '',
    perms: 'system:menu:list',
    sort: 1,
    visible: 1,
    keepAlive: 1,
    status: 1,
    remark: '菜单管理菜单',
    createTime: formatNow(),
  },
  {
    id: 16,
    parentId: 14,
    menuType: 'MENU',
    menuName: '参数管理',
    routeName: 'Params',
    routePath: '/system/access/params',
    component: 'system/params',
    icon: '',
    perms: 'system:config:list',
    sort: 2,
    visible: 1,
    keepAlive: 1,
    status: 1,
    remark: '参数管理菜单',
    createTime: formatNow(),
  },
  {
    id: 24,
    parentId: 14,
    menuType: 'MENU',
    menuName: '平台配置',
    routeName: 'PlatformConfig',
    routePath: '/system/access/platform-config',
    component: 'system/platform-config',
    icon: '',
    perms: 'system:config:update',
    sort: 3,
    visible: 1,
    keepAlive: 1,
    status: 1,
    remark: '平台配置菜单',
    createTime: formatNow(),
  },
  {
    id: 28,
    parentId: 0,
    menuType: 'DIR',
    menuName: '运行监控',
    routeName: 'SystemMonitor',
    routePath: '/monitor',
    component: '',
    icon: 'icon-dashboard',
    perms: '',
    sort: 4,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: '运行监控目录',
    createTime: formatNow(),
  },
  {
    id: 35,
    parentId: 28,
    menuType: 'MENU',
    menuName: '硬件监控',
    routeName: 'SystemMonitorHardware',
    routePath: '/monitor/hardware',
    component: 'system/monitor-hardware',
    icon: '',
    perms: 'system:monitor:view',
    sort: 1,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: '硬件监控菜单',
    createTime: formatNow(),
  },
  {
    id: 36,
    parentId: 28,
    menuType: 'MENU',
    menuName: '服务监控',
    routeName: 'SystemMonitorServices',
    routePath: '/monitor/services',
    component: 'system/monitor-services',
    icon: '',
    perms: 'system:monitor:view',
    sort: 2,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: '服务监控菜单',
    createTime: formatNow(),
  },
  {
    id: 30,
    parentId: 10,
    menuType: 'MENU',
    menuName: '缓存管理',
    routeName: 'SystemCache',
    routePath: '/system/cache',
    component: 'system/cache',
    icon: 'icon-storage',
    perms: 'system:cache:query',
    sort: 6,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: 'Redis 缓存管理菜单',
    createTime: formatNow(),
  },
  {
    id: 31,
    parentId: 30,
    menuType: 'BUTTON',
    menuName: '缓存查询',
    routeName: 'SystemCacheQuery',
    routePath: '',
    component: '',
    icon: '',
    perms: 'system:cache:query',
    sort: 1,
    visible: 0,
    keepAlive: 0,
    status: 1,
    remark: '缓存查询按钮',
    createTime: formatNow(),
  },
  {
    id: 32,
    parentId: 30,
    menuType: 'BUTTON',
    menuName: '缓存详情',
    routeName: 'SystemCacheDetail',
    routePath: '',
    component: '',
    icon: '',
    perms: 'system:cache:detail',
    sort: 2,
    visible: 0,
    keepAlive: 0,
    status: 1,
    remark: '缓存详情按钮',
    createTime: formatNow(),
  },
  {
    id: 29,
    parentId: 28,
    menuType: 'BUTTON',
    menuName: '运行监控刷新',
    routeName: 'SystemMonitorRefresh',
    routePath: '',
    component: '',
    icon: '',
    perms: 'system:monitor:refresh',
    sort: 1,
    visible: 0,
    keepAlive: 0,
    status: 1,
    remark: '运行监控刷新按钮',
    createTime: formatNow(),
  },
  {
    id: 33,
    parentId: 10,
    menuType: 'MENU',
    menuName: '定时任务',
    routeName: 'SystemJobs',
    routePath: '/system/jobs',
    component: 'system/jobs',
    icon: '',
    perms: 'system:job:list',
    sort: 7,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: '定时任务菜单',
    createTime: formatNow(),
  },
  {
    id: 34,
    parentId: 33,
    menuType: 'BUTTON',
    menuName: '任务查询',
    routeName: 'SystemJobQuery',
    routePath: '',
    component: '',
    icon: '',
    perms: 'system:job:query',
    sort: 1,
    visible: 0,
    keepAlive: 0,
    status: 1,
    remark: '任务查询按钮',
    createTime: formatNow(),
  },
  {
    id: 17,
    parentId: 14,
    menuType: 'BUTTON',
    menuName: '菜单新增',
    routeName: 'MenuAdd',
    routePath: '',
    component: '',
    icon: '',
    perms: 'system:menu:add',
    sort: 4,
    visible: 0,
    keepAlive: 0,
    status: 1,
    remark: '菜单新增按钮',
    createTime: formatNow(),
  },
  {
    id: 18,
    parentId: 10,
    menuType: 'MENU',
    menuName: '字典管理',
    routeName: 'Dicts',
    routePath: '/system/dicts',
    component: 'system/dicts',
    icon: '',
    perms: 'system:dict:list',
    sort: 5,
    visible: 1,
    keepAlive: 1,
    status: 1,
    remark: '字典管理菜单',
    createTime: formatNow(),
  },
  {
    id: 19,
    parentId: 10,
    menuType: 'BUTTON',
    menuName: '用户新增',
    routeName: 'UserAdd',
    routePath: '',
    component: '',
    icon: '',
    perms: 'system:user:add',
    sort: 6,
    visible: 0,
    keepAlive: 0,
    status: 1,
    remark: '用户新增按钮',
    createTime: formatNow(),
  },
  {
    id: 20,
    parentId: 0,
    menuType: 'DIR',
    menuName: '日志管理',
    routeName: 'Logs',
    routePath: '/logs',
    component: '',
    icon: 'icon-history',
    perms: '',
    sort: 3,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: '日志管理目录',
    createTime: formatNow(),
  },
  {
    id: 21,
    parentId: 20,
    menuType: 'MENU',
    menuName: '登录日志',
    routeName: 'LoginLogs',
    routePath: '/logs/login',
    component: 'system/login-logs',
    icon: '',
    perms: 'system:log:view',
    sort: 1,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: '登录日志菜单',
    createTime: formatNow(),
  },
  {
    id: 22,
    parentId: 20,
    menuType: 'MENU',
    menuName: '操作日志',
    routeName: 'OpLogs',
    routePath: '/logs/op',
    component: 'system/op-logs',
    icon: '',
    perms: 'system:log:view',
    sort: 2,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: '操作日志菜单',
    createTime: formatNow(),
  },
  {
    id: 23,
    parentId: 20,
    menuType: 'BUTTON',
    menuName: '日志查看',
    routeName: 'LogView',
    routePath: '',
    component: '',
    icon: '',
    perms: 'system:log:view',
    sort: 3,
    visible: 0,
    keepAlive: 0,
    status: 1,
    remark: '日志查看按钮',
    createTime: formatNow(),
  },
  {
    id: 25,
    parentId: 20,
    menuType: 'MENU',
    menuName: '消息台账',
    routeName: 'MqMessages',
    routePath: '/logs/mq-messages',
    component: 'system/mq-messages',
    icon: '',
    perms: 'system:mq-message:query',
    sort: 3,
    visible: 1,
    keepAlive: 0,
    status: 1,
    remark: 'Redis Stream 通用消息台账菜单',
    createTime: formatNow(),
  },
  {
    id: 26,
    parentId: 25,
    menuType: 'BUTTON',
    menuName: '消息台账查询',
    routeName: 'MqMessageQuery',
    routePath: '',
    component: '',
    icon: '',
    perms: 'system:mq-message:query',
    sort: 4,
    visible: 0,
    keepAlive: 0,
    status: 1,
    remark: '消息台账查询按钮',
    createTime: formatNow(),
  },
  {
    id: 27,
    parentId: 25,
    menuType: 'BUTTON',
    menuName: '消息重试',
    routeName: 'MqMessageRetry',
    routePath: '',
    component: '',
    icon: '',
    perms: 'system:mq-message:retry',
    sort: 5,
    visible: 0,
    keepAlive: 0,
    status: 1,
    remark: '消息重试按钮',
    createTime: formatNow(),
  },
];

const permissions = [
  'system:monitor:view',
  'system:user:add',
  'system:user:update',
  'system:user:delete',
  'system:role:add',
  'system:role:update',
  'system:role:delete',
  'system:dept:add',
  'system:dept:update',
  'system:menu:add',
  'system:menu:update',
  'system:dict:add',
  'system:dict:update',
  'system:config:add',
  'system:config:update',
  'system:cache:query',
  'system:cache:detail',
  'system:monitor:refresh',
  'system:log:view',
  'system:mq-message:query',
  'system:mq-message:retry',
];

const profile: UserProfile = {
  userId: 1,
  username: 'admin',
  nickname: '超级管理员',
  deptId: 100,
  deptName: '平台研发部',
  roleCodes: ['SUPER_ADMIN'],
  permCodes: permissions,
};

function compareMenuOrder(left: MockMenuItemRecord, right: MockMenuItemRecord) {
  return left.sort - right.sort || left.id - right.id;
}

function toMenuEntityRecord(record: MockMenuItemRecord): MenuRecord {
  return {
    id: record.id,
    name: record.menuName,
    parentId: record.parentId,
    menuType: record.menuType,
    routeName: record.routeName,
    routePath: record.routePath,
    component: record.component,
    permission: record.perms,
    visible: record.visible === 1 ? '显示' : '隐藏',
    keepAlive: record.keepAlive,
    status: record.status === 1 ? '启用' : '停用',
    remark: record.remark,
    updatedAt: record.createTime,
  };
}

function toMenuVO(record: MockMenuItemRecord) {
  return {
    id: record.id,
    parentId: record.parentId,
    menuType: record.menuType,
    menuName: record.menuName,
    routePath: record.routePath || null,
    routeName: record.routeName || null,
    component: record.component || null,
    icon: record.icon || null,
    sort: record.sort,
    visible: record.visible,
    keepAlive: record.keepAlive,
    perms: record.perms || null,
    status: record.status,
    createTime: record.createTime,
  };
}

function listMockMenuRecords(keyword = '') {
  return mockMenuStore
    .filter((item) => !keyword || item.menuName.includes(keyword) || item.remark.includes(keyword))
    .sort(compareMenuOrder)
    .map(toMenuEntityRecord);
}

function listMockMenuVOs(keyword = '') {
  return mockMenuStore
    .filter((item) => !keyword || item.menuName.includes(keyword) || item.remark.includes(keyword))
    .sort(compareMenuOrder)
    .map(toMenuVO);
}

function resolveMenuRedirect(node: MenuItem): string | undefined {
  const firstVisibleChild = (node.children ?? []).find((child) => child.type !== 'BUTTON');
  if (!firstVisibleChild) {
    return undefined;
  }
  return resolveMenuRedirect(firstVisibleChild);
}

function buildMockMenuTree(): MenuItem[] {
  const records = [...mockMenuStore].sort(compareMenuOrder);
  const nodeMap = new Map<number, MenuItem>();
  const rootNodes: MenuItem[] = [];

  for (const record of records) {
    nodeMap.set(record.id, {
      id: record.id,
      name: record.routeName || `menu-${record.id}`,
      type: record.menuType,
      path: record.routePath,
      component: record.component || undefined,
      title: record.menuName,
      icon: record.icon || undefined,
      hidden: record.visible === 0,
      permission: record.perms || undefined,
      children: [],
    });
  }

  for (const record of records) {
    const node = nodeMap.get(record.id);
    if (!node) {
      continue;
    }
    if (!record.parentId || !nodeMap.has(record.parentId)) {
      rootNodes.push(node);
      continue;
    }
    nodeMap.get(record.parentId)?.children?.push(node);
  }

  const walk = (nodes: MenuItem[]) => {
    for (const node of nodes) {
      if (!node.children?.length) {
        delete node.children;
        continue;
      }
      const redirect = resolveMenuRedirect(node);
      if (redirect) {
        node.redirect = redirect;
      } else {
        delete node.redirect;
      }
      walk(node.children);
    }
  };

  walk(rootNodes);
  return rootNodes;
}

function normalizeText(value: string | number | undefined, fallback = '') {
  return String(value ?? fallback).trim();
}

function normalizeNullableText(value: string | number | undefined) {
  const normalized = normalizeText(value);
  return normalized ? normalized : '';
}

function nextMenuId() {
  return mockMenuStore.length ? Math.max(...mockMenuStore.map((item) => item.id)) + 1 : 1;
}

function createMockMenu(payload: Record<string, string | number>) {
  const record: MockMenuItemRecord = {
    id: nextMenuId(),
    parentId: Number(payload.parentId ?? 0),
    menuType: normalizeText(payload.menuType, 'MENU') as MockMenuItemRecord['menuType'],
    menuName: normalizeText(payload.menuName),
    routeName: normalizeNullableText(payload.routeName),
    routePath: normalizeNullableText(payload.routePath),
    component: normalizeNullableText(payload.component),
    icon: normalizeNullableText(payload.icon),
    perms: normalizeNullableText(payload.perms),
    sort: Number(payload.sort ?? 0),
    visible: Number(payload.visible ?? 1),
    keepAlive: Number(payload.keepAlive ?? 0),
    status: Number(payload.status ?? 1),
    remark: 'Mock 新增菜单',
    createTime: formatNow(),
  };
  mockMenuStore = [record, ...mockMenuStore];
  return record.id;
}

function updateMockMenu(id: number, payload: Record<string, string | number>) {
  mockMenuStore = mockMenuStore.map((record) => {
    if (record.id !== id) {
      return record;
    }
    return {
      ...record,
      parentId: Number(payload.parentId ?? record.parentId),
      menuType: normalizeText(payload.menuType, record.menuType) as MockMenuItemRecord['menuType'],
      menuName: normalizeText(payload.menuName, record.menuName),
      routeName: normalizeNullableText(payload.routeName ?? record.routeName),
      routePath: normalizeNullableText(payload.routePath ?? record.routePath),
      component: normalizeNullableText(payload.component ?? record.component),
      icon: normalizeNullableText(payload.icon ?? record.icon),
      perms: normalizeNullableText(payload.perms ?? record.perms),
      sort: Number(payload.sort ?? record.sort),
      visible: Number(payload.visible ?? record.visible),
      keepAlive: Number(payload.keepAlive ?? record.keepAlive),
      status: Number(payload.status ?? record.status),
      remark: record.remark,
    };
  });
}

function collectMenuDescendants(rootId: number, bucket: Set<number>) {
  bucket.add(rootId);
  const children = mockMenuStore.filter((item) => item.parentId === rootId);
  for (const child of children) {
    collectMenuDescendants(child.id, bucket);
  }
}

function deleteMockMenu(id: number) {
  const ids = new Set<number>();
  collectMenuDescendants(id, ids);
  mockMenuStore = mockMenuStore.filter((item) => !ids.has(item.id));
}

function listMockMenuOptions() {
  return mockMenuStore
    .filter((item) => item.status === 1)
    .sort(compareMenuOrder)
    .map((item) => ({ label: item.menuName, value: String(item.id) }));
}

function getMockMenuById(id: number) {
  const record = mockMenuStore.find((item) => item.id === id);
  if (!record) {
    throw new Error(`未找到菜单: ${id}`);
  }
  return toMenuVO(record);
}

const db: Record<string, DbRecord[]> = {
  users: createRows('用户'),
  roles: createRows('角色'),
  params: createRows('参数'),
  depts: [
    { id: 1, name: '总公司', parentId: 0, leader: '王总', orderNum: 1, status: '启用', remark: '一级部门', updatedAt: formatNow(), children: [] },
    { id: 2, name: '平台研发部', parentId: 1, leader: '周工', orderNum: 10, status: '启用', remark: '负责平台建设', updatedAt: formatNow(), children: [] },
    { id: 3, name: '风控产品部', parentId: 1, leader: '陈工', orderNum: 20, status: '启用', remark: '负责规则与数据产品', updatedAt: formatNow(), children: [] },
  ],
  dictTypes: [
    { id: 1, name: '用户状态', dictType: 'sys_user_status', status: '启用', remark: '用户状态字典', updatedAt: formatNow() },
    { id: 2, name: '通知级别', dictType: 'sys_notice_level', status: '启用', remark: '通知级别字典', updatedAt: formatNow() },
  ],
  dictItems: [
    { id: 1, name: '正常', dictType: 'sys_user_status', dictLabel: '正常', dictValue: '0', status: '启用', remark: '正常状态', updatedAt: formatNow() },
    { id: 2, name: '停用', dictType: 'sys_user_status', dictLabel: '停用', dictValue: '1', status: '启用', remark: '停用状态', updatedAt: formatNow() },
  ],
  loginLogs: [
    { id: 1, name: '登录成功', operator: 'admin', ip: '127.0.0.1', status: '启用', remark: '浏览器登录', updatedAt: formatNow() },
    { id: 2, name: '登录失败', operator: 'guest', ip: '10.0.0.12', status: '停用', remark: '密码错误', updatedAt: formatNow() },
  ],
  opLogs: [
    { id: 1, name: '新增用户', operator: 'admin', ip: '127.0.0.1', status: '启用', remark: '创建测试账号', updatedAt: formatNow() },
    { id: 2, name: '调整菜单', operator: 'admin', ip: '127.0.0.1', status: '启用', remark: '修改菜单顺序', updatedAt: formatNow() },
  ],
};

let mockMqMessages: MockMqMessageRecord[] = [
  {
    id: 1,
    eventId: '1749555555000-0',
    streamKey: 'stream:system:user',
    eventType: 'USER_CREATED',
    bizKey: 'user:9527',
    traceId: 'TRACE-9527',
    status: 'PUBLISHED',
    retryCount: 2,
    source: 'system',
    lastError: 'consumer timeout',
    processingDeadlineAt: '2026-06-10T09:10:30',
    processingTimedOut: true,
    publishedAt: '2026-06-10T09:10:15',
    consumeStartedAt: null,
    consumedAt: null,
    createTime: '2026-06-10T09:10:11',
    payloadSnapshot: '{"userId":9527}',
  },
  {
    id: 2,
    eventId: '1749555566000-0',
    streamKey: 'stream:notify:sms',
    eventType: 'SMS_SEND',
    bizKey: 'sms:10086',
    traceId: 'TRACE-SMS-10086',
    status: 'FAIL',
    retryCount: 1,
    source: 'notify',
    lastError: 'provider unavailable',
    processingDeadlineAt: '2026-06-10T10:00:30',
    processingTimedOut: false,
    publishedAt: '2026-06-10T10:00:03',
    consumeStartedAt: '2026-06-10T10:00:05',
    consumedAt: '2026-06-10T10:00:11',
    createTime: '2026-06-10T10:00:00',
    payloadSnapshot: '{"mobile":"13800000000"}',
  },
  {
    id: 3,
    eventId: '1749555577000-0',
    streamKey: 'stream:order:pay',
    eventType: 'PAY_SUCCESS',
    bizKey: 'order:20260610001',
    traceId: 'TRACE-PAY-1',
    status: 'SUCCESS',
    retryCount: 0,
    source: 'trade',
    lastError: null,
    processingDeadlineAt: '2026-06-10T11:20:30',
    processingTimedOut: false,
    publishedAt: '2026-06-10T11:20:02',
    consumeStartedAt: '2026-06-10T11:20:03',
    consumedAt: '2026-06-10T11:20:05',
    createTime: '2026-06-10T11:20:00',
    payloadSnapshot: '{"orderNo":"20260610001"}',
  },
];

const mockCacheEntries: MockCacheEntryRecord[] = [
  {
    key: 'auth:captcha:demo',
    type: 'string',
    ttlSeconds: 120,
    valuePreview: 'ABCD',
    value: 'ABCD',
  },
  {
    key: 'sa-token:login:10001',
    type: 'hash',
    ttlSeconds: -1,
    valuePreview: 'Hash(2) [token=mock-token-admin, loginTime=2026-06-10 09:00:00]',
    value: {
      token: 'mock-token-admin',
      loginTime: '2026-06-10 09:00:00',
    },
  },
  {
    key: 'stream:notify:recent',
    type: 'list',
    ttlSeconds: 3600,
    valuePreview: 'List(3) ["SMS_SEND","EMAIL_SEND","PUSH_SEND"]',
    value: ['SMS_SEND', 'EMAIL_SEND', 'PUSH_SEND'],
  },
];

const mockSlowSqlRecords = [
  {
    statementId: 'com.manzhushaka.db.system.mapper.UserMapper.selectPage',
    sql: 'SELECT id, username, nickname, status FROM sys_user WHERE status = ? ORDER BY id DESC LIMIT ?',
    costMs: 188,
    resultSize: 20,
    executeTime: '2026-06-11 11:45:02',
  },
  {
    statementId: 'com.manzhushaka.db.system.mapper.SysMqMessageMapper.selectPage',
    sql: 'SELECT * FROM sys_mq_message WHERE status IN (?, ?) ORDER BY create_time DESC LIMIT ?',
    costMs: 133,
    resultSize: 10,
    executeTime: '2026-06-11 11:44:48',
  },
];

const mockLogTailLines = [
  '2026-06-11 11:46:11.213 [http-nio-8080-exec-1] INFO com.manzhushaka.system.controller.ServerMonitorController - monitor refresh requested',
  '2026-06-11 11:46:12.003 [http-nio-8080-exec-1] WARN com.manzhushaka.mq.consumer.OpLogStreamConsumer - stream backlog rising for stream:system:user',
  '2026-06-11 11:46:13.118 [http-nio-8080-exec-2] INFO com.manzhushaka.system.service.impl.PlatformJobDispatchServiceImpl - job 平台心跳 executed successfully',
];

function paginateRecords<T>(records: T[], pageNum: number, pageSize: number) {
  const start = (pageNum - 1) * pageSize;
  return {
    total: records.length,
    records: records.slice(start, start + pageSize),
  };
}

function listMockMqMessages(params: Record<string, string | number | undefined>) {
  const keyword = String(
    params.streamKey
      ?? params.eventType
      ?? params.bizKey
      ?? params.traceId
      ?? '',
  ).trim();
  const status = String(params.status ?? '').trim();
  const source = String(params.source ?? '').trim();
  const pageNum = Number(params.pageNum ?? 1);
  const pageSize = Number(params.pageSize ?? 10);
  const filtered = mockMqMessages.filter((item) => {
    const matchKeyword = !keyword || [
      item.streamKey,
      item.eventType,
      item.bizKey ?? '',
      item.traceId ?? '',
    ].some((field) => field.includes(keyword));
    const matchStatus = !status || item.status === status;
    const matchSource = !source || (item.source ?? '').includes(source);
    return matchKeyword && matchStatus && matchSource;
  });
  return delay(paginateRecords(filtered, pageNum, pageSize));
}

function retryMockMqMessage(id: number) {
  mockMqMessages = mockMqMessages.map((item) => {
    if (item.id !== id) {
      return item;
    }
    return {
      ...item,
      status: 'PUBLISHED',
      retryCount: item.retryCount + 1,
      lastError: null,
      processingTimedOut: false,
      processingDeadlineAt: null,
      publishedAt: formatNow(),
      consumeStartedAt: null,
      consumedAt: null,
    };
  });
  return delay(null);
}

function toMockCacheExpireAt(ttlSeconds: number | null) {
  if (ttlSeconds == null || ttlSeconds < 0) {
    return null;
  }
  return new Date(Date.now() + ttlSeconds * 1000).toISOString();
}

function listMockCacheEntries(params: Record<string, string | number | undefined>) {
  const keyword = String(params.keyword ?? '').trim();
  const limit = Number(params.limit ?? 20);
  const filtered = mockCacheEntries
    .filter((item) => !keyword || item.key.includes(keyword))
    .slice(0, limit)
    .map((item) => ({
      key: item.key,
      type: item.type,
      ttlSeconds: item.ttlSeconds,
      expireAt: toMockCacheExpireAt(item.ttlSeconds),
      valuePreview: item.valuePreview,
    }));
  return delay(filtered);
}

function getMockCacheEntryDetail(key: string) {
  const record = mockCacheEntries.find((item) => item.key === key);
  if (!record) {
    throw new Error(`未找到缓存 Key: ${key}`);
  }
  return delay({
    key: record.key,
    type: record.type,
    ttlSeconds: record.ttlSeconds,
    expireAt: toMockCacheExpireAt(record.ttlSeconds),
    valuePreview: record.valuePreview,
    value: record.value,
  });
}

function createMockServerMonitor() {
  return delay({
    applicationName: 'manzhushaka-admin',
    activeProfile: 'dev',
    javaVersion: '17.0.0',
    osName: 'macOS',
    osArch: 'aarch64',
    startTime: '2026-06-10 09:30:00',
    uptimeMillis: 22680000,
    system: {
      availableProcessors: 8,
      systemCpuUsage: 24.56,
      processCpuUsage: 9.83,
      totalPhysicalMemory: 17179869184,
      freePhysicalMemory: 6871947673,
    },
    jvm: {
      vmName: 'OpenJDK 64-Bit Server VM',
      vmVendor: 'Eclipse Adoptium',
      vmVersion: '17.0.12+7',
      inputArguments: ['-Xms512m', '-Xmx1024m', '-Dspring.profiles.active=dev', '-XX:+UseG1GC'],
      heapInit: 536870912,
      heapUsed: 268435456,
      heapCommitted: 536870912,
      heapMax: 1073741824,
      nonHeapUsed: 201326592,
      nonHeapCommitted: 234881024,
      nonHeapMax: -1,
      liveThreadCount: 86,
      daemonThreadCount: 55,
    },
    redis: {
      available: true,
      version: '7.2.5',
      connectedClients: 12,
      usedMemory: 1048576,
      usedMemoryPeak: 2097152,
      dbSize: 64,
      keyspaceHits: 12890,
      keyspaceMisses: 1120,
      hitRate: 92.01,
      expiredKeys: 860,
      evictedKeys: 3,
      errorMessage: null,
    },
    jobHealth: {
      totalJobs: 6,
      enabledJobs: 4,
      pausedJobs: 2,
      recentSuccessCount: 18,
      recentFailCount: 2,
      recentSuccessRate: 90,
      recentFailures: [
        {
          jobId: 500002,
          jobName: '消息补偿任务',
          runStatus: 'FAIL',
          errorMsg: '连接 Redis Stream 超时',
          startTime: '2026-06-11 11:31:22',
        },
      ],
    },
    messageBacklog: {
      pendingCount: 3,
      processingCount: 1,
      failCount: 1,
      initCount: 1,
      publishedCount: 1,
      timedOutCount: 1,
      oldestPendingEventId: '1749555555000-0',
      oldestPendingCreateTime: '2026-06-10 09:10:11',
      streams: [
        { streamKey: 'stream:system:user', pendingCount: 2, failCount: 0 },
        { streamKey: 'stream:notify:sms', pendingCount: 0, failCount: 1 },
      ],
    },
    slowSql: {
      available: true,
      recentCount: mockSlowSqlRecords.length,
      latestCostMs: mockSlowSqlRecords[0].costMs,
      latestStatementId: mockSlowSqlRecords[0].statementId,
      latestExecuteTime: mockSlowSqlRecords[0].executeTime,
      thresholdMs: 120,
    },
    logTail: {
      available: true,
      entryCount: mockLogTailLines.length,
      capacity: 200,
      lastEntryAt: '2026-06-11 11:46:13',
    },
  });
}

function listMockSlowSqlRecords(params: Record<string, string | number | undefined>) {
  const limit = Number(params.limit ?? 20);
  return delay(mockSlowSqlRecords.slice(0, limit));
}

function getMockMonitorLogTail(params: Record<string, string | number | undefined>) {
  const limit = Number(params.limit ?? 80);
  return delay({
    available: true,
    generatedAt: '2026-06-11 11:46:15',
    lastEntryAt: '2026-06-11 11:46:13',
    lines: mockLogTailLines.slice(Math.max(mockLogTailLines.length - limit, 0)),
  });
}

export async function mockLogin(username: string, password: string) {
  if (!username || !password) {
    throw new Error('用户名和密码不能为空');
  }
  return delay({ token: `mock-token-${username}`, userInfo: profile });
}

export async function mockCaptcha() {
  return delay({
    key: 'mock-captcha-key',
    imageBase64:
      'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNjAiIGhlaWdodD0iNDgiIHZpZXdCb3g9IjAgMCAxNjAgNDgiPjxyZWN0IHdpZHRoPSIxNjAiIGhlaWdodD0iNDgiIHJ4PSIxMiIgZmlsbD0iI2YzZjdmZiIvPjxwYXRoIGQ9Ik0wIDM0YzIwLTEyIDM4LTEyIDU1IDBzMzYgMTIgNTMgMCAzNC0xMiA1MiAwIiBmaWxsPSJub25lIiBzdHJva2U9IiNkMGRkZmYiIHN0cm9rZS13aWR0aD0iMiIvPjx0ZXh0IHg9IjgwIiB5PSIzMSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjIyIiBmb250LXdlaWdodD0iNzAwIiBsZXR0ZXItc3BhY2luZz0iNiIgZmlsbD0iIzI4NWZkOCI+QUJDRDwvdGV4dD48L3N2Zz4=',
  });
}

export async function mockLoginWithCaptcha(
  username: string,
  password: string,
  captchaKey: string,
  captchaCode: string,
) {
  if (!username || !password) {
    throw new Error('用户名和密码不能为空');
  }
  if (!captchaKey) {
    throw new Error('验证码标识不能为空');
  }
  if (!captchaCode) {
    throw new Error('验证码不能为空');
  }
  if (captchaCode.trim().toUpperCase() !== 'ABCD') {
    throw new Error('验证码错误');
  }
  return delay({ token: `mock-token-${username}`, userInfo: profile });
}

export async function mockProfile() {
  return delay(profile);
}

export async function mockMenus() {
  return delay(buildMockMenuTree());
}

export async function mockPermissions() {
  return delay(permissions);
}

export async function listEntities(key: keyof typeof db | 'menus', keyword = '') {
  if (key === 'menus') {
    return delay(listMockMenuRecords(keyword));
  }
  const source = db[key] ?? [];
  const rows = keyword
    ? source.filter((item) => item.name.includes(keyword) || item.remark.includes(keyword))
    : source;
  return delay(rows);
}

export async function createEntity(
  key: keyof typeof db | 'menus',
  payload: Record<string, string | number>,
) {
  if (key === 'menus') {
    return delay({ id: createMockMenu(payload) });
  }
  const rows = db[key] ?? [];
  const item = {
    id: rows.length ? Math.max(...rows.map((row) => row.id)) + 1 : 1,
    updatedAt: formatNow(),
    ...payload,
  } as DbRecord;
  rows.unshift(item);
  db[key] = rows;
  return delay(item);
}

export async function updateEntity(
  key: keyof typeof db | 'menus',
  id: number,
  payload: Record<string, string | number>,
) {
  if (key === 'menus') {
    updateMockMenu(id, payload);
    return delay({ id });
  }
  const rows = db[key] ?? [];
  const index = rows.findIndex((item) => item.id === id);
  if (index >= 0) {
    rows[index] = { ...rows[index], ...payload, updatedAt: formatNow() } as DbRecord;
  }
  return delay(rows[index]);
}

export async function removeEntity(key: keyof typeof db | 'menus', id: number) {
  if (key === 'menus') {
    deleteMockMenu(id);
    return delay(true);
  }
  db[key] = (db[key] ?? []).filter((item) => item.id !== id);
  return delay(true);
}

type MockResponse<T> = {
  code: number;
  data: T;
  message: string;
};

function ok<T>(data: T): MockResponse<T> {
  return {
    code: 0,
    data,
    message: 'ok',
  };
}

function parseMockBody(
  data: string | Record<string, string | number> | undefined,
): Record<string, string | number> {
  if (!data) {
    return {};
  }
  if (typeof data === 'string') {
    return JSON.parse(data) as Record<string, string | number>;
  }
  return data;
}

export async function dispatchMockRequest(config: {
  url?: string;
  method?: string;
  params?: Record<string, string | number | undefined>;
  data?: string | Record<string, string | number>;
}) {
  const { url = '', method = 'get', params } = config;
  const body = parseMockBody(config.data);
  const menuIdMatch = url.match(/^\/system\/menus\/(\d+)$/);
  const mqMessageRetryMatch = url.match(/^\/system\/logs\/mq-messages\/(\d+)\/retry$/);

  if (url === '/auth/captcha' && method === 'get') {
    return ok(await mockCaptcha());
  }
  if (url === '/auth/login' && method === 'post') {
    return ok(
      await mockLoginWithCaptcha(
        String(body.username ?? ''),
        String(body.password ?? ''),
        String(body.captchaKey ?? ''),
        String(body.captchaCode ?? ''),
      ),
    );
  }
  if ((url === '/auth/profile' || url === '/auth/me') && method === 'get') {
    return ok(await mockProfile());
  }
  if (url === '/auth/menus' && method === 'get') {
    return ok(await mockMenus());
  }
  if (url === '/auth/permissions' && method === 'get') {
    return ok(await mockPermissions());
  }
  if (url === '/system/menus' && method === 'get') {
    return ok(await delay(listMockMenuVOs(String(params?.menuName ?? ''))));
  }
  if (url === '/system/menus/options' && method === 'get') {
    return ok(await delay(listMockMenuOptions()));
  }
  if (menuIdMatch && method === 'get') {
    return ok(await delay(getMockMenuById(Number(menuIdMatch[1]))));
  }
  if (url === '/system/menus' && method === 'post') {
    return ok(await delay(createMockMenu(body)));
  }
  if (menuIdMatch && method === 'put') {
    updateMockMenu(Number(menuIdMatch[1]), body);
    return ok(null);
  }
  if (menuIdMatch && method === 'delete') {
    deleteMockMenu(Number(menuIdMatch[1]));
    return ok(null);
  }
  if (url === '/system/platform-config' && method === 'get') {
    return ok(await delay(mockPlatformConfig));
  }
  if (url === '/system/platform-config' && method === 'put') {
    mockPlatformConfig = {
      platformName: normalizeText(body.platformName, mockPlatformConfig.platformName),
      platformSubtitle: normalizeText(body.platformSubtitle, mockPlatformConfig.platformSubtitle),
      logoUrl: normalizeText(body.logoUrl, mockPlatformConfig.logoUrl),
    };
    return ok(null);
  }
  if (url === '/system/monitor/server' && method === 'get') {
    return ok(await createMockServerMonitor());
  }
  if (url === '/system/monitor/slow-sql' && method === 'get') {
    return ok(await listMockSlowSqlRecords(params ?? {}));
  }
  if (url === '/system/monitor/logs/tail' && method === 'get') {
    return ok(await getMockMonitorLogTail(params ?? {}));
  }
  if (url === '/system/cache/entries' && method === 'get') {
    return ok(await listMockCacheEntries(params ?? {}));
  }
  if (url === '/system/cache/entries/detail' && method === 'get') {
    return ok(await getMockCacheEntryDetail(String(params?.key ?? '')));
  }
  if (url === '/system/logs/mq-messages' && method === 'get') {
    return ok(await listMockMqMessages(params ?? {}));
  }
  if (mqMessageRetryMatch && method === 'post') {
    return ok(await retryMockMqMessage(Number(mqMessageRetryMatch[1])));
  }
  if (url.startsWith('/system/') && method === 'get') {
    const key = url.replace('/system/', '') as keyof typeof db;
    return ok(await listEntities(key, String(params?.keyword ?? '')));
  }
  if (url.startsWith('/system/') && method === 'post') {
    const key = url.replace('/system/', '') as keyof typeof db;
    return ok(await createEntity(key, body as Record<string, string | number>));
  }
  if (url.startsWith('/system/') && method === 'put') {
    const key = url.replace('/system/', '') as keyof typeof db;
    return ok(await updateEntity(key, Number(body.id), body as Record<string, string | number>));
  }
  if (url.startsWith('/system/') && method === 'delete') {
    const key = url.replace('/system/', '') as keyof typeof db;
    return ok(await removeEntity(key, Number(params?.id)));
  }

  throw new Error(`未匹配的 mock 请求: ${method.toUpperCase()} ${url}`);
}
