SET NAMES utf8mb4;

ALTER TABLE `sys_import_export_task`
  ADD COLUMN IF NOT EXISTS `task_param` longtext COMMENT '任务参数快照' AFTER `task_message`;

DELETE FROM `sys_menu`
WHERE `permission_code` IN ('system:io:export:create', 'system:io:import:create');

UPDATE `sys_menu`
SET
  `sort` = 2,
  `update_by` = 'system',
  `update_time` = NOW()
WHERE `permission_code` IN ('system:io:export:download', 'system:io:import:download')
  AND `sort` <> 2;
