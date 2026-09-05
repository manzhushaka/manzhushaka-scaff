import request from './interceptor';
import type { QueryParams } from './types';

export type { QueryParams, TableResponse, UserAuthRolesResponse } from './types';

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
export { changeUserStatus, createUser, getUser, getUserAuthRoles, getUserDeptTree, listUsers, removeUsers, resetUserPassword, updateUser, updateUserAuthRoles, uploadUserAvatar } from './system-user';
export { cancelAllRoleUsers, cancelRoleUser, changeRoleStatus, createRole, getRole, getRoleDeptTree, listAllocatedUsers, listRoles, listUnallocatedUsers, removeRoles, selectAllRoleUsers, updateRole, updateRoleDataScope } from './system-role';
export { listMenus, getMenu, getMenuTree, getRoleMenuTree, createMenu, updateMenu, removeMenu } from './system-menu';
export { listDepartments, getDepartmentTree, getDepartment, createDepartment, updateDepartment, removeDepartment } from './system-department';
export { listDictTypes, getDictType, createDictType, updateDictType, removeDictTypes, refreshDictCache, listDictData, getDictData, createDictData, updateDictData, removeDictData, exportDictData } from './system-dict';
export { listConfigs, getConfig, createConfig, updateConfig, removeConfigs, refreshConfigCache } from './system-config';
export { listOnlineUsers, forceLogout, listOperationLogs, getOperationLog, listLoginLogs, removeOperationLogs, removeLoginLogs, cleanOperationLogs, cleanLoginLogs, unlockLoginUser, listSlowSqlLogs, getSlowSql, removeSlowSqlLogs, cleanSlowSqlLogs, listMqLogs, getMqLog, listMqLogDetails, removeMqLogs, cleanMqLogs } from './monitor-logs';
export { listJobs, getJob, createJob, updateJob, removeJobs, changeJobStatus, runJob, listJobLogs, listJobLogDetails, removeJobLogs, cleanJobLogs } from './monitor-jobs';
export { getServerInfo, getCacheInfo, listCacheNames, listCacheKeys, getCacheValue, clearCacheName, clearCacheKey, clearCacheAll, listRuntimeLogs, listRuntimeLogFiles, downloadRuntimeLog } from './monitor-system';
export { getProfile, updateProfile, updateProfilePassword } from './system-profile';
