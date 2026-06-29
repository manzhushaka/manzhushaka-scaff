-- ----------------------------
-- 清理岗位管理功能
-- ----------------------------
-- 适用场景：已有数据库已导入岗位管理功能，但当前仓库已移除相关前后端能力。
-- 脚本会先输出待删除菜单，再删除授权、菜单以及岗位相关表。

create temporary table if not exists tmp_post_feature_menu_ids (
  menu_id bigint(20) not null primary key
) engine=memory;

create temporary table if not exists tmp_post_feature_child_menu_ids (
  menu_id bigint(20) not null primary key
) engine=memory;

truncate table tmp_post_feature_menu_ids;
truncate table tmp_post_feature_child_menu_ids;

insert ignore into tmp_post_feature_menu_ids (menu_id)
select menu_id
from sys_menu
where menu_name = '岗位管理'
   or path = 'post'
   or component = 'system/post/index'
   or perms like 'system:post:%';

insert ignore into tmp_post_feature_child_menu_ids (menu_id)
select child.menu_id
from sys_menu child
where child.parent_id in (select menu_id from tmp_post_feature_menu_ids);

insert ignore into tmp_post_feature_menu_ids (menu_id)
select menu_id
from tmp_post_feature_child_menu_ids;

select menu_id, menu_name, parent_id, path, component, perms
from sys_menu
where menu_id in (select menu_id from tmp_post_feature_menu_ids)
order by parent_id, order_num, menu_id;

delete from sys_role_menu
where menu_id in (select menu_id from tmp_post_feature_menu_ids);

delete from sys_menu
where menu_id in (select menu_id from tmp_post_feature_menu_ids);

drop table if exists sys_user_post;
drop table if exists sys_post;

drop temporary table if exists tmp_post_feature_child_menu_ids;
drop temporary table if exists tmp_post_feature_menu_ids;
