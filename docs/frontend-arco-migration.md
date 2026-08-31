# Arco Design Vue 迁移说明

## 目标

`ui-admin` 保留 Vue 3、Vite、Pinia、Vue Router、Axios、权限和动态路由逻辑，将界面组件与交互规范迁移到 Arco Design Vue。

主题由 `data-ui-theme` 驱动，目前提供：

- `arco-orange`：橙白主题，默认启用。
- `arco-purple`：紫白主题，可在“界面设置”中切换。

## 已迁移

- Arco 全局注册、中文语言包和主题变量映射。
- 主题持久化、界面设置抽屉和全局消息、通知、确认框、Loading。
- 顶栏、侧栏、面包屑、页签操作、公共分页和表格工具栏。
- 登录页。
- 在线用户标准列表页，作为后续列表页迁移模板。

## 迁移约束

- 新页面只使用 Arco Design Vue。
- 存量页面按页面整体迁移，不在同一页面长期混用 Arco 与 Element Plus。
- 保留接口字段、权限字符串、路由名称和业务流程，不把 UI 迁移扩大成业务重构。
- 表单项使用 Arco 的 `field`，校验成功时 `validate()` 返回 `undefined`。
- 表格列放入 `#columns` 插槽，字段使用 `data-index`，单元格使用 `#cell="{ record, rowIndex }"`。
- 表格分页统一关闭内置分页，继续复用全局 `Pagination` 组件。
- 页面迁移完成后同步清理对应的 `.el-*` 局部样式和 Element 图标引用。

## 后续顺序

1. 系统管理列表页：用户、角色、字典、配置。
2. 监控列表页：任务、日志、慢 SQL、运行日志和消息队列日志。
3. 树表页面：部门、菜单及公共树面板。
4. 上传、富文本、详情、注册、锁屏等特殊组件和页面。
5. 清理 Element Plus、Element 图标依赖及迁移期样式映射。
