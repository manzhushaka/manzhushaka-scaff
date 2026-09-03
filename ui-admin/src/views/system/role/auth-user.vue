<template>
  <div class="assign-page">
    <a-page-header @back="router.back" />

    <section class="filter-panel">
      <a-form :model="query" layout="inline" @submit-success="handleQuery">
        <a-form-item field="userName" label="用户名称">
          <a-input v-model="query.userName" allow-clear placeholder="请输入用户名称" />
        </a-form-item>
        <a-form-item field="phonenumber" label="手机号码">
          <a-input v-model="query.phonenumber" allow-clear placeholder="请输入手机号码" />
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
          <a-button type="primary" :disabled="!can('edit')" @click="openSelector">
            <template #icon><icon-plus /></template>
            添加用户
          </a-button>
          <a-button status="danger" :disabled="selectedKeys.length === 0 || !can('edit')" @click="cancelSelected">
            <template #icon><icon-close-circle /></template>
            批量取消授权
          </a-button>
        </a-space>
        <a-tag color="arcoblue">已授权 {{ total }} 人</a-tag>
      </div>
      <a-table
        :data="rows"
        :loading="loading"
        :bordered="false"
        row-key="userId"
        :pagination="pagination"
        page-position="bottom"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
        @selection-change="handleSelectionChange"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #columns>
          <a-table-column title="用户名称" data-index="userName" :width="150" ellipsis tooltip />
          <a-table-column title="用户昵称" data-index="nickName" :width="150" ellipsis tooltip />
          <a-table-column title="邮箱" data-index="email" :width="190" ellipsis tooltip />
          <a-table-column title="手机" data-index="phonenumber" :width="150" />
          <a-table-column title="状态" :width="90">
            <template #cell="{ record }">
              <a-tag :class="['status-tag', record.status === '0' || record.status === 0 ? 'status-tag--success' : 'status-tag--danger']">
                {{ record.status === '0' || record.status === 0 ? '正常' : '停用' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createTime" :width="180" />
          <a-table-column title="操作" align="center" :width="130" fixed="right">
            <template #cell="{ record }">
              <a-button
                type="text"
                class="table-action-button table-action-button--cancel"
                aria-label="取消授权"
                title="取消授权"
                :disabled="!can('edit')"
                @click="cancelOne(record)"
              >
                <template #icon><icon-close-circle /></template>
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </section>

    <a-modal v-model:visible="selectorVisible" title="添加授权用户" :width="820" :footer="false" render-to-body>
      <a-form :model="selectorQuery" layout="inline" @submit-success="loadUnallocated">
        <a-form-item field="userName" label="用户名称">
          <a-input v-model="selectorQuery.userName" allow-clear placeholder="请输入用户名称" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">
            <template #icon><icon-search /></template>
            查询
          </a-button>
        </a-form-item>
      </a-form>
      <a-table
        class="selector-table"
        :data="unallocatedRows"
        :loading="selectorLoading"
        :bordered="false"
        row-key="userId"
        :pagination="false"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
        @selection-change="handleUnallocatedSelection"
      >
        <template #columns>
          <a-table-column title="用户名称" data-index="userName" />
          <a-table-column title="用户昵称" data-index="nickName" />
          <a-table-column title="部门">
            <template #cell="{ record }">{{ record.dept?.deptName || '-' }}</template>
          </a-table-column>
        </template>
      </a-table>
      <div class="selector-footer">
        <a-button @click="selectorVisible = false">取消</a-button>
        <a-button type="primary" :loading="saving" :disabled="unallocatedSelected.length === 0" @click="grantSelected">
          <template #icon><icon-check /></template>
          授予选中用户
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { Message, Modal } from '@arco-design/web-vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    cancelAllRoleUsers,
    cancelRoleUser,
    listAllocatedUsers,
    listUnallocatedUsers,
    selectAllRoleUsers,
  } from '@/api/admin';
  import useUserStore from '@/store/modules/user';

  const route = useRoute();
  const router = useRouter();
  const userStore = useUserStore();
  const roleId = computed(() => route.params.roleId as string);
  const rows = ref<Record<string, any>[]>([]);
  const unallocatedRows = ref<Record<string, any>[]>([]);
  const selectedKeys = ref<Array<string | number>>([]);
  const unallocatedSelected = ref<Array<string | number>>([]);
  const loading = ref(false);
  const selectorLoading = ref(false);
  const saving = ref(false);
  const selectorVisible = ref(false);
  const total = ref(0);
  const page = ref(1);
  const pageSize = ref(10);
  const query = reactive({ userName: '', phonenumber: '' });
  const selectorQuery = reactive({ userName: '' });
  const pagination = computed(() => ({ total: total.value, current: page.value, pageSize: pageSize.value, showPageSize: true, showTotal: true }));

  function can(action: string) {
    return userStore.hasPermission(`system:role:${action}`) || userStore.roles.includes('admin');
  }

  async function loadData() {
    loading.value = true;
    try {
      const response = await listAllocatedUsers({ ...query, roleId: roleId.value, pageNum: page.value, pageSize: pageSize.value });
      rows.value = response.rows || [];
      total.value = response.total || 0;
    } finally {
      loading.value = false;
    }
  }

  async function loadUnallocated() {
    selectorLoading.value = true;
    try {
      const response = await listUnallocatedUsers({ ...selectorQuery, roleId: roleId.value, pageNum: 1, pageSize: 100 });
      unallocatedRows.value = response.rows || [];
      unallocatedSelected.value = [];
    } finally {
      selectorLoading.value = false;
    }
  }

  function openSelector() {
    selectorVisible.value = true;
    loadUnallocated();
  }

  function handleQuery() {
    page.value = 1;
    loadData();
  }

  function resetQuery() {
    query.userName = '';
    query.phonenumber = '';
    handleQuery();
  }

  function handleSelectionChange(keys: Array<string | number>) {
    selectedKeys.value = keys;
  }

  function handleUnallocatedSelection(keys: Array<string | number>) {
    unallocatedSelected.value = keys;
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

  function cancelOne(record: Record<string, any>) {
    Modal.confirm({
      title: '请确认操作',
      content: `确认取消用户“${record.userName || record.userId}”的角色授权吗？`,
      onOk: async () => {
        await cancelRoleUser({ userId: record.userId, roleId: roleId.value });
        Message.success('取消授权成功');
        await loadData();
      },
    });
  }

  function cancelSelected() {
    Modal.confirm({
      title: '请确认操作',
      content: `确认取消选中的 ${selectedKeys.value.length} 个用户授权吗？`,
      onOk: async () => {
        saving.value = true;
        try {
          await cancelAllRoleUsers({ roleId: roleId.value, userIds: selectedKeys.value.join(',') });
          Message.success('取消授权成功');
          selectedKeys.value = [];
          await loadData();
        } finally {
          saving.value = false;
        }
      },
    });
  }

  async function grantSelected() {
    saving.value = true;
    try {
      await selectAllRoleUsers({ roleId: roleId.value, userIds: unallocatedSelected.value.join(',') });
      Message.success('用户授权成功');
      selectorVisible.value = false;
      await loadData();
    } finally {
      saving.value = false;
    }
  }

  onMounted(loadData);
</script>

<script lang="ts">
  export default { name: 'AuthUser' };
</script>

<style lang="less" scoped>
  .assign-page { min-height: 100%; padding: 20px; background: var(--color-fill-2); }
  .filter-panel, .table-panel { border: 1px solid var(--color-border-2); border-radius: 6px; background: var(--color-bg-2); }
  .filter-panel { padding: 18px 20px 2px; margin-bottom: 16px; }
  .table-panel { overflow: hidden; }
  .action-bar { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; }
  .selector-table { margin-top: 18px; }
  .selector-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 18px; }
  :deep(.arco-table) { min-width: 720px; }
  :deep(.arco-table-container) { overflow-x: auto; }
  @media (max-width: 640px) { .assign-page { padding: 12px; } .filter-panel { padding: 14px 14px 2px; } .action-bar { align-items: flex-start; gap: 12px; padding: 12px 14px; } }
</style>
