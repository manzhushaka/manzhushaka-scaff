<template>
  <div>
    <PageHeaderCard title="用户管理" description="按真实用户控制器联调，保留分页查询与最小新增、编辑、删除能力。">
      <a-space>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索用户名或昵称"
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
        <a-button type="primary" v-permission="'system:user:add'" @click="openCreate">新增用户</a-button>
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
        <a-table-column data-index="username" title="用户名" />
        <a-table-column data-index="nickname" title="昵称" />
        <a-table-column data-index="deptName" title="部门" />
        <a-table-column data-index="roleCodesText" title="角色编码" />
        <a-table-column data-index="statusText" title="状态" />
        <a-table-column data-index="createTimeText" title="创建时间" />
        <a-table-column title="操作" :width="220">
          <template #cell="{ record }">
            <a-space>
              <a-button size="mini" v-permission="'system:user:update'" @click="openEdit(record.id)">编辑</a-button>
              <a-popconfirm content="确认删除该用户吗？" @ok="handleDelete(record.id)">
                <a-button size="mini" status="danger" v-permission="'system:user:delete'">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <a-modal v-model:visible="visible" :title="editingId ? '编辑用户' : '新增用户'" @before-ok="submitForm">
      <a-form :model="form" layout="vertical">
        <a-form-item field="username" label="用户名">
          <a-input v-model="form.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item field="nickname" label="昵称">
          <a-input v-model="form.nickname" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item field="password" label="密码">
          <a-input-password v-model="form.password" :placeholder="editingId ? '留空则不修改密码' : '请输入密码'" />
        </a-form-item>
        <a-form-item field="deptId" label="部门">
          <a-select v-model="deptIdValue" :options="deptOptions" allow-clear placeholder="请选择部门" />
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
import type { SelectOption, UserForm, UserRow } from '@/types/system';
import { mapUserRow, statusOptions } from './shared';

const loading = ref(false);
const visible = ref(false);
const keyword = ref('');
const statusFilter = ref<number | undefined>();
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const editingId = ref<number | null>(null);
const rows = ref<UserRow[]>([]);
const deptOptions = ref<SelectOption[]>([]);
const deptIdValue = ref<string | number | undefined>();

const form = reactive<UserForm & { password: string }>({
  username: '',
  password: '',
  nickname: '',
  deptId: null,
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
  form.username = '';
  form.password = '';
  form.nickname = '';
  form.deptId = null;
  deptIdValue.value = undefined;
  form.status = 1;
  editingId.value = null;
}

async function loadDeptOptions() {
  deptOptions.value = await systemApi.listDeptOptions();
}

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listUsers({
      pageNum: current.value,
      pageSize: pageSize.value,
      username: keyword.value || undefined,
      nickname: keyword.value || undefined,
      status: statusFilter.value,
    });
    rows.value = response.records.map(mapUserRow);
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
  const detail = await systemApi.getUser(id);
  editingId.value = id;
  form.username = detail.username;
  form.password = '';
  form.nickname = detail.nickname;
  form.deptId = detail.deptId;
  deptIdValue.value = detail.deptId ?? undefined;
  form.status = detail.status ?? 1;
  visible.value = true;
}

async function submitForm() {
  if (!form.username.trim() || !form.nickname.trim()) {
    Message.warning('用户名和昵称不能为空');
    return false;
  }
  if (!editingId.value && !form.password.trim()) {
    Message.warning('新增用户时密码不能为空');
    return false;
  }

  const payload: UserForm = {
    username: form.username.trim(),
    nickname: form.nickname.trim(),
    deptId: deptIdValue.value == null ? null : Number(deptIdValue.value),
    status: form.status,
    ...(form.password.trim() ? { password: form.password.trim() } : {}),
  };

  if (editingId.value) {
    await systemApi.updateUser(editingId.value, payload);
    Message.success('用户已更新');
  } else {
    await systemApi.createUser(payload);
    Message.success('用户已创建');
  }

  visible.value = false;
  resetForm();
  await fetchRows();
  return true;
}

async function handleDelete(id: number) {
  await systemApi.deleteUser(id);
  Message.success('用户已删除');
  if (rows.value.length === 1 && current.value > 1) {
    current.value -= 1;
  }
  await fetchRows();
}

loadDeptOptions();
fetchRows();
</script>

<style scoped>
.table-card {
  padding: 16px;
}
</style>
