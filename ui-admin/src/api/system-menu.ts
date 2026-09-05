import request from './interceptor';
import type { HttpResponse } from './interceptor';
import type { QueryParams } from './types';
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
