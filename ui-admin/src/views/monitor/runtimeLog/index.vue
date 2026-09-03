<template>
  <div class="monitor-page">
    <a-alert v-if="errorMessage" type="error" show-icon>{{ errorMessage }}</a-alert>
    <a-card :bordered="false" class="log-card">
      <a-tabs v-model:active-key="level" @change="loadData">
        <a-tab-pane key="ALL" title="全部" />
        <a-tab-pane key="INFO" title="INFO" />
        <a-tab-pane key="WARN" title="WARN" />
        <a-tab-pane key="ERROR" title="ERROR" />
      </a-tabs>
      <a-form
        :model="query"
        layout="inline"
        class="filter-form"
        @submit-success="loadData"
      >
        <a-form-item field="keyword" label="关键字">
          <a-input
            v-model="query.keyword"
            class="filter-control"
            allow-clear
            placeholder="请输入关键字"
          />
        </a-form-item>
        <a-form-item field="lineCount" label="读取行数">
          <a-input-number
            v-model="query.lineCount"
            class="filter-control"
            :min="50"
            :max="5000"
            :step="100"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">
            <template #icon><icon-search /></template>
            查询
          </a-button>
        </a-form-item>
      </a-form>
      <a-table :data="logs" :loading="loading" :bordered="false" :pagination="false">
        <template #columns>
          <a-table-column title="行号" data-index="lineNumber" :width="90" />
          <a-table-column title="时间" data-index="time" :width="150" />
          <a-table-column title="级别" :width="90">
            <template #cell="{ record }">
              <a-tag :class="['status-tag', `status-tag--${levelColor(record.level)}`]">{{ record.level || '-' }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="内容" data-index="content" ellipsis tooltip />
          <a-table-column title="堆栈" align="center" :width="90">
            <template #cell="{ record }">
              <a-button
                type="text"
                class="table-action-button table-action-button--view"
                aria-label="查看详情"
                title="查看详情"
                :disabled="!record.stackTraceBlock"
                @click="showDetail(record)"
              >
                <template #icon><icon-eye /></template>
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
    <a-modal
      v-model:visible="detailVisible"
      title="运行日志详情"
      :width="860"
      :footer="false"
      render-to-body
    >
      <pre class="log-detail">{{ detailContent }}</pre>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { listRuntimeLogs } from '@/api/admin';

  const loading = ref(false);
  const fileName = ref('sys-error.log');
  const level = ref('ALL');
  const logs = ref<Record<string, any>[]>([]);
  const detailVisible = ref(false);
  const detailContent = ref('');
  const errorMessage = ref('');
  const query = ref({ keyword: '', lineCount: 500 });

  function getErrorMessage(error: unknown, fallback: string) {
    return error instanceof Error ? error.message : fallback;
  }

  function levelColor(value: string) {
    if (value === 'ERROR') return 'danger';
    if (value === 'WARN') return 'warning';
    return 'success';
  }

  /** 查询当前日志文件内容。 */
  async function loadData() {
    if (!fileName.value) {
      logs.value = [];
      return;
    }

    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await listRuntimeLogs({
        fileName: fileName.value,
        level: level.value === 'ALL' ? undefined : level.value,
        ...query.value,
      });
      logs.value = (response.data || []) as Record<string, any>[];
    } catch (error) {
      logs.value = [];
      errorMessage.value = getErrorMessage(error, '日志内容加载失败');
    } finally {
      loading.value = false;
    }
  }

  /** 打开单条日志的堆栈详情。 */
  function showDetail(record: Record<string, any>) {
    detailContent.value = [record.content, record.stackTraceBlock].filter(Boolean).join('\n');
    detailVisible.value = true;
  }

  onMounted(async () => {
    await loadData();
  });
</script>

<style scoped lang="less">
  .monitor-page {
    min-height: 100%;
    padding: 20px;
    background: var(--color-fill-2);
  }

  .log-card {
    border-radius: 6px;
  }

  .log-card :deep(.arco-card-body) {
    padding: 0;
  }

  :deep(.arco-alert) + .log-card {
    margin-top: 16px;
  }

  .filter-form {
    align-items: center;
    padding: 12px 16px 4px;
  }

  .filter-form :deep(.arco-form-item) {
    align-items: center;
    margin-bottom: 8px;
  }

  .filter-control {
    width: 200px;
  }

  .log-detail {
    max-height: 60vh;
    margin: 0;
    padding: 14px;
    overflow: auto;
    background: var(--color-fill-2);
    white-space: pre-wrap;
    word-break: break-word;
    line-height: 1.6;
  }

  @media (max-width: 760px) {
    .monitor-page {
      padding: 12px;
    }

    :deep(.arco-tabs-nav) {
      flex-wrap: wrap;
    }

    .filter-form :deep(.arco-form-item) {
      width: 100%;
      margin-right: 0;
    }

    .filter-form {
      padding: 12px 0 4px;
    }

    .filter-control {
      width: 100%;
    }
  }
</style>
