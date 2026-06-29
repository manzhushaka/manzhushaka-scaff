-- ----------------------------
-- 修复日志管理菜单空白页
-- ----------------------------
-- 适用场景：已有数据库中“日志管理”目录存在，但缺少“操作日志/登录日志”页面菜单，
-- 导致点击目录后前端只拿到空目录路由并展示空白。
-- 脚本会补齐页面级菜单、把按钮权限挂到正确页面下，并补上登录日志解锁权限。

set @log_menu_id := (
  select menu_id
  from sys_menu
  where menu_type = 'M'
    and parent_id = 1
    and (menu_name = '日志管理' or path = 'log')
  order by menu_id
  limit 1
);

insert into sys_menu (
  menu_id,
  menu_name,
  parent_id,
  order_num,
  path,
  component,
  `query`,
  route_name,
  is_frame,
  is_cache,
  menu_type,
  visible,
  status,
  perms,
  icon,
  create_by,
  create_time,
  update_by,
  update_time,
  remark
)
select
  149,
  '操作日志',
  @log_menu_id,
  1,
  'operlog',
  'monitor/operlog/index',
  '',
  '',
  1,
  0,
  'C',
  '0',
  '0',
  'monitor:operlog:list',
  'form',
  'admin',
  sysdate(),
  '',
  null,
  '操作日志菜单'
from dual
where @log_menu_id is not null
  and not exists (
    select 1
    from sys_menu
    where menu_id = 149
       or component = 'monitor/operlog/index'
       or (parent_id = @log_menu_id and menu_name = '操作日志' and menu_type = 'C')
  );

set @operlog_menu_id := (
  select menu_id
  from sys_menu
  where component = 'monitor/operlog/index'
     or (parent_id = @log_menu_id and menu_name = '操作日志' and menu_type = 'C')
  order by menu_id
  limit 1
);

update sys_menu
set parent_id = @log_menu_id,
    order_num = 1,
    path = 'operlog',
    component = 'monitor/operlog/index',
    `query` = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'monitor:operlog:list',
    icon = 'form',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '操作日志菜单'
where @log_menu_id is not null
  and @operlog_menu_id is not null
  and menu_id = @operlog_menu_id;

insert into sys_menu (
  menu_id,
  menu_name,
  parent_id,
  order_num,
  path,
  component,
  `query`,
  route_name,
  is_frame,
  is_cache,
  menu_type,
  visible,
  status,
  perms,
  icon,
  create_by,
  create_time,
  update_by,
  update_time,
  remark
)
select
  150,
  '登录日志',
  @log_menu_id,
  2,
  'logininfor',
  'monitor/logininfor/index',
  '',
  '',
  1,
  0,
  'C',
  '0',
  '0',
  'monitor:logininfor:list',
  'logininfor',
  'admin',
  sysdate(),
  '',
  null,
  '登录日志菜单'
from dual
where @log_menu_id is not null
  and not exists (
    select 1
    from sys_menu
    where menu_id = 150
       or component = 'monitor/logininfor/index'
       or (parent_id = @log_menu_id and menu_name = '登录日志' and menu_type = 'C')
  );

set @logininfor_menu_id := (
  select menu_id
  from sys_menu
  where component = 'monitor/logininfor/index'
     or (parent_id = @log_menu_id and menu_name = '登录日志' and menu_type = 'C')
  order by menu_id
  limit 1
);

update sys_menu
set parent_id = @log_menu_id,
    order_num = 2,
    path = 'logininfor',
    component = 'monitor/logininfor/index',
    `query` = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'monitor:logininfor:list',
    icon = 'logininfor',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '登录日志菜单'
where @log_menu_id is not null
  and @logininfor_menu_id is not null
  and menu_id = @logininfor_menu_id;

update sys_menu
set parent_id = @operlog_menu_id,
    order_num = case perms
      when 'monitor:operlog:list' then 1
      when 'monitor:operlog:remove' then 2
      when 'monitor:operlog:export' then 3
      when 'monitor:operlog:query' then 4
      else order_num
    end,
    update_by = 'admin',
    update_time = sysdate()
where @operlog_menu_id is not null
  and perms in ('monitor:operlog:list', 'monitor:operlog:remove', 'monitor:operlog:export', 'monitor:operlog:query');

update sys_menu
set parent_id = @logininfor_menu_id,
    order_num = case perms
      when 'monitor:logininfor:list' then 1
      when 'monitor:logininfor:remove' then 2
      when 'monitor:logininfor:export' then 3
      else order_num
    end,
    update_by = 'admin',
    update_time = sysdate()
where @logininfor_menu_id is not null
  and perms in ('monitor:logininfor:list', 'monitor:logininfor:remove', 'monitor:logininfor:export');

insert into sys_menu (
  menu_id,
  menu_name,
  parent_id,
  order_num,
  path,
  component,
  `query`,
  route_name,
  is_frame,
  is_cache,
  menu_type,
  visible,
  status,
  perms,
  icon,
  create_by,
  create_time,
  update_by,
  update_time,
  remark
)
select
  151,
  '登录账户解锁',
  @logininfor_menu_id,
  4,
  '',
  null,
  '',
  '',
  1,
  0,
  'F',
  '0',
  '0',
  'monitor:logininfor:unlock',
  '#',
  'admin',
  sysdate(),
  '',
  null,
  '登录账户解锁按钮'
from dual
where @logininfor_menu_id is not null
  and not exists (
    select 1
    from sys_menu
    where menu_id = 151
       or perms = 'monitor:logininfor:unlock'
  );

update sys_menu
set parent_id = @logininfor_menu_id,
    order_num = 4,
    menu_name = '登录账户解锁',
    menu_type = 'F',
    visible = '0',
    status = '0',
    icon = '#',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '登录账户解锁按钮'
where @logininfor_menu_id is not null
  and perms = 'monitor:logininfor:unlock';

insert ignore into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, @operlog_menu_id
from sys_role_menu rm
where @operlog_menu_id is not null
  and rm.menu_id in (
    select menu_id from sys_menu
    where perms in ('monitor:operlog:list', 'monitor:operlog:remove', 'monitor:operlog:export', 'monitor:operlog:query')
  );

insert ignore into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, @logininfor_menu_id
from sys_role_menu rm
where @logininfor_menu_id is not null
  and rm.menu_id in (
    select menu_id from sys_menu
    where perms in ('monitor:logininfor:list', 'monitor:logininfor:remove', 'monitor:logininfor:export', 'monitor:logininfor:unlock')
  );

insert ignore into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, unlock_menu.menu_id
from sys_role_menu rm
join sys_menu unlock_menu on unlock_menu.perms = 'monitor:logininfor:unlock'
where rm.menu_id = @logininfor_menu_id;

select menu_id, menu_name, parent_id, order_num, path, component, perms
from sys_menu
where menu_id in (@log_menu_id, @operlog_menu_id, @logininfor_menu_id)
   or perms in (
      'monitor:operlog:list',
      'monitor:operlog:remove',
      'monitor:operlog:export',
      'monitor:operlog:query',
      'monitor:logininfor:list',
      'monitor:logininfor:remove',
      'monitor:logininfor:export',
      'monitor:logininfor:unlock'
   )
order by parent_id, order_num, menu_id;
