import request from './interceptor';
import type { HttpResponse } from './interceptor';
import type { QueryParams } from './types';
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
