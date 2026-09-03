import request from '@/api/interceptor';
import type { TaskTableResponse } from './import-task';
import type { TaskRecord } from './import-task';

/** 查询导出任务。 */
export function listExportTasks(params: Record<string, unknown>): Promise<TaskTableResponse> {
  return request.get('/monitor/exportTask/list', { params });
}

/** 查询导出任务详情。 */
export function getExportTask(taskId: number) {
  return request.get<TaskRecord>(`/monitor/exportTask/${taskId}`);
}

/** 提交系统用户导出任务。 */
export function submitExportTask(data: Record<string, unknown>) {
  return request.post('/monitor/exportTask/submit', data);
}

/** 取消导出任务。 */
export function cancelExportTask(taskId: number) {
  return request.delete(`/monitor/exportTask/${taskId}`);
}

/** 下载已完成的导出文件。 */
export function downloadExportTask(taskId: number) {
  return request.get(`/monitor/exportTask/${taskId}/download`, {
    responseType: 'blob',
  });
}
