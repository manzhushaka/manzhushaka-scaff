import request from './interceptor';
import type { QueryParams, TableResponse } from './types';
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
