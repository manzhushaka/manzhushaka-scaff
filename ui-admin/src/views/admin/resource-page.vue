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
          <ResourceTableColumns
            :resource="resource"
            :can="can"
            :supports-edit="supportsEdit"
            :supports-delete="supportsDelete"
            @detail="openDetail"
            @edit="openEdit"
            @run="runJobRecord"
            @remove="removeRecord"
            @status="changeStatus"
          />
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
        <ResourceFormFields
          :resource="resource"
          :form-data="form"
          :required-rule="requiredRule"
          :menu-tree="menuTree"
          :department-tree="departmentTree"
          :tree-field-names="treeFieldNames"
          :dept-tree-field-names="deptTreeFieldNames"
          :selected-icon="selectedIcon"
          :icon-menu="IconMenu"
          @open-icon="openIconPicker"
          @update-form="updateForm"
        />
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
  import {
    creatableResources,
    deletableResources,
    resourcePermissions,
    resourceQueryFields,
    resourceRowKeys,
    resourceTitles,
    type QueryField,
    type Resource,
  } from './resource-config';
  import ResourceTableColumns from './resource-table-columns.vue';
  import ResourceFormFields from './resource-form-fields.vue';

  interface Props { resource: Resource; }
  const props = defineProps<Props>();
  const route = useRoute();
  const userStore = useUserStore();
  const loading = ref(true);
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

  const title = computed(() => resourceTitles[props.resource]);
  const queryFields = computed<QueryField[]>(() => resourceQueryFields[props.resource]);
  const rowKey = computed(() => resourceRowKeys[props.resource]);
  const supportsSelection = computed(() => !['menus', 'departments'].includes(props.resource));
  const supportsCreate = computed(() => creatableResources.includes(props.resource));
  const supportsEdit = computed(() => supportsCreate.value);
  const supportsDelete = computed(() => deletableResources.includes(props.resource));
  const pagination = computed(() => total.value === null ? false : { total: total.value, current: page.value, pageSize: pageSize.value, showPageSize: true, showTotal: true });
  const dialogTitle = computed(() => `${editing.value ? '修改' : '新增'}${title.value}`);

  function can(action: string) {
    return userStore.hasPermission(`${resourcePermissions[props.resource]}:${action}`) || userStore.roles.includes('admin');
  }
  function isEnabledStatus(value: unknown) { return value === '0' || value === 0; }
  function menuTypeLabel(type: string) { return ({ M: '目录', C: '菜单', F: '按钮' } as Record<string, string>)[type] || type || '-'; }
  function resetQuery() { Object.keys(query).forEach((key) => { query[key] = undefined; }); page.value = 1; loadData(); }
  function handleQuery() { page.value = 1; loadData(); }
  function handleSelectionChange(keys: Array<string | number>) { selectedKeys.value = keys; }
  function handlePageChange(value: number) { page.value = value; loadData(); }
  function handlePageSizeChange(value: number) { pageSize.value = value; page.value = 1; loadData(); }
  function updateForm(value: Record<string, any>) {
    Object.keys(form).forEach((key) => delete form[key]);
    Object.assign(form, value);
  }
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
  .resource-page { min-height: 100%; padding: var(--ui-layout-content-padding); background: #f7f8fa; }
  .filter-panel, .table-panel {
    background: #fff;
    border: 1px solid var(--color-border-2);
    border-radius: var(--ui-radius-md);
    box-shadow: 0 1px 2px rgb(29 33 41 / 3%);
  }
  .filter-panel {
    padding: 14px 20px;
    margin-bottom: var(--ui-layout-content-padding);
  }
  .filter-panel :deep(.arco-form) { row-gap: 12px; }
  .filter-panel :deep(.arco-form-item) { margin-bottom: 0; }
  .filter-panel :deep(.arco-input-wrapper),
  .filter-panel :deep(.arco-select-view-single) {
    background-color: #fff;
    border-color: #c9cdd4 !important;
  }
  .filter-panel :deep(.arco-input-wrapper:hover),
  .filter-panel :deep(.arco-select-view-single:hover) {
    background-color: #fff;
    border-color: #c9cdd4 !important;
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
    background: var(--ui-bg-content);
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
    border: 1px solid var(--ui-border);
    border-radius: var(--ui-radius-md);
  }
  .icon-picker-option:hover,
  .icon-picker-option--selected { color: rgb(var(--primary-6)); border-color: rgb(var(--primary-6)); background: rgb(var(--primary-1)); }
  .table-panel { overflow: hidden; }
  .action-bar { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px 5px; border-bottom: 1px solid var(--ui-border); }
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
    background-color: var(--ui-bg-content);
  }
  :deep(.table-expand-icon) {
    font-size: 14px;
    transition: transform 0.16s ease, color 0.16s ease;
  }
  :deep(.table-expand-icon--expanded) { transform: rotate(90deg); }
  @media (max-width: 640px) {
    .resource-page { padding: var(--ui-space-3); }
    .filter-panel { padding: 12px 14px; margin-bottom: var(--ui-space-3); }
    .action-bar { padding: 12px 14px 3px; }
    .icon-picker-toolbar { align-items: stretch; flex-direction: column; }
    .icon-picker-grid {
      --icon-picker-size: 42px;
      gap: 8px;
    }
  }
</style>
