<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索角色编码或名称"
          style="width: 260px"
          @search="handleSearch"
        />
        <a-select
          v-model="statusFilter"
          :options="statusFilterOptions"
          allow-clear
          placeholder="全部状态"
          style="width: 140px"
          @change="handleSearch"
        />
        <a-button type="primary" v-permission="'system:role:add'" @click="openCreate">新增角色</a-button>
      </a-space>
    </PageHeaderCard>

    <div class="page-card table-card">
      <a-table
        :data="rows"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
        @page-change="handlePageChange"
      >
        <a-table-column data-index="roleName" title="角色">
          <template #cell="{ record }">
            <div class="primary-cell">
              <div class="primary-cell-title">{{ record.roleName }}</div>
              <div class="primary-cell-sub code-text">{{ record.roleCode }}</div>
            </div>
          </template>
        </a-table-column>
        <a-table-column data-index="dataScopeText" title="数据范围" />
        <a-table-column data-index="statusText" title="状态" :width="110">
          <template #cell="{ record }">
            <a-tag :color="record.statusValue === 1 ? 'green' : 'red'">{{ record.statusText }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column data-index="createTimeText" title="创建时间" :width="180" />
        <a-table-column title="操作" :width="220">
          <template #cell="{ record }">
            <a-space>
              <a-button size="mini" v-permission="'system:role:update'" @click="openEdit(record.id)">编辑</a-button>
              <a-popconfirm content="确认删除该角色吗？" @ok="handleDelete(record.id)">
                <a-button size="mini" status="danger" v-permission="'system:role:delete'">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <a-modal v-model:visible="visible" :title="editingId ? '编辑角色' : '新增角色'" @before-ok="submitForm">
      <a-form :model="form" layout="vertical">
        <a-form-item field="roleCode" label="角色编码">
          <a-input v-model="form.roleCode" placeholder="请输入角色编码" />
        </a-form-item>
        <a-form-item field="roleName" label="角色名称">
          <a-input v-model="form.roleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item field="dataScope" label="数据范围">
          <a-select v-model="dataScopeValue" :options="dataScopeOptions" placeholder="请选择数据范围" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="form.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import type { RoleForm, RoleRow } from '@/types/system';
import { dataScopeOptions, mapRoleRow, statusOptions } from './shared';

const loading = ref(false);
const visible = ref(false);
const keyword = ref('');
const statusFilter = ref<number | undefined>();
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const editingId = ref<number | null>(null);
const rows = ref<RoleRow[]>([]);
const dataScopeValue = ref<'SELF' | 'DEPT' | 'DEPT_AND_CHILD' | 'ALL'>('SELF');

const form = reactive<RoleForm>({
  roleCode: '',
  roleName: '',
  dataScope: 'SELF',
  status: 1,
});

const statusFilterOptions = [
  { label: '全部状态', value: undefined },
  ...statusOptions,
];

const pagination = computed(() => ({
  current: current.value,
  pageSize: pageSize.value,
  total: total.value,
  showTotal: true,
}));

function resetForm() {
  form.roleCode = '';
  form.roleName = '';
  form.dataScope = 'SELF';
  dataScopeValue.value = 'SELF';
  form.status = 1;
  editingId.value = null;
}

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listRoles({
      pageNum: current.value,
      pageSize: pageSize.value,
      roleCode: keyword.value || undefined,
      roleName: keyword.value || undefined,
      status: statusFilter.value,
    });
    rows.value = response.records.map(mapRoleRow);
    total.value = response.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  current.value = 1;
  fetchRows();
}

function handlePageChange(page: number) {
  current.value = page;
  fetchRows();
}

function openCreate() {
  resetForm();
  visible.value = true;
}

async function openEdit(id: number) {
  const detail = await systemApi.getRole(id);
  editingId.value = id;
  form.roleCode = detail.roleCode;
  form.roleName = detail.roleName;
  form.dataScope = detail.dataScope ?? 'SELF';
  dataScopeValue.value = detail.dataScope ?? 'SELF';
  form.status = detail.status ?? 1;
  visible.value = true;
}

async function submitForm() {
  if (!form.roleCode.trim() || !form.roleName.trim()) {
    Message.warning('角色编码和角色名称不能为空');
    return false;
  }

  const payload: RoleForm = {
    roleCode: form.roleCode.trim(),
    roleName: form.roleName.trim(),
    dataScope: dataScopeValue.value,
    status: form.status,
  };

  if (editingId.value) {
    await systemApi.updateRole(editingId.value, payload);
    Message.success('角色已更新');
  } else {
    await systemApi.createRole(payload);
    Message.success('角色已创建');
  }

  visible.value = false;
  resetForm();
  await fetchRows();
  return true;
}

async function handleDelete(id: number) {
  await systemApi.deleteRole(id);
  Message.success('角色已删除');
  if (rows.value.length === 1 && current.value > 1) {
    current.value -= 1;
  }
  await fetchRows();
}

fetchRows();
</script>

<style scoped>
.system-page {
  display: grid;
  gap: 18px;
}

.primary-cell-title {
  color: #17233c;
  font-weight: 700;
}

.primary-cell-sub {
  margin-top: 4px;
  color: #74839a;
  font-size: 12px;
}
</style>
