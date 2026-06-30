-- ----------------------------
-- 统一日志入口迁移脚本
-- ----------------------------
-- 适用场景：已有数据库中存在“操作日志 / 登录日志 / 请求日志”多个入口，
-- 本脚本将其收敛为一个“统一日志”页面入口，并保留操作日志、登录日志按钮权限。

set @log_menu_id := (
  select menu_id
  from sys_menu
  where menu_type = 'M'
    and (path = 'log' or menu_name in ('日志中心', '日志管理'))
  order by menu_id
  limit 1
);

set @legacy_operlog_menu_id := (
  select menu_id
  from sys_menu
  where component = 'monitor/operlog/index'
     or (parent_id = @log_menu_id and menu_name = '操作日志' and menu_type = 'C')
  order by menu_id
  limit 1
);

set @legacy_logininfor_menu_id := (
  select menu_id
  from sys_menu
  where component = 'monitor/logininfor/index'
     or (parent_id = @log_menu_id and menu_name = '登录日志' and menu_type = 'C')
  order by menu_id
  limit 1
);

delete from sys_role_menu
where menu_id in (
  select menu_id
  from (
    select menu_id
    from sys_menu
    where component = 'monitor/requestLog/index'
       or perms in (
         'monitor:requestlog:list',
         'monitor:requestlog:query',
         'monitor:requestlog:remove',
         'monitor:requestlog:export'
       )
  ) request_log_menu
);

delete from sys_menu
where component = 'monitor/requestLog/index'
   or perms in (
     'monitor:requestlog:list',
     'monitor:requestlog:query',
     'monitor:requestlog:remove',
     'monitor:requestlog:export'
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
  161,
  '统一日志',
  @log_menu_id,
  1,
  'logCenter',
  'monitor/logCenter/index',
  '',
  '',
  1,
  0,
  'C',
  '0',
  '0',
  'monitor:logcenter:list',
  'log',
  'admin',
  sysdate(),
  '',
  null,
  '统一日志菜单'
from dual
where @log_menu_id is not null
  and not exists (
    select 1
    from sys_menu
    where menu_id = 161
       or component = 'monitor/logCenter/index'
       or (parent_id = @log_menu_id and menu_name = '统一日志' and menu_type = 'C')
  );

update sys_menu
set parent_id = @log_menu_id,
    order_num = 1,
    path = 'logCenter',
    component = 'monitor/logCenter/index',
    `query` = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'monitor:logcenter:list',
    icon = 'log',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '统一日志菜单'
where @log_menu_id is not null
  and menu_id = 161;

set @log_center_menu_id := (
  select menu_id
  from sys_menu
  where component = 'monitor/logCenter/index'
     or (parent_id = @log_menu_id and menu_name = '统一日志' and menu_type = 'C')
     or menu_id = 161
  order by menu_id
  limit 1
);

update sys_menu
set parent_id = @log_center_menu_id,
    order_num = case perms
      when 'monitor:operlog:list' then 1
      when 'monitor:operlog:remove' then 2
      when 'monitor:operlog:export' then 3
      when 'monitor:operlog:query' then 4
      when 'monitor:logininfor:list' then 5
      when 'monitor:logininfor:remove' then 6
      when 'monitor:logininfor:export' then 7
      when 'monitor:logininfor:unlock' then 8
      else order_num
    end,
    update_by = 'admin',
    update_time = sysdate()
where @log_center_menu_id is not null
  and perms in (
    'monitor:operlog:list',
    'monitor:operlog:remove',
    'monitor:operlog:export',
    'monitor:operlog:query',
    'monitor:logininfor:list',
    'monitor:logininfor:remove',
    'monitor:logininfor:export',
    'monitor:logininfor:unlock'
  );

insert ignore into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, @log_center_menu_id
from sys_role_menu rm
where @log_center_menu_id is not null
  and (
    rm.menu_id in (@legacy_operlog_menu_id, @legacy_logininfor_menu_id)
    or rm.menu_id in (
      select menu_id
      from sys_menu
      where perms in (
        'monitor:operlog:list',
        'monitor:operlog:remove',
        'monitor:operlog:export',
        'monitor:operlog:query',
        'monitor:logininfor:list',
        'monitor:logininfor:remove',
        'monitor:logininfor:export',
        'monitor:logininfor:unlock'
      )
    )
  );

delete from sys_role_menu
where menu_id in (@legacy_operlog_menu_id, @legacy_logininfor_menu_id)
  and menu_id <> @log_center_menu_id;

delete from sys_menu
where menu_id in (@legacy_operlog_menu_id, @legacy_logininfor_menu_id)
  and menu_type = 'C'
  and menu_id <> @log_center_menu_id;

select menu_id, menu_name, parent_id, order_num, path, component, perms
from sys_menu
where menu_id in (@log_menu_id, @log_center_menu_id)
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
