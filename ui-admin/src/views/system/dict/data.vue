<template>
  <div class="dict-data-page">
    <div class="page-context">
      <div>
        <a-typography-text type="secondary">{{ dictType || '正在读取字典类型' }}</a-typography-text>
      </div>
      <a-space>
        <a-button type="outline" :loading="loading" @click="loadData">
          <template #icon><icon-refresh /></template>
          刷新
        </a-button>
        <a-button type="primary" :disabled="!can('add')" @click="openCreate">
          <template #icon><icon-plus /></template>
          新增
        </a-button>
      </a-space>
    </div>

    <section class="filter-panel">
      <a-form :model="query" layout="inline" @submit-success="handleQuery">
        <a-form-item field="dictLabel" label="字典标签">
          <a-input v-model="query.dictLabel" allow-clear placeholder="请输入字典标签" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="query.status" allow-clear placeholder="请选择状态">
            <a-option value="0">正常</a-option>
            <a-option value="1">停用</a-option>
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
          </a-space>
        </a-form-item>
      </a-form>
    </section>

    <section class="table-panel">
      <div class="action-bar">
        <a-space>
          <a-button status="danger" :disabled="selectedKeys.length === 0 || !can('remove')" @click="removeSelected">
            <template #icon><icon-delete /></template>
            删除选中
          </a-button>
          <a-button type="outline" :disabled="!can('export')" :loading="exporting" @click="exportData">
            <template #icon><icon-download /></template>
            导出
          </a-button>
        </a-space>
        <a-tag color="arcoblue">共 {{ total }} 条</a-tag>
      </div>
      <a-table
        :data="rows"
        :loading="loading"
        :bordered="false"
        row-key="dictCode"
        :pagination="pagination"
        page-position="bottom"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
        @selection-change="handleSelectionChange"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #columns>
          <a-table-column title="字典编码" data-index="dictCode" :width="110" />
          <a-table-column title="字典标签" data-index="dictLabel" :width="160" ellipsis tooltip>
            <template #cell="{ record }">
              <a-tag v-if="record.listClass && record.listClass !== 'default'" :class="['status-tag', `status-tag--${tagColor(record.listClass)}`]">{{ record.dictLabel }}</a-tag>
              <span v-else>{{ record.dictLabel }}</span>
            </template>
          </a-table-column>
          <a-table-column title="字典键值" data-index="dictValue" :width="140" />
          <a-table-column title="显示排序" data-index="dictSort" :width="100" />
          <a-table-column title="状态" :width="90">
            <template #cell="{ record }">
              <a-tag :class="['status-tag', record.status === '0' || record.status === 0 ? 'status-tag--success' : 'status-tag--danger']">
                {{ record.status === '0' || record.status === 0 ? '正常' : '停用' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="备注" data-index="remark" ellipsis tooltip />
          <a-table-column title="创建时间" data-index="createTime" :width="180" />
          <a-table-column title="操作" align="center" :width="150" fixed="right">
            <template #cell="{ record }">
              <a-space class="table-action-buttons">
                <a-button
                  type="text"
                  class="table-action-button table-action-button--edit"
                  aria-label="修改"
                  title="修改"
                  :disabled="!can('edit')"
                  @click="openEdit(record)"
                >
                  <template #icon><icon-edit /></template>
                </a-button>
                <a-button
                  type="text"
                  class="table-action-button table-action-button--delete"
                  aria-label="删除"
                  title="删除"
                  :disabled="!can('remove')"
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

    <a-modal v-model:visible="dialogVisible" :title="dialogTitle" :width="560" :ok-loading="submitting" render-to-body @ok="submitForm">
      <a-form ref="formRef" class="modal-form" :model="form" :rules="rules" :label-col-props="{ span: 5 }" :wrapper-col-props="{ span: 18 }">
        <a-form-item field="dictType" label="字典类型">
          <a-input v-model="form.dictType" disabled />
        </a-form-item>
        <a-form-item field="dictLabel" label="数据标签"><a-input v-model="form.dictLabel" placeholder="请输入数据标签" /></a-form-item>
        <a-form-item field="dictValue" label="数据键值"><a-input v-model="form.dictValue" placeholder="请输入数据键值" /></a-form-item>
        <a-form-item field="cssClass" label="样式属性"><a-input v-model="form.cssClass" placeholder="请输入样式属性" /></a-form-item>
        <a-form-item field="dictSort" label="显示排序"><a-input-number v-model="form.dictSort" :min="0" /></a-form-item>
        <a-form-item field="listClass" label="回显样式">
          <a-select v-model="form.listClass">
            <a-option v-for="item in listClasses" :key="item.value" :value="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">停用</a-radio></a-radio-group>
        </a-form-item>
        <a-form-item field="remark" label="备注"><a-textarea v-model="form.remark" placeholder="请输入备注" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
  /* eslint-disable no-use-before-define */
  import { computed, onMounted, reactive, ref } from 'vue';
  import { Message, Modal } from '@arco-design/web-vue';
  import type { FormInstance } from '@arco-design/web-vue/es/form';
  import { useRoute, useRouter } from 'vue-router';
  import {
    createDictData,
    exportDictData,
    getDictData,
    getDictType,
    listDictData,
    removeDictData,
    updateDictData,
  } from '@/api/admin';
  import useUserStore from '@/store/modules/user';

  const route = useRoute();
  const router = useRouter();
  const userStore = useUserStore();
  const loading = ref(true);
  const submitting = ref(false);
  const exporting = ref(false);
  const dictType = ref('');
  const rows = ref<Record<string, any>[]>([]);
  const selectedKeys = ref<Array<string | number>>([]);
  const total = ref(0);
  const page = ref(1);
  const pageSize = ref(10);
  const dialogVisible = ref(false);
  const editing = ref(false);
  const formRef = ref<FormInstance>();
  const query = reactive({ dictLabel: '', status: '' });
  const form = reactive<Record<string, any>>({});
  const rules = {
    dictLabel: [{ required: true, message: '数据标签不能为空' }],
    dictValue: [{ required: true, message: '数据键值不能为空' }],
    dictSort: [{ required: true, message: '显示排序不能为空' }],
  };
  const listClasses = [
    { value: 'default', label: '默认' },
    { value: 'primary', label: '主要' },
    { value: 'success', label: '成功' },
    { value: 'info', label: '信息' },
    { value: 'warning', label: '警告' },
    { value: 'danger', label: '危险' },
  ];
  const pagination = computed(() => ({ total: total.value, current: page.value, pageSize: pageSize.value, showPageSize: true, showTotal: true }));
  const dialogTitle = computed(() => `${editing.value ? '修改' : '新增'}字典数据`);

  function can(action: string) {
    return userStore.hasPermission(`system:dict:${action}`) || userStore.roles.includes('admin');
  }

  function tagColor(value: string) {
    return ({ primary: 'info', success: 'success', warning: 'warning', danger: 'danger', info: 'neutral' } as Record<string, string>)[value] || 'neutral';
  }

  async function loadType() {
    const response = await getDictType(route.params.dictId as string);
    dictType.value = response.data?.dictType || '';
  }

  async function loadData() {
    loading.value = true;
    try {
      const response = await listDictData({ ...query, dictType: dictType.value, pageNum: page.value, pageSize: pageSize.value });
      rows.value = response.rows || [];
      total.value = response.total || 0;
    } finally {
      loading.value = false;
    }
  }

  function handleQuery() {
    page.value = 1;
    loadData();
  }

  function resetQuery() {
    query.dictLabel = '';
    query.status = '';
    handleQuery();
  }

  function handleSelectionChange(keys: Array<string | number>) {
    selectedKeys.value = keys;
  }

  function handlePageChange(value: number) {
    page.value = value;
    loadData();
  }

  function handlePageSizeChange(value: number) {
    pageSize.value = value;
    page.value = 1;
    loadData();
  }

  function resetForm() {
    Object.keys(form).forEach((key) => delete form[key]);
    Object.assign(form, { dictType: dictType.value, dictLabel: '', dictValue: '', cssClass: '', listClass: 'default', dictSort: 0, status: '0', remark: '' });
  }

  function openCreate() {
    resetForm();
    editing.value = false;
    dialogVisible.value = true;
  }

  async function openEdit(record: Record<string, any>) {
    resetForm();
    const response = await getDictData(record.dictCode);
    Object.assign(form, response.data || record);
    editing.value = true;
    dialogVisible.value = true;
  }

  async function submitForm() {
    if (await formRef.value?.validate()) return;
    submitting.value = true;
    try {
      if (editing.value) await updateDictData(form);
      else await createDictData(form);
      Message.success(`${editing.value ? '修改' : '新增'}成功`);
      dialogVisible.value = false;
      await loadData();
    } finally {
      submitting.value = false;
    }
  }

  function removeRecord(record: Record<string, any>) {
    confirmRemove([record.dictCode], `确认删除字典数据“${record.dictLabel || record.dictCode}”吗？`);
  }

  function removeSelected() {
    confirmRemove(selectedKeys.value, `确认删除选中的 ${selectedKeys.value.length} 条字典数据吗？`);
  }

  function confirmRemove(ids: Array<string | number>, content: string) {
    Modal.confirm({ title: '请确认操作', content, onOk: async () => { await removeDictData(ids); Message.success('删除成功'); selectedKeys.value = []; await loadData(); } });
  }

  async function exportData() {
    exporting.value = true;
    try {
      const response = await exportDictData({ ...query, dictType: dictType.value });
      const blob = new Blob([response as unknown as ArrayBuffer]);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `dict_data_${Date.now()}.xlsx`;
      link.click();
      URL.revokeObjectURL(url);
    } finally {
      exporting.value = false;
    }
  }

  async function init() {
    await loadType();
    await loadData();
  }

  onMounted(init);
</script>

<script lang="ts">
  export default { name: 'DictData' };
</script>

<style lang="less" scoped>
  .dict-data-page { min-height: 100%; padding: 20px; background: var(--color-fill-2); }
  .page-context { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
  .filter-panel, .table-panel { border: 1px solid var(--color-border-2); border-radius: 6px; background: var(--color-bg-2); }
  .filter-panel { padding: 18px 20px 2px; margin-bottom: 16px; }
  .table-panel { overflow: hidden; }
  .action-bar { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; }
  :deep(.arco-table) { min-width: 900px; }
  :deep(.arco-table-container) { overflow-x: auto; }
  @media (max-width: 640px) { .dict-data-page { padding: 12px; } .page-context { align-items: flex-start; gap: 12px; } .filter-panel { padding: 14px 14px 2px; } .action-bar { padding: 12px 14px; } }
</style>
