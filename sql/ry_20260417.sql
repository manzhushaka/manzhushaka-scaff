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
  status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (dept_id)
) engine=innodb auto_increment=200 comment = '部门表';

-- ----------------------------
-- 初始化-部门表数据
-- ----------------------------
insert into sys_dept values(100,  0,   '0',          '若依科技',   0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(101,  100, '0,100',      '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(102,  100, '0,100',      '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(103,  101, '0,100,101',  '研发部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(104,  101, '0,100,101',  '市场部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(105,  101, '0,100,101',  '测试部门',   3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(106,  101, '0,100,101',  '财务部门',   4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(107,  101, '0,100,101',  '运维部门',   5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(108,  102, '0,100,102',  '市场部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(109,  102, '0,100,102',  '财务部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);


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
insert into sys_user values(1,  103, 'admin', '若依', '00', 'ry@163.com', '', '15888888888', '', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '管理员');
insert into sys_user values(2,  105, 'ry',    '若依', '00', 'ry@qq.com',  '',  '15666666666', '', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '测试员');


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
insert into sys_menu values('112',  '服务监控', '2',   '4', 'server',     'monitor/server/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:server:list',     'server',        'admin', sysdate(), '', null, '服务监控菜单');
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
insert into sys_config values(2, '用户管理-账号初始密码',         'sys.user.initPassword',            '123456',        'Y', 'admin', sysdate(), '', null, '初始化密码 123456' );
insert into sys_config values(3, '主框架页-侧边栏主题',           'sys.index.sideTheme',              'theme-dark',    'Y', 'admin', sysdate(), '', null, '深色主题theme-dark，浅色主题theme-light' );
insert into sys_config values(4, '账号自助-验证码开关',           'sys.account.captchaEnabled',       'true',          'Y', 'admin', sysdate(), '', null, '是否开启验证码功能（true开启，false关闭）');
insert into sys_config values(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser',         'false',         'Y', 'admin', sysdate(), '', null, '是否开启注册用户功能（true开启，false关闭）');
insert into sys_config values(6, '用户登录-黑名单列表',           'sys.login.blackIPList',            '',              'Y', 'admin', sysdate(), '', null, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
insert into sys_config values(7, '用户管理-初始密码修改策略',     'sys.account.initPasswordModify',   '1',             'Y', 'admin', sysdate(), '', null, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
insert into sys_config values(8, '用户管理-账号密码更新周期',     'sys.account.passwordValidateDays', '0',             'Y', 'admin', sysdate(), '', null, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
insert into sys_config values(9, '用户管理-密码字符范围',         'sys.account.chrtype',              '0',             'Y', 'admin', sysdate(), '', null, '默认任意字符范围，0任意（密码可以输入任意字符），1数字（密码只能为0-9数字），2英文字母（密码只能为a-z和A-Z字母），3字母和数字（密码必须包含字母，数字）,4字母数字和特殊字符（目前支持的特殊字符包括：~!@#$%^&*()-=_+）');


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

insert into sys_job values(1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams',        '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')',  '0/15 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)',  '0/20 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');


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
