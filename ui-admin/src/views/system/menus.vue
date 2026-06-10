<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索菜单名称"
          style="width: 260px"
          @search="handleSearch"
          @clear="handleSearch"
        />
        <a-button type="primary" v-permission="'system:menu:add'" @click="openCreate">新增菜单</a-button>
      </a-space>
    </PageHeaderCard>

    <div class="content-grid">
      <div class="page-card menu-tree-card">
        <div class="menu-tree-header">
          <div>
            <div class="section-title">菜单结构</div>
            <div class="menu-tree-tip">{{ treeTipText }}</div>
          </div>
        </div>

        <a-spin :loading="loading" class="menu-tree-spin">
          <a-empty v-if="!allMenus.length" description="暂无菜单数据" />
          <a-empty v-else-if="!filteredTree.length" description="未找到匹配的菜单" />
          <a-tree
            v-else
            block-node
            :data="treeData"
            :default-expand-all="true"
            :selected-keys="selectedKeys"
            @select="handleTreeSelect"
          >
            <template #title="nodeData">
              <div class="menu-tree-node">
                <div class="menu-tree-node-main">
                  <span class="menu-tree-node-name">{{ nodeData.menuName }}</span>
                  <span v-if="nodeData.routePath" class="menu-tree-node-path">{{ nodeData.routePath }}</span>
                </div>
                <a-space size="mini" class="menu-tree-node-actions">
                  <a-button
                    type="text"
                    size="mini"
                    v-permission="'system:menu:add'"
                    @click.stop="openCreateWithParent(nodeData.id)"
                  >
                    新增子菜单
                  </a-button>
                  <a-button
                    type="text"
                    size="mini"
                    v-permission="'system:menu:update'"
                    @click.stop="openEdit(nodeData.id)"
                  >
                    编辑
                  </a-button>
                  <a-popconfirm content="确认删除该菜单吗？" @ok="handleDelete(nodeData.id)">
                    <a-button type="text" size="mini" status="danger" v-permission="'system:menu:delete'" @click.stop>
                      删除
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </div>
            </template>
          </a-tree>
        </a-spin>
      </div>

      <div class="page-card menu-detail-card">
        <div class="menu-detail-header">
          <div>
            <div class="section-title">菜单详情</div>
            <div class="menu-detail-tip">{{ detailTipText }}</div>
          </div>
          <a-space v-if="selectedMenu" size="mini">
            <a-button type="outline" size="small" v-permission="'system:menu:add'" @click="openCreateWithParent(selectedMenu.id)">
              新增子菜单
            </a-button>
            <a-button type="outline" size="small" v-permission="'system:menu:update'" @click="openEdit(selectedMenu.id)">
              编辑
            </a-button>
          </a-space>
        </div>

        <a-empty v-if="!allMenus.length" description="暂无菜单数据" />
        <a-empty v-else-if="!filteredTree.length" description="当前筛选下暂无可展示菜单" />
        <a-empty v-else-if="!detailView" description="请选择左侧菜单查看详情" />
        <div v-else class="menu-detail-content">
          <div class="menu-detail-summary">
            <div class="menu-detail-copy">
              <div class="menu-detail-title">{{ detailView.title }}</div>
              <div class="menu-detail-sub">菜单 ID：{{ selectedMenu?.id }} · 上级菜单：{{ parentName }}</div>
            </div>
            <a-tag :color="menuTypeColorMap[detailView.tag] ?? 'arcoblue'">{{ detailView.tag }}</a-tag>
          </div>

          <a-descriptions class="menu-detail-descriptions" :column="2" bordered layout="vertical">
            <a-descriptions-item v-for="group in detailView.groups" :key="group[0]" :label="group[0]">
              <span class="menu-detail-value">{{ group[1] }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </div>
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
import { computed, reactive, ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import type { MenuForm, MenuVO, SelectOption } from '@/types/system';
import { keepAliveOptions, menuTypeOptions, statusOptions, yesNoOptions } from './shared';
import { findMenuSelectionAfterFilter, mapMenuDetail, type MenuTreeNode } from './menus-support';

interface MenuTreeViewNode {
  key: number;
  title: string;
  id: number;
  menuName: string;
  routePath: string | null;
  children?: MenuTreeViewNode[];
}

const keyword = ref('');
const loading = ref(false);
const visible = ref(false);
const editingId = ref<number | null>(null);
const allMenus = ref<MenuVO[]>([]);
const filteredTree = ref<MenuTreeNode[]>([]);
const selectedId = ref<number | null>(null);
const rawParentOptions = ref<SelectOption[]>([]);
const parentIdValue = ref<string | number | undefined>();
const latestMenuRequestId = ref(0);
const menuTypeColorMap: Record<string, 'green' | 'arcoblue' | 'orange'> = {
  DIR: 'orange',
  MENU: 'arcoblue',
  BUTTON: 'green',
};

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

const menuMap = computed(() => {
  const map = new Map<number, MenuVO>();
  allMenus.value.forEach((menu) => {
    map.set(menu.id, menu);
  });
  return map;
});

const selectedMenu = computed(() => {
  if (selectedId.value == null) {
    return undefined;
  }
  return menuMap.value.get(selectedId.value);
});

const selectedKeys = computed<Array<string | number>>(() => (selectedId.value == null ? [] : [selectedId.value]));

const parentName = computed(() => {
  const menu = selectedMenu.value;
  if (!menu) {
    return '根菜单';
  }
  if (menu.parentId == null) {
    return '根菜单';
  }
  return menuMap.value.get(menu.parentId)?.menuName ?? `菜单 #${menu.parentId}`;
});

const detailView = computed(() => mapMenuDetail(selectedMenu.value, parentName.value));
const treeData = computed(() => filteredTree.value.map(toTreeViewNode));

const treeTipText = computed(() => {
  if (!allMenus.value.length) {
    return '暂无菜单可展示';
  }
  if (keyword.value.trim()) {
    return `当前搜索：${keyword.value.trim()}`;
  }
  return `共 ${allMenus.value.length} 个菜单`;
});

const detailTipText = computed(() => {
  if (!selectedMenu.value) {
    return '从左侧选择一个菜单节点';
  }
  return `上级菜单：${parentName.value}`;
});

const excludedParentIds = computed(() => {
  if (editingId.value == null) {
    return new Set<number>();
  }
  return collectDescendantIds(editingId.value);
});

const parentOptions = computed(() =>
  rawParentOptions.value.filter((option) => !excludedParentIds.value.has(Number(option.value))),
);

function resetForm(parentId?: number | null) {
  form.parentId = parentId ?? null;
  parentIdValue.value = parentId ?? undefined;
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

function applyFilteredTree(nextMenus: MenuVO[]) {
  const result = findMenuSelectionAfterFilter(nextMenus, keyword.value, selectedId.value);
  filteredTree.value = result.tree;
  selectedId.value = result.selectedId;
}

async function loadParentOptions() {
  rawParentOptions.value = await systemApi.listMenuOptions();
}

async function fetchRows() {
  const requestId = latestMenuRequestId.value + 1;
  latestMenuRequestId.value = requestId;
  loading.value = true;
  try {
    const response = await systemApi.listMenus();
    if (requestId !== latestMenuRequestId.value) {
      return;
    }
    allMenus.value = response;
    applyFilteredTree(response);
  } finally {
    if (requestId === latestMenuRequestId.value) {
      loading.value = false;
    }
  }
}

function handleSearch() {
  applyFilteredTree(allMenus.value);
}

function handleTreeSelect(selectedKeysValue: Array<string | number>) {
  const [nextSelectedKey] = selectedKeysValue;
  selectedId.value = nextSelectedKey == null ? null : Number(nextSelectedKey);
}

function openCreate() {
  resetForm(null);
  visible.value = true;
}

function openCreateWithParent(parentId: number) {
  resetForm(parentId);
  visible.value = true;
}

async function openEdit(id: number) {
  selectedId.value = id;
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
    selectedId.value = editingId.value;
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
  if (selectedId.value === id) {
    selectedId.value = null;
  }
  Message.success('菜单已删除');
  await Promise.all([fetchRows(), loadParentOptions()]);
}

async function init() {
  await Promise.all([loadParentOptions(), fetchRows()]);
}

void init();

function toTreeViewNode(node: MenuTreeNode): MenuTreeViewNode {
  return {
    key: node.id,
    title: node.menuName,
    id: node.id,
    menuName: node.menuName,
    routePath: node.routePath,
    children: node.children.length ? node.children.map(toTreeViewNode) : undefined,
  };
}

function collectDescendantIds(rootId: number) {
  const excludedIds = new Set<number>([rootId]);
  let changed = true;

  while (changed) {
    changed = false;
    for (const menu of allMenus.value) {
      if (menu.parentId != null && excludedIds.has(menu.parentId) && !excludedIds.has(menu.id)) {
        excludedIds.add(menu.id);
        changed = true;
      }
    }
  }

  return excludedIds;
}
</script>

<style scoped>
.system-page {
  display: grid;
  gap: 18px;
}

.content-grid {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.menu-tree-card,
.menu-detail-card {
  padding: 18px;
}

.menu-tree-header,
.menu-detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.section-title {
  color: #17233c;
  font-size: 16px;
  font-weight: 700;
}

.menu-tree-tip,
.menu-detail-tip,
.menu-detail-sub,
.menu-tree-node-path,
.menu-detail-label {
  color: #74839a;
  font-size: 12px;
}

.menu-tree-spin {
  width: 100%;
}

.menu-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.menu-tree-node-main {
  display: grid;
  min-width: 0;
}

.menu-tree-node-name {
  color: #17233c;
  font-weight: 700;
}

.menu-tree-node-path {
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.menu-tree-node-actions {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.menu-tree-card :deep(.arco-tree-node:hover .menu-tree-node-actions),
.menu-tree-card :deep(.arco-tree-node-selected .menu-tree-node-actions) {
  opacity: 1;
}

.menu-tree-card :deep(.arco-tree-node-title) {
  width: 100%;
  border-radius: 10px;
}

.menu-tree-card :deep(.arco-tree-node-selected .arco-tree-node-title) {
  background: rgba(36, 91, 219, 0.12);
}

.menu-tree-card :deep(.arco-tree-node-selected .menu-tree-node-name) {
  color: #173f9b;
}

.menu-detail-content {
  display: grid;
  gap: 16px;
}

.menu-detail-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 4px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.menu-detail-title {
  color: #17233c;
  font-size: 20px;
  font-weight: 700;
}

.menu-detail-copy {
  min-width: 0;
}

.menu-detail-value {
  color: #17233c;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
}

.menu-detail-descriptions :deep(.arco-descriptions-item-label) {
  color: #74839a;
}

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
