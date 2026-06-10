SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sys_login_log`;
DROP TABLE IF EXISTS `sys_op_log`;
DROP TABLE IF EXISTS `sys_job_log`;
DROP TABLE IF EXISTS `sys_job`;
DROP TABLE IF EXISTS `sys_import_export_task`;
DROP TABLE IF EXISTS `sys_mq_message`;
DROP TABLE IF EXISTS `sys_config`;
DROP TABLE IF EXISTS `sys_dict_item`;
DROP TABLE IF EXISTS `sys_dict_type`;
DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父部门 ID',
  `ancestor_path` varchar(500) NOT NULL DEFAULT ',' COMMENT '祖级路径，逗号包裹',
  `dept_name` varchar(100) NOT NULL COMMENT '部门名称',
  `leader` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_dept_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '主键',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_code` varchar(100) NOT NULL COMMENT '角色编码',
  `data_scope` varchar(32) NOT NULL DEFAULT 'ALL' COMMENT '数据权限：ALL、DEPT_AND_CHILD、DEPT、SELF',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '主键',
  `dept_id` bigint DEFAULT NULL COMMENT '部门 ID',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `nickname` varchar(64) NOT NULL COMMENT '用户昵称',
  `password` varchar(255) NOT NULL COMMENT '登录密码',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录 IP',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_dept_id` (`dept_id`),
  CONSTRAINT `fk_sys_user_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role_user_role` (`user_id`, `role_id`),
  KEY `idx_sys_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_sys_user_role_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_sys_user_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父菜单 ID',
  `menu_name` varchar(100) NOT NULL COMMENT '菜单名称',
  `menu_type` varchar(16) NOT NULL COMMENT '菜单类型：DIR、MENU、BUTTON',
  `route_name` varchar(100) DEFAULT NULL COMMENT '路由名称',
  `route_path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '显示状态：1 显示，0 隐藏',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
  `keep_alive` tinyint NOT NULL DEFAULT 0 COMMENT '是否缓存',
  `always_show` tinyint NOT NULL DEFAULT 0 COMMENT '是否总是显示父级',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_menu_parent_id` (`parent_id`),
  KEY `idx_sys_menu_perms` (`perms`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `menu_id` bigint NOT NULL COMMENT '菜单 ID',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_menu_role_menu` (`role_id`, `menu_id`),
  KEY `idx_sys_role_menu_menu_id` (`menu_id`),
  CONSTRAINT `fk_sys_role_menu_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`),
  CONSTRAINT `fk_sys_role_menu_menu_id` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';

CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL COMMENT '主键',
  `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
  `dict_code` varchar(100) NOT NULL COMMENT '字典编码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_type_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

CREATE TABLE `sys_dict_item` (
  `id` bigint NOT NULL COMMENT '主键',
  `dict_type_id` bigint NOT NULL COMMENT '字典类型 ID',
  `item_label` varchar(100) NOT NULL COMMENT '字典标签',
  `item_value` varchar(100) NOT NULL COMMENT '字典值',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_item_type_value` (`dict_type_id`, `item_value`),
  CONSTRAINT `fk_sys_dict_item_type_id` FOREIGN KEY (`dict_type_id`) REFERENCES `sys_dict_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典项表';

CREATE TABLE `sys_config` (
  `id` bigint NOT NULL COMMENT '主键',
  `config_name` varchar(100) NOT NULL COMMENT '参数名称',
  `config_key` varchar(100) NOT NULL COMMENT '参数键名',
  `config_value` varchar(500) NOT NULL COMMENT '参数键值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_config_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统参数表';

CREATE TABLE `sys_import_export_task` (
  `id` bigint NOT NULL COMMENT '主键',
  `task_no` varchar(64) NOT NULL COMMENT '任务编号',
  `task_type` varchar(16) NOT NULL COMMENT '任务类型：IMPORT、EXPORT',
  `biz_type` varchar(64) NOT NULL COMMENT '业务场景编码',
  `biz_label` varchar(100) NOT NULL COMMENT '业务场景名称',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING、PROCESSING、SUCCESS、FAIL',
  `task_message` varchar(500) DEFAULT NULL COMMENT '任务说明',
  `source_file_name` varchar(255) DEFAULT NULL COMMENT '源文件名',
  `source_object_key` varchar(500) DEFAULT NULL COMMENT '源文件 BOS Key',
  `source_file_size` bigint DEFAULT NULL COMMENT '源文件大小（字节）',
  `result_file_name` varchar(255) DEFAULT NULL COMMENT '结果文件名',
  `result_object_key` varchar(500) DEFAULT NULL COMMENT '结果文件 BOS Key',
  `result_file_size` bigint DEFAULT NULL COMMENT '结果文件大小（字节）',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '总记录数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功记录数',
  `fail_count` int NOT NULL DEFAULT 0 COMMENT '失败记录数',
  `finished_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_import_export_task_task_no` (`task_no`),
  KEY `idx_sys_import_export_task_type_status` (`task_type`, `task_status`),
  KEY `idx_sys_import_export_task_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导入导出任务表';

CREATE TABLE `sys_mq_message` (
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

CREATE TABLE `sys_job` (
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

CREATE TABLE `sys_job_log` (
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

CREATE TABLE `sys_op_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪 ID',
  `module` varchar(100) DEFAULT NULL COMMENT '业务模块',
  `business_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `action` varchar(100) DEFAULT NULL COMMENT '操作名称',
  `request_method` varchar(16) DEFAULT NULL COMMENT '请求方法',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人 ID',
  `request_uri` varchar(255) DEFAULT NULL COMMENT '请求 URI',
  `request_ip` varchar(64) DEFAULT NULL COMMENT '请求 IP',
  `request_snapshot` text COMMENT '请求快照',
  `response_snapshot` text COMMENT '响应快照',
  `cost_ms` bigint DEFAULT NULL COMMENT '耗时（毫秒）',
  `success` tinyint NOT NULL DEFAULT 1 COMMENT '是否成功',
  `error_msg` varchar(1000) DEFAULT NULL COMMENT '错误信息',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_op_log_operator_id` (`operator_id`),
  KEY `idx_sys_op_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `username` varchar(64) NOT NULL COMMENT '登录账号',
  `login_status` varchar(16) NOT NULL DEFAULT 'SUCCESS' COMMENT '登录状态：SUCCESS、FAIL',
  `message` varchar(255) DEFAULT NULL COMMENT '提示消息',
  `ip` varchar(64) DEFAULT NULL COMMENT '登录 IP',
  `login_location` varchar(255) DEFAULT NULL COMMENT '登录地点',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '浏览器标识',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_login_log_username` (`username`),
  KEY `idx_sys_login_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

INSERT INTO `sys_dept` (`id`, `parent_id`, `ancestor_path`, `dept_name`, `leader`, `phone`, `email`, `sort`, `status`, `deleted`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(100, 0, ',', 'manzhushaka', 'admin', '13812345678', 'admin@manzhushaka.com', 0, 1, 0, 'system', NOW(), 'system', NOW()),
(101, 100, ',100,', '平台研发部', 'admin', '13812345678', 'rd@manzhushaka.com', 1, 1, 0, 'system', NOW(), 'system', NOW());

INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `data_scope`, `sort`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(100, '超级管理员', 'SUPER_ADMIN', 'ALL', 1, 1, '拥有系统全部管理权限', 'system', NOW(), 'system', NOW()),
(101, '普通运维', 'OPS', 'DEPT_AND_CHILD', 2, 1, '示例角色，覆盖部门及子部门数据权限', 'system', NOW(), 'system', NOW());

INSERT INTO `sys_user` (`id`, `dept_id`, `username`, `nickname`, `password`, `real_name`, `mobile`, `email`, `status`, `deleted`, `last_login_ip`, `last_login_time`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(100, 100, 'admin', '系统管理员', 'Admin@123456', '系统管理员', '13812345678', 'admin@manzhushaka.com', 1, 0, NULL, NULL, '初始化超级管理员账号', 'system', NOW(), 'system', NOW());

INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(100, 100, 100, 'system', NOW(), 'system', NOW());

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(100, 0, '仪表盘', 'MENU', 'Dashboard', '/dashboard', 'dashboard/index', 'dashboard:view', 'icon-dashboard', 1, 1, 1, 1, 0, '首页仪表盘', 'system', NOW(), 'system', NOW()),
(200, 0, '系统管理', 'DIR', 'System', '/system', NULL, NULL, 'icon-settings', 3, 1, 1, 0, 1, '系统管理目录', 'system', NOW(), 'system', NOW()),
(210, 200, '用户管理', 'MENU', 'SystemUsers', 'users', 'system/users', 'system:user:list', 'icon-user', 1, 1, 1, 1, 0, '用户管理菜单', 'system', NOW(), 'system', NOW()),
(211, 210, '用户查询', 'BUTTON', NULL, NULL, NULL, 'system:user:query', NULL, 1, 1, 1, 0, 0, '用户查询按钮', 'system', NOW(), 'system', NOW()),
(212, 210, '用户新增', 'BUTTON', NULL, NULL, NULL, 'system:user:add', NULL, 2, 1, 1, 0, 0, '用户新增按钮', 'system', NOW(), 'system', NOW()),
(213, 210, '用户修改', 'BUTTON', NULL, NULL, NULL, 'system:user:update', NULL, 3, 1, 1, 0, 0, '用户修改按钮', 'system', NOW(), 'system', NOW()),
(214, 210, '用户删除', 'BUTTON', NULL, NULL, NULL, 'system:user:delete', NULL, 4, 1, 1, 0, 0, '用户删除按钮', 'system', NOW(), 'system', NOW()),
(220, 200, '角色管理', 'MENU', 'SystemRoles', 'roles', 'system/roles', 'system:role:list', 'icon-safe', 2, 1, 1, 1, 0, '角色管理菜单', 'system', NOW(), 'system', NOW()),
(221, 220, '角色查询', 'BUTTON', NULL, NULL, NULL, 'system:role:query', NULL, 1, 1, 1, 0, 0, '角色查询按钮', 'system', NOW(), 'system', NOW()),
(222, 220, '角色新增', 'BUTTON', NULL, NULL, NULL, 'system:role:add', NULL, 2, 1, 1, 0, 0, '角色新增按钮', 'system', NOW(), 'system', NOW()),
(223, 220, '角色修改', 'BUTTON', NULL, NULL, NULL, 'system:role:update', NULL, 3, 1, 1, 0, 0, '角色修改按钮', 'system', NOW(), 'system', NOW()),
(224, 220, '角色授权', 'BUTTON', NULL, NULL, NULL, 'system:role:grant', NULL, 4, 1, 1, 0, 0, '角色授权按钮', 'system', NOW(), 'system', NOW()),
(230, 200, '部门管理', 'MENU', 'SystemDepts', 'depts', 'system/depts', 'system:dept:list', 'icon-branch', 3, 1, 1, 1, 0, '部门管理菜单', 'system', NOW(), 'system', NOW()),
(231, 230, '部门查询', 'BUTTON', NULL, NULL, NULL, 'system:dept:query', NULL, 1, 1, 1, 0, 0, '部门查询按钮', 'system', NOW(), 'system', NOW()),
(232, 230, '部门新增', 'BUTTON', NULL, NULL, NULL, 'system:dept:add', NULL, 2, 1, 1, 0, 0, '部门新增按钮', 'system', NOW(), 'system', NOW()),
(233, 230, '部门修改', 'BUTTON', NULL, NULL, NULL, 'system:dept:update', NULL, 3, 1, 1, 0, 0, '部门修改按钮', 'system', NOW(), 'system', NOW()),
(240, 200, '菜单管理', 'MENU', 'SystemMenus', 'menus', 'system/menus', 'system:menu:list', 'icon-menu', 4, 1, 1, 1, 0, '菜单管理菜单', 'system', NOW(), 'system', NOW()),
(241, 240, '菜单查询', 'BUTTON', NULL, NULL, NULL, 'system:menu:query', NULL, 1, 1, 1, 0, 0, '菜单查询按钮', 'system', NOW(), 'system', NOW()),
(242, 240, '菜单新增', 'BUTTON', NULL, NULL, NULL, 'system:menu:add', NULL, 2, 1, 1, 0, 0, '菜单新增按钮', 'system', NOW(), 'system', NOW()),
(243, 240, '菜单修改', 'BUTTON', NULL, NULL, NULL, 'system:menu:update', NULL, 3, 1, 1, 0, 0, '菜单修改按钮', 'system', NOW(), 'system', NOW()),
(250, 200, '字典管理', 'MENU', 'SystemDicts', 'dicts', 'system/dicts', 'system:dict:list', 'icon-book', 5, 1, 1, 1, 0, '字典管理菜单', 'system', NOW(), 'system', NOW()),
(251, 250, '字典查询', 'BUTTON', NULL, NULL, NULL, 'system:dict:query', NULL, 1, 1, 1, 0, 0, '字典查询按钮', 'system', NOW(), 'system', NOW()),
(252, 250, '字典新增', 'BUTTON', NULL, NULL, NULL, 'system:dict:add', NULL, 2, 1, 1, 0, 0, '字典新增按钮', 'system', NOW(), 'system', NOW()),
(253, 250, '字典修改', 'BUTTON', NULL, NULL, NULL, 'system:dict:update', NULL, 3, 1, 1, 0, 0, '字典修改按钮', 'system', NOW(), 'system', NOW()),
(260, 200, '参数管理', 'MENU', 'SystemParams', 'params', 'system/params', 'system:config:list', 'icon-storage', 6, 1, 1, 1, 0, '参数管理菜单', 'system', NOW(), 'system', NOW()),
(261, 260, '参数查询', 'BUTTON', NULL, NULL, NULL, 'system:config:query', NULL, 1, 1, 1, 0, 0, '参数查询按钮', 'system', NOW(), 'system', NOW()),
(262, 260, '参数修改', 'BUTTON', NULL, NULL, NULL, 'system:config:update', NULL, 2, 1, 1, 0, 0, '参数修改按钮', 'system', NOW(), 'system', NOW()),
(263, 200, '平台配置', 'MENU', 'SystemPlatformConfig', 'platform-config', 'system/platform-config', 'system:config:update', 'icon-storage', 7, 1, 1, 1, 0, '平台配置菜单', 'system', NOW(), 'system', NOW()),
(264, 200, '定时任务', 'MENU', 'SystemJobs', 'jobs', 'system/jobs', 'system:job:list', 'icon-clock-circle', 8, 1, 1, 1, 0, '定时任务菜单', 'system', NOW(), 'system', NOW()),
(265, 264, '任务查询', 'BUTTON', NULL, NULL, NULL, 'system:job:query', NULL, 1, 1, 1, 0, 0, '任务查询按钮', 'system', NOW(), 'system', NOW()),
(266, 264, '任务新增', 'BUTTON', NULL, NULL, NULL, 'system:job:add', NULL, 2, 1, 1, 0, 0, '任务新增按钮', 'system', NOW(), 'system', NOW()),
(267, 264, '任务修改', 'BUTTON', NULL, NULL, NULL, 'system:job:update', NULL, 3, 1, 1, 0, 0, '任务修改按钮', 'system', NOW(), 'system', NOW()),
(268, 264, '任务删除', 'BUTTON', NULL, NULL, NULL, 'system:job:delete', NULL, 4, 1, 1, 0, 0, '任务删除按钮', 'system', NOW(), 'system', NOW()),
(269, 264, '任务执行', 'BUTTON', NULL, NULL, NULL, 'system:job:trigger', NULL, 5, 1, 1, 0, 0, '任务执行按钮', 'system', NOW(), 'system', NOW()),
(273, 264, '任务暂停', 'BUTTON', NULL, NULL, NULL, 'system:job:pause', NULL, 6, 1, 1, 0, 0, '任务暂停按钮', 'system', NOW(), 'system', NOW()),
(274, 264, '任务恢复', 'BUTTON', NULL, NULL, NULL, 'system:job:resume', NULL, 7, 1, 1, 0, 0, '任务恢复按钮', 'system', NOW(), 'system', NOW()),
(275, 264, '任务日志', 'BUTTON', NULL, NULL, NULL, 'system:job:log', NULL, 8, 1, 1, 0, 0, '任务日志按钮', 'system', NOW(), 'system', NOW()),
(280, 0, '导入导出管理', 'DIR', 'SystemImportExport', '/io-tasks', NULL, NULL, 'icon-storage', 2, 1, 1, 0, 1, '导入导出任务目录', 'system', NOW(), 'system', NOW()),
(281, 280, '导出任务管理', 'MENU', 'SystemExportTasks', 'exports', 'system/export-tasks', 'system:io:export:list', 'icon-file', 1, 1, 1, 1, 0, '导出任务管理菜单', 'system', NOW(), 'system', NOW()),
(282, 281, '导出任务查询', 'BUTTON', NULL, NULL, NULL, 'system:io:export:query', NULL, 1, 1, 1, 0, 0, '导出任务查询按钮', 'system', NOW(), 'system', NOW()),
(283, 281, '导出任务创建', 'BUTTON', NULL, NULL, NULL, 'system:io:export:create', NULL, 2, 1, 1, 0, 0, '导出任务创建按钮', 'system', NOW(), 'system', NOW()),
(284, 281, '导出任务下载', 'BUTTON', NULL, NULL, NULL, 'system:io:export:download', NULL, 3, 1, 1, 0, 0, '导出任务下载按钮', 'system', NOW(), 'system', NOW()),
(285, 280, '导入任务管理', 'MENU', 'SystemImportTasks', 'imports', 'system/import-tasks', 'system:io:import:list', 'icon-book', 2, 1, 1, 1, 0, '导入任务管理菜单', 'system', NOW(), 'system', NOW()),
(286, 285, '导入任务查询', 'BUTTON', NULL, NULL, NULL, 'system:io:import:query', NULL, 1, 1, 1, 0, 0, '导入任务查询按钮', 'system', NOW(), 'system', NOW()),
(287, 285, '导入任务创建', 'BUTTON', NULL, NULL, NULL, 'system:io:import:create', NULL, 2, 1, 1, 0, 0, '导入任务创建按钮', 'system', NOW(), 'system', NOW()),
(288, 285, '导入任务下载', 'BUTTON', NULL, NULL, NULL, 'system:io:import:download', NULL, 3, 1, 1, 0, 0, '导入任务下载按钮', 'system', NOW(), 'system', NOW()),
(270, 0, '日志管理', 'DIR', 'SystemLogs', '/logs', NULL, NULL, 'icon-history', 3, 1, 1, 0, 1, '日志管理目录', 'system', NOW(), 'system', NOW()),
(271, 270, '登录日志', 'MENU', 'SystemLoginLogs', 'login', 'system/login-logs', 'system:log:view', 'icon-history', 1, 1, 1, 0, 0, '登录日志菜单', 'system', NOW(), 'system', NOW()),
(272, 270, '操作日志', 'MENU', 'SystemOpLogs', 'op', 'system/op-logs', 'system:log:view', 'icon-file', 2, 1, 1, 0, 0, '操作日志菜单', 'system', NOW(), 'system', NOW()),
(289, 270, '消息台账', 'MENU', 'SystemMqMessages', 'mq-messages', 'system/mq-messages', 'system:mq-message:query', 'icon-list', 3, 1, 1, 0, 0, '消息台账菜单', 'system', NOW(), 'system', NOW()),
(290, 289, '消息查询', 'BUTTON', NULL, NULL, NULL, 'system:mq-message:query', NULL, 1, 1, 1, 0, 0, '消息查询按钮', 'system', NOW(), 'system', NOW()),
(291, 289, '消息重试', 'BUTTON', NULL, NULL, NULL, 'system:mq-message:retry', NULL, 2, 1, 1, 0, 0, '消息重试按钮', 'system', NOW(), 'system', NOW());

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(1000, 100, 100, 'system', NOW(), 'system', NOW()),
(1001, 100, 200, 'system', NOW(), 'system', NOW()),
(1002, 100, 210, 'system', NOW(), 'system', NOW()),
(1003, 100, 211, 'system', NOW(), 'system', NOW()),
(1004, 100, 212, 'system', NOW(), 'system', NOW()),
(1005, 100, 213, 'system', NOW(), 'system', NOW()),
(1006, 100, 214, 'system', NOW(), 'system', NOW()),
(1007, 100, 220, 'system', NOW(), 'system', NOW()),
(1008, 100, 221, 'system', NOW(), 'system', NOW()),
(1009, 100, 222, 'system', NOW(), 'system', NOW()),
(1010, 100, 223, 'system', NOW(), 'system', NOW()),
(1011, 100, 224, 'system', NOW(), 'system', NOW()),
(1012, 100, 230, 'system', NOW(), 'system', NOW()),
(1013, 100, 231, 'system', NOW(), 'system', NOW()),
(1014, 100, 232, 'system', NOW(), 'system', NOW()),
(1015, 100, 233, 'system', NOW(), 'system', NOW()),
(1016, 100, 240, 'system', NOW(), 'system', NOW()),
(1017, 100, 241, 'system', NOW(), 'system', NOW()),
(1018, 100, 242, 'system', NOW(), 'system', NOW()),
(1019, 100, 243, 'system', NOW(), 'system', NOW()),
(1020, 100, 250, 'system', NOW(), 'system', NOW()),
(1021, 100, 251, 'system', NOW(), 'system', NOW()),
(1022, 100, 252, 'system', NOW(), 'system', NOW()),
(1023, 100, 253, 'system', NOW(), 'system', NOW()),
(1024, 100, 260, 'system', NOW(), 'system', NOW()),
(1025, 100, 261, 'system', NOW(), 'system', NOW()),
(1026, 100, 262, 'system', NOW(), 'system', NOW()),
(1027, 100, 263, 'system', NOW(), 'system', NOW()),
(1028, 100, 264, 'system', NOW(), 'system', NOW()),
(1029, 100, 265, 'system', NOW(), 'system', NOW()),
(1030, 100, 266, 'system', NOW(), 'system', NOW()),
(1031, 100, 267, 'system', NOW(), 'system', NOW()),
(1032, 100, 268, 'system', NOW(), 'system', NOW()),
(1033, 100, 269, 'system', NOW(), 'system', NOW()),
(1034, 100, 273, 'system', NOW(), 'system', NOW()),
(1035, 100, 274, 'system', NOW(), 'system', NOW()),
(1036, 100, 275, 'system', NOW(), 'system', NOW()),
(1037, 100, 280, 'system', NOW(), 'system', NOW()),
(1038, 100, 281, 'system', NOW(), 'system', NOW()),
(1039, 100, 282, 'system', NOW(), 'system', NOW()),
(1040, 100, 283, 'system', NOW(), 'system', NOW()),
(1041, 100, 284, 'system', NOW(), 'system', NOW()),
(1042, 100, 285, 'system', NOW(), 'system', NOW()),
(1043, 100, 286, 'system', NOW(), 'system', NOW()),
(1044, 100, 287, 'system', NOW(), 'system', NOW()),
(1045, 100, 288, 'system', NOW(), 'system', NOW()),
(1046, 100, 270, 'system', NOW(), 'system', NOW()),
(1047, 100, 271, 'system', NOW(), 'system', NOW()),
(1048, 100, 272, 'system', NOW(), 'system', NOW()),
(1049, 101, 100, 'system', NOW(), 'system', NOW()),
(1050, 101, 200, 'system', NOW(), 'system', NOW()),
(1051, 101, 264, 'system', NOW(), 'system', NOW()),
(1052, 101, 265, 'system', NOW(), 'system', NOW()),
(1053, 101, 269, 'system', NOW(), 'system', NOW()),
(1054, 101, 275, 'system', NOW(), 'system', NOW()),
(1055, 101, 270, 'system', NOW(), 'system', NOW()),
(1056, 101, 271, 'system', NOW(), 'system', NOW()),
(1057, 101, 272, 'system', NOW(), 'system', NOW()),
(1058, 100, 289, 'system', NOW(), 'system', NOW()),
(1059, 100, 290, 'system', NOW(), 'system', NOW()),
(1060, 100, 291, 'system', NOW(), 'system', NOW());

INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_code`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(100, '用户状态', 'sys_user_status', 1, '用户启停状态', 'system', NOW(), 'system', NOW()),
(101, '通用状态', 'sys_common_status', 1, '通用启停状态', 'system', NOW(), 'system', NOW()),
(102, '是否枚举', 'sys_yes_no', 1, '通用是否枚举', 'system', NOW(), 'system', NOW());

INSERT INTO `sys_dict_item` (`id`, `dict_type_id`, `item_label`, `item_value`, `sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(1000, 100, '启用', '1', 1, 1, 'system', NOW(), 'system', NOW()),
(1001, 100, '停用', '0', 2, 1, 'system', NOW(), 'system', NOW()),
(1010, 101, '启用', '1', 1, 1, 'system', NOW(), 'system', NOW()),
(1011, 101, '停用', '0', 2, 1, 'system', NOW(), 'system', NOW()),
(1020, 102, '是', 'Y', 1, 1, 'system', NOW(), 'system', NOW()),
(1021, 102, '否', 'N', 2, 1, 'system', NOW(), 'system', NOW());

INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(100, '系统名称', 'sys.platform.name', 'manzhushaka 管理台', 1, 'system', NOW(), 'system', NOW()),
(101, '系统副标题', 'sys.platform.subtitle', 'PLATFORM CONSOLE', 1, 'system', NOW(), 'system', NOW()),
(102, '平台 Logo', 'sys.platform.logo-url', '', 1, 'system', NOW(), 'system', NOW()),
(103, '默认密码', 'sys.user.default-password', 'Admin@123456', 1, 'system', NOW(), 'system', NOW()),
(104, '前端标题', 'sys.ui.title', 'Manzhushaka Admin', 1, 'system', NOW(), 'system', NOW());

SET FOREIGN_KEY_CHECKS = 1;
