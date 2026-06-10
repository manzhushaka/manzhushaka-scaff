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
        :columns="columns"
        @page-change="handlePageChange"
      >
        <template #roleCell="{ record }">
          <div class="primary-cell">
            <div class="primary-cell-title">{{ record.roleName }}</div>
            <div class="primary-cell-sub code-text">{{ record.roleCode }}</div>
          </div>
        </template>
        <template #statusCell="{ record }">
          <a-tag :color="record.statusValue === 1 ? 'green' : 'red'">{{ record.statusText }}</a-tag>
        </template>
        <template #actionsCell="{ record }">
          <a-space>
            <a-button size="mini" v-permission="'system:role:update'" @click="openEdit(record.id)">编辑</a-button>
            <a-popconfirm content="确认删除该角色吗？" @ok="handleDelete(record.id)">
              <a-button size="mini" status="danger" v-permission="'system:role:delete'">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
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
        <a-form-item field="menuIds" label="菜单权限">
          <div class="menu-tree-panel">
            <a-spin :loading="menuLoading" class="menu-tree-spin">
              <a-empty v-if="!menuTreeData.length" description="暂无可分配菜单" />
              <a-tree
                v-else
                v-model:checked-keys="checkedMenuKeys"
                v-model:half-checked-keys="halfCheckedMenuKeys"
                block-node
                checkable
                :data="menuTreeData"
                :default-expand-all="true"
              />
            </a-spin>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { Message, type TableColumnData, type TreeNodeData } from '@arco-design/web-vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import type { RoleForm, RoleRow } from '@/types/system';
import { buildRoleMenuCheckedKeys, buildRoleMenuTreeData, normalizeRoleMenuIds } from './roles-support';
import { dataScopeOptions, mapRoleRow, statusOptions } from './shared';

const loading = ref(false);
const visible = ref(false);
const menuLoading = ref(false);
const keyword = ref('');
const statusFilter = ref<number | undefined>();
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const editingId = ref<number | null>(null);
const rows = ref<RoleRow[]>([]);
const menuTreeData = ref<TreeNodeData[]>([]);
const checkedMenuKeys = ref<Array<string | number>>([]);
const halfCheckedMenuKeys = ref<Array<string | number>>([]);
const dataScopeValue = ref<'SELF' | 'DEPT' | 'DEPT_AND_CHILD' | 'ALL'>('SELF');

const form = reactive<RoleForm>({
  roleCode: '',
  roleName: '',
  dataScope: 'SELF',
  status: 1,
  menuIds: [],
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
    dataIndex: 'roleName',
    title: '角色',
    slotName: 'roleCell',
  },
  {
    dataIndex: 'dataScopeText',
    title: '数据范围',
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

function resetForm() {
  form.roleCode = '';
  form.roleName = '';
  form.dataScope = 'SELF';
  dataScopeValue.value = 'SELF';
  form.status = 1;
  form.menuIds = [];
  checkedMenuKeys.value = [];
  halfCheckedMenuKeys.value = [];
  editingId.value = null;
}

async function loadMenuTree() {
  menuLoading.value = true;
  try {
    const menus = await systemApi.listMenus({ status: 1 });
    menuTreeData.value = buildRoleMenuTreeData(menus);
    return menus;
  } finally {
    menuLoading.value = false;
  }
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

async function openCreate() {
  resetForm();
  await loadMenuTree();
  visible.value = true;
}

async function openEdit(id: number) {
  const [detail, menus] = await Promise.all([systemApi.getRole(id), loadMenuTree()]);
  editingId.value = id;
  form.roleCode = detail.roleCode;
  form.roleName = detail.roleName;
  form.dataScope = detail.dataScope ?? 'SELF';
  dataScopeValue.value = detail.dataScope ?? 'SELF';
  form.status = detail.status ?? 1;
  form.menuIds = detail.menuIds ?? [];
  checkedMenuKeys.value = buildRoleMenuCheckedKeys(menus, form.menuIds);
  halfCheckedMenuKeys.value = [];
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
    menuIds: normalizeRoleMenuIds(checkedMenuKeys.value, halfCheckedMenuKeys.value),
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

.menu-tree-panel {
  max-height: 320px;
  overflow: auto;
  padding: 12px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  background: #fbfcfe;
}

.menu-tree-spin {
  width: 100%;
}
</style>
