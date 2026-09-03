import request, { HttpResponse } from './interceptor';

export type QueryParams = Record<string, unknown>;

export interface BackendRoute {
  name: string;
  path: string;
  hidden?: boolean;
  redirect?: string;
  component?: string;
  query?: string;
  alwaysShow?: boolean;
  meta?: {
    title?: string;
    icon?: string;
    noCache?: boolean;
    link?: string;
  };
  children?: BackendRoute[];
}

export interface TableResponse<T> extends HttpResponse<T> {
  rows?: T[];
  total?: number;
}

export interface UserAuthRolesResponse {
  user?: Record<string, unknown>;
  roles?: Record<string, unknown>[];
}

export interface TreeNode {
  id?: number | string;
  label?: string;
  children?: TreeNode[];
  [key: string]: unknown;
}

/** 登录并返回后端令牌。 */
export function login(data: QueryParams) {
  return request.post('/login', data, {
    headers: { isToken: 'false', repeatSubmit: 'false' },
  });
}

/** 注册系统用户。 */
export function register(data: QueryParams) {
  return request.post('/register', data, { headers: { isToken: 'false' } });
}

/** 解锁当前屏幕。 */
export function unlockScreen(password: string) {
  return request.post('/unlockscreen', { password });
}

/** 查询用户列表。 */
export function listUsers(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/user/list', { params });
}

/** 查询用户详情。 */
export function getUser(id: string | number) {
  return request.get(`/system/user/${id}`);
}

/** 新增用户。 */
export function createUser(data: QueryParams) {
  return request.post('/system/user', data);
}

/** 修改用户。 */
export function updateUser(data: QueryParams) {
  return request.put('/system/user', data);
}

/** 删除用户。 */
export function removeUsers(ids: Array<string | number>) {
  return request.delete(`/system/user/${ids.join(',')}`);
}

/** 修改用户状态。 */
export function changeUserStatus(data: QueryParams) {
  return request.put('/system/user/changeStatus', data);
}

/** 重置用户密码。 */
export function resetUserPassword(data: QueryParams) {
  return request.put('/system/user/resetPwd', data);
}

/** 查询用户授权角色。 */
export function getUserAuthRoles(id: string | number): Promise<HttpResponse<UserAuthRolesResponse>> {
  return request.get(`/system/user/authRole/${id}`);
}

/** 保存用户授权角色。 */
export function updateUserAuthRoles(data: QueryParams) {
  const params = { ...data };
  if (Array.isArray(params.roleIds)) {
    params.roleIds = params.roleIds.join(',');
  }
  return request.put('/system/user/authRole', undefined, { params });
}

/** 上传当前用户头像。 */
export function uploadUserAvatar(file: FormData) {
  return request.post('/system/user/profile/avatar', file, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

/** 查询部门树。 */
export function getUserDeptTree(params: QueryParams = {}) {
  return request.get('/system/user/deptTree', { params });
}

/** 查询角色列表。 */
export function listRoles(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/role/list', { params });
}

/** 查询角色详情。 */
export function getRole(id: string | number) {
  return request.get(`/system/role/${id}`);
}

/** 新增角色。 */
export function createRole(data: QueryParams) {
  return request.post('/system/role', data);
}

/** 修改角色。 */
export function updateRole(data: QueryParams) {
  return request.put('/system/role', data);
}

/** 删除角色。 */
export function removeRoles(ids: Array<string | number>) {
  return request.delete(`/system/role/${ids.join(',')}`);
}

/** 修改角色状态。 */
export function changeRoleStatus(data: QueryParams) {
  return request.put('/system/role/changeStatus', data);
}

/** 修改角色数据权限。 */
export function updateRoleDataScope(data: QueryParams) {
  return request.put('/system/role/dataScope', data);
}

/** 查询角色部门树。 */
export function getRoleDeptTree(id: string | number) {
  return request.get(`/system/role/deptTree/${id}`);
}

/** 查询角色已分配用户。 */
export function listAllocatedUsers(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/role/authUser/allocatedList', { params });
}

/** 查询角色未分配用户。 */
export function listUnallocatedUsers(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/role/authUser/unallocatedList', { params });
}

/** 取消用户角色授权。 */
export function cancelRoleUser(data: QueryParams) {
  return request.put('/system/role/authUser/cancel', data);
}

/** 批量取消用户角色授权。 */
export function cancelAllRoleUsers(params: QueryParams) {
  const query = { ...params };
  if (Array.isArray(query.userIds)) {
    query.userIds = query.userIds.join(',');
  }
  return request.put('/system/role/authUser/cancelAll', undefined, { params: query });
}

/** 批量授予用户角色。 */
export function selectAllRoleUsers(params: QueryParams) {
  const query = { ...params };
  if (Array.isArray(query.userIds)) {
    query.userIds = query.userIds.join(',');
  }
  return request.put('/system/role/authUser/selectAll', undefined, { params: query });
}

/** 查询菜单列表。 */
export function listMenus(params: QueryParams): Promise<HttpResponse<Record<string, unknown>[]>> {
  return request.get('/system/menu/list', { params });
}

/** 查询菜单详情。 */
export function getMenu(id: string | number) {
  return request.get(`/system/menu/${id}`);
}

/** 查询菜单树。 */
export function getMenuTree() {
  return request.get('/system/menu/treeselect');
}

/** 查询角色菜单树。 */
export function getRoleMenuTree(id: string | number) {
  return request.get(`/system/menu/roleMenuTreeselect/${id}`);
}

/** 新增菜单。 */
export function createMenu(data: QueryParams) {
  return request.post('/system/menu', data);
}

/** 修改菜单。 */
export function updateMenu(data: QueryParams) {
  return request.put('/system/menu', data);
}

/** 删除菜单。 */
export function removeMenu(id: string | number) {
  return request.delete(`/system/menu/${id}`);
}

/** 查询部门列表。 */
export function listDepartments(params: QueryParams): Promise<HttpResponse<Record<string, unknown>[]>> {
  return request.get('/system/dept/list', { params });
}

/** 查询部门树。 */
export function getDepartmentTree(params: QueryParams = {}) {
  return request.get('/system/dept/tree', { params });
}

/** 查询部门详情。 */
export function getDepartment(id: string | number) {
  return request.get(`/system/dept/${id}`);
}

/** 新增部门。 */
export function createDepartment(data: QueryParams) {
  return request.post('/system/dept', data);
}

/** 修改部门。 */
export function updateDepartment(data: QueryParams) {
  return request.put('/system/dept', data);
}

/** 删除部门。 */
export function removeDepartment(id: string | number) {
  return request.delete(`/system/dept/${id}`);
}

/** 查询字典类型列表。 */
export function listDictTypes(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/dict/type/list', { params });
}

/** 查询字典类型详情。 */
export function getDictType(id: string | number) {
  return request.get(`/system/dict/type/${id}`);
}

/** 新增字典类型。 */
export function createDictType(data: QueryParams) {
  return request.post('/system/dict/type', data);
}

/** 修改字典类型。 */
export function updateDictType(data: QueryParams) {
  return request.put('/system/dict/type', data);
}

/** 删除字典类型。 */
export function removeDictTypes(ids: Array<string | number>) {
  return request.delete(`/system/dict/type/${ids.join(',')}`);
}

/** 刷新字典缓存。 */
export function refreshDictCache() {
  return request.delete('/system/dict/type/refreshCache');
}

/** 查询字典数据列表。 */
export function listDictData(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/dict/data/list', { params });
}

/** 查询字典数据详情。 */
export function getDictData(id: string | number) {
  return request.get(`/system/dict/data/${id}`);
}

/** 新增字典数据。 */
export function createDictData(data: QueryParams) {
  return request.post('/system/dict/data', data);
}

/** 修改字典数据。 */
export function updateDictData(data: QueryParams) {
  return request.put('/system/dict/data', data);
}

/** 删除字典数据。 */
export function removeDictData(ids: Array<string | number>) {
  return request.delete(`/system/dict/data/${ids.join(',')}`);
}

/** 导出字典数据。 */
export function exportDictData(params: QueryParams) {
  return request.post('/system/dict/data/export', params, {
    responseType: 'blob',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    transformRequest: [(values) => {
      const searchParams = new URLSearchParams();
      Object.entries(values || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          searchParams.append(key, String(value));
        }
      });
      return searchParams.toString();
    }],
  });
}

/** 查询参数配置列表。 */
export function listConfigs(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/config/list', { params });
}

/** 查询参数配置详情。 */
export function getConfig(id: string | number) {
  return request.get(`/system/config/${id}`);
}

/** 新增参数配置。 */
export function createConfig(data: QueryParams) {
  return request.post('/system/config', data);
}

/** 修改参数配置。 */
export function updateConfig(data: QueryParams) {
  return request.put('/system/config', data);
}

/** 删除参数配置。 */
export function removeConfigs(ids: Array<string | number>) {
  return request.delete(`/system/config/${ids.join(',')}`);
}

/** 刷新参数缓存。 */
export function refreshConfigCache() {
  return request.delete('/system/config/refreshCache');
}

/** 查询在线用户。 */
export function listOnlineUsers(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/monitor/online/list', { params });
}

/** 强制退出在线用户。 */
export function forceLogout(tokenId: string) {
  return request.delete(`/monitor/online/${encodeURIComponent(tokenId)}`);
}

/** 查询操作日志。 */
export function listOperationLogs(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/monitor/operlog/list', { params });
}

/** 查询登录日志。 */
export function listLoginLogs(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/monitor/logininfor/list', { params });
}

/** 删除操作日志。 */
export function removeOperationLogs(ids: Array<string | number>) {
  return request.delete(`/monitor/operlog/${ids.join(',')}`);
}

/** 删除登录日志。 */
export function removeLoginLogs(ids: Array<string | number>) {
  return request.delete(`/monitor/logininfor/${ids.join(',')}`);
}

/** 清空操作日志。 */
export function cleanOperationLogs() {
  return request.delete('/monitor/operlog/clean');
}

/** 清空登录日志。 */
export function cleanLoginLogs() {
  return request.delete('/monitor/logininfor/clean');
}

/** 解锁登录账户。 */
export function unlockLoginUser(userName: string) {
  return request.get(`/monitor/logininfor/unlock/${encodeURIComponent(userName)}`);
}

/** 查询慢 SQL 日志。 */
export function listSlowSqlLogs(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/monitor/slowSql/list', { params });
}

/** 查询慢 SQL 详情。 */
export function getSlowSql(id: string | number) {
  return request.get(`/monitor/slowSql/${id}`);
}

/** 删除慢 SQL 日志。 */
export function removeSlowSqlLogs(ids: Array<string | number>) {
  return request.delete(`/monitor/slowSql/${ids.join(',')}`);
}

/** 清空慢 SQL 日志。 */
export function cleanSlowSqlLogs() {
  return request.delete('/monitor/slowSql/clean');
}

/** 查询消息队列台账。 */
export function listMqLogs(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/monitor/mqLog/list', { params });
}

/** 查询消息队列台账详情。 */
export function getMqLog(id: string | number) {
  return request.get(`/monitor/mqLog/${id}`);
}

/** 查询消息队列执行明细。 */
export function listMqLogDetails(id: string | number) {
  return request.get(`/monitor/mqLog/${id}/details`);
}

/** 删除消息队列台账。 */
export function removeMqLogs(ids: Array<string | number>) {
  return request.delete(`/monitor/mqLog/${ids.join(',')}`);
}

/** 清空消息队列台账。 */
export function cleanMqLogs() {
  return request.delete('/monitor/mqLog/clean');
}

/** 查询定时任务。 */
export function listJobs(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/monitor/job/list', { params });
}

/** 查询定时任务详情。 */
export function getJob(id: string | number) {
  return request.get(`/monitor/job/${id}`);
}

/** 新增定时任务。 */
export function createJob(data: QueryParams) {
  return request.post('/monitor/job', data);
}

/** 修改定时任务。 */
export function updateJob(data: QueryParams) {
  return request.put('/monitor/job', data);
}

/** 删除定时任务。 */
export function removeJobs(ids: Array<string | number>) {
  return request.delete(`/monitor/job/${ids.join(',')}`);
}

/** 修改定时任务状态。 */
export function changeJobStatus(data: QueryParams) {
  return request.put('/monitor/job/changeStatus', data);
}

/** 立即执行定时任务。 */
export function runJob(data: QueryParams) {
  return request.put('/monitor/job/run', data);
}

/** 查询调度日志。 */
export function listJobLogs(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/monitor/jobLog/list', { params });
}

/** 查询调度日志明细。 */
export function listJobLogDetails(id: string | number) {
  return request.get(`/monitor/jobLog/${id}/details`);
}

/** 删除调度日志。 */
export function removeJobLogs(ids: Array<string | number>) {
  return request.delete(`/monitor/jobLog/${ids.join(',')}`);
}

/** 清空调度日志。 */
export function cleanJobLogs() {
  return request.delete('/monitor/jobLog/clean');
}

/** 查询服务器监控信息。 */
export function getServerInfo() {
  return request.get('/monitor/server');
}

/** 查询缓存监控信息。 */
export function getCacheInfo() {
  return request.get('/monitor/cache');
}

/** 查询缓存名称。 */
export function listCacheNames() {
  return request.get('/monitor/cache/getNames');
}

/** 查询缓存键。 */
export function listCacheKeys(cacheName: string) {
  return request.get(`/monitor/cache/getKeys/${encodeURIComponent(cacheName)}`);
}

/** 查询缓存值。 */
export function getCacheValue(cacheName: string, cacheKey: string) {
  return request.get(`/monitor/cache/getValue/${encodeURIComponent(cacheName)}/${encodeURIComponent(cacheKey)}`);
}

/** 清理缓存名称。 */
export function clearCacheName(cacheName: string) {
  return request.delete(`/monitor/cache/clearCacheName/${encodeURIComponent(cacheName)}`);
}

/** 清理缓存键。 */
export function clearCacheKey(cacheKey: string) {
  return request.delete(`/monitor/cache/clearCacheKey/${encodeURIComponent(cacheKey)}`);
}

/** 清理全部缓存。 */
export function clearCacheAll() {
  return request.delete('/monitor/cache/clearCacheAll');
}

/** 查询运行日志文件内容。 */
export function listRuntimeLogs(params: QueryParams) {
  return request.get('/monitor/runtimeLog/list', { params });
}

/** 查询可用运行日志文件。 */
export function listRuntimeLogFiles() {
  return request.get('/monitor/runtimeLog/files');
}

/** 下载运行日志。 */
export function downloadRuntimeLog(fileName: string) {
  return request.get('/monitor/runtimeLog/download', {
    params: { fileName },
    responseType: 'blob',
  });
}

/** 查询个人资料。 */
export function getProfile() {
  return request.get('/system/user/profile');
}

/** 修改个人资料。 */
export function updateProfile(data: QueryParams) {
  return request.put('/system/user/profile', data);
}

/** 修改个人密码。 */
export function updateProfilePassword(data: QueryParams) {
  return request.put('/system/user/profile/updatePwd', data);
}
