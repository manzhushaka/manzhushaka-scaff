<template>
  <div class="monitor-page">
    <div class="page-actions">
      <a-space>
        <a-select
          v-model="fileName"
          :loading="filesLoading"
          :style="{ width: '220px' }"
          @change="loadData"
        >
          <a-option
            v-for="file in files"
            :key="file.fileName"
            :value="file.fileName"
          >
            {{ file.fileName }}
          </a-option>
        </a-select>
        <a-button type="outline" :loading="loading" @click="loadData">
          <template #icon><icon-refresh /></template>
          刷新
        </a-button>
        <a-button type="primary" :loading="downloading" @click="download">
          <template #icon><icon-download /></template>
          下载
        </a-button>
      </a-space>
    </div>
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
          <a-input v-model="query.keyword" allow-clear placeholder="请输入关键字" />
        </a-form-item>
        <a-form-item field="lineCount" label="读取行数">
          <a-input-number v-model="query.lineCount" :min="50" :max="5000" :step="100" />
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
              <a-tag :color="levelColor(record.level)">{{ record.level || '-' }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="内容" data-index="content" ellipsis tooltip />
          <a-table-column title="堆栈" :width="90">
            <template #cell="{ record }">
              <a-button
                type="text"
                :disabled="!record.stackTraceBlock"
                @click="showDetail(record)"
              >
                <template #icon><icon-eye /></template>
                查看
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
  import { Message } from '@arco-design/web-vue';
  import { downloadRuntimeLog, listRuntimeLogFiles, listRuntimeLogs } from '@/api/admin';

  interface RuntimeLogFile {
    fileName: string;
    fileSize: number;
    updateTime: string;
  }

  const loading = ref(false);
  const filesLoading = ref(false);
  const downloading = ref(false);
  const files = ref<RuntimeLogFile[]>([]);
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
    if (value === 'ERROR') return 'red';
    if (value === 'WARN') return 'orange';
    return 'green';
  }

  /** 加载 Java 返回的可用日志文件。 */
  async function loadFiles() {
    filesLoading.value = true;
    errorMessage.value = '';
    try {
      const response = await listRuntimeLogFiles();
      files.value = (response.data || []) as RuntimeLogFile[];
      if (files.value.length && !files.value.some((file) => file.fileName === fileName.value)) {
        fileName.value = files.value[0].fileName;
      }
    } catch (error) {
      files.value = [];
      fileName.value = '';
      errorMessage.value = getErrorMessage(error, '日志文件列表加载失败');
    } finally {
      filesLoading.value = false;
    }
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

  /** 下载当前日志文件。 */
  async function download() {
    if (!fileName.value) return;

    downloading.value = true;
    try {
      const response: any = await downloadRuntimeLog(fileName.value);
      const blob = new Blob([response]);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName.value;
      link.click();
      URL.revokeObjectURL(url);
      Message.success('日志下载已开始');
    } catch (error) {
      errorMessage.value = getErrorMessage(error, '日志下载失败');
    } finally {
      downloading.value = false;
    }
  }

  /** 打开单条日志的堆栈详情。 */
  function showDetail(record: Record<string, any>) {
    detailContent.value = [record.content, record.stackTraceBlock].filter(Boolean).join('\n');
    detailVisible.value = true;
  }

  onMounted(async () => {
    await loadFiles();
    await loadData();
  });
</script>

<style scoped lang="less">
  .monitor-page {
    min-height: 100%;
    padding: 20px;
    background: var(--color-fill-2);
  }

  .page-actions {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 16px;
  }

  .log-card {
    margin-top: 16px;
    border-radius: 6px;
  }

  .filter-form {
    padding: 18px 0 2px;
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

    .page-actions .arco-space {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-end;
    }

    .filter-form :deep(.arco-form-item) {
      width: 100%;
    }
  }
</style>
