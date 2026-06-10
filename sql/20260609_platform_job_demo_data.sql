SET NAMES utf8mb4;

INSERT INTO `sys_job` (
  `id`, `job_name`, `handler_name`, `cron_expression`, `status`, `job_param`, `remark`,
  `last_run_status`, `last_trigger_time`, `next_trigger_time`,
  `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  500001,
  '平台心跳巡检示例',
  'platformHeartbeatJob',
  '0 0/5 * * * ?',
  1,
  '{"scene":"demo","message":"heartbeat-ok"}',
  '演示任务：每 5 分钟执行一次，主要用于展示列表和成功日志。',
  'SUCCESS',
  DATE_SUB(NOW(), INTERVAL 12 MINUTE),
  DATE_ADD(NOW(), INTERVAL 3 MINUTE),
  'system',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_job` WHERE `id` = 500001
);

INSERT INTO `sys_job` (
  `id`, `job_name`, `handler_name`, `cron_expression`, `status`, `job_param`, `remark`,
  `last_run_status`, `last_trigger_time`, `next_trigger_time`,
  `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  500002,
  '夜间汇总停用示例',
  'platformHeartbeatJob',
  '0 0 2 * * ?',
  0,
  '{"scene":"demo","message":"nightly-summary"}',
  '演示任务：当前停用，用于展示暂停状态和失败日志。',
  'FAIL',
  DATE_SUB(NOW(), INTERVAL 1 DAY),
  NULL,
  'system',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_job` WHERE `id` = 500002
);

INSERT INTO `sys_job_log` (
  `id`, `job_id`, `job_name_snapshot`, `handler_name_snapshot`, `trigger_type`, `run_status`,
  `start_time`, `end_time`, `cost_ms`, `executor_host`, `error_msg`, `log_content`,
  `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  600001,
  500001,
  '平台心跳巡检示例',
  'platformHeartbeatJob',
  'SCHEDULE',
  'SUCCESS',
  DATE_SUB(NOW(), INTERVAL 12 MINUTE),
  DATE_ADD(DATE_SUB(NOW(), INTERVAL 12 MINUTE), INTERVAL 180000 MICROSECOND),
  180,
  'demo-node-01',
  NULL,
  '[2026-06-09 20:36:00] [INFO] 任务开始执行。' '\n'
  '[2026-06-09 20:36:00] [INFO] 平台心跳任务开始执行，任务名称：平台心跳巡检示例' '\n'
  '[2026-06-09 20:36:00] [INFO] 当前触发方式：SCHEDULE' '\n'
  '[2026-06-09 20:36:00] [INFO] 任务参数：{"scene":"demo","message":"heartbeat-ok"}' '\n'
  '[2026-06-09 20:36:00] [INFO] 平台心跳任务执行完成。' '\n'
  '[2026-06-09 20:36:00] [INFO] 任务执行完成。',
  'system',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_job_log` WHERE `id` = 600001
);

INSERT INTO `sys_job_log` (
  `id`, `job_id`, `job_name_snapshot`, `handler_name_snapshot`, `trigger_type`, `run_status`,
  `start_time`, `end_time`, `cost_ms`, `executor_host`, `error_msg`, `log_content`,
  `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  600002,
  500001,
  '平台心跳巡检示例',
  'platformHeartbeatJob',
  'MANUAL',
  'SUCCESS',
  DATE_SUB(NOW(), INTERVAL 4 MINUTE),
  DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 MINUTE), INTERVAL 95000 MICROSECOND),
  95,
  'demo-node-01',
  NULL,
  '[2026-06-09 20:44:00] [INFO] 任务开始执行。' '\n'
  '[2026-06-09 20:44:00] [INFO] 平台心跳任务开始执行，任务名称：平台心跳巡检示例' '\n'
  '[2026-06-09 20:44:00] [INFO] 当前触发方式：MANUAL' '\n'
  '[2026-06-09 20:44:00] [INFO] 任务参数：{"scene":"demo","message":"heartbeat-ok"}' '\n'
  '[2026-06-09 20:44:00] [INFO] 平台心跳任务执行完成。' '\n'
  '[2026-06-09 20:44:00] [INFO] 任务执行完成。',
  'system',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_job_log` WHERE `id` = 600002
);

INSERT INTO `sys_job_log` (
  `id`, `job_id`, `job_name_snapshot`, `handler_name_snapshot`, `trigger_type`, `run_status`,
  `start_time`, `end_time`, `cost_ms`, `executor_host`, `error_msg`, `log_content`,
  `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  600003,
  500002,
  '夜间汇总停用示例',
  'platformHeartbeatJob',
  'SCHEDULE',
  'FAIL',
  DATE_SUB(NOW(), INTERVAL 1 DAY),
  DATE_ADD(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 1320000 MICROSECOND),
  1320,
  'demo-node-02',
  'RuntimeException: 模拟下游服务超时',
  '[2026-06-08 02:00:00] [INFO] 任务开始执行。' '\n'
  '[2026-06-08 02:00:00] [INFO] 平台心跳任务开始执行，任务名称：夜间汇总停用示例' '\n'
  '[2026-06-08 02:00:00] [INFO] 当前触发方式：SCHEDULE' '\n'
  '[2026-06-08 02:00:01] [ERROR] 任务执行失败。' '\n'
  'java.lang.RuntimeException: 模拟下游服务超时' '\n'
  '\tat com.manzhushaka.system.demo.NightlySummaryJob.execute(NightlySummaryJob.java:42)' '\n'
  '\tat com.manzhushaka.framework.job.PlatformQuartzDispatchJob.execute(PlatformQuartzDispatchJob.java:18)',
  'system',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_job_log` WHERE `id` = 600003
);
