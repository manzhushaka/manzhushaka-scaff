SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_mq_message` (
  `id` bigint NOT NULL COMMENT '主键',
  `event_id` varchar(64) NOT NULL COMMENT '逻辑事件 ID',
  `stream_key` varchar(128) NOT NULL COMMENT 'Redis Stream Key',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型',
  `biz_key` varchar(128) DEFAULT NULL COMMENT '业务键',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪 ID',
  `source` varchar(64) DEFAULT NULL COMMENT '事件来源',
  `status` varchar(32) NOT NULL COMMENT '台账状态',
  `payload_snapshot` longtext COMMENT '消息体快照',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `last_error` varchar(1000) DEFAULT NULL COMMENT '最近一次错误摘要',
  `consumer_group` varchar(64) DEFAULT NULL COMMENT '最近一次消费组',
  `consumer_name` varchar(64) DEFAULT NULL COMMENT '最近一次消费者',
  `processing_deadline_at` datetime DEFAULT NULL COMMENT '处理超时时间',
  `published_at` datetime DEFAULT NULL COMMENT '发布时间',
  `consume_started_at` datetime DEFAULT NULL COMMENT '开始消费时间',
  `consumed_at` datetime DEFAULT NULL COMMENT '消费完成时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_mq_message_event_id` (`event_id`),
  KEY `idx_sys_mq_message_stream_key` (`stream_key`),
  KEY `idx_sys_mq_message_status` (`status`),
  KEY `idx_sys_mq_message_biz_key` (`biz_key`),
  KEY `idx_sys_mq_message_trace_id` (`trace_id`),
  KEY `idx_sys_mq_message_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息台账表';

SET @next_menu_id := IFNULL((SELECT MAX(`id`) FROM `sys_menu`), 0);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  (@next_menu_id := GREATEST(@next_menu_id + 1, 289)), 270, '消息台账', 'MENU', 'SystemMqMessages', 'mq-messages', 'system/mq-messages',
  'system:mq-message:query', 'icon-list', 3, 1, 1, 0, 0,
  '消息台账菜单', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu`
  WHERE `route_name` = 'SystemMqMessages'
);

SET @mq_menu_id := (
  SELECT `id`
  FROM `sys_menu`
  WHERE `route_name` = 'SystemMqMessages'
  LIMIT 1
);

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  (@next_menu_id := @next_menu_id + 1), @mq_menu_id, '消息查询', 'BUTTON', NULL, NULL, NULL,
  'system:mq-message:query', NULL, 1, 1, 1, 0, 0,
  '消息查询按钮', 'system', NOW(), 'system', NOW()
FROM DUAL
WHERE @mq_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_menu`
    WHERE `parent_id` = @mq_menu_id
      AND `menu_type` = 'BUTTON'
      AND `perms` = 'system:mq-message:query'
  );

INSERT INTO `sys_menu` (
  `id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`,
  `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`,
  `remark`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  (@next_menu_id := @next_menu_id + 1), @mq_menu_id, '消息重试', 'BUTTON', NULL, NULL, NULL,
  'system:mq-message:retry', NULL, 2, 1, 1, 0, 0,
  '消息重试按钮', 'system', NOW(), 'system', NOW()
FROM DUAL
WHERE @mq_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_menu`
    WHERE `parent_id` = @mq_menu_id
      AND `menu_type` = 'BUTTON'
      AND `perms` = 'system:mq-message:retry'
  );

SET @mq_query_button_id := (
  SELECT `id`
  FROM `sys_menu`
  WHERE `parent_id` = @mq_menu_id
    AND `menu_type` = 'BUTTON'
    AND `perms` = 'system:mq-message:query'
  LIMIT 1
);

SET @mq_retry_button_id := (
  SELECT `id`
  FROM `sys_menu`
  WHERE `parent_id` = @mq_menu_id
    AND `menu_type` = 'BUTTON'
    AND `perms` = 'system:mq-message:retry'
  LIMIT 1
);

SET @super_admin_role_id := (
  SELECT `id`
  FROM `sys_role`
  WHERE `role_code` = 'SUPER_ADMIN'
  LIMIT 1
);

SET @next_role_menu_id := IFNULL((SELECT MAX(`id`) FROM `sys_role_menu`), 0);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  (@next_role_menu_id := @next_role_menu_id + 1),
  @super_admin_role_id,
  source.`menu_id`,
  'system',
  NOW(),
  'system',
  NOW()
FROM (
  SELECT @mq_menu_id AS `menu_id`
  UNION ALL
  SELECT @mq_query_button_id
  UNION ALL
  SELECT @mq_retry_button_id
) source
WHERE @super_admin_role_id IS NOT NULL
  AND source.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu`
    WHERE `role_id` = @super_admin_role_id
      AND `menu_id` = source.`menu_id`
  );
