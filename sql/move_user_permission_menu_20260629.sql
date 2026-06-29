-- ----------------------------
-- 调整用户与权限菜单为一级菜单
-- ----------------------------
-- 适用场景：已有数据库已导入菜单数据，需要把用户管理、角色管理、菜单管理、部门管理迁移到新的一级“用户与权限”菜单下。
-- 脚本会补建目录菜单、迁移四个页面菜单，并为已有拥有这些页面权限的角色补齐新目录授权。

create temporary table if not exists tmp_user_permission_child_menu_ids (
  menu_id bigint(20) not null primary key
) engine=memory;

truncate table tmp_user_permission_child_menu_ids;

insert ignore into tmp_user_permission_child_menu_ids (menu_id)
select menu_id
from sys_menu
where (menu_name = '用户管理' and component = 'system/user/index')
   or (menu_name = '角色管理' and component = 'system/role/index')
   or (menu_name = '菜单管理' and component = 'system/menu/index')
   or (menu_name = '部门管理' and component = 'system/dept/index')
   or perms in ('system:user:list', 'system:role:list', 'system:menu:list', 'system:dept:list');

insert into sys_menu (
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
  '用户与权限',
  0,
  3,
  'userAuth',
  '',
  '',
  '',
  1,
  0,
  'M',
  '0',
  '0',
  '',
  'people',
  'admin',
  sysdate(),
  '',
  null,
  '用户与权限目录'
from dual
where not exists (
    select 1
    from sys_menu
    where parent_id = 0
      and menu_type = 'M'
      and (menu_name = '用户与权限' or path = 'userAuth')
  );

set @user_permission_menu_id := (
  select menu_id
  from sys_menu
  where parent_id = 0
    and menu_type = 'M'
    and (menu_name = '用户与权限' or path = 'userAuth')
  order by menu_id
  limit 1
);

update sys_menu
set menu_name = '用户与权限',
    parent_id = 0,
    order_num = 3,
    path = 'userAuth',
    component = '',
    `query` = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'M',
    visible = '0',
    status = '0',
    perms = '',
    icon = 'people',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '用户与权限目录'
where @user_permission_menu_id is not null
  and menu_id = @user_permission_menu_id;

update sys_menu
set parent_id = @user_permission_menu_id
where @user_permission_menu_id is not null
  and menu_id in (select menu_id from tmp_user_permission_child_menu_ids);

update sys_menu
set order_num = case
  when component = 'system/user/index' then 1
  when component = 'system/role/index' then 2
  when component = 'system/menu/index' then 3
  when component = 'system/dept/index' then 4
  else order_num
end
where menu_id in (select menu_id from tmp_user_permission_child_menu_ids);

insert ignore into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, @user_permission_menu_id
from sys_role_menu rm
where @user_permission_menu_id is not null
  and rm.menu_id in (select menu_id from tmp_user_permission_child_menu_ids);

select menu_id, menu_name, parent_id, order_num, path, component, perms
from sys_menu
where menu_id = @user_permission_menu_id
   or menu_id in (select menu_id from tmp_user_permission_child_menu_ids)
order by parent_id, order_num, menu_id;

drop temporary table if exists tmp_user_permission_child_menu_ids;
