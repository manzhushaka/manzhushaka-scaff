<template>
  <div>
    <PageHeaderCard title="菜单管理" description="按真实菜单控制器联调，保留目录、菜单、按钮三类的基础维护。">
      <a-space>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索菜单名称"
          style="width: 260px"
          @search="fetchRows"
        />
        <a-button type="primary" v-permission="'system:menu:add'" @click="openCreate">新增菜单</a-button>
      </a-space>
    </PageHeaderCard>

    <div class="page-card table-card">
      <a-table :data="rows" :loading="loading" row-key="id" :pagination="false">
        <a-table-column data-index="menuName" title="菜单名称" />
        <a-table-column data-index="menuType" title="类型" />
        <a-table-column data-index="routePath" title="路由地址" />
        <a-table-column data-index="component" title="组件路径" />
        <a-table-column data-index="perms" title="权限标识" />
        <a-table-column data-index="visibleText" title="显示状态" />
        <a-table-column data-index="statusText" title="状态" />
        <a-table-column data-index="sort" title="排序" />
        <a-table-column title="操作" :width="220">
          <template #cell="{ record }">
            <a-space>
              <a-button size="mini" v-permission="'system:menu:update'" @click="openEdit(record.id)">编辑</a-button>
              <a-popconfirm content="确认删除该菜单吗？" @ok="handleDelete(record.id)">
                <a-button size="mini" status="danger" v-permission="'system:menu:delete'">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <a-modal v-model:visible="visible" :title="editingId ? '编辑菜单' : '新增菜单'" @before-ok="submitForm">
      <a-form :model="form" layout="vertical">
        <a-form-item field="menuName" label="菜单名称">
          <a-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </a-form-item>
        <a-form-item field="parentId" label="上级菜单">
          <a-select v-model="parentIdValue" :options="parentOptions" allow-clear placeholder="根菜单可留空" />
        </a-form-item>
        <a-form-item field="menuType" label="菜单类型">
          <a-select v-model="form.menuType" :options="menuTypeOptions" />
        </a-form-item>
        <a-form-item field="routePath" label="路由地址">
          <a-input v-model="form.routePath" placeholder="请输入路由地址" />
        </a-form-item>
        <a-form-item field="routeName" label="路由名称">
          <a-input v-model="form.routeName" placeholder="请输入路由名称" />
        </a-form-item>
        <a-form-item field="component" label="组件路径">
          <a-input v-model="form.component" placeholder="请输入组件路径" />
        </a-form-item>
        <a-form-item field="perms" label="权限标识">
          <a-input v-model="form.perms" placeholder="请输入权限标识" />
        </a-form-item>
        <a-form-item field="sort" label="排序">
          <a-input-number v-model="form.sort" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item field="visible" label="显示状态">
          <a-select v-model="form.visible" :options="yesNoOptions" />
        </a-form-item>
        <a-form-item field="keepAlive" label="缓存策略">
          <a-select v-model="form.keepAlive" :options="keepAliveOptions" />
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
import type { MenuForm, MenuRow, SelectOption } from '@/types/system';
import { keepAliveOptions, mapMenuRow, menuTypeOptions, statusOptions, yesNoOptions } from './shared';

const keyword = ref('');
const loading = ref(false);
const visible = ref(false);
const editingId = ref<number | null>(null);
const rows = ref<MenuRow[]>([]);
const parentOptions = ref<SelectOption[]>([]);
const parentIdValue = ref<string | number | undefined>();

const form = reactive<Required<MenuForm>>({
  parentId: null,
  menuType: 'MENU',
  menuName: '',
  routePath: '',
  routeName: '',
  component: '',
  icon: '',
  sort: 0,
  visible: 1,
  keepAlive: 0,
  perms: '',
  status: 1,
});

function resetForm() {
  form.parentId = null;
  parentIdValue.value = undefined;
  form.menuType = 'MENU';
  form.menuName = '';
  form.routePath = '';
  form.routeName = '';
  form.component = '';
  form.icon = '';
  form.sort = 0;
  form.visible = 1;
  form.keepAlive = 0;
  form.perms = '';
  form.status = 1;
  editingId.value = null;
}

async function loadParentOptions() {
  parentOptions.value = await systemApi.listMenuOptions();
}

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listMenus({
      menuName: keyword.value || undefined,
    });
    rows.value = response.map(mapMenuRow);
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  resetForm();
  visible.value = true;
}

async function openEdit(id: number) {
  const detail = await systemApi.getMenu(id);
  editingId.value = id;
  form.parentId = detail.parentId;
  parentIdValue.value = detail.parentId ?? undefined;
  form.menuType = detail.menuType as 'DIR' | 'MENU' | 'BUTTON';
  form.menuName = detail.menuName;
  form.routePath = detail.routePath ?? '';
  form.routeName = detail.routeName ?? '';
  form.component = detail.component ?? '';
  form.icon = detail.icon ?? '';
  form.sort = detail.sort ?? 0;
  form.visible = detail.visible ?? 1;
  form.keepAlive = detail.keepAlive ?? 0;
  form.perms = detail.perms ?? '';
  form.status = detail.status ?? 1;
  visible.value = true;
}

async function submitForm() {
  if (!form.menuName.trim()) {
    Message.warning('菜单名称不能为空');
    return false;
  }

  const payload: MenuForm = {
    parentId: parentIdValue.value == null ? null : Number(parentIdValue.value),
    menuType: form.menuType,
    menuName: form.menuName.trim(),
    routePath: form.routePath.trim(),
    routeName: form.routeName.trim(),
    component: form.component.trim(),
    icon: form.icon.trim(),
    sort: form.sort,
    visible: form.visible,
    keepAlive: form.keepAlive,
    perms: form.perms.trim(),
    status: form.status,
  };

  if (editingId.value) {
    await systemApi.updateMenu(editingId.value, payload);
    Message.success('菜单已更新');
  } else {
    await systemApi.createMenu(payload);
    Message.success('菜单已创建');
  }

  visible.value = false;
  resetForm();
  await Promise.all([fetchRows(), loadParentOptions()]);
  return true;
}

async function handleDelete(id: number) {
  await systemApi.deleteMenu(id);
  Message.success('菜单已删除');
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
