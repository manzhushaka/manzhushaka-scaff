<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索用户名或昵称"
          style="width: 280px"
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

    <div class="content-grid">
      <div class="page-card dept-tree-card">
        <div class="dept-tree-header">
          <div>
            <div class="section-title">部门树</div>
            <div class="dept-tree-tip">当前筛选：{{ activeDeptName }}</div>
          </div>
          <a-button type="text" size="mini" @click="clearDeptFilter">全部</a-button>
        </div>
        <a-empty v-if="!deptTreeData.length" description="暂无部门数据" />
        <a-tree
          v-else
          block-node
          :data="deptTreeData"
          :default-expand-all="true"
          :selected-keys="selectedDeptKeys"
          @select="handleDeptSelect"
        />
      </div>

      <div class="page-card table-card">
        <a-table
          :data="rows"
          :loading="loading"
          row-key="id"
          :pagination="pagination"
          :columns="columns"
          @page-change="handlePageChange"
        >
          <template #usernameCell="{ record }">
            <div class="primary-cell">
              <div class="primary-cell-title">{{ record.username }}</div>
              <div class="primary-cell-sub">{{ record.nickname }}</div>
            </div>
          </template>
          <template #roleCodesCell="{ record }">
            <span class="code-text">{{ record.roleCodesText }}</span>
          </template>
          <template #statusCell="{ record }">
            <a-tag :color="record.statusValue === 1 ? 'green' : 'red'">{{ record.statusText }}</a-tag>
          </template>
          <template #actionsCell="{ record }">
            <a-space>
              <a-button size="mini" v-permission="'system:user:update'" @click="openEdit(record.id)">编辑</a-button>
              <a-popconfirm content="确认删除该用户吗？" @ok="handleDelete(record.id)">
                <a-button size="mini" status="danger" v-permission="'system:user:delete'">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table>
      </div>
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
import { Message, type TableColumnData } from '@arco-design/web-vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import type { SelectOption, UserForm, UserRow } from '@/types/system';
import { mapUserRow, statusOptions } from './shared';
import { buildUserDeptTreeData, buildUserListQuery, type UserDeptTreeNode } from './users-support';

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
const deptTreeData = ref<UserDeptTreeNode[]>([]);
const deptIdValue = ref<string | number | undefined>();
const selectedDeptKeys = ref<Array<string | number>>([]);

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
const columns: TableColumnData[] = [
  {
    dataIndex: 'username',
    title: '用户名',
    slotName: 'usernameCell',
  },
  {
    dataIndex: 'deptName',
    title: '部门',
  },
  {
    dataIndex: 'roleCodesText',
    title: '角色编码',
    slotName: 'roleCodesCell',
  },
  {
    dataIndex: 'statusText',
    title: '状态',
    width: 110,
    slotName: 'statusCell',
  },
  {
    dataIndex: 'createTimeText',
    title: '创建时间',
    width: 180,
  },
  {
    dataIndex: 'actions',
    title: '操作',
    width: 220,
    slotName: 'actionsCell',
  },
];

const activeDeptId = computed(() => {
  const [deptId] = selectedDeptKeys.value;
  return deptId == null ? undefined : Number(deptId);
});

const activeDeptName = computed(() => {
  if (activeDeptId.value == null) {
    return '全部部门';
  }
  return findDeptName(deptTreeData.value, activeDeptId.value) ?? `部门 #${activeDeptId.value}`;
});

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

async function loadDeptTree() {
  const response = await systemApi.listDeptTree({ status: 1 });
  deptTreeData.value = buildUserDeptTreeData(response);
}

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listUsers(
      buildUserListQuery({
        pageNum: current.value,
        pageSize: pageSize.value,
        keyword: keyword.value.trim(),
        status: statusFilter.value,
        deptId: activeDeptId.value,
      }),
    );
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

function handleDeptSelect(selectedKeys: Array<string | number>) {
  selectedDeptKeys.value = selectedKeys;
  current.value = 1;
  fetchRows();
}

function clearDeptFilter() {
  if (!selectedDeptKeys.value.length) {
    return;
  }
  selectedDeptKeys.value = [];
  current.value = 1;
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

function findDeptName(nodes: UserDeptTreeNode[], deptId: number): string | null {
  for (const node of nodes) {
    if (Number(node.key) === deptId) {
      return node.title;
    }
    if (node.children?.length) {
      const childName = findDeptName(node.children, deptId);
      if (childName) {
        return childName;
      }
    }
  }
  return null;
}

async function init() {
  await Promise.all([loadDeptOptions(), loadDeptTree()]);
  await fetchRows();
}

void init();
</script>

<style scoped>
.system-page {
  display: grid;
  gap: 18px;
}

.content-grid {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.dept-tree-card {
  padding: 18px;
}

.dept-tree-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.dept-tree-header .section-title {
  margin-bottom: 4px;
}

.dept-tree-tip {
  color: #74839a;
  font-size: 12px;
}

.dept-tree-card :deep(.arco-tree-node-title) {
  border-radius: 10px;
}

.dept-tree-card :deep(.arco-tree-node-selected .arco-tree-node-title) {
  color: #173f9b;
  font-weight: 700;
  background: rgba(36, 91, 219, 0.12);
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

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
