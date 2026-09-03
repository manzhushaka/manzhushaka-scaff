<template>
  <div class="resource-page">
    <section class="filter-panel">
      <a-form :model="query" layout="inline" @submit-success="handleQuery">
        <a-form-item
          v-for="field in queryFields"
          :key="field.key"
          :field="field.key"
          :label="field.label"
        >
          <a-input
            v-if="field.type === 'input'"
            v-model="query[field.key]"
            allow-clear
            :placeholder="`请输入${field.label}`"
            @keyup.enter="handleQuery"
          />
          <a-select
            v-else-if="field.type === 'status'"
            v-model="query[field.key]"
            allow-clear
            :placeholder="`请选择${field.label}`"
          >
            <a-option value="0">正常</a-option>
            <a-option value="1">停用</a-option>
          </a-select>
          <a-select
            v-else-if="field.type === 'menuType'"
            v-model="query[field.key]"
            allow-clear
            :placeholder="`请选择${field.label}`"
          >
            <a-option value="M">目录</a-option>
            <a-option value="C">菜单</a-option>
            <a-option value="F">按钮</a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">
              <template #icon><icon-search /></template>
              查询
            </a-button>
            <a-button @click="resetQuery">
              <template #icon><icon-refresh /></template>
              重置
            </a-button>
            <a-button type="outline" :loading="loading" @click="loadData">
              <template #icon><icon-refresh /></template>
              刷新
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </section>

    <section class="table-panel">
      <div class="action-bar">
        <a-space wrap>
          <a-button
            v-if="can('add') && supportsCreate"
            type="primary"
            @click="openCreate"
          >
            <template #icon><icon-plus /></template>
            新增
          </a-button>
          <a-button
            v-if="can('remove') && supportsDelete"
            status="danger"
            :disabled="selectedKeys.length === 0"
            @click="removeSelected"
          >
            <template #icon><icon-delete /></template>
            删除
          </a-button>
          <a-button
            v-if="resource === 'dictTypes' || resource === 'configs'"
            @click="refreshCache"
          >
            <template #icon><icon-sync /></template>
            刷新缓存
          </a-button>
          <a-button
            v-if="resource === 'jobs'"
            :disabled="selectedKeys.length !== 1"
            @click="runSelectedJob"
          >
            <template #icon><icon-play-arrow /></template>
            立即执行
          </a-button>
        </a-space>
      </div>

      <a-table
        :data="rows"
        :loading="loading"
        :bordered="false"
        :pagination="pagination"
        page-position="bottom"
        :row-key="rowKey"
        hide-expand-button-on-empty
        :row-selection="supportsSelection ? { type: 'checkbox', showCheckedAll: true } : undefined"
        @selection-change="handleSelectionChange"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #expand-icon="{ expanded }">
          <icon-right
            class="table-expand-icon"
            :class="{ 'table-expand-icon--expanded': expanded }"
          />
        </template>
        <template #columns>
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
                :model-value="record.status === '0' || record.status === 0"
                :disabled="!can('edit')"
                @change="(value: string | number | boolean) => changeStatus(record, Boolean(value))"
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
              <a-tag :class="['status-tag', record.status === '0' || record.status === 0 ? 'status-tag--success' : 'status-tag--danger']">
                {{ record.status === '0' || record.status === 0 ? '正常' : '停用' }}
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
              <a-tag :class="['status-tag', record.status === '0' || record.status === 0 ? 'status-tag--success' : 'status-tag--danger']">
                {{ record.status === '0' || record.status === 0 ? '正常' : '停用' }}
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
                :model-value="record.status === '0' || record.status === 0"
                :disabled="!can('edit')"
                @change="(value: string | number | boolean) => changeStatus(record, Boolean(value))"
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
                  @click="openDetail(record)"
                >
                  <template #icon><icon-eye /></template>
                </a-button>
                <a-button
                  v-if="can('edit') && supportsEdit"
                  type="text"
                  class="table-action-button table-action-button--edit"
                  aria-label="编辑"
                  title="编辑"
                  @click="openEdit(record)"
                >
                  <template #icon><icon-edit /></template>
                </a-button>
                <a-button
                  v-if="resource === 'jobs'"
                  type="text"
                  class="table-action-button table-action-button--execute"
                  aria-label="立即执行"
                  title="立即执行"
                  @click="runJobRecord(record)"
                >
                  <template #icon><icon-play-arrow /></template>
                </a-button>
                <a-button
                  v-if="can('remove') && supportsDelete"
                  type="text"
                  class="table-action-button table-action-button--delete"
                  aria-label="删除"
                  title="删除"
                  @click="removeRecord(record)"
                >
                  <template #icon><icon-delete /></template>
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </section>

    <a-modal
      v-model:visible="dialogVisible"
      :title="dialogTitle"
      :width="resource === 'users' ? 720 : 560"
      :ok-loading="submitting"
      render-to-body
      @ok="submitForm"
      @cancel="closeDialog"
    >
      <a-form ref="formRef" class="modal-form" :model="form" :label-col-props="{ span: 5 }" :wrapper-col-props="{ span: 18 }">
        <a-form-item v-if="resource === 'users' && !form.userId" field="userName" label="用户名称" :rules="requiredRule">
          <a-input v-model="form.userName" placeholder="请输入用户名称" />
        </a-form-item>
        <a-form-item v-if="resource === 'users' && !form.userId" field="password" label="用户密码" :rules="requiredRule">
          <a-input-password v-model="form.password" placeholder="请输入用户密码" />
        </a-form-item>
        <a-form-item v-if="resource === 'users'" field="nickName" label="用户昵称" :rules="requiredRule">
          <a-input v-model="form.nickName" placeholder="请输入用户昵称" />
        </a-form-item>
        <a-form-item v-if="resource === 'users'" field="phonenumber" label="手机号码">
          <a-input v-model="form.phonenumber" placeholder="请输入手机号码" />
        </a-form-item>
        <a-form-item v-if="resource === 'users'" field="email" label="邮箱">
          <a-input v-model="form.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item v-if="resource === 'users'" field="sex" label="性别">
          <a-select v-model="form.sex"><a-option value="0">男</a-option><a-option value="1">女</a-option><a-option value="2">未知</a-option></a-select>
        </a-form-item>
        <a-form-item v-if="resource === 'users'" field="status" label="状态">
          <a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">停用</a-radio></a-radio-group>
        </a-form-item>

        <a-form-item v-if="resource === 'roles'" field="roleName" label="角色名称" :rules="requiredRule">
          <a-input v-model="form.roleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item v-if="resource === 'roles'" field="roleKey" label="角色权限" :rules="requiredRule">
          <a-input v-model="form.roleKey" placeholder="请输入角色权限" />
        </a-form-item>
        <a-form-item v-if="resource === 'roles'" field="roleSort" label="显示顺序" :rules="requiredRule">
          <a-input-number v-model="form.roleSort" :min="0" />
        </a-form-item>
        <a-form-item v-if="resource === 'roles'" field="status" label="状态">
          <a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">停用</a-radio></a-radio-group>
        </a-form-item>
        <a-form-item v-if="resource === 'roles'" field="remark" label="备注"><a-textarea v-model="form.remark" /></a-form-item>

        <a-form-item v-if="resource === 'menus'" field="parentId" label="上级菜单">
          <a-tree-select v-model="form.parentId" :data="menuTree" :field-names="treeFieldNames" allow-clear placeholder="请选择上级菜单" />
        </a-form-item>
        <a-form-item v-if="resource === 'menus'" field="menuName" label="菜单名称" :rules="requiredRule"><a-input v-model="form.menuName" /></a-form-item>
        <a-form-item v-if="resource === 'menus'" field="menuType" label="菜单类型"><a-radio-group v-model="form.menuType"><a-radio value="M">目录</a-radio><a-radio value="C">菜单</a-radio><a-radio value="F">按钮</a-radio></a-radio-group></a-form-item>
        <a-form-item v-if="resource === 'menus' && form.menuType !== 'F'" field="path" label="路由地址"><a-input v-model="form.path" /></a-form-item>
        <a-form-item v-if="resource === 'menus' && form.menuType !== 'F'" field="component" label="组件路径"><a-input v-model="form.component" /></a-form-item>
        <a-form-item v-if="resource === 'menus'" field="orderNum" label="显示排序"><a-input-number v-model="form.orderNum" :min="0" /></a-form-item>
        <a-form-item v-if="resource === 'menus'" field="perms" label="权限标识"><a-input v-model="form.perms" /></a-form-item>
        <a-form-item v-if="resource === 'menus'" field="icon" label="图标">
          <a-input
            :model-value="form.icon"
            readonly
            placeholder="请选择图标"
            aria-label="选择图标"
            @click="openIconPicker"
            @keyup.enter="openIconPicker"
            @keyup.space.prevent="openIconPicker"
          >
            <template #prefix>
              <component :is="selectedIcon?.component || IconMenu" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item v-if="resource === 'departments' && form.parentId !== 0" field="parentId" label="上级部门">
          <a-tree-select v-model="form.parentId" :data="departmentTree" :field-names="deptTreeFieldNames" allow-clear />
        </a-form-item>
        <a-form-item v-if="resource === 'departments'" field="deptName" label="部门名称" :rules="requiredRule"><a-input v-model="form.deptName" /></a-form-item>
        <a-form-item v-if="resource === 'departments'" field="orderNum" label="显示排序"><a-input-number v-model="form.orderNum" :min="0" /></a-form-item>
        <a-form-item v-if="resource === 'departments'" field="leader" label="负责人"><a-input v-model="form.leader" /></a-form-item>
        <a-form-item v-if="resource === 'departments'" field="phone" label="联系电话"><a-input v-model="form.phone" /></a-form-item>
        <a-form-item v-if="resource === 'departments'" field="email" label="邮箱"><a-input v-model="form.email" /></a-form-item>
        <a-form-item v-if="resource === 'departments'" field="status" label="状态"><a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">停用</a-radio></a-radio-group></a-form-item>

        <a-form-item v-if="resource === 'dictTypes'" field="dictName" label="字典名称" :rules="requiredRule"><a-input v-model="form.dictName" /></a-form-item>
        <a-form-item v-if="resource === 'dictTypes'" field="dictType" label="字典类型" :rules="requiredRule"><a-input v-model="form.dictType" /></a-form-item>
        <a-form-item v-if="resource === 'dictTypes'" field="status" label="状态"><a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">停用</a-radio></a-radio-group></a-form-item>
        <a-form-item v-if="resource === 'dictTypes'" field="remark" label="备注"><a-textarea v-model="form.remark" /></a-form-item>

        <a-form-item v-if="resource === 'configs'" field="configName" label="参数名称" :rules="requiredRule"><a-input v-model="form.configName" /></a-form-item>
        <a-form-item v-if="resource === 'configs'" field="configKey" label="参数键名" :rules="requiredRule"><a-input v-model="form.configKey" /></a-form-item>
        <a-form-item v-if="resource === 'configs'" field="configValue" label="参数键值" :rules="requiredRule"><a-textarea v-model="form.configValue" /></a-form-item>
        <a-form-item v-if="resource === 'configs'" field="configType" label="系统内置"><a-radio-group v-model="form.configType"><a-radio value="Y">是</a-radio><a-radio value="N">否</a-radio></a-radio-group></a-form-item>
        <a-form-item v-if="resource === 'configs'" field="remark" label="备注"><a-textarea v-model="form.remark" /></a-form-item>

        <a-form-item v-if="resource === 'jobs'" field="jobName" label="任务名称" :rules="requiredRule"><a-input v-model="form.jobName" /></a-form-item>
        <a-form-item v-if="resource === 'jobs'" field="jobGroup" label="任务组名"><a-input v-model="form.jobGroup" /></a-form-item>
        <a-form-item v-if="resource === 'jobs'" field="invokeTarget" label="调用目标" :rules="requiredRule"><a-input v-model="form.invokeTarget" /></a-form-item>
        <a-form-item v-if="resource === 'jobs'" field="cronExpression" label="Cron 表达式"><a-input v-model="form.cronExpression" /></a-form-item>
        <a-form-item v-if="resource === 'jobs'" field="status" label="状态"><a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">暂停</a-radio></a-radio-group></a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="iconPickerVisible"
      title="选择图标"
      :width="560"
      :footer="false"
      render-to-body
    >
      <div class="icon-picker-toolbar">
        <a-input v-model="iconSearch" allow-clear placeholder="搜索图标名称">
          <template #prefix><component :is="searchIconComponent" /></template>
        </a-input>
        <a-button type="text" status="danger" @click="clearIcon">
          <template #icon><icon-delete /></template>
          清除图标
        </a-button>
      </div>
      <div class="icon-picker-grid">
        <a-button
          v-for="option in filteredIconOptions"
          :key="option.name"
          class="icon-picker-option"
          :aria-label="`选择 ${option.name} 图标`"
          :title="option.name"
          :class="{ 'icon-picker-option--selected': form.icon === option.name }"
          @click="selectIcon(option.name)"
        >
          <template #icon><component :is="option.component" /></template>
        </a-button>
      </div>
      <a-empty v-if="filteredIconOptions.length === 0" description="未找到匹配图标" />
    </a-modal>

    <a-modal
      v-model:visible="detailVisible"
      title="操作日志详情"
      :width="720"
      :footer="false"
      render-to-body
    >
      <a-spin :loading="detailLoading" class="detail-spin">
        <a-descriptions class="detail-descriptions" :column="{ xs: 1, sm: 2 }" bordered>
          <a-descriptions-item label="日志编号">{{ detailRecord.operId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="系统模块">{{ detailRecord.title || '-' }}</a-descriptions-item>
          <a-descriptions-item label="操作人员">{{ detailRecord.operName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="所属部门">{{ detailRecord.deptName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="请求方式">{{ detailRecord.requestMethod || '-' }}</a-descriptions-item>
          <a-descriptions-item label="请求方法" :span="2">
            <div class="detail-inline-content">{{ detailRecord.method || '-' }}</div>
          </a-descriptions-item>
          <a-descriptions-item label="请求地址" :span="2">
            <div class="detail-inline-content">{{ detailRecord.operUrl || '-' }}</div>
          </a-descriptions-item>
          <a-descriptions-item label="操作地址">{{ detailRecord.operIp || '-' }}</a-descriptions-item>
          <a-descriptions-item label="操作时间">{{ detailRecord.operTime || '-' }}</a-descriptions-item>
          <a-descriptions-item label="消耗时间">{{ detailRecord.costTime ?? '-' }} 毫秒</a-descriptions-item>
          <a-descriptions-item label="请求参数" :span="2">
            <pre class="detail-content">{{ detailRecord.operParam || '-' }}</pre>
          </a-descriptions-item>
          <a-descriptions-item label="返回参数" :span="2">
            <pre class="detail-content">{{ detailRecord.jsonResult || '-' }}</pre>
          </a-descriptions-item>
          <a-descriptions-item v-if="detailRecord.errorMsg" label="错误消息" :span="2">
            <span class="detail-error">{{ detailRecord.errorMsg }}</span>
          </a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
  /* eslint-disable no-use-before-define */
  import { computed, onMounted, reactive, ref, watch } from 'vue';
  import type { Component } from 'vue';
  import { Modal, Message } from '@arco-design/web-vue';
  import type { FormInstance } from '@arco-design/web-vue/es/form';
  import * as ArcoIcons from '@arco-design/web-vue/es/icon';
  import { IconMenu } from '@arco-design/web-vue/es/icon';
  import { useRoute } from 'vue-router';
  import {
    changeJobStatus,
    changeRoleStatus,
    changeUserStatus,
    cleanJobLogs,
    cleanLoginLogs,
    cleanOperationLogs,
    cleanSlowSqlLogs,
    createConfig,
    createDepartment,
    createDictType,
    createJob,
    createMenu,
    createRole,
    createUser,
    getDepartment,
    getConfig,
    getDictType,
    getJob,
    getMenu,
    getOperationLog,
    getRole,
    getUser,
    listConfigs,
    listDepartments,
    listDictTypes,
    listJobs,
    listJobLogs,
    listLoginLogs,
    listMenus,
    listOnlineUsers,
    listOperationLogs,
    listRoles,
    listSlowSqlLogs,
    listUsers,
    removeConfigs,
    removeDepartment,
    removeDictTypes,
    removeJobs,
    removeMenu,
    removeOperationLogs,
    removeLoginLogs,
    removeSlowSqlLogs,
    removeRoles,
    removeUsers,
    updateConfig,
    updateDepartment,
    updateDictType,
    updateJob,
    updateMenu,
    updateRole,
    updateUser,
    runJob,
    refreshConfigCache,
    refreshDictCache,
  } from '@/api/admin';
  import useUserStore from '@/store/modules/user';

  type Resource =
    | 'users'
    | 'roles'
    | 'menus'
    | 'departments'
    | 'dictTypes'
    | 'configs'
    | 'online'
    | 'operationLogs'
    | 'loginLogs'
    | 'slowSql'
    | 'mqLogs'
    | 'jobs'
    | 'jobLogs';
  interface Props { resource: Resource; }
  interface QueryField { key: string; label: string; type: string; }
  const props = defineProps<Props>();
  const route = useRoute();
  const userStore = useUserStore();
  const loading = ref(false);
  const submitting = ref(false);
  const rows = ref<Record<string, any>[]>([]);
  const selectedKeys = ref<Array<string | number>>([]);
  const total = ref<number | null>(0);
  const page = ref(1);
  const pageSize = ref(10);
  const dialogVisible = ref(false);
  const iconPickerVisible = ref(false);
  const iconSearch = ref('');
  const detailVisible = ref(false);
  const detailLoading = ref(false);
  const detailRecord = ref<Record<string, any>>({});
  const editing = ref(false);
  const formRef = ref<FormInstance>();
  const form = reactive<Record<string, any>>({});
  const query = reactive<Record<string, any>>({});
  const menuTree = ref<Record<string, any>[]>([]);
  const departmentTree = ref<Record<string, any>[]>([]);
  const requiredRule = [{ required: true, message: '该字段不能为空' }];
  const treeFieldNames = { key: 'id', title: 'label', children: 'children' };
  const deptTreeFieldNames = { key: 'deptId', title: 'deptName', children: 'children' };
  const iconOptions: Array<{ name: string; component: Component }> = Object.entries(ArcoIcons)
    .filter(([name]) => /^Icon[A-Z]/.test(name))
    .map(([name, component]) => ({
      name: `icon-${name.slice(4).replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase()}`,
      component: component as Component,
    }))
    .sort((left, right) => left.name.localeCompare(right.name));
  const filteredIconOptions = computed(() => {
    const keyword = iconSearch.value.trim().toLowerCase();
    return keyword
      ? iconOptions.filter((option) => option.name.toLowerCase().includes(keyword))
      : iconOptions;
  });
  const searchIconComponent = ArcoIcons.IconSearch as Component;
  const iconAliases: Record<string, string> = {
    '#': 'icon-menu', system: 'icon-settings', monitor: 'icon-dashboard', people: 'icon-user-group',
    user: 'icon-user', peoples: 'icon-user-group', 'tree-table': 'icon-unordered-list', tree: 'icon-mind-mapping',
    dict: 'icon-book', edit: 'icon-edit', log: 'icon-file', druid: 'icon-bar-chart', message: 'icon-message',
    online: 'icon-user-group', job: 'icon-calendar-clock', server: 'icon-computer', redis: 'icon-storage',
    'redis-list': 'icon-list', upload: 'icon-upload', download: 'icon-download',
  };
  const selectedIcon = computed(() => iconOptions.find((option) => option.name === (iconAliases[form.icon] || form.icon)));

  const titleMap: Record<Resource, string> = {
    users: '用户管理', roles: '角色管理', menus: '菜单管理', departments: '部门管理',
    dictTypes: '字典管理', configs: '参数设置', online: '在线用户', operationLogs: '操作日志',
    loginLogs: '登录日志', slowSql: '慢 SQL 日志', mqLogs: '消息队列台账', jobs: '定时任务', jobLogs: '调度日志',
  };
  const title = computed(() => titleMap[props.resource]);
  const queryFields = computed<QueryField[]>(() => {
    const fields: Record<Resource, QueryField[]> = {
      users: [{ key: 'userName', label: '用户名称', type: 'input' }, { key: 'phonenumber', label: '手机号码', type: 'input' }, { key: 'status', label: '状态', type: 'status' }],
      roles: [{ key: 'roleName', label: '角色名称', type: 'input' }, { key: 'roleKey', label: '角色权限', type: 'input' }, { key: 'status', label: '状态', type: 'status' }],
      menus: [{ key: 'menuName', label: '菜单名称', type: 'input' }, { key: 'menuType', label: '菜单类型', type: 'menuType' }],
      departments: [{ key: 'deptName', label: '部门名称', type: 'input' }, { key: 'status', label: '状态', type: 'status' }],
      dictTypes: [{ key: 'dictName', label: '字典名称', type: 'input' }, { key: 'dictType', label: '字典类型', type: 'input' }, { key: 'status', label: '状态', type: 'status' }],
      configs: [{ key: 'configName', label: '参数名称', type: 'input' }, { key: 'configKey', label: '参数键名', type: 'input' }, { key: 'configType', label: '系统内置', type: 'input' }],
      online: [{ key: 'ipaddr', label: '登录地址', type: 'input' }, { key: 'userName', label: '用户名称', type: 'input' }],
      operationLogs: [{ key: 'title', label: '系统模块', type: 'input' }, { key: 'operName', label: '操作人员', type: 'input' }],
      loginLogs: [{ key: 'userName', label: '登录名称', type: 'input' }, { key: 'ipaddr', label: '登录地址', type: 'input' }],
      slowSql: [{ key: 'requestUrl', label: '请求地址', type: 'input' }],
      mqLogs: [{ key: 'messageTopic', label: '消息主题', type: 'input' }],
      jobs: [{ key: 'jobName', label: '任务名称', type: 'input' }, { key: 'jobGroup', label: '任务组名', type: 'input' }],
      jobLogs: [{ key: 'jobName', label: '任务名称', type: 'input' }, { key: 'jobGroup', label: '任务组名', type: 'input' }],
    };
    return fields[props.resource];
  });
  const rowKey = computed(() => ({ users: 'userId', roles: 'roleId', menus: 'menuId', departments: 'deptId', dictTypes: 'dictId', configs: 'configId', online: 'tokenId', operationLogs: 'operId', loginLogs: 'infoId', slowSql: 'slowSqlId', mqLogs: 'messageLogId', jobs: 'jobId', jobLogs: 'jobLogId' }[props.resource]));
  const supportsSelection = computed(() => !['menus', 'departments'].includes(props.resource));
  const supportsCreate = computed(() => ['users', 'roles', 'menus', 'departments', 'dictTypes', 'configs', 'jobs'].includes(props.resource));
  const supportsEdit = computed(() => supportsCreate.value);
  const supportsDelete = computed(() => ['users', 'roles', 'menus', 'departments', 'dictTypes', 'configs', 'slowSql', 'operationLogs', 'loginLogs', 'jobs', 'jobLogs'].includes(props.resource));
  const pagination = computed(() => total.value === null ? false : { total: total.value, current: page.value, pageSize: pageSize.value, showPageSize: true, showTotal: true });
  const dialogTitle = computed(() => `${editing.value ? '修改' : '新增'}${title.value}`);

  function can(action: string) {
    const prefix: Record<Resource, string> = { users: 'system:user', roles: 'system:role', menus: 'system:menu', departments: 'system:dept', dictTypes: 'system:dict', configs: 'system:config', online: 'monitor:online', operationLogs: 'monitor:operlog', loginLogs: 'monitor:logininfor', slowSql: 'monitor:slowsql', mqLogs: 'monitor:mqlog', jobs: 'monitor:job', jobLogs: 'monitor:job' };
    return userStore.hasPermission(`${prefix[props.resource]}:${action}`) || userStore.roles.includes('admin');
  }
  function menuTypeLabel(type: string) { return ({ M: '目录', C: '菜单', F: '按钮' } as Record<string, string>)[type] || type || '-'; }
  function resetQuery() { Object.keys(query).forEach((key) => { query[key] = undefined; }); page.value = 1; loadData(); }
  function handleQuery() { page.value = 1; loadData(); }
  function handleSelectionChange(keys: Array<string | number>) { selectedKeys.value = keys; }
  function handlePageChange(value: number) { page.value = value; loadData(); }
  function handlePageSizeChange(value: number) { pageSize.value = value; page.value = 1; loadData(); }
  function resetForm() { Object.keys(form).forEach((key) => delete form[key]); Object.assign(form, { status: '0', menuType: 'C', orderNum: 1, roleSort: 1, configType: 'N', parentId: 0 }); }
  /** 打开菜单图标选择器。 */
  function openIconPicker() { iconSearch.value = ''; iconPickerVisible.value = true; }
  /** 选择菜单图标并关闭选择器。 */
  function selectIcon(icon: string) { form.icon = icon; iconPickerVisible.value = false; }
  /** 清除当前菜单图标。 */
  function clearIcon() { form.icon = ''; iconPickerVisible.value = false; }
  function openCreate() { resetForm(); dialogVisible.value = true; editing.value = false; if (props.resource === 'menus') loadMenuTree(); if (props.resource === 'departments') loadDepartmentTree(); }
  async function openEdit(record: Record<string, any>) { resetForm(); editing.value = true; const id = record[rowKey.value as string]; const response = await getDetail(id); Object.assign(form, response?.data || record); dialogVisible.value = true; if (props.resource === 'menus') loadMenuTree(); if (props.resource === 'departments') loadDepartmentTree(); }
  async function openDetail(record: Record<string, any>) {
    detailRecord.value = record;
    detailVisible.value = true;
    detailLoading.value = true;
    try {
      const response = await getOperationLog(record.operId);
      detailRecord.value = response?.data || record;
    } finally {
      detailLoading.value = false;
    }
  }
  function closeDialog() { dialogVisible.value = false; }
  function confirm(message: string, action: () => Promise<void>) { Modal.confirm({ title: '请确认操作', content: message, onOk: action }); }

  async function getDetail(id: string | number) {
    const calls: Partial<Record<Resource, (value: string | number) => Promise<any>>> = { users: getUser, roles: getRole, menus: getMenu, departments: getDepartment, dictTypes: getDictType, configs: getConfig, jobs: getJob };
    return calls[props.resource]?.(id);
  }

  async function loadMenuTree() { const response = await (await import('@/api/admin')).getMenuTree(); menuTree.value = response.data || []; }
  async function loadDepartmentTree() { const response = await (await import('@/api/admin')).getDepartmentTree(); departmentTree.value = response.data || []; }

  /** 将菜单、部门的扁平列表转换为 Arco Table 可展开的树结构。 */
  function buildTableTree(data: Record<string, any>[], keyField: string) {
    const nodeMap = new Map<string, Record<string, any>>();
    const nodes = data.map((record) => {
      const node = { ...record };
      delete node.children;
      nodeMap.set(String(node[keyField]), node);
      return node;
    });
    const rootNodes: Record<string, any>[] = [];

    nodes.forEach((node) => {
      const parent = nodeMap.get(String(node.parentId));
      if (parent && parent !== node) {
        if (!parent.children) parent.children = [];
        parent.children.push(node);
      } else {
        rootNodes.push(node);
      }
    });
    return rootNodes;
  }

  /** 加载当前资源列表，并为树形资源恢复父子层级。 */
  async function loadData() {
    loading.value = true;
    try {
      const params = { ...query, pageNum: page.value, pageSize: pageSize.value };
      let response: any;
      switch (props.resource) {
        case 'users': response = await listUsers(params); break;
        case 'roles': response = await listRoles(params); break;
        case 'menus': response = await listMenus(query); break;
        case 'departments': response = await listDepartments(query); break;
        case 'dictTypes': response = await listDictTypes(params); break;
        case 'configs': response = await listConfigs(params); break;
        case 'online': response = await listOnlineUsers(params); break;
        case 'operationLogs': response = await listOperationLogs(params); break;
        case 'loginLogs': response = await listLoginLogs(params); break;
        case 'slowSql': response = await listSlowSqlLogs(params); break;
        case 'mqLogs': response = await (await import('@/api/admin')).listMqLogs(params); break;
        case 'jobs': response = await listJobs(params); break;
        case 'jobLogs': response = await listJobLogs(params); break;
        default: response = { rows: [], total: 0 };
      }
      const responseRows = response.rows || response.data || [];
      rows.value = ['menus', 'departments'].includes(props.resource)
        ? buildTableTree(responseRows, rowKey.value as string)
        : responseRows;
      total.value = response.total === undefined ? null : response.total;
    } catch (error) {
      rows.value = [];
    } finally { loading.value = false; }
  }

  async function submitForm() {
    const errors = await formRef.value?.validate();
    if (errors) return;
    submitting.value = true;
    try {
      const calls: Record<string, (data: Record<string, any>) => Promise<any>> = {
        users: editing.value ? updateUser : createUser,
        roles: editing.value ? updateRole : createRole,
        menus: editing.value ? updateMenu : createMenu,
        departments: editing.value ? updateDepartment : createDepartment,
        dictTypes: editing.value ? updateDictType : createDictType,
        configs: editing.value ? updateConfig : createConfig,
        jobs: editing.value ? updateJob : createJob,
      };
      await calls[props.resource](form);
      Message.success(`${editing.value ? '修改' : '新增'}成功`);
      dialogVisible.value = false;
      await loadData();
    } finally { submitting.value = false; }
  }

  function idsFor(records: Record<string, any>[]) { return records.map((record) => record[rowKey.value as string]); }
  function removeSelected() { const ids = selectedKeys.value; confirm(`确认删除选中的 ${ids.length} 条${title.value}吗？`, async () => { await removeByIds(ids); Message.success('删除成功'); selectedKeys.value = []; await loadData(); }); }
  function recordDisplayName(record: Record<string, any>) {
    const nameFields: Partial<Record<Resource, string>> = {
      users: 'userName', roles: 'roleName', menus: 'menuName', departments: 'deptName',
      dictTypes: 'dictName', configs: 'configName', jobs: 'jobName', operationLogs: 'operId',
      loginLogs: 'infoId', slowSql: 'slowSqlId', jobLogs: 'jobLogId',
    };
    const value = record[nameFields[props.resource] || ''] || record[rowKey.value as string] || '-';
    return props.resource === 'operationLogs' ? `操作日志 #${value}` : value;
  }
  function removeRecord(record: Record<string, any>) { const id = record[rowKey.value as string]; confirm(`确认删除“${recordDisplayName(record)}”吗？`, async () => { await removeByIds([id]); Message.success('删除成功'); await loadData(); }); }
  async function removeByIds(ids: Array<string | number>) {
    const calls: Partial<Record<Resource, (values: Array<string | number>) => Promise<any>>> = { users: removeUsers, roles: removeRoles, dictTypes: removeDictTypes, configs: removeConfigs, operationLogs: removeOperationLogs, loginLogs: removeLoginLogs, slowSql: removeSlowSqlLogs, jobs: removeJobs, jobLogs: async (values) => (await import('@/api/admin')).removeJobLogs(values), menus: async (values) => removeMenu(values[0]), departments: async (values) => removeDepartment(values[0]) };
    await calls[props.resource]?.(ids);
  }
  async function changeStatus(record: Record<string, any>, value: boolean) {
    const status = value ? '0' : '1';
    if (props.resource === 'users') await changeUserStatus({ userId: record.userId, status });
    if (props.resource === 'roles') await changeRoleStatus({ roleId: record.roleId, status });
    if (props.resource === 'jobs') await changeJobStatus({ jobId: record.jobId, status });
    record.status = status;
    Message.success('状态更新成功');
  }
  async function refreshCache() { if (props.resource === 'dictTypes') await refreshDictCache(); if (props.resource === 'configs') await refreshConfigCache(); Message.success('缓存刷新成功'); }
  function runSelectedJob() { const record = rows.value.find((row) => String(row[rowKey.value as string]) === String(selectedKeys.value[0])); if (record) runJobRecord(record); }
  function runJobRecord(record: Record<string, any>) { confirm(`确认立即执行任务“${record.jobName}”吗？`, async () => { await runJob({ jobId: record.jobId, jobGroup: record.jobGroup }); Message.success('任务已提交执行'); }); }

  watch(() => props.resource, () => { resetQuery(); });
  onMounted(loadData);
</script>

<style scoped lang="less">
  .resource-page { min-height: 100%; padding: 16px 20px 20px; background: var(--color-bg-1); }
  .filter-panel, .table-panel { background: var(--color-bg-2); border: 1px solid var(--color-border-2); border-radius: 6px; }
  .filter-panel {
    padding: 14px 20px;
    margin-bottom: 16px;
  }
  .filter-panel :deep(.arco-form) { row-gap: 12px; }
  .filter-panel :deep(.arco-form-item) { margin-bottom: 0; }
  .filter-panel :deep(.arco-input-wrapper),
  .filter-panel :deep(.arco-select-view-single) {
    background-color: var(--color-bg-2);
    border-color: var(--color-border-2);
  }
  .filter-panel :deep(.arco-input-wrapper:hover),
  .filter-panel :deep(.arco-select-view-single:hover) {
    background-color: var(--color-bg-2);
    border-color: var(--color-border-2);
  }
  .detail-spin { width: 100%; }
  .detail-descriptions {
    max-height: calc(100vh - 180px);
    overflow-y: auto;
  }
  .detail-descriptions :deep(.arco-descriptions-table) {
    width: 100%;
    table-layout: fixed;
  }
  .detail-descriptions :deep(.arco-descriptions-item-label) {
    width: 112px;
    white-space: nowrap;
  }
  .detail-descriptions :deep(.arco-descriptions-item-value) {
    min-width: 0;
    overflow-wrap: anywhere;
  }
  .detail-content {
    max-height: 180px;
    margin: 0;
    padding: 8px;
    overflow: auto;
    background: var(--color-fill-2);
    white-space: pre-wrap;
    word-break: break-word;
  }
  .detail-inline-content {
    max-height: 96px;
    overflow: auto;
    white-space: pre-wrap;
    overflow-wrap: anywhere;
  }
  .detail-error { color: rgb(var(--red-6)); }
  .icon-picker-toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
  .icon-picker-toolbar :deep(.arco-input-wrapper) { flex: 1; }
  .icon-picker-grid {
    --icon-picker-size: 48px;
    display: grid;
    grid-template-columns: repeat(auto-fill, var(--icon-picker-size));
    gap: 10px;
    justify-content: start;
    max-height: 390px;
    overflow-y: auto;
    padding: 2px;
  }
  .icon-picker-option {
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    width: var(--icon-picker-size);
    min-width: var(--icon-picker-size);
    height: var(--icon-picker-size);
    aspect-ratio: 1;
    padding: 0;
    color: var(--color-text-2);
    font-size: 22px;
    border: 1px solid var(--color-border-2);
    border-radius: 6px;
  }
  .icon-picker-option:hover,
  .icon-picker-option--selected { color: rgb(var(--primary-6)); border-color: rgb(var(--primary-6)); background: rgb(var(--primary-1)); }
  .table-panel { overflow: hidden; }
  .action-bar { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px 5px; }
  :deep(.arco-table-container) { overflow-x: auto; }
  :deep(.arco-table-element) { min-width: 880px; }
  :deep(.arco-table-expand-btn) {
    width: 22px;
    height: 22px;
    color: var(--color-text-3);
    background-color: transparent;
    border: 0;
    border-radius: 4px;
    transition: color 0.16s ease, background-color 0.16s ease;
  }
  :deep(.arco-table-expand-btn:hover),
  :deep(.arco-table-expand-btn:focus-visible) {
    color: var(--color-primary-6);
    background-color: var(--color-fill-2);
  }
  :deep(.table-expand-icon) {
    font-size: 14px;
    transition: transform 0.16s ease, color 0.16s ease;
  }
  :deep(.table-expand-icon--expanded) { transform: rotate(90deg); }
  @media (max-width: 640px) {
    .resource-page { padding: 16px 12px 12px; }
    .filter-panel { padding: 12px 14px; }
    .action-bar { padding: 12px 14px 3px; }
    .icon-picker-toolbar { align-items: stretch; flex-direction: column; }
    .icon-picker-grid {
      --icon-picker-size: 42px;
      gap: 8px;
    }
  }
</style>
