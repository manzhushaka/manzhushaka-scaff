-- ----------------------------
-- 清理旧业务菜单：以旧换新、商品管理、导入与导出
-- ----------------------------
-- 适用场景：已有数据库曾导入过旧业务菜单，但当前仓库作为脚手架不保留业务功能。
-- 脚本会先输出待删除菜单，再删除菜单授权和菜单记录。

create temporary table if not exists tmp_legacy_business_menu_ids (
  menu_id bigint(20) not null primary key
) engine=memory;

truncate table tmp_legacy_business_menu_ids;

insert ignore into tmp_legacy_business_menu_ids (menu_id)
select menu_id
from sys_menu
where menu_name in (
  '以旧换新',
  '商品管理',
  '导入与导出',
  '交易流水',
  '订单包管理',
  '审核管理',
  '订单资料',
  '安装信息管理',
  '补贴结算',
  '退货退款台账',
  '资料规则',
  '接入方管理',
  '品牌管理',
  '资料转存任务',
  'SKU 管理',
  'SN 码管理',
  '价格管理',
  '异步导入任务'
)
or path in ('biz', 'goods', 'importExport')
or path like 'biz/%'
or path like 'goods/%'
or path like 'importExport/%'
or component like 'biz/%'
or component like 'goods/%'
or component like 'importExport/%'
or perms like 'biz:%'
or perms like 'goods:%'
or perms like 'importExport:%';

insert ignore into tmp_legacy_business_menu_ids (menu_id)
select child.menu_id
from sys_menu child
inner join tmp_legacy_business_menu_ids parent on child.parent_id = parent.menu_id;

insert ignore into tmp_legacy_business_menu_ids (menu_id)
select child.menu_id
from sys_menu child
inner join tmp_legacy_business_menu_ids parent on child.parent_id = parent.menu_id;

select menu_id, menu_name, parent_id, path, component, perms
from sys_menu
where menu_id in (select menu_id from tmp_legacy_business_menu_ids)
order by parent_id, order_num, menu_id;

delete from sys_role_menu
where menu_id in (select menu_id from tmp_legacy_business_menu_ids);

delete from sys_menu
where menu_id in (select menu_id from tmp_legacy_business_menu_ids);

drop temporary table if exists tmp_legacy_business_menu_ids;
