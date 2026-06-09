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

type LogRecord = EntityRecord & {
  operator: string;
  ip: string;
};

type MenuRecord = EntityRecord & {
  menuType: string;
  routePath: string;
  component: string;
  permission: string;
  visible: string;
};

const delay = async <T>(data: T, ms = 180): Promise<T> =>
  new Promise((resolve) => {
    window.setTimeout(() => resolve(data), ms);
  });

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

const menus: MenuItem[] = [
  {
    id: 1,
    name: 'Dashboard',
    type: 'MENU',
    path: '/dashboard',
    component: 'dashboard/index',
    title: '工作台',
    icon: 'icon-dashboard',
  },
  {
    id: 10,
    name: 'System',
    type: 'DIR',
    path: '/system',
    title: '系统管理',
    icon: 'icon-settings',
    redirect: '/system/users',
    children: [
      { id: 11, name: 'Users', type: 'MENU', path: '/system/users', component: 'system/users', title: '用户管理' },
      { id: 12, name: 'Roles', type: 'MENU', path: '/system/roles', component: 'system/roles', title: '角色管理' },
      { id: 13, name: 'Depts', type: 'MENU', path: '/system/depts', component: 'system/depts', title: '部门管理' },
      {
        id: 14,
        name: 'MenuRoot',
        type: 'DIR',
        path: '/system/access',
        title: '访问控制',
        redirect: '/system/access/menus',
        children: [
          {
            id: 15,
            name: 'Menus',
            type: 'MENU',
            path: '/system/access/menus',
            component: 'system/menus',
            title: '菜单管理',
          },
          {
            id: 16,
            name: 'Params',
            type: 'MENU',
            path: '/system/access/params',
            component: 'system/params',
            title: '参数管理',
          },
          {
            id: 17,
            name: 'MenuAdd',
            type: 'BUTTON',
            path: '',
            title: '菜单新增',
            permission: 'system:menu:add',
          },
        ],
      },
      { id: 18, name: 'Dicts', type: 'MENU', path: '/system/dicts', component: 'system/dicts', title: '字典管理' },
      { id: 19, name: 'UserAdd', type: 'BUTTON', path: '', title: '用户新增', permission: 'system:user:add' },
    ],
  },
  {
    id: 20,
    name: 'Logs',
    type: 'DIR',
    path: '/logs',
    title: '日志管理',
    icon: 'icon-history',
    redirect: '/logs/login',
    children: [
      { id: 21, name: 'LoginLogs', type: 'MENU', path: '/logs/login', component: 'system/login-logs', title: '登录日志' },
      { id: 22, name: 'OpLogs', type: 'MENU', path: '/logs/op', component: 'system/op-logs', title: '操作日志' },
      { id: 23, name: 'LogView', type: 'BUTTON', path: '', title: '日志查看', permission: 'system:log:view' },
    ],
  },
];

const permissions = [
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
  'system:log:view',
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

const db: Record<string, DbRecord[]> = {
  users: createRows('用户'),
  roles: createRows('角色'),
  params: createRows('参数'),
  depts: [
    { id: 1, name: '总公司', parentId: 0, leader: '王总', orderNum: 1, status: '启用', remark: '一级部门', updatedAt: formatNow(), children: [] },
    { id: 2, name: '平台研发部', parentId: 1, leader: '周工', orderNum: 10, status: '启用', remark: '负责平台建设', updatedAt: formatNow(), children: [] },
    { id: 3, name: '风控产品部', parentId: 1, leader: '陈工', orderNum: 20, status: '启用', remark: '负责规则与数据产品', updatedAt: formatNow(), children: [] },
  ],
  menus: [
    { id: 1, name: '系统管理', menuType: 'DIR', routePath: '/system', component: 'Layout', permission: '', visible: '显示', status: '启用', remark: '系统目录', updatedAt: formatNow() },
    { id: 2, name: '菜单管理', menuType: 'MENU', routePath: '/system/access/menus', component: 'system/menus', permission: 'system:menu:list', visible: '显示', status: '启用', remark: '菜单页', updatedAt: formatNow() },
    { id: 3, name: '菜单新增', menuType: 'BUTTON', routePath: '', component: '', permission: 'system:menu:add', visible: '隐藏', status: '启用', remark: '按钮权限', updatedAt: formatNow() },
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

export async function mockLogin(username: string, password: string) {
  if (!username || !password) {
    throw new Error('用户名和密码不能为空');
  }
  return delay({ token: `mock-token-${username}` });
}

export async function mockProfile() {
  return delay(profile);
}

export async function mockMenus() {
  return delay(menus);
}

export async function mockPermissions() {
  return delay(permissions);
}

export async function listEntities(key: keyof typeof db, keyword = '') {
  const source = db[key] ?? [];
  const rows = keyword
    ? source.filter((item) => item.name.includes(keyword) || item.remark.includes(keyword))
    : source;
  return delay(rows);
}

export async function createEntity(
  key: keyof typeof db,
  payload: Record<string, string | number>,
) {
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
  key: keyof typeof db,
  id: number,
  payload: Record<string, string | number>,
) {
  const rows = db[key] ?? [];
  const index = rows.findIndex((item) => item.id === id);
  if (index >= 0) {
    rows[index] = { ...rows[index], ...payload, updatedAt: formatNow() } as DbRecord;
  }
  return delay(rows[index]);
}

export async function removeEntity(key: keyof typeof db, id: number) {
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

export async function dispatchMockRequest(config: {
  url?: string;
  method?: string;
  params?: Record<string, string>;
  data?: string;
}) {
  const { url = '', method = 'get', params } = config;
  const body = config.data ? (JSON.parse(config.data) as Record<string, string | number>) : {};

  if (url === '/auth/login' && method === 'post') {
    return ok(await mockLogin(String(body.username ?? ''), String(body.password ?? '')));
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
  if (url.startsWith('/system/') && method === 'get') {
    const key = url.replace('/system/', '') as keyof typeof db;
    return ok(await listEntities(key, params?.keyword ?? ''));
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
