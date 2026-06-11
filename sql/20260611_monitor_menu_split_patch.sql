SET NAMES utf8mb4;

UPDATE `sys_menu`
SET
  `parent_id` = 0,
  `menu_type` = 'DIR',
  `route_path` = '/monitor',
  `component` = NULL,
  `perms` = NULL,
  `sort` = 4,
  `always_show` = 1,
  `remark` = '运行监控目录',
  `update_by` = 'system',
  `update_time` = NOW()
WHERE `id` = 292
  AND (
    `parent_id` <> 0
    OR `menu_type` <> 'DIR'
    OR `route_path` <> '/monitor'
    OR `component` IS NOT NULL
    OR `perms` IS NOT NULL
    OR `sort` <> 4
    OR `always_show` <> 1
    OR `remark` <> '运行监控目录'
  );

SET @next_menu_id := IFNULL((SELECT MAX(`id`) FROM `sys_menu`), 0);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  (@next_menu_id := GREATEST(@next_menu_id + 1, 297)), 292, '硬件监控', 'MENU', 'SystemMonitorHardware', 'hardware', 'system/monitor-hardware',
  'system:monitor:view', NULL, 1, 1, 1, 0, 0,
  '硬件监控菜单', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu`
  WHERE `route_name` IN ('SystemMonitorOverview', 'SystemMonitorHardware')
     OR `id` = 297
);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  (@next_menu_id := GREATEST(@next_menu_id + 1, 298)), 292, '服务监控', 'MENU', 'SystemMonitorServices', 'services', 'system/monitor-services',
  'system:monitor:view', NULL, 2, 1, 1, 0, 0,
  '服务监控菜单', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu`
  WHERE `route_name` IN ('SystemMonitorDiagnostics', 'SystemMonitorServices')
     OR `id` = 298
);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  (@next_menu_id := GREATEST(@next_menu_id + 1, 299)), 292, '慢 SQL', 'MENU', 'SystemMonitorSlowSql', 'slow-sql', 'system/monitor-slow-sql',
  'system:monitor:view', NULL, 3, 1, 1, 0, 0,
  '慢 SQL 监控菜单', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu`
  WHERE `route_name` = 'SystemMonitorSlowSql'
     OR `id` = 299
);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  (@next_menu_id := GREATEST(@next_menu_id + 1, 300)), 292, '在线日志', 'MENU', 'SystemMonitorLiveLog', 'live-log', 'system/monitor-live-log',
  'system:monitor:view', NULL, 4, 1, 1, 0, 0,
  '在线日志监控菜单', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu`
  WHERE `route_name` = 'SystemMonitorLiveLog'
     OR `id` = 300
);

UPDATE `sys_menu`
SET
  `parent_id` = 292,
  `menu_name` = '硬件监控',
  `menu_type` = 'MENU',
  `route_name` = 'SystemMonitorHardware',
  `route_path` = 'hardware',
  `component` = 'system/monitor-hardware',
  `perms` = 'system:monitor:view',
  `sort` = 1,
  `visible` = 1,
  `status` = 1,
  `keep_alive` = 0,
  `always_show` = 0,
  `remark` = '硬件监控菜单',
  `update_by` = 'system',
  `update_time` = NOW()
WHERE (`route_name` IN ('SystemMonitorOverview', 'SystemMonitorHardware') OR `id` = 297)
  AND (
    `parent_id` <> 292
    OR `menu_name` <> '硬件监控'
    OR `menu_type` <> 'MENU'
    OR `route_name` <> 'SystemMonitorHardware'
    OR `route_path` <> 'hardware'
    OR `component` <> 'system/monitor-hardware'
    OR `perms` <> 'system:monitor:view'
    OR `sort` <> 1
    OR `remark` <> '硬件监控菜单'
  );

UPDATE `sys_menu`
SET
  `parent_id` = 292,
  `menu_name` = '服务监控',
  `menu_type` = 'MENU',
  `route_name` = 'SystemMonitorServices',
  `route_path` = 'services',
  `component` = 'system/monitor-services',
  `perms` = 'system:monitor:view',
  `sort` = 2,
  `visible` = 1,
  `status` = 1,
  `keep_alive` = 0,
  `always_show` = 0,
  `remark` = '服务监控菜单',
  `update_by` = 'system',
  `update_time` = NOW()
WHERE (`route_name` IN ('SystemMonitorDiagnostics', 'SystemMonitorServices') OR `id` = 298)
  AND (
    `parent_id` <> 292
    OR `menu_name` <> '服务监控'
    OR `menu_type` <> 'MENU'
    OR `route_name` <> 'SystemMonitorServices'
    OR `route_path` <> 'services'
    OR `component` <> 'system/monitor-services'
    OR `perms` <> 'system:monitor:view'
    OR `sort` <> 2
    OR `remark` <> '服务监控菜单'
  );

UPDATE `sys_menu`
SET
  `parent_id` = 292,
  `menu_name` = '慢 SQL',
  `menu_type` = 'MENU',
  `route_name` = 'SystemMonitorSlowSql',
  `route_path` = 'slow-sql',
  `component` = 'system/monitor-slow-sql',
  `perms` = 'system:monitor:view',
  `sort` = 3,
  `visible` = 1,
  `status` = 1,
  `keep_alive` = 0,
  `always_show` = 0,
  `remark` = '慢 SQL 监控菜单',
  `update_by` = 'system',
  `update_time` = NOW()
WHERE (`route_name` = 'SystemMonitorSlowSql' OR `id` = 299)
  AND (
    `parent_id` <> 292
    OR `menu_name` <> '慢 SQL'
    OR `menu_type` <> 'MENU'
    OR `route_name` <> 'SystemMonitorSlowSql'
    OR `route_path` <> 'slow-sql'
    OR `component` <> 'system/monitor-slow-sql'
    OR `perms` <> 'system:monitor:view'
    OR `sort` <> 3
    OR `remark` <> '慢 SQL 监控菜单'
  );

UPDATE `sys_menu`
SET
  `parent_id` = 292,
  `menu_name` = '在线日志',
  `menu_type` = 'MENU',
  `route_name` = 'SystemMonitorLiveLog',
  `route_path` = 'live-log',
  `component` = 'system/monitor-live-log',
  `perms` = 'system:monitor:view',
  `sort` = 4,
  `visible` = 1,
  `status` = 1,
  `keep_alive` = 0,
  `always_show` = 0,
  `remark` = '在线日志监控菜单',
  `update_by` = 'system',
  `update_time` = NOW()
WHERE (`route_name` = 'SystemMonitorLiveLog' OR `id` = 300)
  AND (
    `parent_id` <> 292
    OR `menu_name` <> '在线日志'
    OR `menu_type` <> 'MENU'
    OR `route_name` <> 'SystemMonitorLiveLog'
    OR `route_path` <> 'live-log'
    OR `component` <> 'system/monitor-live-log'
    OR `perms` <> 'system:monitor:view'
    OR `sort` <> 4
    OR `remark` <> '在线日志监控菜单'
  );

SET @monitor_hardware_id := (
  SELECT `id`
  FROM `sys_menu`
  WHERE `route_name` = 'SystemMonitorHardware'
  LIMIT 1
);

SET @monitor_services_id := (
  SELECT `id`
  FROM `sys_menu`
  WHERE `route_name` = 'SystemMonitorServices'
  LIMIT 1
);

SET @monitor_slow_sql_id := (
  SELECT `id`
  FROM `sys_menu`
  WHERE `route_name` = 'SystemMonitorSlowSql'
  LIMIT 1
);

SET @monitor_live_log_id := (
  SELECT `id`
  FROM `sys_menu`
  WHERE `route_name` = 'SystemMonitorLiveLog'
  LIMIT 1
);

SET @next_role_menu_id := IFNULL((SELECT MAX(`id`) FROM `sys_role_menu`), 0);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  (@next_role_menu_id := @next_role_menu_id + 1),
  source.`role_id`,
  @monitor_hardware_id,
  'system',
  NOW(),
  'system',
  NOW()
FROM `sys_role_menu` source
WHERE source.`menu_id` = 292
  AND @monitor_hardware_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` target
    WHERE target.`role_id` = source.`role_id`
      AND target.`menu_id` = @monitor_hardware_id
  );

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  (@next_role_menu_id := @next_role_menu_id + 1),
  source.`role_id`,
  @monitor_services_id,
  'system',
  NOW(),
  'system',
  NOW()
FROM `sys_role_menu` source
WHERE source.`menu_id` = 292
  AND @monitor_services_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` target
    WHERE target.`role_id` = source.`role_id`
      AND target.`menu_id` = @monitor_services_id
  );

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  (@next_role_menu_id := @next_role_menu_id + 1),
  source.`role_id`,
  @monitor_slow_sql_id,
  'system',
  NOW(),
  'system',
  NOW()
FROM `sys_role_menu` source
WHERE source.`menu_id` = 292
  AND @monitor_slow_sql_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` target
    WHERE target.`role_id` = source.`role_id`
      AND target.`menu_id` = @monitor_slow_sql_id
  );

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  (@next_role_menu_id := @next_role_menu_id + 1),
  source.`role_id`,
  @monitor_live_log_id,
  'system',
  NOW(),
  'system',
  NOW()
FROM `sys_role_menu` source
WHERE source.`menu_id` = 292
  AND @monitor_live_log_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` target
    WHERE target.`role_id` = source.`role_id`
      AND target.`menu_id` = @monitor_live_log_id
  );
