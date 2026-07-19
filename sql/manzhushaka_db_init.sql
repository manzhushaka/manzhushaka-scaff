-- ============================================================================
-- 执行前请确保目标数据库默认字符集为 utf8mb4，并使用 utf8mb4 客户端导入，
-- 避免中文注释、菜单名称和初始化数据出现乱码。
-- 示例：
-- CREATE DATABASE `manzhushaka` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- mysql --default-character-set=utf8mb4 -uroot -p manzhushaka < sql/manzhushaka_db_init.sql
-- ============================================================================
SET NAMES utf8mb4;

-- ----------------------------
-- 1、部门表
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
  dept_id           bigint(20)      not null auto_increment    comment '部门id',
  parent_id         bigint(20)      default 0                  comment '父部门id',
  ancestors         varchar(50)     default ''                 comment '祖级列表',
  dept_name         varchar(30)     default ''                 comment '部门名称',
  order_num         int(4)          default 0                  comment '显示顺序',
  leader            varchar(20)     default null               comment '负责人',
  phone             varchar(11)     default null               comment '联系电话',
  email             varchar(50)     default null               comment '邮箱',
  dept_type         varchar(16)     not null default 'platform_org' comment '部门类型',
  region_code       varchar(6)      default null               comment '行政区划代码',
  region_level      tinyint         default null               comment '行政区划级别',
  status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (dept_id),
  key idx_sys_dept_type (dept_type),
  key idx_sys_dept_region (region_code),
  key idx_sys_dept_ancestors (ancestors)
) engine=innodb auto_increment=200 comment = '部门表';

-- ----------------------------
-- 初始化-部门表数据
-- ----------------------------
insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) values(100,  0,   '0',          'manzhushaka科技',   0, 'manzhushaka', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) values(101,  100, '0,100',      '深圳总公司', 1, 'manzhushaka', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) values(102,  100, '0,100',      '长沙分公司', 2, 'manzhushaka', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) values(103,  101, '0,100,101',  '研发部门',   1, 'manzhushaka', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) values(104,  101, '0,100,101',  '市场部门',   2, 'manzhushaka', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) values(105,  101, '0,100,101',  '测试部门',   3, 'manzhushaka', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) values(106,  101, '0,100,101',  '财务部门',   4, 'manzhushaka', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) values(107,  101, '0,100,101',  '运维部门',   5, 'manzhushaka', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) values(108,  102, '0,100,102',  '市场部门',   1, 'manzhushaka', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time) values(109,  102, '0,100,102',  '财务部门',   2, 'manzhushaka', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);

-- ----------------------------
-- 2、用户信息表
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id           bigint(20)      not null auto_increment    comment '用户ID',
  dept_id           bigint(20)      default null               comment '部门ID',
  user_name         varchar(30)     not null                   comment '用户账号',
  nick_name         varchar(30)     not null                   comment '用户昵称',
  user_type         varchar(2)      default '00'               comment '用户类型（00系统用户）',
  email             varchar(512)    default ''                 comment '用户邮箱密文',
  email_hash        varchar(128)    default ''                 comment '用户邮箱检索摘要',
  phonenumber       varchar(512)    default ''                 comment '手机号码密文',
  phonenumber_hash  varchar(128)    default ''                 comment '手机号码检索摘要',
  sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',
  avatar            varchar(100)    default ''                 comment '头像地址',
  password          varchar(100)    default ''                 comment '密码',
  status            char(1)         default '0'                comment '账号状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  login_ip          varchar(128)    default ''                 comment '最后登录IP',
  login_date        datetime                                   comment '最后登录时间',
  pwd_update_date   datetime                                   comment '密码最后更新时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (user_id),
  key idx_email_hash (email_hash),
  key idx_phonenumber_hash (phonenumber_hash)
) engine=innodb auto_increment=100 comment = '用户信息表';

-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------
insert into sys_user values(1,  103, 'admin', 'manzhushaka', '00', 'ry@163.com', '', '15888888888', '', '1', '', '$2a$10$umRDAx0sJh41uibwHlzo8ukCWT1uEmoNMcewoHi260IRaS2M/SoSa', '0', '0', '127.0.0.1', sysdate(), null, 'admin', sysdate(), '', null, '管理员');
insert into sys_user values(2,  105, 'ry',    'manzhushaka', '00', 'ry@qq.com',  '',  '15666666666', '', '1', '', '$2a$10$umRDAx0sJh41uibwHlzo8ukCWT1uEmoNMcewoHi260IRaS2M/SoSa', '0', '0', '127.0.0.1', sysdate(), null, 'admin', sysdate(), '', null, '测试员');


-- ----------------------------
-- 3、角色信息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id              bigint(20)      not null auto_increment    comment '角色ID',
  role_name            varchar(30)     not null                   comment '角色名称',
  role_key             varchar(100)    not null                   comment '角色权限字符串',
  role_sort            int(4)          not null                   comment '显示顺序',
  data_scope           char(1)         default '1'                comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
  dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
  status               char(1)         not null                   comment '角色状态（0正常 1停用）',
  del_flag             char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by            varchar(64)     default ''                 comment '创建者',
  create_time          datetime                                   comment '创建时间',
  update_by            varchar(64)     default ''                 comment '更新者',
  update_time          datetime                                   comment '更新时间',
  remark               varchar(500)    default null               comment '备注',
  primary key (role_id)
) engine=innodb auto_increment=100 comment = '角色信息表';

-- ----------------------------
-- 初始化-角色信息表数据
-- ----------------------------
insert into sys_role values('1', '超级管理员',  'admin',  1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '超级管理员');
insert into sys_role values('2', '普通角色',    'common', 2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '普通角色');

-- ----------------------------
-- 5、菜单权限表
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
  menu_id           bigint(20)      not null auto_increment    comment '菜单ID',
  menu_name         varchar(50)     not null                   comment '菜单名称',
  parent_id         bigint(20)      default 0                  comment '父菜单ID',
  order_num         int(4)          default 0                  comment '显示顺序',
  path              varchar(200)    default ''                 comment '路由地址',
  component         varchar(255)    default null               comment '组件路径',
  query             varchar(255)    default null               comment '路由参数',
  route_name        varchar(50)     default ''                 comment '路由名称',
  is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',
  is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
  menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',
  visible           char(1)         default 0                  comment '菜单状态（0显示 1隐藏）',
  status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',
  perms             varchar(100)    default null               comment '权限标识',
  icon              varchar(100)    default '#'                comment '菜单图标',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '菜单权限表';

-- ----------------------------
-- 初始化-菜单信息表数据
-- ----------------------------
-- 一级菜单
insert into sys_menu values('1', '系统管理', '0', '1', 'system',           null, '', '', 1, 0, 'M', '0', '0', '', 'system',   'admin', sysdate(), '', null, '系统管理目录');
insert into sys_menu values('2', '系统监控', '0', '2', 'monitor',          null, '', '', 1, 0, 'M', '0', '0', '', 'monitor',  'admin', sysdate(), '', null, '系统监控目录');
insert into sys_menu values('160', '用户与权限', '0', '3', 'userAuth',     null, '', '', 1, 0, 'M', '0', '0', '', 'people',   'admin', sysdate(), '', null, '用户与权限目录');
-- 二级菜单
insert into sys_menu values('100',  '用户管理', '160', '1', 'user',       'system/user/index',        '', '', 1, 0, 'C', '0', '0', 'system:user:list',        'user',          'admin', sysdate(), '', null, '用户管理菜单');
insert into sys_menu values('101',  '角色管理', '160', '2', 'role',       'system/role/index',        '', '', 1, 0, 'C', '0', '0', 'system:role:list',        'peoples',       'admin', sysdate(), '', null, '角色管理菜单');
insert into sys_menu values('102',  '菜单管理', '160', '3', 'menu',       'system/menu/index',        '', '', 1, 0, 'C', '0', '0', 'system:menu:list',        'tree-table',    'admin', sysdate(), '', null, '菜单管理菜单');
insert into sys_menu values('103',  '部门管理', '160', '4', 'dept',       'system/dept/index',        '', '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', sysdate(), '', null, '部门管理菜单');
insert into sys_menu values('105',  '字典管理', '1',   '5', 'dict',       'system/dict/index',        '', '', 1, 0, 'C', '0', '0', 'system:dict:list',        'dict',          'admin', sysdate(), '', null, '字典管理菜单');
insert into sys_menu values('106',  '参数设置', '1',   '6', 'config',     'system/config/index',      '', '', 1, 0, 'C', '0', '0', 'system:config:list',      'edit',          'admin', sysdate(), '', null, '参数设置菜单');
insert into sys_menu values('108',  '日志中心', '2',   '1', 'log',        '',                         '', '', 1, 0, 'M', '0', '0', '',                        'log',           'admin', sysdate(), '', null, '日志中心菜单');
insert into sys_menu values('161',  '统一日志', '108', '1', 'logCenter',  'monitor/logCenter/index',  '', '', 1, 0, 'C', '0', '0', 'monitor:logcenter:list',  'log',           'admin', sysdate(), '', null, '统一日志菜单');
insert into sys_menu values('162',  '运行日志', '108', '2', 'runtimeLog', 'monitor/runtimeLog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:runtimelog:list', 'log',           'admin', sysdate(), '', null, '运行日志菜单');
insert into sys_menu values('163',  '慢 SQL 日志', '108', '3', 'slowSql', 'monitor/slowSql/index',    '', '', 1, 0, 'C', '0', '0', 'monitor:slowsql:list',    'druid',         'admin', sysdate(), '', null, '慢 SQL 日志菜单');
insert into sys_menu values('175',  '消息队列台账', '108', '6', 'mqLog',     'monitor/mqLog/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:mqlog:list',     'message',       'admin', sysdate(), '', null, '消息队列台账菜单');
insert into sys_menu values('109',  '在线用户', '2',   '1', 'online',     'monitor/online/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:online:list',     'online',        'admin', sysdate(), '', null, '在线用户菜单');
insert into sys_menu values('110',  '定时任务', '2',   '2', 'job',        'monitor/job/index',        '', '', 1, 0, 'C', '0', '0', 'monitor:job:list',        'job',           'admin', sysdate(), '', null, '定时任务菜单');
insert into sys_menu values('111',  '数据监控', '2',   '3', 'druid',      'monitor/druid/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list',      'druid',         'admin', sysdate(), '', null, '数据监控菜单');
insert into sys_menu values('112',  '宿主机监控', '2',   '4', 'server',     'monitor/server/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:server:list',     'server',        'admin', sysdate(), '', null, '宿主机监控菜单');
insert into sys_menu values('113',  '缓存监控', '2',   '5', 'cache',      'monitor/cache/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis',         'admin', sysdate(), '', null, '缓存监控菜单');
insert into sys_menu values('114',  '缓存列表', '2',   '6', 'cacheList',  'monitor/cache/list',       '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis-list',    'admin', sysdate(), '', null, '缓存列表菜单');


-- ----------------------------
-- 6、用户和角色关联表  用户N-1角色
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  user_id   bigint(20) not null comment '用户ID',
  role_id   bigint(20) not null comment '角色ID',
  primary key(user_id, role_id)
) engine=innodb comment = '用户和角色关联表';

-- ----------------------------
-- 初始化-用户和角色关联表数据
-- ----------------------------
insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('2', '2');


-- ----------------------------
-- 7、角色和菜单关联表  角色1-N菜单
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id   bigint(20) not null comment '角色ID',
  menu_id   bigint(20) not null comment '菜单ID',
  primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';

-- ----------------------------
-- 初始化-角色和菜单关联表数据
-- ----------------------------
-- 按钮权限菜单（系统管理模块）
insert into sys_menu values('116', '用户新增', '100', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'system:user:add',             '#',                'admin', sysdate(), '', null, '用户新增按钮');
insert into sys_menu values('117', '用户修改', '100', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'system:user:edit',            '#',                'admin', sysdate(), '', null, '用户修改按钮');
insert into sys_menu values('118', '用户删除', '100', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'system:user:remove',          '#',                'admin', sysdate(), '', null, '用户删除按钮');
insert into sys_menu values('119', '角色新增', '101', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'system:role:add',             '#',                'admin', sysdate(), '', null, '角色新增按钮');
insert into sys_menu values('120', '角色修改', '101', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'system:role:edit',            '#',                'admin', sysdate(), '', null, '角色修改按钮');
insert into sys_menu values('121', '角色删除', '101', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'system:role:remove',          '#',                'admin', sysdate(), '', null, '角色删除按钮');
insert into sys_menu values('122', '菜单新增', '102', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'system:menu:add',             '#',                'admin', sysdate(), '', null, '菜单新增按钮');
insert into sys_menu values('123', '菜单修改', '102', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',            '#',                'admin', sysdate(), '', null, '菜单修改按钮');
insert into sys_menu values('124', '菜单删除', '102', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',          '#',                'admin', sysdate(), '', null, '菜单删除按钮');
insert into sys_menu values('125', '部门新增', '103', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'system:dept:add',             '#',                'admin', sysdate(), '', null, '部门新增按钮');
insert into sys_menu values('126', '部门修改', '103', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',            '#',                'admin', sysdate(), '', null, '部门修改按钮');
insert into sys_menu values('127', '部门删除', '103', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',          '#',                'admin', sysdate(), '', null, '部门删除按钮');
insert into sys_menu values('131', '字典新增', '105', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'system:dict:add',             '#',                'admin', sysdate(), '', null, '字典新增按钮');
insert into sys_menu values('132', '字典修改', '105', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'system:dict:edit',            '#',                'admin', sysdate(), '', null, '字典修改按钮');
insert into sys_menu values('133', '字典删除', '105', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'system:dict:remove',          '#',                'admin', sysdate(), '', null, '字典删除按钮');
insert into sys_menu values('134', '参数新增', '106', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'system:config:add',           '#',                'admin', sysdate(), '', null, '参数新增按钮');
insert into sys_menu values('135', '参数修改', '106', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'system:config:edit',          '#',                'admin', sysdate(), '', null, '参数修改按钮');
insert into sys_menu values('136', '参数删除', '106', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'system:config:remove',        '#',                'admin', sysdate(), '', null, '参数删除按钮');
-- 监控模块按钮权限
insert into sys_menu values('141', '在线查询', '109', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:online:list',         '#',                'admin', sysdate(), '', null, '在线查询按钮');
insert into sys_menu values('168', '运行日志查询', '162', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:runtimelog:list',  '#',                'admin', sysdate(), '', null, '运行日志查询按钮');
insert into sys_menu values('169', '运行日志详情', '162', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:runtimelog:query', '#',                'admin', sysdate(), '', null, '运行日志详情按钮');
insert into sys_menu values('170', '运行日志下载', '162', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:runtimelog:download', '#',              'admin', sysdate(), '', null, '运行日志下载按钮');
insert into sys_menu values('171', '慢 SQL 查询', '163', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:slowsql:list',       '#',                'admin', sysdate(), '', null, '慢 SQL 查询按钮');
insert into sys_menu values('172', '慢 SQL 详情', '163', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:slowsql:query',      '#',                'admin', sysdate(), '', null, '慢 SQL 详情按钮');
insert into sys_menu values('173', '慢 SQL 删除', '163', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:slowsql:remove',    '#',                'admin', sysdate(), '', null, '慢 SQL 删除按钮');
insert into sys_menu values('174', '慢 SQL 导出', '163', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:slowsql:export',    '#',                'admin', sysdate(), '', null, '慢 SQL 导出按钮');
insert into sys_menu values('142', '操作日志查询', '161', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:list',       '#',                'admin', sysdate(), '', null, '操作日志查询按钮');
insert into sys_menu values('143', '操作日志删除', '161', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#',                'admin', sysdate(), '', null, '操作日志删除按钮');
insert into sys_menu values('144', '操作日志导出', '161', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#',                'admin', sysdate(), '', null, '操作日志导出按钮');
insert into sys_menu values('145', '操作日志详情', '161', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#',                'admin', sysdate(), '', null, '操作日志详情按钮');
insert into sys_menu values('146', '登录日志查询', '161', '5', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:list',    '#',                'admin', sysdate(), '', null, '登录日志查询按钮');
insert into sys_menu values('147', '登录日志删除', '161', '6', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#',                'admin', sysdate(), '', null, '登录日志删除按钮');
insert into sys_menu values('148', '登录日志导出', '161', '7', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#',                'admin', sysdate(), '', null, '登录日志导出按钮');
insert into sys_menu values('151', '登录账户解锁', '161', '8', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',  '#',                'admin', sysdate(), '', null, '登录账户解锁按钮');
-- 定时任务按钮权限
insert into sys_menu values('180', '定时任务查询', '110', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:job:list',         '#',                'admin', sysdate(), '', null, '定时任务查询按钮');
insert into sys_menu values('181', '定时任务详情', '110', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:job:query',        '#',                'admin', sysdate(), '', null, '定时任务详情按钮');
insert into sys_menu values('182', '定时任务新增', '110', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:job:add',          '#',                'admin', sysdate(), '', null, '定时任务新增按钮');
insert into sys_menu values('183', '定时任务修改', '110', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit',         '#',                'admin', sysdate(), '', null, '定时任务修改按钮');
insert into sys_menu values('184', '定时任务删除', '110', '5', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove',       '#',                'admin', sysdate(), '', null, '定时任务删除按钮');
insert into sys_menu values('185', '定时任务导出', '110', '6', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:job:export',       '#',                'admin', sysdate(), '', null, '定时任务导出按钮');
insert into sys_menu values('186', '定时任务状态', '110', '7', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#',                'admin', sysdate(), '', null, '定时任务状态按钮');
-- 消息队列台账按钮权限
insert into sys_menu values('176', '消息队列台账查询', '175', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:mqlog:list',  '#',                'admin', sysdate(), '', null, '消息队列台账查询按钮');
insert into sys_menu values('177', '消息队列台账详情', '175', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:mqlog:query', '#',                'admin', sysdate(), '', null, '消息队列台账详情按钮');
insert into sys_menu values('178', '消息队列台账删除', '175', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:mqlog:remove', '#',                'admin', sysdate(), '', null, '消息队列台账删除按钮');
insert into sys_menu values('179', '消息队列台账导出', '175', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:mqlog:export', '#',                'admin', sysdate(), '', null, '消息队列台账导出按钮');

insert into sys_role_menu values ('2', '1');
insert into sys_role_menu values ('2', '2');
insert into sys_role_menu values ('2', '160');
insert into sys_role_menu values ('2', '100');
insert into sys_role_menu values ('2', '101');
insert into sys_role_menu values ('2', '102');
insert into sys_role_menu values ('2', '103');
insert into sys_role_menu values ('2', '105');
insert into sys_role_menu values ('2', '106');
insert into sys_role_menu values ('2', '108');
insert into sys_role_menu values ('2', '161');
insert into sys_role_menu values ('2', '162');
insert into sys_role_menu values ('2', '163');
insert into sys_role_menu values ('2', '109');
insert into sys_role_menu values ('2', '110');
insert into sys_role_menu values ('2', '111');
insert into sys_role_menu values ('2', '112');
insert into sys_role_menu values ('2', '113');
insert into sys_role_menu values ('2', '114');
insert into sys_role_menu values ('2', '116');
insert into sys_role_menu values ('2', '117');
insert into sys_role_menu values ('2', '118');
insert into sys_role_menu values ('2', '119');
insert into sys_role_menu values ('2', '120');
insert into sys_role_menu values ('2', '121');
insert into sys_role_menu values ('2', '122');
insert into sys_role_menu values ('2', '123');
insert into sys_role_menu values ('2', '124');
insert into sys_role_menu values ('2', '125');
insert into sys_role_menu values ('2', '126');
insert into sys_role_menu values ('2', '127');
insert into sys_role_menu values ('2', '131');
insert into sys_role_menu values ('2', '132');
insert into sys_role_menu values ('2', '133');
insert into sys_role_menu values ('2', '134');
insert into sys_role_menu values ('2', '135');
insert into sys_role_menu values ('2', '136');
insert into sys_role_menu values ('2', '141');
insert into sys_role_menu values ('2', '168');
insert into sys_role_menu values ('2', '169');
insert into sys_role_menu values ('2', '170');
insert into sys_role_menu values ('2', '171');
insert into sys_role_menu values ('2', '172');
insert into sys_role_menu values ('2', '173');
insert into sys_role_menu values ('2', '174');
insert into sys_role_menu values ('2', '142');
insert into sys_role_menu values ('2', '143');
insert into sys_role_menu values ('2', '144');
insert into sys_role_menu values ('2', '145');
insert into sys_role_menu values ('2', '146');
insert into sys_role_menu values ('2', '147');
insert into sys_role_menu values ('2', '148');
insert into sys_role_menu values ('2', '151');
insert into sys_role_menu values ('2', '180');
insert into sys_role_menu values ('2', '181');
insert into sys_role_menu values ('2', '182');
insert into sys_role_menu values ('2', '183');
insert into sys_role_menu values ('2', '184');
insert into sys_role_menu values ('2', '185');
insert into sys_role_menu values ('2', '186');
insert into sys_role_menu values ('2', '175');
insert into sys_role_menu values ('2', '176');
insert into sys_role_menu values ('2', '177');
insert into sys_role_menu values ('2', '178');
insert into sys_role_menu values ('2', '179');

-- ----------------------------
-- 8、角色和部门关联表  角色1-N部门
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
  role_id   bigint(20) not null comment '角色ID',
  dept_id   bigint(20) not null comment '部门ID',
  primary key(role_id, dept_id)
) engine=innodb comment = '角色和部门关联表';

-- ----------------------------
-- 初始化-角色和部门关联表数据
-- ----------------------------
insert into sys_role_dept values ('2', '100');
insert into sys_role_dept values ('2', '101');
insert into sys_role_dept values ('2', '105');


-- ----------------------------
-- 9、操作日志记录
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint(20)      not null auto_increment    comment '日志主键',
  title             varchar(50)     default ''                 comment '模块标题',
  business_type     int(2)          default 0                  comment '业务类型（0其它 1新增 2修改 3删除）',
  method            varchar(200)    default ''                 comment '方法名称',
  request_method    varchar(10)     default ''                 comment '请求方式',
  operator_type     int(1)          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
  oper_name         varchar(50)     default ''                 comment '操作人员',
  dept_name         varchar(50)     default ''                 comment '部门名称',
  oper_url          varchar(255)    default ''                 comment '请求URL',
  oper_ip           varchar(128)    default ''                 comment '主机地址',
  oper_location     varchar(255)    default ''                 comment '操作地点',
  oper_param        varchar(2000)   default ''                 comment '请求参数',
  json_result       varchar(2000)   default ''                 comment '返回参数',
  status            int(1)          default 0                  comment '操作状态（0正常 1异常）',
  error_msg         varchar(2000)   default ''                 comment '错误消息',
  oper_time         datetime                                   comment '操作时间',
  cost_time         bigint(20)      default 0                  comment '消耗时间',
  primary key (oper_id),
  key idx_sys_oper_log_bt (business_type),
  key idx_sys_oper_log_s  (status),
  key idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = '操作日志记录';

-- ----------------------------
-- 10.1、慢 SQL 日志记录
-- ----------------------------
drop table if exists sys_slow_sql_log;
create table sys_slow_sql_log (
  slow_sql_id       bigint(20)      not null auto_increment    comment '慢SQL日志主键',
  mapper_id         varchar(255)    default ''                 comment 'Mapper方法',
  sql_text          text                                       comment 'SQL文本',
  data_source_name  varchar(64)     default ''                 comment '数据源名称',
  cost_time         bigint(20)      default 0                  comment '消耗时间',
  error_msg         varchar(2000)   default ''                 comment '错误消息',
  execute_time      datetime                                   comment '执行时间',
  primary key (slow_sql_id),
  key idx_sys_slow_sql_ct (execute_time),
  key idx_sys_slow_sql_cost (cost_time),
  key idx_sys_slow_sql_mapper (mapper_id)
) engine=innodb auto_increment=100 comment = '慢 SQL 日志记录';


-- ----------------------------
-- 11、字典类型表
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
  dict_id          bigint(20)      not null auto_increment    comment '字典主键',
  dict_name        varchar(100)    default ''                 comment '字典名称',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_id),
  unique (dict_type)
) engine=innodb auto_increment=100 comment = '字典类型表';

insert into sys_dict_type values(1,  '用户性别', 'sys_user_sex',        '0', 'admin', sysdate(), '', null, '用户性别列表');
insert into sys_dict_type values(2,  '菜单状态', 'sys_show_hide',       '0', 'admin', sysdate(), '', null, '菜单状态列表');
insert into sys_dict_type values(3,  '系统开关', 'sys_normal_disable',  '0', 'admin', sysdate(), '', null, '系统开关列表');
insert into sys_dict_type values(4,  '任务状态', 'sys_job_status',      '0', 'admin', sysdate(), '', null, '任务状态列表');
insert into sys_dict_type values(5,  '任务分组', 'sys_job_group',       '0', 'admin', sysdate(), '', null, '任务分组列表');
insert into sys_dict_type values(6,  '系统是否', 'sys_yes_no',          '0', 'admin', sysdate(), '', null, '系统是否列表');
insert into sys_dict_type values(9,  '操作类型', 'sys_oper_type',       '0', 'admin', sysdate(), '', null, '操作类型列表');
insert into sys_dict_type values(10, '系统状态', 'sys_common_status',   '0', 'admin', sysdate(), '', null, '登录状态列表');


-- ----------------------------
-- 12、字典数据表
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
  dict_code        bigint(20)      not null auto_increment    comment '字典编码',
  dict_sort        int(4)          default 0                  comment '字典排序',
  dict_label       varchar(100)    default ''                 comment '字典标签',
  dict_value       varchar(100)    default ''                 comment '字典键值',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  css_class        varchar(100)    default null               comment '样式属性（其他样式扩展）',
  list_class       varchar(100)    default null               comment '表格回显样式',
  is_default       char(1)         default 'N'                comment '是否默认（Y是 N否）',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_code)
) engine=innodb auto_increment=100 comment = '字典数据表';

insert into sys_dict_data values(1,  1,  '男',       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', sysdate(), '', null, '性别男');
insert into sys_dict_data values(2,  2,  '女',       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别女');
insert into sys_dict_data values(3,  3,  '未知',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别未知');
insert into sys_dict_data values(4,  1,  '显示',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '显示菜单');
insert into sys_dict_data values(5,  2,  '隐藏',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '隐藏菜单');
insert into sys_dict_data values(6,  1,  '正常',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(7,  2,  '停用',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(8,  1,  '正常',     '0',       'sys_job_status',      '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(9,  2,  '暂停',     '1',       'sys_job_status',      '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(10, 1,  '默认',     'DEFAULT', 'sys_job_group',       '',   '',        'Y', '0', 'admin', sysdate(), '', null, '默认分组');
insert into sys_dict_data values(11, 2,  '系统',     'SYSTEM',  'sys_job_group',       '',   '',        'N', '0', 'admin', sysdate(), '', null, '系统分组');
insert into sys_dict_data values(12, 1,  '是',       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '系统默认是');
insert into sys_dict_data values(13, 2,  '否',       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '系统默认否');
insert into sys_dict_data values(18, 99, '其他',     '0',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '其他操作');
insert into sys_dict_data values(19, 1,  '新增',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '新增操作');
insert into sys_dict_data values(20, 2,  '修改',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '修改操作');
insert into sys_dict_data values(21, 3,  '删除',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '删除操作');
insert into sys_dict_data values(22, 4,  '授权',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '授权操作');
insert into sys_dict_data values(23, 5,  '导出',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导出操作');
insert into sys_dict_data values(24, 6,  '导入',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导入操作');
insert into sys_dict_data values(25, 7,  '强退',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '强退操作');
insert into sys_dict_data values(26, 8,  '生成代码', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '生成操作');
insert into sys_dict_data values(27, 9,  '清空数据', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '清空操作');
insert into sys_dict_data values(28, 1,  '成功',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(29, 2,  '失败',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');


-- ----------------------------
-- 13、参数配置表
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
  config_id         int(5)          not null auto_increment    comment '参数主键',
  config_name       varchar(100)    default ''                 comment '参数名称',
  config_key        varchar(100)    default ''                 comment '参数键名',
  config_value      varchar(500)    default ''                 comment '参数键值',
  config_type       char(1)         default 'N'                comment '系统内置（Y是 N否）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (config_id)
) engine=innodb auto_increment=100 comment = '参数配置表';

insert into sys_config values(1, '主框架页-默认皮肤样式名称',     'sys.index.skinName',               'skin-blue',     'Y', 'admin', sysdate(), '', null, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow' );
insert into sys_config values(2, '用户管理-账号初始密码',         'sys.user.initPassword',            'admin@123',     'Y', 'admin', sysdate(), '', null, '初始化密码 admin@123' );
insert into sys_config values(3, '主框架页-侧边栏主题',           'sys.index.sideTheme',              'theme-dark',    'Y', 'admin', sysdate(), '', null, '深色主题theme-dark，浅色主题theme-light' );
insert into sys_config values(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser',         'false',         'Y', 'admin', sysdate(), '', null, '是否开启注册用户功能（true开启，false关闭）');
insert into sys_config values(6, '用户登录-黑名单列表',           'sys.login.blackIPList',            '',              'Y', 'admin', sysdate(), '', null, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
insert into sys_config values(7, '用户管理-初始密码修改策略',     'sys.account.initPasswordModify',   '1',             'Y', 'admin', sysdate(), '', null, '0：初始密码修改策略关闭，没有任何提示，1：强制用户首次登录修改初始密码');
insert into sys_config values(8, '用户管理-账号密码更新周期',     'sys.account.passwordValidateDays', '0',             'Y', 'admin', sysdate(), '', null, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
insert into sys_config values(9, '用户管理-密码字符范围',         'sys.account.chrtype',              '4',             'Y', 'admin', sysdate(), '', null, '默认强密码字符范围，0任意，1数字，2英文字母，3字母和数字，4字母数字和特殊字符（目前支持的特殊字符包括：~!@#$%^&*()-=_+）');


-- ----------------------------
-- 14、系统访问记录
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint(20)     not null auto_increment   comment '访问ID',
  user_name      varchar(50)    default ''                comment '用户账号',
  ipaddr         varchar(128)   default ''                comment '登录IP地址',
  login_location varchar(255)   default ''                comment '登录地点',
  browser        varchar(50)    default ''                comment '浏览器类型',
  os             varchar(50)    default ''                comment '操作系统',
  status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
  msg            varchar(255)   default ''                comment '提示消息',
  login_time     datetime                                 comment '访问时间',
  primary key (info_id),
  key idx_sys_logininfor_s  (status),
  key idx_sys_logininfor_lt (login_time)
) engine=innodb auto_increment=100 comment = '系统访问记录';


-- ----------------------------
-- 15、定时任务调度表
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
  job_id              bigint(20)    not null auto_increment    comment '任务ID',
  job_name            varchar(64)   default ''                 comment '任务名称',
  job_group           varchar(64)   default 'DEFAULT'          comment '任务组名',
  invoke_target       varchar(500)  not null                   comment '调用目标字符串',
  cron_expression     varchar(255)  default ''                 comment 'cron执行表达式',
  misfire_policy      varchar(20)   default '3'                comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  concurrent          char(1)       default '1'                comment '是否并发执行（0允许 1禁止）',
  status              char(1)       default '0'                comment '状态（0正常 1暂停）',
  create_by           varchar(64)   default ''                 comment '创建者',
  create_time         datetime                                 comment '创建时间',
  update_by           varchar(64)   default ''                 comment '更新者',
  update_time         datetime                                 comment '更新时间',
  remark              varchar(500)  default ''                 comment '备注信息',
  primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = '定时任务调度表';

insert into sys_job values(1, '系统默认（无参）', 'DEFAULT', 'scaffTask.scaffNoParams',        '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(2, '系统默认（有参）', 'DEFAULT', 'scaffTask.scaffParams(\'scaff\')',  '0/15 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(3, '系统默认（多参）', 'DEFAULT', 'scaffTask.scaffMultipleParams(\'scaff\', true, 2000L, 316.50D, 100)',  '0/20 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 16、定时任务调度日志表
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
  job_log_id          bigint(20)     not null auto_increment    comment '任务日志ID',
  job_name            varchar(64)    not null                   comment '任务名称',
  job_group           varchar(64)    not null                   comment '任务组名',
  invoke_target       varchar(500)   not null                   comment '调用目标字符串',
  job_message         varchar(500)                              comment '日志信息',
  status              char(1)        default '0'                comment '执行状态（0正常 1失败）',
  exception_info      varchar(2000)  default ''                 comment '异常信息',
  start_time          datetime                                  comment '执行开始时间',
  end_time            datetime                                  comment '执行结束时间',
  create_time         datetime                                  comment '创建时间',
  primary key (job_log_id)
) engine=innodb comment = '定时任务调度日志表';

-- ----------------------------
-- 16.1、定时任务调度过程日志明细表
-- ----------------------------
drop table if exists sys_job_log_detail;
create table sys_job_log_detail (
  detail_id           bigint(20)     not null auto_increment    comment '明细主键',
  job_log_id          bigint(20)     not null                   comment '任务日志ID',
  log_level           varchar(10)    not null                   comment '日志级别（INFO WARN ERROR）',
  log_content         varchar(1000)  not null                   comment '日志内容',
  sort_no             int(4)         default 0                  comment '排序号',
  create_time         datetime                                  comment '创建时间',
  primary key (detail_id),
  key idx_sys_job_log_detail_log_id (job_log_id),
  key idx_sys_job_log_detail_level (log_level),
  key idx_sys_job_log_detail_ct (create_time)
) engine=innodb comment = '定时任务调度过程日志明细表';


-- ----------------------------
-- 10.2、消息队列主台账
-- ----------------------------
drop table if exists sys_mq_message_log;
create table sys_mq_message_log (
  message_log_id     bigint(20)      not null auto_increment    comment '消息台账主键',
  message_type       varchar(100)    default ''                 comment '消息类型',
  stream_key         varchar(200)    default ''                 comment '原始Stream',
  message_id         varchar(100)    default ''                 comment 'Redis Stream消息ID',
  consumer_group     varchar(100)    default ''                 comment '消费者组',
  business_key       varchar(200)    default ''                 comment '业务幂等键',
  payload            text                                       comment '消息内容',
  status             char(1)         default '0'                comment '状态（0执行中 1成功 2失败 3已跳过 4死信）',
  retry_times        int(4)          default 0                  comment '已尝试次数',
  max_retry_times    int(4)          default 0                  comment '最大重试次数',
  first_consume_time datetime                                   comment '首次消费时间',
  last_consume_time  datetime                                   comment '最后消费时间',
  success_time       datetime                                   comment '成功时间',
  dead_letter_time   datetime                                   comment '进入死信时间',
  last_error_msg     varchar(2000)   default ''                 comment '最后错误信息',
  create_time        datetime                                   comment '创建时间',
  update_time        datetime                                   comment '更新时间',
  primary key (message_log_id),
  unique key uk_stream_message (stream_key, message_id),
  key idx_sys_mq_message_log_type (message_type),
  key idx_sys_mq_message_log_status (status),
  key idx_sys_mq_message_log_business_key (business_key),
  key idx_sys_mq_message_log_ct (create_time)
) engine=innodb auto_increment=100 comment = '消息队列主台账';

-- ----------------------------
-- 10.3、消息队列执行明细
-- ----------------------------
drop table if exists sys_mq_message_log_detail;
create table sys_mq_message_log_detail (
  detail_id      bigint(20)      not null auto_increment    comment '执行明细主键',
  message_log_id bigint(20)      not null                   comment '消息台账主键',
  attempt_no     int(4)          default 0                  comment '执行次数',
  consumer_name  varchar(100)    default ''                 comment '消费者名称',
  status         char(1)         default '0'                comment '状态（0执行中 1成功 2失败 3已跳过）',
  start_time     datetime                                   comment '开始时间',
  end_time       datetime                                   comment '结束时间',
  cost_time      bigint(20)      default 0                  comment '耗时毫秒',
  error_msg      varchar(2000)   default ''                 comment '错误信息',
  create_time    datetime                                   comment '创建时间',
  primary key (detail_id),
  key idx_sys_mq_message_log_detail_log_id (message_log_id),
  key idx_sys_mq_message_log_detail_status (status),
  key idx_sys_mq_message_log_detail_ct (create_time)
) engine=innodb auto_increment=100 comment = '消息队列执行明细';
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;

-- ----------------------------
-- 1、存储每一个已配置的 jobDetail 的详细信息
-- ----------------------------
create table QRTZ_JOB_DETAILS (
    sched_name           varchar(120)    not null            comment '调度名称',
    job_name             varchar(200)    not null            comment '任务名称',
    job_group            varchar(200)    not null            comment '任务组名',
    description          varchar(250)    null                comment '相关介绍',
    job_class_name       varchar(250)    not null            comment '执行任务类名称',
    is_durable           varchar(1)      not null            comment '是否持久化',
    is_nonconcurrent     varchar(1)      not null            comment '是否并发',
    is_update_data       varchar(1)      not null            comment '是否更新数据',
    requests_recovery    varchar(1)      not null            comment '是否接受恢复执行',
    job_data             blob            null                comment '存放持久化job对象',
    primary key (sched_name, job_name, job_group)
) engine=innodb comment = '任务详细信息表';

-- ----------------------------
-- 2、 存储已配置的 Trigger 的信息
-- ----------------------------
create table QRTZ_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment '触发器的名字',
    trigger_group        varchar(200)    not null            comment '触发器所属组的名字',
    job_name             varchar(200)    not null            comment 'qrtz_job_details表job_name的外键',
    job_group            varchar(200)    not null            comment 'qrtz_job_details表job_group的外键',
    description          varchar(250)    null                comment '相关介绍',
    next_fire_time       bigint(13)      null                comment '上一次触发时间（毫秒）',
    prev_fire_time       bigint(13)      null                comment '下一次触发时间（默认为-1表示不触发）',
    priority             integer         null                comment '优先级',
    trigger_state        varchar(16)     not null            comment '触发器状态',
    trigger_type         varchar(8)      not null            comment '触发器的类型',
    start_time           bigint(13)      not null            comment '开始时间',
    end_time             bigint(13)      null                comment '结束时间',
    calendar_name        varchar(200)    null                comment '日程表名称',
    misfire_instr        smallint(2)     null                comment '补偿执行的策略',
    job_data             blob            null                comment '存放持久化job对象',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, job_name, job_group) references QRTZ_JOB_DETAILS(sched_name, job_name, job_group)
) engine=innodb comment = '触发器详细信息表';

-- ----------------------------
-- 3、 存储简单的 Trigger，包括重复次数，间隔，以及已触发的次数
-- ----------------------------
create table QRTZ_SIMPLE_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    repeat_count         bigint(7)       not null            comment '重复的次数统计',
    repeat_interval      bigint(12)      not null            comment '重复的间隔时间',
    times_triggered      bigint(10)      not null            comment '已经触发的次数',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = '简单触发器的信息表';

-- ----------------------------
-- 4、 存储 Cron Trigger，包括 Cron 表达式和时区信息
-- ----------------------------
create table QRTZ_CRON_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    cron_expression      varchar(200)    not null            comment 'cron表达式',
    time_zone_id         varchar(80)                         comment '时区',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'Cron类型的触发器表';

-- ----------------------------
-- 5、 Trigger 作为 Blob 类型存储(用于 Quartz 用户用 JDBC 创建他们自己定制的 Trigger 类型，JobStore 并不知道如何存储实例的时候)
-- ----------------------------
create table QRTZ_BLOB_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    blob_data            blob            null                comment '存放持久化Trigger对象',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'Blob类型的触发器表';

-- ----------------------------
-- 6、 以 Blob 类型存储存放日历信息， quartz可配置一个日历来指定一个时间范围
-- ----------------------------
create table QRTZ_CALENDARS (
    sched_name           varchar(120)    not null            comment '调度名称',
    calendar_name        varchar(200)    not null            comment '日历名称',
    calendar             blob            not null            comment '存放持久化calendar对象',
    primary key (sched_name, calendar_name)
) engine=innodb comment = '日历信息表';

-- ----------------------------
-- 7、 存储已暂停的 Trigger 组的信息
-- ----------------------------
create table QRTZ_PAUSED_TRIGGER_GRPS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    primary key (sched_name, trigger_group)
) engine=innodb comment = '暂停的触发器表';

-- ----------------------------
-- 8、 存储与已触发的 Trigger 相关的状态信息，以及相联 Job 的执行信息
-- ----------------------------
create table QRTZ_FIRED_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    entry_id             varchar(95)     not null            comment '调度器实例id',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    instance_name        varchar(200)    not null            comment '调度器实例名',
    fired_time           bigint(13)      not null            comment '触发的时间',
    sched_time           bigint(13)      not null            comment '定时器制定的时间',
    priority             integer         not null            comment '优先级',
    state                varchar(16)     not null            comment '状态',
    job_name             varchar(200)    null                comment '任务名称',
    job_group            varchar(200)    null                comment '任务组名',
    is_nonconcurrent     varchar(1)      null                comment '是否并发',
    requests_recovery    varchar(1)      null                comment '是否接受恢复执行',
    primary key (sched_name, entry_id)
) engine=innodb comment = '已触发的触发器表';

-- ----------------------------
-- 9、 存储少量的有关 Scheduler 的状态信息，假如是用于集群中，可以看到其他的 Scheduler 实例
-- ----------------------------
create table QRTZ_SCHEDULER_STATE (
    sched_name           varchar(120)    not null            comment '调度名称',
    instance_name        varchar(200)    not null            comment '实例名称',
    last_checkin_time    bigint(13)      not null            comment '上次检查时间',
    checkin_interval     bigint(13)      not null            comment '检查间隔时间',
    primary key (sched_name, instance_name)
) engine=innodb comment = '调度器状态表';

-- ----------------------------
-- 10、 存储程序的悲观锁的信息(假如使用了悲观锁)
-- ----------------------------
create table QRTZ_LOCKS (
    sched_name           varchar(120)    not null            comment '调度名称',
    lock_name            varchar(40)     not null            comment '悲观锁名称',
    primary key (sched_name, lock_name)
) engine=innodb comment = '存储的悲观锁信息表';

-- ----------------------------
-- 11、 Quartz集群实现同步机制的行锁表
-- ----------------------------
create table QRTZ_SIMPROP_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    str_prop_1           varchar(512)    null                comment 'String类型的trigger的第一个参数',
    str_prop_2           varchar(512)    null                comment 'String类型的trigger的第二个参数',
    str_prop_3           varchar(512)    null                comment 'String类型的trigger的第三个参数',
    int_prop_1           int             null                comment 'int类型的trigger的第一个参数',
    int_prop_2           int             null                comment 'int类型的trigger的第二个参数',
    long_prop_1          bigint          null                comment 'long类型的trigger的第一个参数',
    long_prop_2          bigint          null                comment 'long类型的trigger的第二个参数',
    dec_prop_1           numeric(13,4)   null                comment 'decimal类型的trigger的第一个参数',
    dec_prop_2           numeric(13,4)   null                comment 'decimal类型的trigger的第二个参数',
    bool_prop_1          varchar(1)      null                comment 'Boolean类型的trigger的第一个参数',
    bool_prop_2          varchar(1)      null                comment 'Boolean类型的trigger的第二个参数',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = '同步机制的行锁表';

-- ============================================================================
-- iip 发票积分平台（业务表 + 菜单权限 + 种子数据 + 定时任务）
-- ============================================================================

-- ----------------------------
-- iip 1、小程序用户表
-- ----------------------------
drop table if exists iip_member;
create table iip_member (
  member_id         bigint(20)      not null auto_increment    comment '用户ID',
  nickname          varchar(64)     default ''                 comment '昵称',
  avatar            varchar(255)    default ''                 comment '头像',
  phone             varchar(20)     default ''                 comment '手机号',
  gender            char(1)         default '2'                comment '性别（0男 1女 2未知）',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  last_login_time   datetime        default null               comment '最近登录时间',
  last_login_ip     varchar(64)     default ''                 comment '最近登录IP',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (member_id)
) engine=innodb auto_increment=100 comment = '小程序用户表';

-- ----------------------------
-- iip 2、用户平台账号表
-- ----------------------------
drop table if exists iip_member_account;
create table iip_member_account (
  account_id        bigint(20)      not null auto_increment    comment '账号ID',
  member_id         bigint(20)      not null                   comment '用户ID',
  platform          varchar(16)     not null                   comment '平台（wechat/alipay/unionpay）',
  openid            varchar(128)    not null                   comment '平台用户标识',
  unionid           varchar(128)    default ''                 comment '平台联合标识',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (account_id),
  unique key uk_platform_openid (platform, openid),
  key idx_member (member_id)
) engine=innodb auto_increment=100 comment = '用户平台账号表';

-- ----------------------------
-- iip 3、商户表
-- ----------------------------
drop table if exists iip_merchant;
create table iip_merchant (
  merchant_id       bigint(20)      not null auto_increment    comment '商户ID',
  merchant_no       varchar(32)     not null                   comment '商户编号（M+年月+5位序号）',
  merchant_name     varchar(128)    not null                   comment '商户名称',
  category          varchar(64)     default ''                 comment '商户类别（餐饮/住宿/加油/景区等）',
  city              varchar(64)     default ''                 comment '所在市县（用于活动地域匹配）',
  contact_name      varchar(64)     default ''                 comment '联系人',
  contact_phone     varchar(20)     default ''                 comment '联系电话',
  address           varchar(255)    default ''                 comment '商户地址',
  description       varchar(500)    default null               comment '商家介绍',
  logo              varchar(255)    default null               comment '商户logo图片',
  business_hours    varchar(64)     default null               comment '营业时间',
  longitude         decimal(10,6)   default null               comment '经度',
  latitude          decimal(10,6)   default null               comment '纬度',
  business_license  varchar(255)    default ''                 comment '营业执照图片',
  member_id         bigint(20)      default null               comment '绑定的登录用户ID',
  status            char(1)         default '2'                comment '状态（0正常 1停用 2待审核）',
  is_recommend      char(1)         default '1'                comment '是否推荐（0推荐 1不推荐）',
  audit_by          varchar(64)     default ''                 comment '审核人',
  audit_time        datetime        default null               comment '审核时间',
  audit_remark      varchar(255)    default ''                 comment '审核备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (merchant_id),
  unique key uk_merchant_no (merchant_no),
  key idx_member (member_id)
) engine=innodb auto_increment=100 comment = '商户表';

-- ----------------------------
-- iip 4、发票表
-- ----------------------------
drop table if exists iip_invoice;
create table iip_invoice (
  invoice_id        bigint(20)      not null auto_increment    comment '发票ID',
  member_id         bigint(20)      not null                   comment '上传用户ID',
  merchant_id       bigint(20)      default null               comment '关联商户ID',
  merchant_name     varchar(128)    not null                   comment '商户名称',
  invoice_code      varchar(20)     default ''                 comment '发票代码',
  invoice_no        varchar(30)     not null                   comment '发票号码',
  invoice_date      date            default null               comment '开票日期',
  amount            decimal(10,2)   not null                   comment '发票金额',
  image_url         varchar(255)    not null                   comment '发票图片地址',
  status            char(1)         default '0'                comment '状态（0待审核 1已通过 2已驳回）',
  points            int(11)         default 0                  comment '发放积分数',
  activity_id       bigint(20)      default null               comment '发分依据的活动ID',
  audit_by          varchar(64)     default ''                 comment '审核人',
  audit_time        datetime        default null               comment '审核时间',
  audit_remark      varchar(255)    default ''                 comment '审核备注（驳回填原因）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (invoice_id),
  unique key uk_invoice (invoice_code, invoice_no),
  key idx_member (member_id)
) engine=innodb auto_increment=1000 comment = '发票表';

-- ----------------------------
-- iip 5、积分账户表
-- ----------------------------
drop table if exists iip_points_account;
create table iip_points_account (
  account_id        bigint(20)      not null auto_increment    comment '账户ID',
  member_id         bigint(20)      not null                   comment '用户ID',
  total_points      int(11)         default 0                  comment '累计获得积分',
  available_points  int(11)         default 0                  comment '可用积分',
  used_points       int(11)         default 0                  comment '已使用积分',
  expired_points    int(11)         default 0                  comment '已过期积分',
  version           int(11)         default 0                  comment '乐观锁版本号',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (account_id),
  unique key uk_member (member_id)
) engine=innodb auto_increment=100 comment = '积分账户表';

-- ----------------------------
-- iip 6、积分流水表
-- ----------------------------
drop table if exists iip_points_record;
create table iip_points_record (
  record_id         bigint(20)      not null auto_increment    comment '流水ID',
  member_id         bigint(20)      not null                   comment '用户ID',
  change_type       varchar(16)     not null                   comment '变动类型（earn获得 consume消费 expire过期 adjust调整）',
  points            int(11)         not null                   comment '变动数量（正数）',
  balance_after     int(11)         not null                   comment '变动后可用余额',
  biz_type          varchar(32)     default ''                 comment '业务来源（invoice_audit/coupon_exchange/admin_adjust/point_expire）',
  biz_id            varchar(64)     default ''                 comment '业务单据ID（幂等用）',
  remaining         int(11)         default 0                  comment '该批剩余未消耗（仅earn）',
  expire_time       datetime        default null               comment '批次过期时间（仅earn）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (record_id),
  key idx_member_type (member_id, change_type),
  key idx_expire (change_type, expire_time)
) engine=innodb auto_increment=1000 comment = '积分流水表';

-- ----------------------------
-- iip 7、券定义表
-- ----------------------------
drop table if exists iip_coupon;
create table iip_coupon (
  coupon_id          bigint(20)      not null auto_increment    comment '券ID',
  coupon_name        varchar(128)    not null                   comment '券名称',
  coupon_type        varchar(16)     default 'ticket'           comment '券类型（ticket门票 virtual虚拟物品 full_reduction满减 discount折扣）',
  category           varchar(32)     default 'general'          comment '券品类（general通用 scenic_ticket景区门票 hotel酒店券 dining餐饮券 flight_package机票+权益包 duty_free免税周边）',
  cover_image        varchar(255)    default ''                 comment '封面图片',
  target_name        varchar(128)    default ''                 comment '适用对象（如景区名）',
  points_cost        int(11)         not null                   comment '兑换所需积分',
  total_stock        int(11)         default -1                 comment '总库存（-1不限）',
  remain_stock       int(11)         default 0                  comment '剩余库存',
  per_member_limit   int(11)         default 1                  comment '每人限兑数量（-1不限）',
  exchange_start_time datetime       default null               comment '兑换开始时间',
  exchange_end_time  datetime        default null               comment '兑换结束时间',
  valid_type         varchar(16)     default 'fixed'            comment '有效期类型（fixed固定区间 days领取后N天）',
  valid_start_time   datetime        default null               comment '有效期开始',
  valid_end_time     datetime        default null               comment '有效期结束',
  valid_days         int(11)         default 0                  comment '领取后有效天数',
  threshold_amount   decimal(10,2)   default null               comment '满减门槛金额',
  discount_amount    decimal(10,2)   default null               comment '满减面额',
  merchant_id        bigint(20)      default null               comment '指定可用商户ID（null通用）',
  sponsor_type       varchar(20)     default 'platform'         comment '赞助方类型（platform平台 bank银行 merchant商户）',
  sponsor_name       varchar(128)    default ''                 comment '赞助方名称（如海南农商银行）',
  use_desc           text                                       comment '使用说明',
  status             char(1)         default '0'                comment '状态（0上架 1下架）',
  sort               int(11)         default 0                  comment '显示顺序',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default ''                 comment '备注',
  primary key (coupon_id)
) engine=innodb auto_increment=100 comment = '券定义表';

-- ----------------------------
-- iip 8、券实例表（兑换记录）
-- ----------------------------
drop table if exists iip_coupon_record;
create table iip_coupon_record (
  record_id          bigint(20)      not null auto_increment    comment '记录ID',
  coupon_id          bigint(20)      not null                   comment '券ID',
  coupon_name        varchar(128)    default ''                 comment '券名称（冗余）',
  coupon_type        varchar(16)     default ''                 comment '券类型（冗余）',
  member_id          bigint(20)      not null                   comment '兑换用户ID',
  points_cost        int(11)         default 0                  comment '兑换消耗积分',
  verify_code        varchar(32)     not null                   comment '核销码',
  status             char(1)         default '0'                comment '状态（0未使用 1已使用 2已过期）',
  exchange_time      datetime        default null               comment '兑换时间',
  valid_start_time   datetime        default null               comment '有效期开始',
  valid_end_time     datetime        default null               comment '有效期结束',
  verify_time        datetime        default null               comment '核销时间',
  verify_merchant_id bigint(20)      default null               comment '核销商户ID',
  verify_by          varchar(64)     default ''                 comment '核销操作人',
  activity_id        bigint(20)      default null               comment '来源活动ID',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default ''                 comment '备注',
  primary key (record_id),
  unique key uk_verify_code (verify_code),
  key idx_coupon (coupon_id),
  key idx_member_status (member_id, status)
) engine=innodb auto_increment=1000 comment = '券实例表';

-- ----------------------------
-- iip 9、活动表
-- ----------------------------
drop table if exists iip_activity;
create table iip_activity (
  activity_id       bigint(20)      not null auto_increment    comment '活动ID',
  activity_no       varchar(32)     not null                   comment '活动编号（A+年月+4位序号）',
  activity_name     varchar(128)    not null                   comment '活动名称',
  cover_image       varchar(255)    default ''                 comment '活动封面',
  description       text                                       comment '活动描述',
  start_time        datetime        not null                   comment '开始时间',
  end_time          datetime        not null                   comment '结束时间',
  points_ratio      decimal(10,2)   default 1.00               comment '发票金额积分比例',
  merchant_limit    int(11)         default -1                 comment '参与商户数上限（-1不限）',
  coupon_quota      int(11)         default -1                 comment '发券总额度（-1不限）',
  city              varchar(64)     default ''                 comment '适用市县（空=全省通用）',
  region_type       varchar(20)     default 'province'         comment '地域类型（province全省 city市县 business_district商圈 scenic景区）',
  region_name       varchar(128)    default ''                 comment '商圈/景区名称（region_type为商圈/景区时填）',
  priority          int(11)         default 0                  comment '优先级（数值越大越优先）',
  status            char(1)         default '0'                comment '状态（0启用 1停用）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (activity_id),
  unique key uk_activity_no (activity_no)
) engine=innodb auto_increment=100 comment = '活动表';

-- ----------------------------
-- iip 10、活动商户关联表
-- ----------------------------
drop table if exists iip_activity_merchant;
create table iip_activity_merchant (
  id                bigint(20)      not null auto_increment    comment '主键ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  merchant_id       bigint(20)      not null                   comment '商户ID',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (id),
  unique key uk_act_mer (activity_id, merchant_id)
) engine=innodb auto_increment=100 comment = '活动商户关联表';

-- ----------------------------
-- iip 11、活动券配置表
-- ----------------------------
drop table if exists iip_activity_coupon;
create table iip_activity_coupon (
  id                bigint(20)      not null auto_increment    comment '主键ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  coupon_id         bigint(20)      not null                   comment '券ID',
  issue_limit       int(11)         default -1                 comment '发行上限（-1不限）',
  issued_count      int(11)         default 0                  comment '已发行数量',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (id),
  unique key uk_act_coupon (activity_id, coupon_id)
) engine=innodb auto_increment=100 comment = '活动券配置表';

-- ----------------------------
-- iip 11.1、首页轮播banner表
-- ----------------------------
drop table if exists iip_banner;
create table iip_banner (
  banner_id          bigint(20)      not null auto_increment    comment 'banner ID',
  activity_id        bigint(20)      default null               comment '关联活动ID',
  title              varchar(128)    not null default ''        comment '标题',
  image_url          varchar(255)    not null default ''        comment '图片地址',
  link_type          varchar(16)     not null default 'none'    comment '跳转类型 none纯展示/rules活动规则/mall积分商城',
  link_value         varchar(255)    not null default ''        comment '跳转参数',
  sort               int(11)         not null default 0         comment '排序越小越前',
  status             char(1)         not null default '0'       comment '状态（0启用 1停用）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (banner_id)
) engine=innodb comment = '首页轮播banner';

-- ----------------------------
-- iip 12、菜单权限（menu_id 200~241）
-- ----------------------------
-- 一级目录
insert into sys_menu values('200', '发票积分', '0', '8', 'iip', null, '', '', 1, 0, 'M', '0', '0', '', 'guide', 'admin', sysdate(), '', null, '发票积分目录');
-- 页面菜单
insert into sys_menu values('201', '数据概览', '200', '1', 'overview', 'iip/overview/index', '', '', 1, 0, 'C', '0', '0', 'iip:overview:list', 'chart', 'admin', sysdate(), '', null, '数据概览菜单');
insert into sys_menu values('202', '会员管理', '200', '2', 'member', 'iip/member/index', '', '', 1, 0, 'C', '0', '0', 'iip:member:list', 'peoples', 'admin', sysdate(), '', null, '用户管理菜单');
insert into sys_menu values('203', '商户管理', '200', '3', 'merchant', 'iip/merchant/index', '', '', 1, 0, 'C', '0', '0', 'iip:merchant:list', 'shop', 'admin', sysdate(), '', null, '商户管理菜单');
insert into sys_menu values('204', '发票审核', '200', '4', 'invoice', 'iip/invoice/index', '', '', 1, 0, 'C', '0', '0', 'iip:invoice:list', 'form', 'admin', sysdate(), '', null, '发票审核菜单');
insert into sys_menu values('205', '积分流水', '200', '5', 'points', 'iip/points/index', '', '', 1, 0, 'C', '0', '0', 'iip:points:list', 'star', 'admin', sysdate(), '', null, '积分流水菜单');
insert into sys_menu values('206', '券管理', '200', '6', 'coupon', 'iip/coupon/index', '', '', 1, 0, 'C', '0', '0', 'iip:coupon:list', 'gift', 'admin', sysdate(), '', null, '券管理菜单');
insert into sys_menu values('207', '兑换记录', '200', '7', 'exchange', 'iip/exchange/index', '', '', 1, 0, 'C', '0', '0', 'iip:exchange:list', 'list', 'admin', sysdate(), '', null, '兑换记录菜单');
insert into sys_menu values('208', '活动管理', '200', '8', 'activity', 'iip/activity/index', '', '', 1, 0, 'C', '0', '0', 'iip:activity:list', 'tab', 'admin', sysdate(), '', null, '活动管理菜单');
insert into sys_menu values('237', 'banner管理', '200', '9', 'banner', 'iip/banner/index', '', '', 1, 0, 'C', '0', '0', 'iip:banner:list', 'tab', 'admin', sysdate(), '', null, 'banner管理菜单');
-- 用户管理按钮
insert into sys_menu values('211', '用户查询', '202', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:member:query', '#', 'admin', sysdate(), '', null, '用户查询按钮');
insert into sys_menu values('212', '用户停用启用', '202', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:member:edit', '#', 'admin', sysdate(), '', null, '用户停用启用按钮');
insert into sys_menu values('213', '用户导出', '202', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:member:export', '#', 'admin', sysdate(), '', null, '用户导出按钮');
-- 商户管理按钮
insert into sys_menu values('214', '商户查询', '203', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:merchant:query', '#', 'admin', sysdate(), '', null, '商户查询按钮');
insert into sys_menu values('215', '商户新增', '203', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:merchant:add', '#', 'admin', sysdate(), '', null, '商户新增按钮');
insert into sys_menu values('216', '商户修改', '203', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:merchant:edit', '#', 'admin', sysdate(), '', null, '商户修改按钮');
insert into sys_menu values('217', '商户删除', '203', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:merchant:remove', '#', 'admin', sysdate(), '', null, '商户删除按钮');
insert into sys_menu values('218', '商户审核', '203', '5', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:merchant:audit', '#', 'admin', sysdate(), '', null, '商户审核按钮');
insert into sys_menu values('219', '商户导出', '203', '6', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:merchant:export', '#', 'admin', sysdate(), '', null, '商户导出按钮');
-- 发票审核按钮
insert into sys_menu values('220', '发票查询', '204', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:invoice:query', '#', 'admin', sysdate(), '', null, '发票查询按钮');
insert into sys_menu values('221', '发票审核', '204', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:invoice:audit', '#', 'admin', sysdate(), '', null, '发票审核按钮');
insert into sys_menu values('222', '发票导出', '204', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:invoice:export', '#', 'admin', sysdate(), '', null, '发票导出按钮');
-- 积分流水按钮
insert into sys_menu values('223', '积分查询', '205', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:points:query', '#', 'admin', sysdate(), '', null, '积分查询按钮');
insert into sys_menu values('224', '手工调整', '205', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:points:adjust', '#', 'admin', sysdate(), '', null, '手工调整按钮');
-- 券管理按钮
insert into sys_menu values('225', '券查询', '206', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:coupon:query', '#', 'admin', sysdate(), '', null, '券查询按钮');
insert into sys_menu values('226', '券新增', '206', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:coupon:add', '#', 'admin', sysdate(), '', null, '券新增按钮');
insert into sys_menu values('227', '券修改', '206', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:coupon:edit', '#', 'admin', sysdate(), '', null, '券修改按钮');
insert into sys_menu values('228', '券删除', '206', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:coupon:remove', '#', 'admin', sysdate(), '', null, '券删除按钮');
insert into sys_menu values('229', '券导出', '206', '5', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:coupon:export', '#', 'admin', sysdate(), '', null, '券导出按钮');
-- 兑换记录按钮
insert into sys_menu values('230', '兑换查询', '207', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:exchange:query', '#', 'admin', sysdate(), '', null, '兑换查询按钮');
insert into sys_menu values('231', '兑换导出', '207', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:exchange:export', '#', 'admin', sysdate(), '', null, '兑换导出按钮');
-- 活动管理按钮
insert into sys_menu values('232', '活动查询', '208', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:activity:query', '#', 'admin', sysdate(), '', null, '活动查询按钮');
insert into sys_menu values('233', '活动新增', '208', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:activity:add', '#', 'admin', sysdate(), '', null, '活动新增按钮');
insert into sys_menu values('234', '活动修改', '208', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:activity:edit', '#', 'admin', sysdate(), '', null, '活动修改按钮');
insert into sys_menu values('235', '活动删除', '208', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:activity:remove', '#', 'admin', sysdate(), '', null, '活动删除按钮');
insert into sys_menu values('236', '活动配置', '208', '5', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:activity:config', '#', 'admin', sysdate(), '', null, '活动配置按钮');
-- banner管理按钮
insert into sys_menu values('238', 'banner查询', '237', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:banner:query', '#', 'admin', sysdate(), '', null, 'banner查询按钮');
insert into sys_menu values('239', 'banner新增', '237', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:banner:add', '#', 'admin', sysdate(), '', null, 'banner新增按钮');
insert into sys_menu values('240', 'banner修改', '237', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:banner:edit', '#', 'admin', sysdate(), '', null, 'banner修改按钮');
insert into sys_menu values('241', 'banner删除', '237', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'iip:banner:remove', '#', 'admin', sysdate(), '', null, 'banner删除按钮');
-- 角色默认授权（role_id=2 授予 200~241 全部）
insert into sys_role_menu values ('2', '200');
insert into sys_role_menu values ('2', '201');
insert into sys_role_menu values ('2', '202');
insert into sys_role_menu values ('2', '203');
insert into sys_role_menu values ('2', '204');
insert into sys_role_menu values ('2', '205');
insert into sys_role_menu values ('2', '206');
insert into sys_role_menu values ('2', '207');
insert into sys_role_menu values ('2', '208');
insert into sys_role_menu values ('2', '211');
insert into sys_role_menu values ('2', '212');
insert into sys_role_menu values ('2', '213');
insert into sys_role_menu values ('2', '214');
insert into sys_role_menu values ('2', '215');
insert into sys_role_menu values ('2', '216');
insert into sys_role_menu values ('2', '217');
insert into sys_role_menu values ('2', '218');
insert into sys_role_menu values ('2', '219');
insert into sys_role_menu values ('2', '220');
insert into sys_role_menu values ('2', '221');
insert into sys_role_menu values ('2', '222');
insert into sys_role_menu values ('2', '223');
insert into sys_role_menu values ('2', '224');
insert into sys_role_menu values ('2', '225');
insert into sys_role_menu values ('2', '226');
insert into sys_role_menu values ('2', '227');
insert into sys_role_menu values ('2', '228');
insert into sys_role_menu values ('2', '229');
insert into sys_role_menu values ('2', '230');
insert into sys_role_menu values ('2', '231');
insert into sys_role_menu values ('2', '232');
insert into sys_role_menu values ('2', '233');
insert into sys_role_menu values ('2', '234');
insert into sys_role_menu values ('2', '235');
insert into sys_role_menu values ('2', '236');
insert into sys_role_menu values ('2', '237');
insert into sys_role_menu values ('2', '238');
insert into sys_role_menu values ('2', '239');
insert into sys_role_menu values ('2', '240');
insert into sys_role_menu values ('2', '241');

-- ----------------------------
-- iip 13、种子数据（商户/券/活动/关联）
-- ----------------------------
insert into iip_merchant (merchant_id, merchant_no, merchant_name, category, contact_name, contact_phone, address, description, logo, business_hours, longitude, latitude, status, is_recommend, audit_by, audit_time, create_by, create_time, remark) values
(1, 'M20260700001', '老字号烩面馆', '餐饮', '王掌柜', '0372-5550001', '安阳市文峰区老街12号', '安阳本地经营三十余年的老字号面馆，主打手工烩面、羊肉鲜汤和豫北家常菜，汤头每日现熬、面条现擀现煮，是街坊聚餐和游客品尝地道安阳味道的热门去处。', '/profile/upload/2026/07/19/merchant-demo-1.jpg', '10:00-22:00', 114.357500, 36.098700, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '种子商户'),
(2, 'M20260700002', '殷都宾馆', '住宿', '李经理', '0372-5550002', '安阳市殷都区殷墟路8号', '紧邻殷墟景区的中档商务宾馆，设有标准间、大床房和多功能会议室，提供自助早餐、免费停车和景区票务代办服务，适合探访殷商文化的游客与商务差旅人士入住。', '/profile/upload/2026/07/19/merchant-demo-2.jpg', '00:00-24:00', 114.316700, 36.109300, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '种子商户'),
(3, 'M20260700003', '中石化安阳加油站', '加油', '赵站长', '0372-5550003', '安阳市北关区人民大道100号', '中石化直营站点，供应92号、95号汽油及0号柴油，油品计量准确、质量有保障，配套易捷便利店、免费洗车和司机休息区，支持加油卡与移动支付，全天24小时营业。', '/profile/avatar/merchant/3.png', '00:00-24:00', 114.372100, 36.112500, '0', '1', 'admin', sysdate(), 'admin', sysdate(), '种子商户');

insert into iip_coupon (coupon_id, coupon_name, coupon_type, target_name, points_cost, total_stock, remain_stock, per_member_limit, valid_type, valid_start_time, valid_end_time, threshold_amount, discount_amount, use_desc, status, sort, create_by, create_time, remark) values
(1, '殷墟博物馆首道门票', 'ticket', '殷墟博物馆', 2000, 1000, 1000, 2, 'fixed', '2026-07-01 00:00:00', '2027-05-31 23:59:59', null, null, '凭核销码至景区售票处换票入园', '0', 1, 'admin', sysdate(), '种子券'),
(2, '殷墟宫殿宗庙遗址门票', 'ticket', '殷墟宫殿宗庙遗址', 2000, 1000, 1000, 2, 'fixed', '2026-07-01 00:00:00', '2027-05-31 23:59:59', null, null, '凭核销码至景区售票处换票入园', '0', 2, 'admin', sysdate(), '种子券'),
(3, '红旗渠景区门票', 'ticket', '红旗渠景区', 2000, 1000, 1000, 2, 'fixed', '2026-07-01 00:00:00', '2027-05-31 23:59:59', null, null, '凭核销码至景区售票处换票入园', '0', 3, 'admin', sysdate(), '种子券'),
(4, '太行大峡谷门票', 'ticket', '太行大峡谷景区', 2000, 1000, 1000, 2, 'fixed', '2026-07-01 00:00:00', '2027-05-31 23:59:59', null, null, '凭核销码至景区售票处换票入园', '0', 4, 'admin', sysdate(), '种子券'),
(5, '羑里城门票', 'ticket', '羑里城景区', 1500, 1000, 1000, 2, 'fixed', '2026-07-01 00:00:00', '2027-05-31 23:59:59', null, null, '凭核销码至景区售票处换票入园', '0', 5, 'admin', sysdate(), '种子券'),
(6, '餐饮满100减20券', 'full_reduction', '参与餐饮商户', 500, 5000, 5000, 5, 'fixed', '2026-07-01 00:00:00', '2027-05-31 23:59:59', 100.00, 20.00, '单笔消费满100元可用，核销后直接抵扣20元', '0', 6, 'admin', sysdate(), '种子券');

insert into iip_activity (activity_id, activity_no, activity_name, description, start_time, end_time, points_ratio, merchant_limit, coupon_quota, status, create_by, create_time, remark) values
(1, 'A2026070001', '乐享安阳——发票核验积分兑换活动', '在参与商户消费取得发票并上传，审核通过后按发票面额1:1发放积分，积分可兑换景区门票与商户优惠券。', '2026-07-01 00:00:00', '2027-05-31 23:59:59', 1.00, -1, -1, '0', 'admin', sysdate(), '种子活动');

insert into iip_activity_merchant (activity_id, merchant_id, status, create_by, create_time) values
(1, 1, '0', 'admin', sysdate()),
(1, 2, '0', 'admin', sysdate()),
(1, 3, '0', 'admin', sysdate());

insert into iip_activity_coupon (activity_id, coupon_id, issue_limit, issued_count, create_by, create_time) values
(1, 1, -1, 0, 'admin', sysdate()),
(1, 2, -1, 0, 'admin', sysdate()),
(1, 3, -1, 0, 'admin', sysdate()),
(1, 4, -1, 0, 'admin', sysdate()),
(1, 5, -1, 0, 'admin', sysdate()),
(1, 6, -1, 0, 'admin', sysdate());

-- ----------------------------
-- iip 13.1、海南化种子（海南商户/活动/特色券/关联）
-- ----------------------------
insert into iip_merchant (merchant_id, merchant_no, merchant_name, category, city, contact_name, contact_phone, address, description, logo, business_hours, longitude, latitude, status, is_recommend, audit_by, audit_time, create_by, create_time, remark) values
(4, 'M20260800001', '三亚湾海鲜广场', '餐饮', '三亚', '陈老板', '0898-88000001', '三亚市天涯区三亚湾路18号', '坐落于三亚湾路的临海海鲜餐饮广场，每日直供南海新鲜渔获，主打和乐蟹、芒果螺、清蒸石斑鱼等现捞现做的琼味海鲜，设有观海餐位，是游客赏三亚湾日落、品海鲜盛宴的热门打卡地。', '/profile/avatar/merchant/4.png', '10:00-23:00', 109.508300, 18.254700, '0', '1', 'admin', sysdate(), 'admin', sysdate(), '海南种子商户'),
(5, 'M20260800002', '东坡文化旅游区文创店', '景区', '儋州', '符店长', '0898-23000002', '儋州市中和镇东坡文化旅游区内', '位于东坡文化旅游区内的特色文创零售店，售卖东坡书院主题文具、儋州调声非遗周边、椰雕和黎锦工艺品，并提供景区纪念盖章服务，是游客选购海南文化伴手礼的官方门店。', '/profile/upload/2026/07/19/merchant-demo-5.jpg', '08:30-17:30', 109.576800, 19.521000, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '海南种子商户');

insert into iip_activity (activity_id, activity_no, activity_name, description, start_time, end_time, points_ratio, merchant_limit, coupon_quota, city, region_type, region_name, priority, status, create_by, create_time, remark) values
(2, 'A2026080001', '海南发票积分常态化促消费活动', '全省常态化活动：在参与商户消费取得发票并上传，审核通过后按发票面额1:1发放积分，积分可兑换海南特色券。', '2026-08-01 00:00:00', '2030-12-31 23:59:59', 1.00, -1, -1, '', 'province', '', 0, '0', 'admin', sysdate(), '海南种子活动'),
(3, 'A2026080002', '三亚商圈联营促消费活动', '三亚湾商圈联营活动：三亚市参与商户发票按1:1.2发放积分。', '2026-08-01 00:00:00', '2027-07-31 23:59:59', 1.20, -1, -1, '三亚', 'business_district', '三亚湾商圈', 10, '0', 'admin', sysdate(), '海南种子活动'),
(4, 'A2026080003', '儋州景区联营促消费活动', '东坡文化旅游区联营活动：儋州市参与商户发票按1:1.2发放积分。', '2026-08-01 00:00:00', '2027-07-31 23:59:59', 1.20, -1, -1, '儋州', 'scenic', '东坡文化旅游区', 10, '0', 'admin', sysdate(), '海南种子活动'),
(5, 'A2026080004', '中国国际消费品博览会专场', '消博会会期专场：全省参与商户发票按1:1.5发放积分。', '2027-04-13 00:00:00', '2027-04-18 23:59:59', 1.50, -1, -1, '', 'province', '', 20, '0', 'admin', sysdate(), '海南种子活动'),
(6, 'A2026080005', '海南欢乐节专场', '海南欢乐节窗口专场：全省参与商户发票按1:1.5发放积分。', '2026-12-20 00:00:00', '2026-12-31 23:59:59', 1.50, -1, -1, '', 'province', '', 20, '0', 'admin', sysdate(), '海南种子活动'),
(7, 'A2026080006', '海南春节旅游季专场', '2027春节旅游季专场：全省参与商户发票按1:1.5发放积分。', '2027-02-01 00:00:00', '2027-02-14 23:59:59', 1.50, -1, -1, '', 'province', '', 20, '0', 'admin', sysdate(), '海南种子活动');

insert into iip_coupon (coupon_id, coupon_name, coupon_type, category, target_name, points_cost, total_stock, remain_stock, per_member_limit, valid_type, valid_start_time, valid_end_time, threshold_amount, discount_amount, merchant_id, sponsor_type, sponsor_name, use_desc, status, sort, create_by, create_time, remark) values
(7, '蜈支洲岛景区门票券', 'ticket', 'scenic_ticket', '蜈支洲岛景区', 2000, 1000, 1000, 2, 'fixed', '2026-08-01 00:00:00', '2027-07-31 23:59:59', null, null, null, 'platform', '', '凭核销码至景区售票处换票入园', '0', 7, 'admin', sysdate(), '海南特色券'),
(8, '海口星级酒店住宿券 满500减100', 'full_reduction', 'hotel', '海口参与酒店', 1500, 500, 500, 2, 'fixed', '2026-08-01 00:00:00', '2027-07-31 23:59:59', 500.00, 100.00, null, 'platform', '', '单笔住宿消费满500元可用，核销后直接抵扣100元', '0', 8, 'admin', sysdate(), '海南特色券'),
(9, '海南特色餐饮券 满200减50', 'full_reduction', 'dining', '海南参与餐饮商户', 800, 2000, 2000, 5, 'fixed', '2026-08-01 00:00:00', '2027-07-31 23:59:59', 200.00, 50.00, null, 'platform', '', '单笔餐饮消费满200元可用，核销后直接抵扣50元', '0', 9, 'admin', sysdate(), '海南特色券'),
(10, '机票+免税出行权益包', 'virtual', 'flight_package', '海南进出岛旅客', 3000, 300, 300, 1, 'fixed', '2026-08-01 00:00:00', '2027-07-31 23:59:59', null, null, null, 'platform', '', '含机票立减权益与免税购物礼包，兑换后按短信指引领取', '0', 10, 'admin', sysdate(), '海南特色券'),
(11, 'cdf免税周边礼品券', 'ticket', 'duty_free', 'cdf海口国际免税城', 1000, 800, 800, 2, 'fixed', '2026-08-01 00:00:00', '2027-07-31 23:59:59', null, null, null, 'platform', '', '凭核销码至免税城服务台领取周边礼品', '0', 11, 'admin', sysdate(), '海南特色券'),
(12, '海南农商银行满减券 满100减30', 'full_reduction', 'general', '海南参与商户', 400, 3000, 3000, 5, 'fixed', '2026-08-01 00:00:00', '2027-07-31 23:59:59', 100.00, 30.00, null, 'bank', '海南农商银行', '单笔消费满100元可用，核销后直接抵扣30元', '0', 12, 'admin', sysdate(), '海南特色券'),
(13, '三亚湾海鲜广场9折券', 'discount', 'dining', '三亚湾海鲜广场', 300, 500, 500, 5, 'fixed', '2026-08-01 00:00:00', '2027-07-31 23:59:59', null, null, 4, 'merchant', '三亚湾海鲜广场', '凭核销码至三亚湾海鲜广场消费享9折优惠', '0', 13, 'admin', sysdate(), '海南特色券'),
(14, '天涯海角景区门票券', 'ticket', 'scenic_ticket', '天涯海角游览区', 1800, 1000, 1000, 2, 'fixed', '2026-08-01 00:00:00', '2027-07-31 23:59:59', null, null, null, 'platform', '', '凭核销码至景区售票处换票入园', '0', 14, 'admin', sysdate(), '海南特色券');

-- 海南特色券挂载到常态化活动（issue_limit 不超过券总库存）
insert into iip_activity_coupon (activity_id, coupon_id, issue_limit, issued_count, create_by, create_time) values
(2, 7, 500, 0, 'admin', sysdate()),
(2, 8, 300, 0, 'admin', sysdate()),
(2, 9, 1000, 0, 'admin', sysdate()),
(2, 10, 200, 0, 'admin', sysdate()),
(2, 11, 400, 0, 'admin', sysdate()),
(2, 12, 2000, 0, 'admin', sysdate()),
(2, 13, 300, 0, 'admin', sysdate()),
(2, 14, 500, 0, 'admin', sysdate());

-- ----------------------------
-- iip 13.2、安阳景区演示商户（推荐位演示数据）
-- ----------------------------
insert into iip_merchant (merchant_id, merchant_no, merchant_name, category, city, contact_name, contact_phone, address, description, logo, business_hours, longitude, latitude, status, is_recommend, audit_by, audit_time, create_by, create_time, remark) values
(6, 'M20260700004', '殷墟博物馆', '景区', '安阳', '周馆长', '0372-5550006', '安阳市殷都区殷墟路1号', '世界文化遗产殷墟的核心展示场馆，馆藏甲骨文、青铜器、玉器等殷商文物四千余件，系统展示三千多年前商代都城文明，是探访中华文明源头、研学殷商历史文化的必到之地。', '/profile/upload/2026/07/19/merchant-scenic-1.jpg', '08:00-17:30', 114.318500, 36.123600, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '安阳景区演示商户'),
(7, 'M20260700005', '红旗渠风景区', '景区', '安阳', '秦主任', '0372-5550007', '安阳市林州市太行路225号', '国家5A级旅游景区，以“人工天河”红旗渠为主体，含分水苑、青年洞、络丝潭三大景区，渠水蜿蜒于太行绝壁之间，是感悟红旗渠精神、观赏太行山水的红色旅游胜地。', '/profile/upload/2026/07/19/merchant-scenic-2.jpg', '08:00-18:00', 113.824600, 36.064200, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '安阳景区演示商户'),
(8, 'M20260700006', '太行大峡谷景区', '景区', '安阳', '杨经理', '0372-5550008', '安阳市林州市石板岩镇', '国家5A级旅游景区，境内断崖高起、群峰峥嵘，拥有桃花谷、王相岩、太行天路等核心景点，谷内溪流飞瀑、植被葱郁，是北雄风光的典型代表和避暑休闲胜地。', '/profile/upload/2026/07/19/merchant-scenic-3.jpg', '07:30-18:00', 113.715800, 36.128900, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '安阳景区演示商户'),
(9, 'M20260700007', '羑里城遗址', '景区', '安阳', '孔馆长', '0372-5550009', '安阳市汤阴县文王路羑里城遗址', '中国历史上有文字记载的第一座国家监狱遗址，周文王在此推演八卦、著《周易》，现存演易坊、大殿、御碑亭等建筑，是周易文化发祥地和易学研学圣地。', '/profile/upload/2026/07/19/merchant-scenic-4.jpg', '08:30-17:30', 114.367200, 35.921800, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '安阳景区演示商户');

-- ----------------------------
-- iip 14、定时任务种子（积分过期结转）
-- ----------------------------
insert into sys_job values(4, '积分过期结转', 'DEFAULT', 'pointExpireTask.expire()', '0 17 2 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, '每日扫描过期积分批次并结转');
