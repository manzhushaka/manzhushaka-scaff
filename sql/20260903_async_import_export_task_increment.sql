-- 异步导入导出任务增量脚本（已部署数据库执行）
create table if not exists sys_import_task (
  task_id bigint(20) not null auto_increment,
  handler_type varchar(128) not null,
  status varchar(32) not null,
  file_key varchar(255) not null,
  file_name varchar(255) default null,
  content_type varchar(128) default null,
  requested_by bigint(20) not null,
  update_support tinyint(1) not null default 0,
  options_snapshot text default null,
  security_snapshot text not null,
  total_count bigint(20) not null default 0,
  processed_count bigint(20) not null default 0,
  success_count bigint(20) not null default 0,
  failure_count bigint(20) not null default 0,
  error_message varchar(2000) default null,
  started_time datetime default null,
  finished_time datetime default null,
  lease_until datetime default null,
  lease_token varchar(64) default null,
  create_time datetime not null,
  update_time datetime default null,
  primary key (task_id),
  key idx_import_task_status (status, lease_until),
  key idx_import_task_requester (requested_by, create_time)
) engine=innodb comment = '异步导入任务表';

create table if not exists sys_export_task (
  task_id bigint(20) not null auto_increment,
  handler_type varchar(128) not null,
  status varchar(32) not null,
  file_key varchar(255) not null,
  file_name varchar(255) default null,
  content_type varchar(128) default null,
  requested_by bigint(20) not null,
  query_snapshot text default null,
  security_snapshot text not null,
  total_count bigint(20) not null default 0,
  processed_count bigint(20) not null default 0,
  success_count bigint(20) not null default 0,
  failure_count bigint(20) not null default 0,
  error_message varchar(2000) default null,
  started_time datetime default null,
  finished_time datetime default null,
  lease_until datetime default null,
  lease_token varchar(64) default null,
  create_time datetime not null,
  update_time datetime default null,
  primary key (task_id),
  key idx_export_task_status (status, lease_until),
  key idx_export_task_requester (requested_by, create_time)
) engine=innodb comment = '异步导出任务表';

insert ignore into sys_menu values('2041', '导入任务', '2', '7', 'importTask', 'monitor/importTask/index', '', '', 1, 0, 'C', '0', '0', 'monitor:importtask:list', 'upload', 'admin', sysdate(), '', null, '异步导入任务菜单');
insert ignore into sys_menu values('2042', '导入任务查询', '2041', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:importtask:query', '#', 'admin', sysdate(), '', null, '异步导入任务详情');
insert ignore into sys_menu values('2043', '导入任务取消', '2041', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:importtask:cancel', '#', 'admin', sysdate(), '', null, '异步导入任务取消');
insert ignore into sys_menu values('2044', '导入任务提交', '2041', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:importtask:submit', '#', 'admin', sysdate(), '', null, '异步导入任务提交');
insert ignore into sys_menu values('2051', '导出任务', '2', '8', 'exportTask', 'monitor/exportTask/index', '', '', 1, 0, 'C', '0', '0', 'monitor:exporttask:list', 'download', 'admin', sysdate(), '', null, '异步导出任务菜单');
insert ignore into sys_menu values('2052', '导出任务查询', '2051', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:exporttask:query', '#', 'admin', sysdate(), '', null, '异步导出任务详情');
insert ignore into sys_menu values('2053', '导出任务取消', '2051', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:exporttask:cancel', '#', 'admin', sysdate(), '', null, '异步导出任务取消');
insert ignore into sys_menu values('2054', '导出任务提交', '2051', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:exporttask:submit', '#', 'admin', sysdate(), '', null, '异步导出任务提交');
insert ignore into sys_menu values('2055', '导出文件下载', '2051', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:exporttask:download', '#', 'admin', sysdate(), '', null, '异步导出文件下载');

insert ignore into sys_role_menu values('2', '2041');
insert ignore into sys_role_menu values('2', '2042');
insert ignore into sys_role_menu values('2', '2043');
insert ignore into sys_role_menu values('2', '2044');
insert ignore into sys_role_menu values('2', '2051');
insert ignore into sys_role_menu values('2', '2052');
insert ignore into sys_role_menu values('2', '2053');
insert ignore into sys_role_menu values('2', '2054');
insert ignore into sys_role_menu values('2', '2055');
