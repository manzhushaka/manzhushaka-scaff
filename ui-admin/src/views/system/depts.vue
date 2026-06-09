<template>
  <div>
    <PageHeaderCard title="部门管理" description="保持部门树语义，按真实 `/system/depts/tree` 和部门维护接口联调。">
      <a-space>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索部门名称"
          style="width: 240px"
          @search="fetchRows"
        />
        <a-button type="primary" v-permission="'system:dept:add'" @click="openCreate">新增部门</a-button>
      </a-space>
    </PageHeaderCard>

    <div class="page-card table-card">
      <a-table :data="rows" :loading="loading" row-key="id" :pagination="false" :default-expand-all-rows="true">
        <a-table-column data-index="deptName" title="部门名称" />
        <a-table-column data-index="sort" title="排序" />
        <a-table-column data-index="statusText" title="状态" />
        <a-table-column title="操作" :width="220">
          <template #cell="{ record }">
            <a-space>
              <a-button size="mini" v-permission="'system:dept:update'" @click="openEdit(record.id)">编辑</a-button>
              <a-popconfirm content="确认删除该部门吗？" @ok="handleDelete(record.id)">
                <a-button size="mini" status="danger" v-permission="'system:dept:delete'">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <a-modal v-model:visible="visible" :title="editingId ? '编辑部门' : '新增部门'" @before-ok="submitForm">
      <a-form :model="form" layout="vertical">
        <a-form-item field="deptName" label="部门名称">
          <a-input v-model="form.deptName" placeholder="请输入部门名称" />
        </a-form-item>
        <a-form-item field="parentId" label="上级部门">
          <a-select v-model="parentIdValue" :options="parentOptions" allow-clear placeholder="根部门可留空" />
        </a-form-item>
        <a-form-item field="sort" label="排序">
          <a-input-number v-model="form.sort" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="form.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import type { DeptForm, DeptRow, SelectOption } from '@/types/system';
import { mapDeptRow, statusOptions } from './shared';

const keyword = ref('');
const loading = ref(false);
const visible = ref(false);
const editingId = ref<number | null>(null);
const rows = ref<DeptRow[]>([]);
const parentOptions = ref<SelectOption[]>([]);
const parentIdValue = ref<string | number | undefined>();

const form = reactive<DeptForm>({
  parentId: null,
  deptName: '',
  sort: 0,
  status: 1,
});

function resetForm() {
  form.parentId = null;
  parentIdValue.value = undefined;
  form.deptName = '';
  form.sort = 0;
  form.status = 1;
  editingId.value = null;
}

async function loadParentOptions() {
  parentOptions.value = await systemApi.listDeptOptions();
}

function openCreate() {
  resetForm();
  visible.value = true;
}

async function openEdit(id: number) {
  const detail = await systemApi.getDept(id);
  editingId.value = id;
  form.parentId = detail.parentId;
  parentIdValue.value = detail.parentId ?? undefined;
  form.deptName = detail.deptName;
  form.sort = detail.sort ?? 0;
  form.status = detail.status ?? 1;
  visible.value = true;
}

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listDeptTree({
      deptName: keyword.value || undefined,
    });
    rows.value = response.map(mapDeptRow);
  } finally {
    loading.value = false;
  }
}

async function submitForm() {
  if (!form.deptName.trim()) {
    Message.warning('部门名称不能为空');
    return false;
  }

  const payload: DeptForm = {
    parentId: parentIdValue.value == null ? null : Number(parentIdValue.value),
    deptName: form.deptName.trim(),
    sort: form.sort,
    status: form.status,
  };

  if (editingId.value) {
    await systemApi.updateDept(editingId.value, payload);
    Message.success('部门已更新');
  } else {
    await systemApi.createDept(payload);
    Message.success('部门已创建');
  }

  visible.value = false;
  resetForm();
  await Promise.all([fetchRows(), loadParentOptions()]);
  return true;
}

async function handleDelete(id: number) {
  await systemApi.deleteDept(id);
  Message.success('部门已删除');
  await Promise.all([fetchRows(), loadParentOptions()]);
}

loadParentOptions();
fetchRows();
</script>

<style scoped>
.table-card {
  padding: 16px;
}
</style>
