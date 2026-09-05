import request from './interceptor';
import type { QueryParams, TableResponse } from './types';
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
