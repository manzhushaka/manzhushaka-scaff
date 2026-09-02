import request, { HttpResponse } from './interceptor';

export type AdminRecord = Record<string, any>;

export interface AdminList<T extends AdminRecord = AdminRecord> {
  rows: T[];
  total: number;
}

export interface AdminSnapshot {
  users: AdminList;
  roles: AdminList;
  departments: AdminList;
  menus: AdminList;
  operationLogs: AdminList;
  loginLogs: AdminList;
}

const silentHeaders = { silent: 'true' };

function emptyList(): AdminList {
  return { rows: [], total: 0 };
}

function normalizeList(response?: HttpResponse): AdminList {
  if (!response) return emptyList();
  let rows: unknown[] = [];
  if (Array.isArray(response.rows)) {
    rows = response.rows;
  } else if (Array.isArray(response.data)) {
    rows = response.data;
  }
  const total = response.total === undefined ? rows.length : Number(response.total);
  return { rows: rows as AdminRecord[], total: Number.isNaN(total) ? rows.length : total };
}

function getList(url: string, params: Record<string, unknown>) {
  return request
    .get(url, { params, headers: silentHeaders })
    .then((response) => normalizeList(response as unknown as HttpResponse))
    .catch(() => emptyList());
}

let snapshotPromise: Promise<AdminSnapshot> | undefined;

/**
 * 查询后台首页和分析页面共用的真实管理数据。
 * 权限不足的子查询会转换为空列表，避免普通账号打开工作台时被无关错误打断。
 *
 * @param force 是否忽略当前页面生命周期内的快照缓存
 * @return 管理数据快照
 */
export function queryAdminSnapshot(force = false): Promise<AdminSnapshot> {
  if (snapshotPromise && !force) return snapshotPromise;

  snapshotPromise = Promise.all([
    getList('/system/user/list', { pageNum: 1, pageSize: 100 }),
    getList('/system/role/list', { pageNum: 1, pageSize: 100 }),
    getList('/system/dept/list', {}),
    getList('/system/menu/list', {}),
    getList('/monitor/operlog/list', { pageNum: 1, pageSize: 100 }),
    getList('/monitor/logininfor/list', { pageNum: 1, pageSize: 100 }),
  ]).then(([users, roles, departments, menus, operationLogs, loginLogs]) => ({
    users,
    roles,
    departments,
    menus,
    operationLogs,
    loginLogs,
  }));

  return snapshotPromise;
}
