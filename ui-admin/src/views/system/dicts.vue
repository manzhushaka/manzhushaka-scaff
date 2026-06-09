<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索字典名称或编码"
          style="width: 280px"
          @search="handleSearch"
        />
        <a-button type="primary" v-permission="'system:dict:add'" @click="openTypeCreate">新增字典类型</a-button>
        <a-button type="primary" v-permission="'system:dict:add'" :disabled="!selectedTypeId" @click="openItemCreate">
          新增字典项
        </a-button>
      </a-space>
    </PageHeaderCard>

    <a-row :gutter="16">
      <a-col :span="12">
        <div class="page-card table-card">
          <div class="section-title">字典类型</div>
          <a-table
            :data="typeRows"
            :loading="loadingTypes"
            row-key="id"
            :pagination="typePagination"
            @page-change="handleTypePageChange"
            @row-click="handleRowClick"
          >
            <a-table-column data-index="dictName" title="字典类型">
              <template #cell="{ record }">
                <div class="primary-cell">
                  <div class="primary-cell-title">{{ record.dictName }}</div>
                  <div class="primary-cell-sub code-text">{{ record.dictCode }}</div>
                </div>
              </template>
            </a-table-column>
            <a-table-column data-index="statusText" title="状态" :width="110">
              <template #cell="{ record }">
                <a-tag :color="record.statusValue === 1 ? 'green' : 'red'">{{ record.statusText }}</a-tag>
              </template>
            </a-table-column>
            <a-table-column data-index="createTimeText" title="创建时间" :width="180" />
            <a-table-column title="操作" :width="220">
              <template #cell="{ record }">
                <a-space>
                  <a-button size="mini" v-permission="'system:dict:update'" @click.stop="openTypeEdit(record.id)">编辑</a-button>
                  <a-popconfirm content="确认删除该字典类型吗？" @ok="handleDeleteType(record.id)">
                    <a-button size="mini" status="danger" v-permission="'system:dict:delete'" @click.stop>
                      删除
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </a-table-column>
          </a-table>
        </div>
      </a-col>
      <a-col :span="12">
        <div class="page-card table-card">
          <div class="section-title">字典项</div>
          <a-table :data="itemRows" :loading="loadingItems" row-key="id" :pagination="false">
            <a-table-column data-index="itemLabel" title="字典项">
              <template #cell="{ record }">
                <div class="primary-cell">
                  <div class="primary-cell-title">{{ record.itemLabel }}</div>
                  <div class="primary-cell-sub code-text">{{ record.itemValue }}</div>
                </div>
              </template>
            </a-table-column>
            <a-table-column data-index="sort" title="排序" :width="90" />
            <a-table-column data-index="statusText" title="状态" :width="110">
              <template #cell="{ record }">
                <a-tag :color="record.statusValue === 1 ? 'green' : 'red'">{{ record.statusText }}</a-tag>
              </template>
            </a-table-column>
            <a-table-column title="操作" :width="220">
              <template #cell="{ record }">
                <a-space>
                  <a-button size="mini" v-permission="'system:dict:update'" @click="openItemEdit(record.id)">编辑</a-button>
                  <a-popconfirm content="确认删除该字典项吗？" @ok="handleDeleteItem(record.id)">
                    <a-button size="mini" status="danger" v-permission="'system:dict:delete'">删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </a-table-column>
          </a-table>
        </div>
      </a-col>
    </a-row>

    <a-modal v-model:visible="typeVisible" :title="editingTypeId ? '编辑字典类型' : '新增字典类型'" @before-ok="submitTypeForm">
      <a-form :model="typeForm" layout="vertical">
        <a-form-item field="dictName" label="字典名称">
          <a-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </a-form-item>
        <a-form-item field="dictCode" label="字典编码">
          <a-input v-model="typeForm.dictCode" placeholder="请输入字典编码" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="typeForm.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:visible="itemVisible" :title="editingItemId ? '编辑字典项' : '新增字典项'" @before-ok="submitItemForm">
      <a-form :model="itemForm" layout="vertical">
        <a-form-item field="itemLabel" label="字典标签">
          <a-input v-model="itemForm.itemLabel" placeholder="请输入字典标签" />
        </a-form-item>
        <a-form-item field="itemValue" label="字典值">
          <a-input v-model="itemForm.itemValue" placeholder="请输入字典值" />
        </a-form-item>
        <a-form-item field="sort" label="排序">
          <a-input-number v-model="itemForm.sort" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="itemForm.status" :options="statusOptions" />
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
import type { DictItemForm, DictItemRow, DictTypeForm, DictTypeRow, DictTypeVO } from '@/types/system';
import { mapDictItemRow, mapDictTypeRow, statusOptions } from './shared';

const keyword = ref('');
const loadingTypes = ref(false);
const loadingItems = ref(false);
const typeVisible = ref(false);
const itemVisible = ref(false);
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const editingTypeId = ref<number | null>(null);
const editingItemId = ref<number | null>(null);
const selectedTypeId = ref<number | null>(null);
const typeRows = ref<DictTypeRow[]>([]);
const itemRows = ref<DictItemRow[]>([]);
const currentItems = ref<Record<number, DictItemRow>>({});

const typeForm = reactive<DictTypeForm>({
  dictName: '',
  dictCode: '',
  status: 1,
});

const itemForm = reactive<DictItemForm>({
  dictTypeId: 0,
  itemLabel: '',
  itemValue: '',
  sort: 0,
  status: 1,
});

const typePagination = computed(() => ({
  current: current.value,
  pageSize: pageSize.value,
  total: total.value,
  showTotal: true,
}));

function resetTypeForm() {
  typeForm.dictName = '';
  typeForm.dictCode = '';
  typeForm.status = 1;
  editingTypeId.value = null;
}

function resetItemForm() {
  itemForm.dictTypeId = selectedTypeId.value ?? 0;
  itemForm.itemLabel = '';
  itemForm.itemValue = '';
  itemForm.sort = 0;
  itemForm.status = 1;
  editingItemId.value = null;
}

async function fetchTypes() {
  loadingTypes.value = true;
  try {
    const response = await systemApi.listDictTypes({
      pageNum: current.value,
      pageSize: pageSize.value,
      dictName: keyword.value || undefined,
      dictCode: keyword.value || undefined,
    });
    typeRows.value = response.records.map(mapDictTypeRow);
    total.value = response.total;

    const firstId = response.records[0]?.id ?? null;
    if (!response.records.some((item) => item.id === selectedTypeId.value)) {
      selectedTypeId.value = firstId;
    }
  } finally {
    loadingTypes.value = false;
  }
  await fetchItems();
}

async function fetchItems() {
  if (!selectedTypeId.value) {
    itemRows.value = [];
    currentItems.value = {};
    return;
  }
  loadingItems.value = true;
  try {
    const response = await systemApi.listDictItemsByType(selectedTypeId.value);
    const rows = response.map(mapDictItemRow);
    itemRows.value = rows;
    currentItems.value = Object.fromEntries(rows.map((item) => [item.id, item]));
  } finally {
    loadingItems.value = false;
  }
}

function handleSearch() {
  current.value = 1;
  fetchTypes();
}

function handleTypePageChange(page: number) {
  current.value = page;
  fetchTypes();
}

function handleRowClick(record: Record<string, unknown>) {
  selectedTypeId.value = Number(record.id);
  fetchItems();
}

function openTypeCreate() {
  resetTypeForm();
  typeVisible.value = true;
}

async function openTypeEdit(id: number) {
  const detail = await systemApi.getDictType(id);
  editingTypeId.value = id;
  typeForm.dictName = detail.dictName;
  typeForm.dictCode = detail.dictCode;
  typeForm.status = detail.status ?? 1;
  typeVisible.value = true;
}

function openItemCreate() {
  if (!selectedTypeId.value) {
    Message.warning('请先选择一个字典类型');
    return;
  }
  resetItemForm();
  itemVisible.value = true;
}

async function openItemEdit(id: number) {
  if (!selectedTypeId.value) {
    return;
  }
  const items = await systemApi.listDictItemsByType(selectedTypeId.value);
  const currentItem = items.find((item) => item.id === id);
  if (!currentItem) {
    Message.warning('字典项不存在或已被删除');
    return;
  }
  editingItemId.value = id;
  itemForm.dictTypeId = currentItem.dictTypeId;
  itemForm.itemLabel = currentItem.itemLabel;
  itemForm.itemValue = currentItem.itemValue;
  itemForm.sort = currentItem.sort ?? 0;
  itemForm.status = currentItem.status ?? 1;
  itemVisible.value = true;
}

async function submitTypeForm() {
  if (!typeForm.dictName.trim() || !typeForm.dictCode.trim()) {
    Message.warning('字典名称和字典编码不能为空');
    return false;
  }

  const payload: DictTypeForm = {
    dictName: typeForm.dictName.trim(),
    dictCode: typeForm.dictCode.trim(),
    status: typeForm.status,
  };

  if (editingTypeId.value) {
    await systemApi.updateDictType(editingTypeId.value, payload);
    Message.success('字典类型已更新');
  } else {
    await systemApi.createDictType(payload);
    Message.success('字典类型已创建');
  }

  typeVisible.value = false;
  resetTypeForm();
  await fetchTypes();
  return true;
}

async function submitItemForm() {
  if (!selectedTypeId.value) {
    Message.warning('请先选择一个字典类型');
    return false;
  }
  if (!itemForm.itemLabel.trim() || !itemForm.itemValue.trim()) {
    Message.warning('字典标签和字典值不能为空');
    return false;
  }

  const payload: DictItemForm = {
    dictTypeId: selectedTypeId.value,
    itemLabel: itemForm.itemLabel.trim(),
    itemValue: itemForm.itemValue.trim(),
    sort: itemForm.sort,
    status: itemForm.status,
  };

  if (editingItemId.value) {
    await systemApi.updateDictItem(editingItemId.value, payload);
    Message.success('字典项已更新');
  } else {
    await systemApi.createDictItem(payload);
    Message.success('字典项已创建');
  }

  itemVisible.value = false;
  resetItemForm();
  await fetchItems();
  return true;
}

async function handleDeleteType(id: number) {
  await systemApi.deleteDictType(id);
  Message.success('字典类型已删除');
  if (selectedTypeId.value === id) {
    selectedTypeId.value = null;
  }
  await fetchTypes();
}

async function handleDeleteItem(id: number) {
  await systemApi.deleteDictItem(id);
  Message.success('字典项已删除');
  await fetchItems();
}

fetchTypes();
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
