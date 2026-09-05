import request from './interceptor';
import type { QueryParams, TableResponse } from './types';

export function listRoles(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/role/list', { params });
}

export function getRole(id: string | number) { return request.get(`/system/role/${id}`); }
export function createRole(data: QueryParams) { return request.post('/system/role', data); }
export function updateRole(data: QueryParams) { return request.put('/system/role', data); }
export function removeRoles(ids: Array<string | number>) { return request.delete(`/system/role/${ids.join(',')}`); }
export function changeRoleStatus(data: QueryParams) { return request.put('/system/role/changeStatus', data); }
export function updateRoleDataScope(data: QueryParams) { return request.put('/system/role/dataScope', data); }
export function getRoleDeptTree(id: string | number) { return request.get(`/system/role/deptTree/${id}`); }

export function listAllocatedUsers(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/role/authUser/allocatedList', { params });
}

export function listUnallocatedUsers(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/role/authUser/unallocatedList', { params });
}

export function cancelRoleUser(data: QueryParams) { return request.put('/system/role/authUser/cancel', data); }

export function cancelAllRoleUsers(params: QueryParams) {
  const query = { ...params };
  if (Array.isArray(query.userIds)) query.userIds = query.userIds.join(',');
  return request.put('/system/role/authUser/cancelAll', undefined, { params: query });
}

export function selectAllRoleUsers(params: QueryParams) {
  const query = { ...params };
  if (Array.isArray(query.userIds)) query.userIds = query.userIds.join(',');
  return request.put('/system/role/authUser/selectAll', undefined, { params: query });
}
