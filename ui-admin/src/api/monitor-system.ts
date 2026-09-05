import request from './interceptor';
import type { QueryParams } from './types';
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
