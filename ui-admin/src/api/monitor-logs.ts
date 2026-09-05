import request from './interceptor';
import type { QueryParams, TableResponse } from './types';
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

/** 查询操作日志详情。 */
export function getOperationLog(id: string | number) {
  return request.get(`/monitor/operlog/${id}`);
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
