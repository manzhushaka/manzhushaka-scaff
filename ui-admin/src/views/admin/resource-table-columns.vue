<template>
          <a-table-column v-if="resource === 'users'" title="用户编号" data-index="userId" :width="100" />
          <a-table-column v-if="resource === 'users'" title="用户名称" data-index="userName" :width="140" ellipsis tooltip />
          <a-table-column v-if="resource === 'users'" title="用户昵称" data-index="nickName" :width="140" ellipsis tooltip />
          <a-table-column v-if="resource === 'users'" title="部门" :width="150" ellipsis tooltip>
            <template #cell="{ record }">{{ record.dept?.deptName || '-' }}</template>
          </a-table-column>
          <a-table-column v-if="resource === 'users'" title="手机号码" data-index="phonenumber" :width="140" />
          <a-table-column v-if="resource === 'users'" title="状态" :width="100">
            <template #cell="{ record }">
              <a-switch
                :model-value="isEnabledStatus(record.status)"
                :disabled="!can('edit')"
                @change="(value: string | number | boolean) => $emit('status', record, Boolean(value))"
              />
            </template>
          </a-table-column>
          <a-table-column v-if="resource === 'users'" title="创建时间" data-index="createTime" :width="170" />

          <a-table-column v-if="resource === 'roles'" title="角色编号" data-index="roleId" :width="100" />
          <a-table-column v-if="resource === 'roles'" title="角色名称" data-index="roleName" :width="150" ellipsis tooltip />
          <a-table-column v-if="resource === 'roles'" title="角色权限" data-index="roleKey" :width="160" ellipsis tooltip />
          <a-table-column v-if="resource === 'roles'" title="显示顺序" data-index="roleSort" :width="100" />
          <a-table-column v-if="resource === 'roles'" title="状态" :width="100">
            <template #cell="{ record }">
              <a-tag :class="['status-tag', isEnabledStatus(record.status) ? 'status-tag--success' : 'status-tag--danger']">
                {{ isEnabledStatus(record.status) ? '正常' : '停用' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column v-if="resource === 'roles'" title="创建时间" data-index="createTime" :width="170" />

          <a-table-column v-if="resource === 'menus'" title="菜单名称" data-index="menuName" :width="220" ellipsis tooltip />
          <a-table-column v-if="resource === 'menus'" title="菜单类型" data-index="menuType" :width="100">
            <template #cell="{ record }">
              <a-tag class="menu-type-tag">{{ menuTypeLabel(record.menuType) }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column v-if="resource === 'menus'" title="图标" data-index="icon" :width="120" />
          <a-table-column v-if="resource === 'menus'" title="排序" data-index="orderNum" :width="90" />
          <a-table-column v-if="resource === 'menus'" title="权限标识" data-index="perms" :width="220" ellipsis tooltip />
          <a-table-column v-if="resource === 'menus'" title="路由地址" data-index="path" :width="180" ellipsis tooltip />

          <a-table-column v-if="resource === 'departments'" title="部门名称" data-index="deptName" :width="240" ellipsis tooltip />
          <a-table-column v-if="resource === 'departments'" title="排序" data-index="orderNum" :width="90" />
          <a-table-column v-if="resource === 'departments'" title="负责人" data-index="leader" :width="120" />
          <a-table-column v-if="resource === 'departments'" title="联系电话" data-index="phone" :width="140" />
          <a-table-column v-if="resource === 'departments'" title="状态" :width="100">
            <template #cell="{ record }">
              <a-tag :class="['status-tag', isEnabledStatus(record.status) ? 'status-tag--success' : 'status-tag--danger']">
                {{ isEnabledStatus(record.status) ? '正常' : '停用' }}
              </a-tag>
            </template>
          </a-table-column>

          <a-table-column v-if="resource === 'dictTypes'" title="字典编号" data-index="dictId" :width="100" />
          <a-table-column v-if="resource === 'dictTypes'" title="字典名称" data-index="dictName" :width="180" ellipsis tooltip />
          <a-table-column v-if="resource === 'dictTypes'" title="字典类型" data-index="dictType" :width="200" ellipsis tooltip />
          <a-table-column v-if="resource === 'dictTypes'" title="备注" data-index="remark" ellipsis tooltip />

          <a-table-column v-if="resource === 'configs'" title="参数编号" data-index="configId" :width="100" />
          <a-table-column v-if="resource === 'configs'" title="参数名称" data-index="configName" :width="180" ellipsis tooltip />
          <a-table-column v-if="resource === 'configs'" title="参数键名" data-index="configKey" :width="220" ellipsis tooltip />
          <a-table-column v-if="resource === 'configs'" title="参数键值" data-index="configValue" ellipsis tooltip />
          <a-table-column v-if="resource === 'configs'" title="系统内置" data-index="configType" :width="100" />

          <a-table-column v-if="resource === 'online'" title="会话编号" data-index="tokenId" :width="220" ellipsis tooltip />
          <a-table-column v-if="resource === 'online'" title="登录名称" data-index="loginName" :width="140" />
          <a-table-column v-if="resource === 'online'" title="部门名称" data-index="deptName" :width="160" />
          <a-table-column v-if="resource === 'online'" title="IP 地址" data-index="ipaddr" :width="150" />
          <a-table-column v-if="resource === 'online'" title="登录地点" data-index="loginLocation" :width="180" />
          <a-table-column v-if="resource === 'online'" title="浏览器" data-index="browser" :width="160" />
          <a-table-column v-if="resource === 'online'" title="登录时间" data-index="loginTime" :width="170" />

          <a-table-column v-if="resource === 'operationLogs'" title="日志编号" data-index="operId" :width="100" />
          <a-table-column v-if="resource === 'operationLogs'" title="系统模块" data-index="title" :width="150" />
          <a-table-column v-if="resource === 'operationLogs'" title="操作人员" data-index="operName" :width="130" />
          <a-table-column v-if="resource === 'operationLogs'" title="请求方式" data-index="requestMethod" :width="110" />
          <a-table-column v-if="resource === 'operationLogs'" title="操作地址" data-index="operIp" :width="150" />
          <a-table-column v-if="resource === 'operationLogs'" title="操作时间" data-index="operTime" :width="170" />

          <a-table-column v-if="resource === 'loginLogs'" title="访问编号" data-index="infoId" :width="100" />
          <a-table-column v-if="resource === 'loginLogs'" title="登录名称" data-index="userName" :width="140" />
          <a-table-column v-if="resource === 'loginLogs'" title="登录地址" data-index="ipaddr" :width="150" />
          <a-table-column v-if="resource === 'loginLogs'" title="登录地点" data-index="loginLocation" :width="180" />
          <a-table-column v-if="resource === 'loginLogs'" title="登录状态" data-index="status" :width="110" />
          <a-table-column v-if="resource === 'loginLogs'" title="访问时间" data-index="loginTime" :width="170" />

          <a-table-column v-if="resource === 'slowSql'" title="编号" data-index="slowSqlId" :width="100" />
          <a-table-column v-if="resource === 'slowSql'" title="执行耗时(ms)" data-index="executeTime" :width="130" />
          <a-table-column v-if="resource === 'slowSql'" title="请求地址" data-index="requestUrl" :width="220" ellipsis tooltip />
          <a-table-column v-if="resource === 'slowSql'" title="执行时间" data-index="createTime" :width="170" />

          <a-table-column v-if="resource === 'mqLogs'" title="消息编号" data-index="messageLogId" :width="100" />
          <a-table-column v-if="resource === 'mqLogs'" title="消息类型" data-index="messageType" :width="150" />
          <a-table-column v-if="resource === 'mqLogs'" title="消息主题" data-index="messageTopic" :width="220" ellipsis tooltip />
          <a-table-column v-if="resource === 'mqLogs'" title="处理状态" data-index="status" :width="120" />
          <a-table-column v-if="resource === 'mqLogs'" title="创建时间" data-index="createTime" :width="170" />

          <a-table-column v-if="resource === 'jobs'" title="任务编号" data-index="jobId" :width="100" />
          <a-table-column v-if="resource === 'jobs'" title="任务名称" data-index="jobName" :width="180" ellipsis tooltip />
          <a-table-column v-if="resource === 'jobs'" title="任务组名" data-index="jobGroup" :width="130" />
          <a-table-column v-if="resource === 'jobs'" title="调用目标字符串" data-index="invokeTarget" ellipsis tooltip />
          <a-table-column v-if="resource === 'jobs'" title="状态" :width="100">
            <template #cell="{ record }">
              <a-switch
                :model-value="isEnabledStatus(record.status)"
                :disabled="!can('edit')"
                @change="(value: string | number | boolean) => $emit('status', record, Boolean(value))"
              />
            </template>
          </a-table-column>
          <a-table-column v-if="resource === 'jobs'" title="下次执行时间" data-index="nextValidTime" :width="180" />

          <a-table-column v-if="resource === 'jobLogs'" title="日志编号" data-index="jobLogId" :width="100" />
          <a-table-column v-if="resource === 'jobLogs'" title="任务名称" data-index="jobName" :width="180" />
          <a-table-column v-if="resource === 'jobLogs'" title="任务组名" data-index="jobGroup" :width="130" />
          <a-table-column v-if="resource === 'jobLogs'" title="执行状态" data-index="status" :width="110" />
          <a-table-column v-if="resource === 'jobLogs'" title="执行时间" data-index="createTime" :width="170" />

          <a-table-column title="操作" align="center" :width="resource === 'users' || resource === 'jobs' ? 220 : 160" fixed="right">
            <template #cell="{ record }">
              <a-space class="table-action-buttons">
                <a-button
                  v-if="resource === 'operationLogs' && can('query')"
                  type="text"
                  class="table-action-button table-action-button--view"
                  aria-label="查看详情"
                  title="查看详情"
                  @click="$emit('detail', record)"
                >
                  <template #icon><icon-eye /></template>
                </a-button>
                <a-button
                  v-if="can('edit') && supportsEdit"
                  type="text"
                  class="table-action-button table-action-button--edit"
                  aria-label="编辑"
                  title="编辑"
                  @click="$emit('edit', record)"
                >
                  <template #icon><icon-edit /></template>
                </a-button>
                <a-button
                  v-if="resource === 'jobs'"
                  type="text"
                  class="table-action-button table-action-button--execute"
                  aria-label="立即执行"
                  title="立即执行"
                  @click="$emit('run', record)"
                >
                  <template #icon><icon-play-arrow /></template>
                </a-button>
                <a-button
                  v-if="can('remove') && supportsDelete"
                  type="text"
                  class="table-action-button table-action-button--delete"
                  aria-label="删除"
                  title="删除"
                  @click="$emit('remove', record)"
                >
                  <template #icon><icon-delete /></template>
                </a-button>
              </a-space>
            </template>
          </a-table-column>
</template>

<script lang="ts" setup>
  import type { Resource } from './resource-config';

  defineProps<{
    resource: Resource;
    can: (action: string) => boolean;
    supportsEdit: boolean;
    supportsDelete: boolean;
  }>();
  defineEmits<{
    (event: 'detail', record: Record<string, unknown>): void;
    (event: 'edit', record: Record<string, unknown>): void;
    (event: 'run', record: Record<string, unknown>): void;
    (event: 'remove', record: Record<string, unknown>): void;
    (event: 'status', record: Record<string, unknown>, value: boolean): void;
  }>();

  function isEnabledStatus(value: unknown) {
    return value === '0' || value === 0;
  }

  function menuTypeLabel(type: string) {
    return ({ M: '目录', C: '菜单', F: '按钮' } as Record<string, string>)[type] || type || '-';
  }
</script>
