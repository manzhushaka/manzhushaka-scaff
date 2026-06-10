SET NAMES utf8mb4;

UPDATE `sys_menu`
SET
  `menu_name` = '参数管理',
  `remark` = '参数管理菜单',
  `update_by` = 'system',
  `update_time` = NOW()
WHERE `id` = 260
  AND (
    `menu_name` <> '参数管理'
    OR `remark` <> '参数管理菜单'
  );

SET @next_menu_id := IFNULL((SELECT MAX(`id`) FROM `sys_menu`), 0);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  (@next_menu_id := GREATEST(@next_menu_id + 1, 263)), 200, '平台配置', 'MENU', 'SystemPlatformConfig', 'platform-config', 'system/platform-config',
  'system:config:update', 'icon-storage', 7, 1, 1, 1, 0,
  '平台配置菜单', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu`
  WHERE `route_name` = 'SystemPlatformConfig'
);

SET @next_role_menu_id := IFNULL((SELECT MAX(`id`) FROM `sys_role_menu`), 0);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  (@next_role_menu_id := @next_role_menu_id + 1),
  source.`role_id`,
  (
    SELECT `id`
    FROM `sys_menu`
    WHERE `route_name` = 'SystemPlatformConfig'
    LIMIT 1
  ),
  'system',
  NOW(),
  'system',
  NOW()
FROM `sys_role_menu` source
WHERE source.`menu_id` = 260
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` target
    WHERE target.`role_id` = source.`role_id`
      AND target.`menu_id` = (
        SELECT `id`
        FROM `sys_menu`
        WHERE `route_name` = 'SystemPlatformConfig'
        LIMIT 1
      )
  );

SET @next_config_id := IFNULL((SELECT MAX(`id`) FROM `sys_config`), 0);

INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  (@next_config_id := @next_config_id + 1),
  '平台 Logo',
  'sys.platform.logo-url',
  '',
  1,
  'system',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_config`
  WHERE `config_key` = 'sys.platform.logo-url'
);

INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  (@next_config_id := @next_config_id + 1),
  '系统名称',
  'sys.platform.name',
  COALESCE((
    SELECT `config_value`
    FROM `sys_config`
    WHERE `config_key` = 'sys.app.name'
    LIMIT 1
  ), 'manzhushaka 管理台'),
  1,
  'system',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_config`
  WHERE `config_key` = 'sys.platform.name'
);
