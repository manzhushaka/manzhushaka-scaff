import type { ImportExportTaskRow, ImportExportTaskVO } from '@/types/system';
import { formatStandardDateTime } from '@/utils/date-time';

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
  return formatStandardDateTime(value);
}
