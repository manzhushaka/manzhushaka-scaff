<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索 Stream、事件类型、业务键或 Trace ID"
          style="width: 320px"
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
        <a-input
          v-model="sourceFilter"
          allow-clear
          placeholder="来源 source"
          style="width: 180px"
          @press-enter="handleSearch"
          @clear="handleSearch"
        />
        <a-button v-permission="'system:mq-message:query'" @click="fetchRows">刷新</a-button>
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
        <template #eventIdCell="{ record }">
          <div class="primary-cell">
            <div class="primary-cell-title code-text">{{ record.eventId }}</div>
            <div class="primary-cell-sub">Trace ID：{{ record.traceId }}</div>
          </div>
        </template>
        <template #statusCell="{ record }">
          <a-space size="mini">
            <a-tag :color="statusColorMap[record.statusValue] ?? 'gray'">{{ record.statusText }}</a-tag>
            <a-tag v-if="record.processingTimedOut" color="orange">超时</a-tag>
          </a-space>
        </template>
        <template #errorCell="{ record }">
          <span class="error-text">{{ record.lastError }}</span>
        </template>
        <template #timelineCell="{ record }">
          <div class="timeline-cell">
            <div>创建：{{ record.createTimeText }}</div>
            <div>发布：{{ record.publishedAtText }}</div>
            <div>开始：{{ record.consumeStartedAtText }}</div>
            <div>完成：{{ record.consumedAtText }}</div>
            <div>处理截止：{{ record.processingDeadlineAtText }}</div>
          </div>
        </template>
        <template #actionsCell="{ record }">
          <a-space wrap>
            <a-button size="mini" v-permission="'system:mq-message:query'" @click="openDetail(record)">详情</a-button>
            <a-popconfirm
              v-if="record.canRetry"
              content="确认重新投递这条消息吗？"
              @ok="handleRetry(record)"
            >
              <a-button size="mini" v-permission="'system:mq-message:retry'">重试</a-button>
            </a-popconfirm>
            <a-button
              v-else
              size="mini"
              disabled
              v-permission="'system:mq-message:retry'"
            >
              重试
            </a-button>
          </a-space>
        </template>
      </a-table>
    </div>

    <a-modal v-model:visible="detailVisible" title="消息详情" :footer="false" width="820px">
      <div v-if="activeDetail" class="detail-content">
        <a-descriptions :column="2" bordered layout="vertical">
          <a-descriptions-item label="事件 ID">{{ activeDetail.eventId }}</a-descriptions-item>
          <a-descriptions-item label="状态">{{ activeDetail.statusText }}</a-descriptions-item>
          <a-descriptions-item label="Stream">{{ activeDetail.streamKey }}</a-descriptions-item>
          <a-descriptions-item label="来源">{{ activeDetail.source }}</a-descriptions-item>
          <a-descriptions-item label="事件类型">{{ activeDetail.eventType }}</a-descriptions-item>
          <a-descriptions-item label="业务键">{{ activeDetail.bizKey }}</a-descriptions-item>
          <a-descriptions-item label="Trace ID">{{ activeDetail.traceId }}</a-descriptions-item>
          <a-descriptions-item label="重试次数">{{ activeDetail.retryCountText }}</a-descriptions-item>
          <a-descriptions-item label="处理截止">{{ activeDetail.processingDeadlineAtText }}</a-descriptions-item>
          <a-descriptions-item :span="2" label="关键时间">{{ activeDetail.timelineText }}</a-descriptions-item>
        </a-descriptions>

        <div class="detail-block">
          <div class="detail-label">Payload Snapshot</div>
          <a-textarea :model-value="activeDetail.payloadSnapshot" readonly :auto-size="{ minRows: 8, maxRows: 14 }" />
        </div>

        <div class="detail-block">
          <div class="detail-label">Last Error</div>
          <a-textarea :model-value="activeDetail.lastError" readonly :auto-size="{ minRows: 4, maxRows: 8 }" />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { Message, type TableColumnData } from '@arco-design/web-vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import type { MqMessageRow } from '@/types/system';
import {
  buildMqMessageListQuery,
  mapMqMessageRow,
  mqMessageStatusOptions,
} from './mq-messages-support';

const loading = ref(false);
const detailVisible = ref(false);
const keyword = ref('');
const statusFilter = ref<string | undefined>();
const sourceFilter = ref('');
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const rows = ref<MqMessageRow[]>([]);
const activeDetail = ref<MqMessageRow | null>(null);

const statusColorMap: Record<string, string> = {
  INIT: 'gray',
  PUBLISHED: 'arcoblue',
  PROCESSING: 'gold',
  SUCCESS: 'green',
  FAIL: 'red',
};

const statusFilterOptions = [
  { label: '全部状态', value: undefined },
  ...mqMessageStatusOptions,
];

const columns: TableColumnData[] = [
  { dataIndex: 'eventId', title: '事件 ID', width: 230, slotName: 'eventIdCell' },
  { dataIndex: 'streamKey', title: 'Stream', width: 200 },
  { dataIndex: 'eventType', title: '事件类型', width: 180 },
  { dataIndex: 'bizKey', title: '业务键', width: 160 },
  { dataIndex: 'statusText', title: '状态', width: 130, slotName: 'statusCell' },
  { dataIndex: 'retryCountText', title: '重试次数', width: 110 },
  { dataIndex: 'source', title: '来源', width: 140 },
  { dataIndex: 'lastError', title: '最近错误', width: 220, slotName: 'errorCell', ellipsis: true, tooltip: true },
  { dataIndex: 'timelineText', title: '关键时间', width: 280, slotName: 'timelineCell' },
  { dataIndex: 'actions', title: '操作', width: 160, slotName: 'actionsCell', fixed: 'right' },
];

const pagination = computed(() => ({
  current: current.value,
  pageSize: pageSize.value,
  total: total.value,
  showTotal: true,
}));

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listMqMessages(buildMqMessageListQuery({
      pageNum: current.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      status: statusFilter.value,
      source: sourceFilter.value,
    }));
    rows.value = response.records.map(mapMqMessageRow);
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

function openDetail(record: MqMessageRow) {
  activeDetail.value = record;
  detailVisible.value = true;
}

async function handleRetry(record: MqMessageRow) {
  await systemApi.retryMqMessage(record.id);
  Message.success(`已提交重试：${record.eventId}`);
  fetchRows();
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

.primary-cell-sub,
.detail-label {
  margin-top: 4px;
  color: #74839a;
  font-size: 12px;
}

.timeline-cell,
.detail-content {
  display: grid;
  gap: 8px;
}

.detail-block {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}

.error-text {
  color: #4e5969;
}
</style>
