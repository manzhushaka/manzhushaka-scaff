<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="输入关键词、前缀或 Redis 匹配模式，例如 auth: 或 auth:*"
          style="width: 360px"
          @search="handleSearch"
        />
        <a-select
          v-model="limit"
          :options="cacheLimitOptions"
          style="width: 120px"
          @change="handleSearch"
        />
        <a-button v-permission="'system:cache:query'" :loading="loading" @click="fetchRows">查询</a-button>
      </a-space>
    </PageHeaderCard>

    <a-alert class="cache-hint" type="info" :show-icon="true">
      这是只读缓存排查页。建议输入关键词或前缀后再查，避免一次性扫描过多 Redis Key。
    </a-alert>

    <div class="page-card table-card">
      <a-table
        :data="rows"
        :loading="loading"
        row-key="key"
        :pagination="false"
        :columns="columns"
      >
        <template #keyCell="{ record }">
          <div class="primary-cell">
            <div class="primary-cell-title code-text">{{ record.key }}</div>
            <div class="primary-cell-sub">TTL：{{ record.ttlText }} / 过期：{{ record.expireAtText }}</div>
          </div>
        </template>
        <template #typeCell="{ record }">
          <a-tag :color="typeColorMap[record.type] ?? 'gray'">{{ record.type }}</a-tag>
        </template>
        <template #previewCell="{ record }">
          <span class="preview-text">{{ record.valuePreview }}</span>
        </template>
        <template #actionsCell="{ record }">
          <a-button size="mini" v-permission="'system:cache:detail'" @click="openDetail(record.key)">详情</a-button>
        </template>
      </a-table>
    </div>

    <a-modal v-model:visible="detailVisible" title="缓存详情" :footer="false" width="860px">
      <div v-if="activeDetail" class="detail-content">
        <a-descriptions :column="2" bordered layout="vertical">
          <a-descriptions-item label="Key">{{ activeDetail.key }}</a-descriptions-item>
          <a-descriptions-item label="类型">{{ activeDetail.type.toUpperCase() }}</a-descriptions-item>
          <a-descriptions-item label="TTL">{{ formatCacheTtl(activeDetail.ttlSeconds) }}</a-descriptions-item>
          <a-descriptions-item label="过期时间">{{ formatDateTime(activeDetail.expireAt) }}</a-descriptions-item>
          <a-descriptions-item :span="2" label="值预览">{{ activeDetail.valuePreview || '--' }}</a-descriptions-item>
        </a-descriptions>

        <div class="detail-block">
          <div class="detail-label">Value Payload</div>
          <a-textarea :model-value="detailValueText" readonly :auto-size="{ minRows: 10, maxRows: 18 }" />
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
import type { CacheEntryDetailVO, CacheEntryRow } from '@/types/system';
import {
  buildCacheEntryQuery,
  cacheLimitOptions,
  formatCacheTtl,
  mapCacheEntryRow,
  stringifyCacheValue,
} from './cache-support';
import { formatDateTime } from './shared';

const loading = ref(false);
const keyword = ref('auth:');
const limit = ref(20);
const rows = ref<CacheEntryRow[]>([]);
const detailVisible = ref(false);
const activeDetail = ref<CacheEntryDetailVO | null>(null);

const typeColorMap: Record<string, string> = {
  STRING: 'arcoblue',
  HASH: 'green',
  LIST: 'purple',
  SET: 'gold',
  ZSET: 'orangered',
};

const columns: TableColumnData[] = [
  { dataIndex: 'key', title: '缓存 Key', width: 320, slotName: 'keyCell' },
  { dataIndex: 'type', title: '类型', width: 110, slotName: 'typeCell' },
  { dataIndex: 'ttlText', title: 'TTL', width: 110 },
  { dataIndex: 'expireAtText', title: '过期时间', width: 180 },
  { dataIndex: 'valuePreview', title: '值预览', slotName: 'previewCell' },
  { dataIndex: 'actions', title: '操作', width: 100, slotName: 'actionsCell' },
];

const detailValueText = computed(() => stringifyCacheValue(activeDetail.value?.value));

async function fetchRows() {
  const normalizedKeyword = keyword.value.trim();
  if (!normalizedKeyword) {
    rows.value = [];
    Message.warning('请输入关键词、前缀或匹配模式后再查询');
    return;
  }
  loading.value = true;
  try {
    const response = await systemApi.listCacheEntries(buildCacheEntryQuery({
      keyword: normalizedKeyword,
      limit: limit.value,
    }));
    rows.value = response.map(mapCacheEntryRow);
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  fetchRows();
}

async function openDetail(key: string) {
  activeDetail.value = await systemApi.getCacheEntryDetail(key);
  detailVisible.value = true;
}

fetchRows();
</script>

<style scoped>
.system-page {
  display: grid;
  gap: 18px;
}

.cache-hint {
  margin-top: -2px;
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

.preview-text {
  color: #4e5969;
}

.detail-content {
  display: grid;
  gap: 12px;
}

.detail-block {
  display: grid;
  gap: 8px;
}
</style>
