import type { ImportExportTaskRow, ImportExportTaskVO } from '@/types/system';

export const importExportTaskStatusOptions = [
  { label: '待执行', value: 'PENDING' },
  { label: '执行中', value: 'PROCESSING' },
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAIL' },
];

export function toImportExportTaskStatusText(status: string | null | undefined) {
  switch (status) {
    case 'PENDING':
      return '待执行';
    case 'PROCESSING':
      return '执行中';
    case 'SUCCESS':
      return '成功';
    case 'FAIL':
      return '失败';
    default:
      return '--';
  }
}

export function mapImportExportTaskRow(task: ImportExportTaskVO): ImportExportTaskRow {
  return {
    id: task.id,
    taskNo: task.taskNo,
    taskType: task.taskType,
    bizLabel: task.bizLabel,
    taskName: task.taskName,
    taskStatusText: toImportExportTaskStatusText(task.taskStatus),
    taskStatusValue: task.taskStatus ?? '',
    taskMessage: task.taskMessage ?? '--',
    sourceFileName: task.sourceFileName ?? '--',
    resultFileName: task.resultFileName ?? '--',
    countSummary: `${task.totalCount ?? 0} / ${task.successCount ?? 0} / ${task.failCount ?? 0}`,
    createBy: task.createBy ?? '--',
    createTimeText: formatTaskDateTime(task.createTime),
    finishedTimeText: formatTaskDateTime(task.finishedTime),
  };
}

function formatTaskDateTime(value: string | null | undefined) {
  if (!value) {
    return '--';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  const second = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
}
