-- ----------------------------
-- 清理通知公告与若依官网菜单
-- ----------------------------
-- 适用场景：已有数据库已导入通知公告功能或若依官网菜单，但当前仓库已移除相关前后端能力。
-- 脚本会先输出待删除菜单，再删除授权、菜单、通知字典，并移除通知相关表。

create temporary table if not exists tmp_notice_feature_menu_ids (
  menu_id bigint(20) not null primary key
) engine=memory;

truncate table tmp_notice_feature_menu_ids;

insert ignore into tmp_notice_feature_menu_ids (menu_id)
select menu_id
from sys_menu
where menu_name in ('通知公告', '若依官网')
   or path in ('notice', 'http://ruoyi.vip')
   or component = 'system/notice/index'
   or perms like 'system:notice:%';

insert ignore into tmp_notice_feature_menu_ids (menu_id)
select child.menu_id
from sys_menu child
inner join tmp_notice_feature_menu_ids parent on child.parent_id = parent.menu_id;

insert ignore into tmp_notice_feature_menu_ids (menu_id)
select child.menu_id
from sys_menu child
inner join tmp_notice_feature_menu_ids parent on child.parent_id = parent.menu_id;

select menu_id, menu_name, parent_id, path, component, perms
from sys_menu
where menu_id in (select menu_id from tmp_notice_feature_menu_ids)
order by parent_id, order_num, menu_id;

delete from sys_role_menu
where menu_id in (select menu_id from tmp_notice_feature_menu_ids);

delete from sys_menu
where menu_id in (select menu_id from tmp_notice_feature_menu_ids);

delete from sys_dict_data
where dict_type in ('sys_notice_type', 'sys_notice_status');

delete from sys_dict_type
where dict_type in ('sys_notice_type', 'sys_notice_status');

drop table if exists sys_notice_read;
drop table if exists sys_notice;

drop temporary table if exists tmp_notice_feature_menu_ids;
