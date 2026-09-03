import request, { HttpResponse } from '@/api/interceptor';

export interface TaskRecord {
  taskId: number;
  handlerType: string;
  status: string;
  fileName?: string;
  requestedBy: number;
  totalCount: number;
  processedCount: number;
  successCount: number;
  failureCount: number;
  errorMessage?: string;
  startedTime?: string;
  finishedTime?: string;
  createTime?: string;
}

export interface TaskTableResponse extends HttpResponse<TaskRecord> {
  rows?: TaskRecord[];
  total?: number;
}

/** 查询导入任务。 */
export function listImportTasks(params: Record<string, unknown>): Promise<TaskTableResponse> {
  return request.get('/monitor/importTask/list', { params });
}

/** 查询导入任务详情。 */
export function getImportTask(taskId: number) {
  return request.get<TaskRecord>(`/monitor/importTask/${taskId}`);
}

/** 提交系统用户导入任务。 */
export function submitImportTask(file: Blob, updateSupport: boolean) {
  const data = new FormData();
  data.append('file', file);
  data.append('updateSupport', String(updateSupport));
  return request.post('/monitor/importTask/submit', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

/** 取消导入任务。 */
export function cancelImportTask(taskId: number) {
  return request.delete(`/monitor/importTask/${taskId}`);
}
