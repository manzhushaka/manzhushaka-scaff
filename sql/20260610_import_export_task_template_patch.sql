SET NAMES utf8mb4;

SET @task_param_exists := (
  SELECT COUNT(1)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'sys_import_export_task'
    AND `column_name` = 'task_param'
);

SET @task_param_alter_sql := IF(
  @task_param_exists = 0,
  'ALTER TABLE `sys_import_export_task` ADD COLUMN `task_param` longtext COMMENT ''任务参数快照'' AFTER `task_message`',
  'SELECT 1'
);

PREPARE task_param_stmt FROM @task_param_alter_sql;
EXECUTE task_param_stmt;
DEALLOCATE PREPARE task_param_stmt;

DELETE rm
FROM `sys_role_menu` rm
INNER JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
WHERE m.`perms` IN ('system:io:export:create', 'system:io:import:create');

DELETE FROM `sys_menu`
WHERE `perms` IN ('system:io:export:create', 'system:io:import:create');

UPDATE `sys_menu`
SET
  `sort` = 2,
  `update_by` = 'system',
  `update_time` = NOW()
WHERE `perms` IN ('system:io:export:download', 'system:io:import:download')
  AND `sort` <> 2;
