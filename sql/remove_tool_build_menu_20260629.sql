-- ----------------------------
-- 清理系统工具与表单构建菜单
-- ----------------------------
-- 适用场景：已有数据库曾导入过系统工具和表单构建菜单，但当前仓库不再保留该菜单分组。
-- 脚本会先输出待删除菜单，再删除菜单授权和菜单记录。

create temporary table if not exists tmp_tool_build_menu_ids (
  menu_id bigint(20) not null primary key
) engine=memory;

truncate table tmp_tool_build_menu_ids;

insert ignore into tmp_tool_build_menu_ids (menu_id)
select menu_id
from sys_menu
where menu_name in ('系统工具', '表单构建')
or path in ('tool', 'build')
or path like 'tool/%'
or component like 'tool/%'
or perms like 'tool:build:%';

insert ignore into tmp_tool_build_menu_ids (menu_id)
select child.menu_id
from sys_menu child
inner join tmp_tool_build_menu_ids parent on child.parent_id = parent.menu_id;

insert ignore into tmp_tool_build_menu_ids (menu_id)
select child.menu_id
from sys_menu child
inner join tmp_tool_build_menu_ids parent on child.parent_id = parent.menu_id;

select menu_id, menu_name, parent_id, path, component, perms
from sys_menu
where menu_id in (select menu_id from tmp_tool_build_menu_ids)
order by parent_id, order_num, menu_id;

delete from sys_role_menu
where menu_id in (select menu_id from tmp_tool_build_menu_ids);

delete from sys_menu
where menu_id in (select menu_id from tmp_tool_build_menu_ids);

drop temporary table if exists tmp_tool_build_menu_ids;
