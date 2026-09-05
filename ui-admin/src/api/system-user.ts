import request from './interceptor';
import type { HttpResponse } from './interceptor';
import type { QueryParams, TableResponse, UserAuthRolesResponse } from './types';

export function listUsers(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/system/user/list', { params });
}

export function getUser(id: string | number) { return request.get(`/system/user/${id}`); }
export function createUser(data: QueryParams) { return request.post('/system/user', data); }
export function updateUser(data: QueryParams) { return request.put('/system/user', data); }
export function removeUsers(ids: Array<string | number>) { return request.delete(`/system/user/${ids.join(',')}`); }
export function changeUserStatus(data: QueryParams) { return request.put('/system/user/changeStatus', data); }
export function resetUserPassword(data: QueryParams) { return request.put('/system/user/resetPwd', data); }

export function getUserAuthRoles(id: string | number): Promise<HttpResponse<UserAuthRolesResponse>> {
  return request.get(`/system/user/authRole/${id}`);
}

export function updateUserAuthRoles(data: QueryParams) {
  const params = { ...data };
  if (Array.isArray(params.roleIds)) params.roleIds = params.roleIds.join(',');
  return request.put('/system/user/authRole', undefined, { params });
}

export function uploadUserAvatar(file: FormData) {
  return request.post('/system/user/profile/avatar', file, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function getUserDeptTree(params: QueryParams = {}) {
  return request.get('/system/user/deptTree', { params });
}
