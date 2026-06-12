import axios from 'axios';
import { Message } from '@arco-design/web-vue';
import { dispatchMockRequest } from './mock';
import request from './request';
import { unwrapSystemResponse } from './system-client';
import type {
  ConfigForm,
  ConfigQuery,
  ConfigVO,
  DownloadUrlVO,
  PlatformConfigVO,
  CacheEntryDetailVO,
  CacheEntryQuery,
  CacheEntryVO,
  MonitorLogTailVO,
  MonitorSlowSqlVO,
  ServerMonitorVO,
  PlatformJobForm,
  PlatformJobLogDetailVO,
  PlatformJobLogQuery,
  PlatformJobLogVO,
  PlatformJobQuery,
  PlatformJobVO,
  DepartmentRecord,
  DeptForm,
  DeptQuery,
  DeptTreeVO,
  DictItemForm,
  DictItemVO,
  DictTypeForm,
  DictTypeQuery,
  DictTypeVO,
  EntityRecord,
  ImportExportTaskQuery,
  ImportExportTaskVO,
  LoginLogQuery,
  LoginLogVO,
  MenuForm,
  MenuQuery,
  MenuVO,
  MqMessageQuery,
  MqMessageVO,
  OpLogQuery,
  OpLogVO,
  PageResult,
  RoleForm,
  RoleQuery,
  RoleVO,
  SelectOption,
  UserForm,
  UserQuery,
  UserVO,
} from '@/types/system';
import {
  formatDateTime,
  mapConfigRow,
  mapDictItemRow,
  mapDictTypeRow,
  mapLoginLogRow,
  mapOpLogRow,
  mapRoleRow,
  mapUserRow,
  toStatusText,
  toVisibleText,
} from '@/views/system/shared';

const systemRequest = axios.create({
  baseURL: '/api',
  timeout: 8000,
  withCredentials: true,
});

systemRequest.interceptors.request.use(async (config) => {
  const useMock = import.meta.env.VITE_USE_MOCK === 'true';
  if (!useMock) {
    return config;
  }

  const result = await dispatchMockRequest({
    url: config.url,
    method: config.method,
    params: config.params as Record<string, string | number | undefined> | undefined,
    data: config.data as Record<string, string | number> | string | undefined,
  });

  config.adapter = async () => ({
    data: result,
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
    request: {},
  });

  return config;
});

systemRequest.interceptors.response.use(
  (response) => {
    try {
      return unwrapSystemResponse(response.data);
    } catch (error) {
      const normalized = error instanceof Error ? error : new Error('请求失败，请稍后重试');
      Message.error(normalized.message);
      return Promise.reject(normalized);
    }
  },
  (error) => {
    const normalized = error instanceof Error
      ? error
      : new Error(error?.response?.data?.message ?? '请求失败，请稍后重试');
    Message.error(normalized.message);
    return Promise.reject(normalized);
  },
);

function get<T>(url: string, params?: object) {
  return systemRequest.get<T, T>(url, { params });
}

function post<T>(url: string, data?: unknown) {
  return systemRequest.post<T, T>(url, data);
}

function put<T>(url: string, data?: unknown) {
  return systemRequest.put<T, T>(url, data);
}

function del<T>(url: string) {
  return systemRequest.delete<T, T>(url);
}

export const systemApi = {
  listUsers(params: UserQuery) {
    return get<PageResult<UserVO>>('/system/users', params);
  },
  getUser(id: number) {
    return get<UserVO>(`/system/users/${id}`);
  },
  createUser(payload: UserForm) {
    return post<number>('/system/users', payload);
  },
  updateUser(id: number, payload: UserForm) {
    return put<void>(`/system/users/${id}`, payload);
  },
  deleteUser(id: number) {
    return del<void>(`/system/users/${id}`);
  },

  listRoles(params: RoleQuery) {
    return get<PageResult<RoleVO>>('/system/roles', params);
  },
  listRoleOptions() {
    return get<SelectOption[]>('/system/roles/options');
  },
  getRole(id: number) {
    return get<RoleVO>(`/system/roles/${id}`);
  },
  createRole(payload: RoleForm) {
    return post<number>('/system/roles', payload);
  },
  updateRole(id: number, payload: RoleForm) {
    return put<void>(`/system/roles/${id}`, payload);
  },
  deleteRole(id: number) {
    return del<void>(`/system/roles/${id}`);
  },

  listDeptTree(params?: DeptQuery) {
    return get<DeptTreeVO[]>('/system/depts/tree', params);
  },
  listDeptOptions() {
    return get<SelectOption[]>('/system/depts/options');
  },
  getDept(id: number) {
    return get<DeptTreeVO>(`/system/depts/${id}`);
  },
  createDept(payload: DeptForm) {
    return post<number>('/system/depts', payload);
  },
  updateDept(id: number, payload: DeptForm) {
    return put<void>(`/system/depts/${id}`, payload);
  },
  deleteDept(id: number) {
    return del<void>(`/system/depts/${id}`);
  },

  listMenus(params?: MenuQuery) {
    return request.get<MenuVO[]>('/system/menus', { params });
  },
  listMenuOptions() {
    return request.get<SelectOption[]>('/system/menus/options');
  },
  getMenu(id: number) {
    return request.get<MenuVO>(`/system/menus/${id}`);
  },
  createMenu(payload: MenuForm) {
    return request.post<number>('/system/menus', payload);
  },
  updateMenu(id: number, payload: MenuForm) {
    return request.put<void>(`/system/menus/${id}`, payload);
  },
  deleteMenu(id: number) {
    return request.delete<void>(`/system/menus/${id}`);
  },

  listDictTypes(params: DictTypeQuery) {
    return get<PageResult<DictTypeVO>>('/system/dicts/types', params);
  },
  listDictTypeOptions() {
    return get<SelectOption[]>('/system/dicts/types/options');
  },
  getDictType(id: number) {
    return get<DictTypeVO>(`/system/dicts/types/${id}`);
  },
  listDictItemsByType(id: number) {
    return get<DictItemVO[]>(`/system/dicts/types/${id}/items`);
  },
  createDictType(payload: DictTypeForm) {
    return post<number>('/system/dicts/types', payload);
  },
  updateDictType(id: number, payload: DictTypeForm) {
    return put<void>(`/system/dicts/types/${id}`, payload);
  },
  deleteDictType(id: number) {
    return del<void>(`/system/dicts/types/${id}`);
  },
  createDictItem(payload: DictItemForm) {
    return post<number>('/system/dicts/items', payload);
  },
  updateDictItem(id: number, payload: DictItemForm) {
    return put<void>(`/system/dicts/items/${id}`, payload);
  },
  deleteDictItem(id: number) {
    return del<void>(`/system/dicts/items/${id}`);
  },

  listConfigs(params: ConfigQuery) {
    return get<PageResult<ConfigVO>>('/system/configs', params);
  },
  getConfig(id: number) {
    return get<ConfigVO>(`/system/configs/${id}`);
  },
  createConfig(payload: ConfigForm) {
    return post<number>('/system/configs', payload);
  },
  updateConfig(id: number, payload: ConfigForm) {
    return put<void>(`/system/configs/${id}`, payload);
  },
  deleteConfig(id: number) {
    return del<void>(`/system/configs/${id}`);
  },
  getPlatformConfig() {
    return get<PlatformConfigVO>('/system/platform-config');
  },
  updatePlatformConfig(payload: PlatformConfigVO) {
    return put<void>('/system/platform-config', payload);
  },
  listCacheEntries(params: CacheEntryQuery) {
    return get<CacheEntryVO[]>('/system/cache/entries', params);
  },
  getCacheEntryDetail(key: string) {
    return get<CacheEntryDetailVO>('/system/cache/entries/detail', { key });
  },
  getServerMonitor() {
    return get<ServerMonitorVO>('/system/monitor/server');
  },
  listMonitorSlowSql(limit = 20) {
    return get<MonitorSlowSqlVO[]>('/system/monitor/slow-sql', { limit });
  },
  getMonitorLogTail(limit = 80) {
    return get<MonitorLogTailVO>('/system/monitor/logs/tail', { limit });
  },
  listPlatformJobs(params: PlatformJobQuery) {
    return get<PageResult<PlatformJobVO>>('/system/jobs', params);
  },
  getPlatformJob(id: number) {
    return get<PlatformJobVO>(`/system/jobs/${id}`);
  },
  createPlatformJob(payload: PlatformJobForm) {
    return post<number>('/system/jobs', payload);
  },
  updatePlatformJob(id: number, payload: PlatformJobForm) {
    return put<void>(`/system/jobs/${id}`, payload);
  },
  deletePlatformJob(id: number) {
    return del<void>(`/system/jobs/${id}`);
  },
  pausePlatformJob(id: number) {
    return post<void>(`/system/jobs/${id}/pause`);
  },
  resumePlatformJob(id: number) {
    return post<void>(`/system/jobs/${id}/resume`);
  },
  triggerPlatformJob(id: number) {
    return post<void>(`/system/jobs/${id}/trigger`);
  },
  listPlatformJobHandlerOptions() {
    return get<SelectOption[]>('/system/jobs/handlers/options');
  },
  listPlatformJobLogs(id: number, params: PlatformJobLogQuery) {
    return get<PageResult<PlatformJobLogVO>>(`/system/jobs/${id}/logs`, params);
  },
  getPlatformJobLogDetail(logId: number) {
    return get<PlatformJobLogDetailVO>(`/system/jobs/logs/${logId}`);
  },

  listLoginLogs(params: LoginLogQuery) {
    return get<PageResult<LoginLogVO>>('/system/logs/login', params);
  },
  listOpLogs(params: OpLogQuery) {
    return get<PageResult<OpLogVO>>('/system/logs/op', params);
  },
  listMqMessages(params: MqMessageQuery) {
    return get<PageResult<MqMessageVO>>('/system/logs/mq-messages', params);
  },
  retryMqMessage(id: number) {
    return post<void>(`/system/logs/mq-messages/${id}/retry`);
  },
  listImportExportTasks(params: ImportExportTaskQuery) {
    return get<PageResult<ImportExportTaskVO>>('/system/io-tasks', params);
  },
  listImportExportSceneOptions(taskType: 'EXPORT' | 'IMPORT') {
    return get<SelectOption[]>('/system/io-tasks/scenes', { taskType });
  },
  getImportExportDownloadUrl(id: number, fileRole: 'SOURCE' | 'RESULT') {
    return get<DownloadUrlVO>(`/system/io-tasks/${id}/download-url`, { fileRole });
  },
};

function toPageQuery(keyword = '') {
  return {
    pageNum: 1,
    pageSize: 200,
    keyword,
  };
}

function toUserEntity(row: ReturnType<typeof mapUserRow>): EntityRecord {
  return {
    id: row.id,
    name: row.nickname,
    status: row.statusText as '启用' | '停用',
    remark: row.roleCodesText,
    updatedAt: row.createTimeText,
    username: row.username,
    deptName: row.deptName,
  };
}

function toRoleEntity(row: ReturnType<typeof mapRoleRow>): EntityRecord {
  return {
    id: row.id,
    name: row.roleName,
    status: row.statusText as '启用' | '停用',
    remark: row.dataScopeText,
    updatedAt: row.createTimeText,
    roleCode: row.roleCode,
  };
}

function toDeptRecord(dept: DeptTreeVO): DepartmentRecord {
  return {
    id: dept.id,
    name: dept.deptName,
    parentId: dept.parentId,
    leader: '--',
    orderNum: dept.sort ?? 0,
    status: toStatusText(dept.status) as '启用' | '停用',
    remark: '',
    updatedAt: '--',
    children: dept.children.map(toDeptRecord),
  };
}

function toMenuEntity(menu: MenuVO): EntityRecord {
  return {
    id: menu.id,
    name: menu.menuName,
    status: toStatusText(menu.status) as '启用' | '停用',
    remark: menu.perms ?? '--',
    updatedAt: formatDateTime(menu.createTime),
    menuType: menu.menuType,
    routePath: menu.routePath ?? '--',
    component: menu.component ?? '--',
    visible: toVisibleText(menu.visible),
  };
}

function toDictTypeEntity(row: ReturnType<typeof mapDictTypeRow>): EntityRecord {
  return {
    id: row.id,
    name: row.dictName,
    status: row.statusText as '启用' | '停用',
    remark: row.dictCode,
    updatedAt: row.createTimeText,
  };
}

function toDictItemEntity(row: ReturnType<typeof mapDictItemRow>, dictCode: string): EntityRecord {
  return {
    id: row.id,
    name: row.itemLabel,
    status: row.statusText as '启用' | '停用',
    remark: dictCode,
    updatedAt: '--',
    dictLabel: row.itemLabel,
    dictValue: row.itemValue,
  };
}

function toConfigEntity(row: ReturnType<typeof mapConfigRow>): EntityRecord {
  return {
    id: row.id,
    name: row.configName,
    status: row.statusText as '启用' | '停用',
    remark: `${row.configKey}=${row.configValue}`,
    updatedAt: row.createTimeText,
  };
}

function toLoginLogEntity(row: ReturnType<typeof mapLoginLogRow>): EntityRecord {
  return {
    id: row.id,
    name: row.loginStatus,
    status: '启用',
    remark: row.message,
    updatedAt: row.createTimeText,
    operator: row.username,
    ip: row.ip,
  };
}

function toOpLogEntity(row: ReturnType<typeof mapOpLogRow>): EntityRecord {
  return {
    id: row.id,
    name: `${row.module} / ${row.action}`,
    status: '启用',
    remark: `${row.successText} ${row.costMsText}`,
    updatedAt: row.createTimeText,
    operator: row.operatorName,
    ip: row.requestUri,
  };
}

export async function getEntityList(key: string, keyword = '') {
  switch (key) {
    case 'users': {
      const result = await systemApi.listUsers({ ...toPageQuery(keyword), username: keyword, nickname: keyword });
      return result.records.map((item) => toUserEntity(mapUserRow(item)));
    }
    case 'roles': {
      const result = await systemApi.listRoles({ ...toPageQuery(keyword), roleCode: keyword, roleName: keyword });
      return result.records.map((item) => toRoleEntity(mapRoleRow(item)));
    }
    case 'depts': {
      const result = await systemApi.listDeptTree({ deptName: keyword || undefined });
      return result.map(toDeptRecord);
    }
    case 'menus': {
      const result = await systemApi.listMenus({ menuName: keyword || undefined });
      return result.map(toMenuEntity);
    }
    case 'dictTypes': {
      const result = await systemApi.listDictTypes({ ...toPageQuery(keyword), dictName: keyword, dictCode: keyword });
      return result.records.map((item) => toDictTypeEntity(mapDictTypeRow(item)));
    }
    case 'dictItems': {
      const types = await systemApi.listDictTypes({ pageNum: 1, pageSize: 200 });
      const groups = await Promise.all(
        types.records.map(async (type) => ({
          dictCode: type.dictCode,
          items: await systemApi.listDictItemsByType(type.id),
        })),
      );
      return groups.flatMap((group) => group.items.map((item) => toDictItemEntity(mapDictItemRow(item), group.dictCode)));
    }
    case 'params': {
      const result = await systemApi.listConfigs({ ...toPageQuery(keyword), configName: keyword, configKey: keyword });
      return result.records.map((item) => toConfigEntity(mapConfigRow(item)));
    }
    case 'loginLogs': {
      const result = await systemApi.listLoginLogs({ ...toPageQuery(keyword), username: keyword });
      return result.records.map((item) => toLoginLogEntity(mapLoginLogRow(item)));
    }
    case 'opLogs': {
      const result = await systemApi.listOpLogs({ ...toPageQuery(keyword), module: keyword, action: keyword, operatorName: keyword });
      return result.records.map((item) => toOpLogEntity(mapOpLogRow(item)));
    }
    default:
      return [];
  }
}

export async function addEntity(_key: string, _payload: Record<string, unknown>) {
  throw new Error('当前页面暂未接入通用写操作，请使用对应业务页面处理');
}

export async function editEntity(_key: string, _id: number, _payload: Record<string, unknown>) {
  throw new Error('当前页面暂未接入通用写操作，请使用对应业务页面处理');
}

export async function deleteEntity(_key: string, _id: number) {
  throw new Error('当前页面暂未接入通用写操作，请使用对应业务页面处理');
}
