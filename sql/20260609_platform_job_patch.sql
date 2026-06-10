SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_job` (
  `id` bigint NOT NULL COMMENT '主键',
  `job_name` varchar(100) NOT NULL COMMENT '任务名称',
  `handler_name` varchar(100) NOT NULL COMMENT '处理器名称',
  `cron_expression` varchar(120) NOT NULL COMMENT 'Cron 表达式',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
  `job_param` text COMMENT '任务参数',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `last_run_status` varchar(16) DEFAULT NULL COMMENT '最近一次执行状态：SUCCESS、FAIL、SKIPPED',
  `last_trigger_time` datetime DEFAULT NULL COMMENT '最近一次执行时间',
  `next_trigger_time` datetime DEFAULT NULL COMMENT '下一次执行时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_status` (`status`),
  KEY `idx_sys_job_handler_name` (`handler_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台定时任务表';

CREATE TABLE IF NOT EXISTS `sys_job_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `job_id` bigint NOT NULL COMMENT '任务 ID',
  `job_name_snapshot` varchar(100) NOT NULL COMMENT '任务名称快照',
  `handler_name_snapshot` varchar(100) NOT NULL COMMENT '处理器名称快照',
  `trigger_type` varchar(16) NOT NULL COMMENT '触发方式：SCHEDULE、MANUAL',
  `run_status` varchar(16) NOT NULL COMMENT '执行状态：RUNNING、SUCCESS、FAIL、SKIPPED',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `cost_ms` bigint DEFAULT NULL COMMENT '耗时（毫秒）',
  `executor_host` varchar(128) DEFAULT NULL COMMENT '执行节点',
  `error_msg` varchar(1000) DEFAULT NULL COMMENT '错误摘要',
  `log_content` mediumtext COMMENT '执行日志',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_job_log_job_id` (`job_id`),
  KEY `idx_sys_job_log_run_status` (`run_status`),
  KEY `idx_sys_job_log_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台定时任务执行日志表';

SET @next_menu_id := IFNULL((SELECT MAX(`id`) FROM `sys_menu`), 0);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  264, 200, '定时任务', 'MENU', 'SystemJobs', 'jobs', 'system/jobs',
  'system:job:list', 'icon-clock-circle', 8, 1, 1, 1, 0,
  '定时任务菜单', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `route_name` = 'SystemJobs'
);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT 265, 264, '任务查询', 'BUTTON', NULL, NULL, NULL, 'system:job:query', NULL, 1, 1, 1, 0, 0, '任务查询按钮', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 265);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT 266, 264, '任务新增', 'BUTTON', NULL, NULL, NULL, 'system:job:add', NULL, 2, 1, 1, 0, 0, '任务新增按钮', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 266);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT 267, 264, '任务修改', 'BUTTON', NULL, NULL, NULL, 'system:job:update', NULL, 3, 1, 1, 0, 0, '任务修改按钮', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 267);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT 268, 264, '任务删除', 'BUTTON', NULL, NULL, NULL, 'system:job:delete', NULL, 4, 1, 1, 0, 0, '任务删除按钮', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 268);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT 269, 264, '任务执行', 'BUTTON', NULL, NULL, NULL, 'system:job:trigger', NULL, 5, 1, 1, 0, 0, '任务执行按钮', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 269);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT 273, 264, '任务暂停', 'BUTTON', NULL, NULL, NULL, 'system:job:pause', NULL, 6, 1, 1, 0, 0, '任务暂停按钮', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 273);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT 274, 264, '任务恢复', 'BUTTON', NULL, NULL, NULL, 'system:job:resume', NULL, 7, 1, 1, 0, 0, '任务恢复按钮', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 274);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT 275, 264, '任务日志', 'BUTTON', NULL, NULL, NULL, 'system:job:log', NULL, 8, 1, 1, 0, 0, '任务日志按钮', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 275);

SET @next_role_menu_id := IFNULL((SELECT MAX(`id`) FROM `sys_role_menu`), 0);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT (@next_role_menu_id := @next_role_menu_id + 1), 100, source.menu_id, 'system', NOW(), 'system', NOW()
FROM (
  SELECT 264 AS menu_id
  UNION ALL SELECT 265
  UNION ALL SELECT 266
  UNION ALL SELECT 267
  UNION ALL SELECT 268
  UNION ALL SELECT 269
  UNION ALL SELECT 273
  UNION ALL SELECT 274
  UNION ALL SELECT 275
) AS source
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_role_menu`
  WHERE `role_id` = 100
    AND `menu_id` = source.menu_id
);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT (@next_role_menu_id := @next_role_menu_id + 1), 101, source.menu_id, 'system', NOW(), 'system', NOW()
FROM (
  SELECT 264 AS menu_id
  UNION ALL SELECT 265
  UNION ALL SELECT 269
  UNION ALL SELECT 275
) AS source
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_role_menu`
  WHERE `role_id` = 101
    AND `menu_id` = source.menu_id
);
