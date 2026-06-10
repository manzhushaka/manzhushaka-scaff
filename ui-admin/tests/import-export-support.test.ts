import test from 'node:test';
import assert from 'node:assert/strict';
import { mapImportExportTaskRow, toImportExportTaskStatusText } from '../src/views/system/import-export-support.ts';

test('formats async task statuses for the import export pages', () => {
  assert.equal(toImportExportTaskStatusText('PENDING'), '待执行');
  assert.equal(toImportExportTaskStatusText('PROCESSING'), '执行中');
  assert.equal(toImportExportTaskStatusText('SUCCESS'), '成功');
  assert.equal(toImportExportTaskStatusText('FAIL'), '失败');
});

test('maps import export task rows for the task tables', () => {
  assert.deepEqual(
    mapImportExportTaskRow({
      id: 1,
      taskNo: 'EXP-20260609190000-AAAAAA',
      taskType: 'EXPORT',
      bizType: 'SYS_USER_EXPORT',
      bizLabel: '系统用户导出示例',
      taskName: '系统用户导出',
      taskStatus: 'PROCESSING',
      taskMessage: '任务处理中',
      sourceFileName: null,
      resultFileName: 'sys-users-export.csv',
      totalCount: 12,
      successCount: 10,
      failCount: 2,
      createBy: 'admin',
      createTime: '2026-06-09T19:00:00',
      finishedTime: null,
    }),
    {
      id: 1,
      taskNo: 'EXP-20260609190000-AAAAAA',
      taskType: 'EXPORT',
      bizLabel: '系统用户导出示例',
      taskName: '系统用户导出',
      taskStatusText: '执行中',
      taskStatusValue: 'PROCESSING',
      taskMessage: '任务处理中',
      sourceFileName: '--',
      resultFileName: 'sys-users-export.csv',
      countSummary: '12 / 10 / 2',
      createBy: 'admin',
      createTimeText: '2026-06-09 19:00:00',
      finishedTimeText: '--',
    },
  );
});
