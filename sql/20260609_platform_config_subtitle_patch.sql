SET NAMES utf8mb4;

SET @next_config_id := IFNULL((SELECT MAX(`id`) FROM `sys_config`), 0);

INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  (@next_config_id := @next_config_id + 1),
  '系统副标题',
  'sys.platform.subtitle',
  'PLATFORM CONSOLE',
  1,
  'system',
  NOW(),
  'system',
  NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_config`
  WHERE `config_key` = 'sys.platform.subtitle'
);
