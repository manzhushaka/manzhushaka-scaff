SET NAMES utf8mb4;

UPDATE `sys_menu`
SET
  `parent_id` = 0,
  `route_path` = '/io-tasks',
  `sort` = 2,
  `update_by` = 'system',
  `update_time` = NOW()
WHERE `route_name` = 'SystemImportExport'
  AND (
    `parent_id` <> 0
    OR `route_path` <> '/io-tasks'
    OR `sort` <> 2
  );

UPDATE `sys_menu`
SET
  `sort` = 3,
  `update_by` = 'system',
  `update_time` = NOW()
WHERE `id` = 200
  AND `sort` <> 3;
