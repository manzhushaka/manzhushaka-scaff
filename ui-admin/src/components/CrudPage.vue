<template>
  <div class="crud-page">
    <PageHeaderCard :title="meta.title" :description="description">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="按名称或备注搜索"
          style="width: 240px"
          @search="fetchRows"
        />
        <a-button type="primary" v-permission="`${meta.permissionPrefix}:add`" @click="openCreate">
          新增
        </a-button>
      </a-space>
    </PageHeaderCard>

    <div class="page-card table-card">
      <a-table :data="rows" :loading="loading" :pagination="false" row-key="id" :columns="columns">
        <template #actionsCell="{ record }">
          <a-space>
            <a-button size="mini" v-permission="`${meta.permissionPrefix}:update`" @click="openEdit(record)">
              编辑
            </a-button>
            <a-popconfirm
              content="确认删除这条记录吗？"
              @ok="handleDelete(record.id)"
            >
              <a-button size="mini" status="danger" v-permission="`${meta.permissionPrefix}:delete`">
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </div>

    <a-modal v-model:visible="visible" :title="modalTitle" @before-ok="submitForm">
      <a-form :model="form">
        <a-form-item v-for="field in meta.fields" :key="field.field" :field="field.field" :label="field.label">
          <a-input v-model="form[field.field as 'name']" :placeholder="field.placeholder" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item field="remark" label="备注">
          <a-textarea v-model="form.remark" :auto-size="{ minRows: 3, maxRows: 5 }" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { Message, type TableColumnData } from '@arco-design/web-vue';
import { addEntity, deleteEntity, editEntity, getEntityList } from '@/api/system';
import type { CrudMeta, EntityRecord } from '@/types/system';
import PageHeaderCard from './PageHeaderCard.vue';

const props = defineProps<{
  meta: CrudMeta;
  description: string;
}>();

type FormState = {
  name: string;
  status: '启用' | '停用';
  remark: string;
};

const loading = ref(false);
const visible = ref(false);
const keyword = ref('');
const editingId = ref<number | null>(null);
const rows = ref<EntityRecord[]>([]);
const form = reactive<FormState>({
  name: '',
  status: '启用',
  remark: '',
});

const statusOptions = ['启用', '停用'];
const modalTitle = computed(() => (editingId.value ? `编辑${props.meta.title}` : `新增${props.meta.title}`));
const columns = computed<TableColumnData[]>(() => [
  ...props.meta.columns.map((column) => ({
    dataIndex: column.dataIndex,
    title: column.title,
  })),
  {
    dataIndex: 'actions',
    title: '操作',
    width: 220,
    slotName: 'actionsCell',
  },
]);

function resetForm() {
  form.name = '';
  form.status = '启用';
  form.remark = '';
  editingId.value = null;
}

async function fetchRows() {
  loading.value = true;
  try {
    rows.value = (await getEntityList(props.meta.key, keyword.value)) as EntityRecord[];
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  resetForm();
  visible.value = true;
}

function openEdit(record: EntityRecord) {
  editingId.value = record.id;
  form.name = String(record.name);
  form.status = record.status as '启用' | '停用';
  form.remark = String(record.remark);
  visible.value = true;
}

async function submitForm() {
  if (!form.name.trim()) {
    Message.warning('名称不能为空');
    return false;
  }
  if (editingId.value) {
    await editEntity(props.meta.key, editingId.value, form);
    Message.success(`${props.meta.title}已更新`);
  } else {
    await addEntity(props.meta.key, form);
    Message.success(`${props.meta.title}已创建`);
  }
  visible.value = false;
  resetForm();
  await fetchRows();
  return true;
}

async function handleDelete(id: number) {
  await deleteEntity(props.meta.key, id);
  Message.success(`${props.meta.title}已删除`);
  await fetchRows();
}

fetchRows();
</script>

<style scoped>
.crud-page {
  display: grid;
  gap: 18px;
}
</style>
