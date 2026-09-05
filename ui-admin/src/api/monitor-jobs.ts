import request from './interceptor';
import type { QueryParams, TableResponse } from './types';
/** 查询定时任务。 */
export function listJobs(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/monitor/job/list', { params });
}
/** 查询定时任务详情。 */
export function getJob(id: string | number) {
  return request.get(`/monitor/job/${id}`);
}

/** 新增定时任务。 */
export function createJob(data: QueryParams) {
  return request.post('/monitor/job', data);
}

/** 修改定时任务。 */
export function updateJob(data: QueryParams) {
  return request.put('/monitor/job', data);
}

/** 删除定时任务。 */
export function removeJobs(ids: Array<string | number>) {
  return request.delete(`/monitor/job/${ids.join(',')}`);
}

/** 修改定时任务状态。 */
export function changeJobStatus(data: QueryParams) {
  return request.put('/monitor/job/changeStatus', data);
}

/** 立即执行定时任务。 */
export function runJob(data: QueryParams) {
  return request.put('/monitor/job/run', data);
}

/** 查询调度日志。 */
export function listJobLogs(params: QueryParams): Promise<TableResponse<Record<string, unknown>>> {
  return request.get('/monitor/jobLog/list', { params });
}

/** 查询调度日志明细。 */
export function listJobLogDetails(id: string | number) {
  return request.get(`/monitor/jobLog/${id}/details`);
}

/** 删除调度日志。 */
export function removeJobLogs(ids: Array<string | number>) {
  return request.delete(`/monitor/jobLog/${ids.join(',')}`);
}

/** 清空调度日志。 */
export function cleanJobLogs() {
  return request.delete('/monitor/jobLog/clean');
}
