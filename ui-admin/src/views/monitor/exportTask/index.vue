<template>
  <div class="app-container ui-list-page task-page">
    <section class="ui-filter-card">
      <a-form :model="query" layout="inline" @submit-success="handleQuery">
        <a-form-item field="status" label="任务状态">
          <a-select v-model="query.status" allow-clear placeholder="全部状态">
            <a-option v-for="item in statusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="loading">
              <template #icon><icon-search /></template>查询
            </a-button>
            <a-button @click="resetQuery">
              <template #icon><icon-refresh /></template>重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </section>

    <section class="task-workspace">
      <div class="ui-action-bar">
        <a-space>
          <a-button
            v-hasPermi="['monitor:exporttask:submit']"
            type="primary"
            @click="openSubmit"
          >
            <template #icon><icon-download /></template>新建导出任务
          </a-button>
          <a-button :loading="loading" @click="loadData">
            <template #icon><icon-refresh /></template>刷新
          </a-button>
        </a-space>
        <a-tag color="arcoblue">共 {{ total }} 项</a-tag>
      </div>
      <div class="ui-table-card">
        <a-table
          :data="rows"
          :loading="loading"
          :bordered="false"
          :pagination="false"
          row-key="taskId"
          :scroll="{ x: 1120 }"
        >
          <template #columns>
            <a-table-column title="任务编号" data-index="taskId" :width="110" />
            <a-table-column title="文件名称" data-index="fileName" :width="210" ellipsis tooltip />
            <a-table-column title="状态" :width="120">
              <template #cell="{ record }">
                <a-tag :class="['status-tag', `status-tag--${statusColor(record.status)}`]">{{ statusLabel(record.status) }}</a-tag>
              </template>
            </a-table-column>
            <a-table-column title="处理进度" :width="210">
              <template #cell="{ record }">
                <div class="progress-cell">
                  <a-progress :percent="progress(record)" :show-text="false" />
                  <span>{{ record.processedCount || 0 }} / {{ record.totalCount || 0 }}</span>
                </div>
              </template>
            </a-table-column>
            <a-table-column title="提交时间" data-index="createTime" :width="180" />
            <a-table-column title="完成时间" data-index="finishedTime" :width="180" />
            <a-table-column title="操作" align="center" :width="160" fixed="right">
              <template #cell="{ record }">
                <a-space class="table-action-buttons">
                  <a-tooltip content="查看详情">
                    <a-button
                      v-hasPermi="['monitor:exporttask:query']"
                      type="text"
                      class="table-action-button"
                      aria-label="查看详情"
                      @click="showDetail(record.taskId)"
                    ><template #icon><icon-eye /></template></a-button>
                  </a-tooltip>
                  <a-tooltip content="下载文件">
                    <a-button
                      v-hasPermi="['monitor:exporttask:download']"
                      type="text"
                      class="table-action-button"
                      aria-label="下载文件"
                      :loading="downloadingId === record.taskId"
                      :disabled="!isDownloadable(record.status)"
                      @click="downloadTask(record)"
                    ><template #icon><icon-download /></template></a-button>
                  </a-tooltip>
                  <a-tooltip content="取消任务">
                    <a-button
                      v-hasPermi="['monitor:exporttask:cancel']"
                      type="text"
                      status="danger"
                      class="table-action-button"
                      aria-label="取消任务"
                      :disabled="!isCancellable(record.status)"
                      @click="cancelTask(record)"
                    ><template #icon><icon-close-circle /></template></a-button>
                  </a-tooltip>
                </a-space>
              </template>
            </a-table-column>
          </template>
        </a-table>
        <div class="task-pagination">
          <a-pagination
            v-model:current="page"
            v-model:page-size="pageSize"
            :total="total"
            show-total
            show-page-size
            @change="loadData"
            @page-size-change="handlePageSizeChange"
          />
        </div>
      </div>
    </section>

    <a-modal
      v-model:visible="submitVisible"
      title="新建用户导出任务"
      :width="680"
      :ok-loading="submitting"
      render-to-body
      @ok="submitTask"
    >
      <a-form class="modal-form" :model="exportQuery" :label-col-props="{ span: 6 }">
        <a-form-item field="userName" label="登录名称">
          <a-input v-model="exportQuery.userName" allow-clear placeholder="请输入登录名称" />
        </a-form-item>
        <a-form-item field="phonenumber" label="手机号码">
          <a-input v-model="exportQuery.phonenumber" allow-clear placeholder="请输入手机号码" />
        </a-form-item>
        <a-form-item field="status" label="账号状态">
          <a-select v-model="exportQuery.status" allow-clear placeholder="全部状态">
            <a-option value="0">正常</a-option>
            <a-option value="1">停用</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="deptId" label="部门编号">
          <a-input-number v-model="exportQuery.deptId" :min="1" placeholder="请输入部门编号" />
        </a-form-item>
        <a-form-item field="dateRange" label="创建时间">
          <a-range-picker v-model="exportQuery.dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:visible="detailVisible" title="导出任务详情" :footer="false" :width="640">
      <a-descriptions v-if="detail" :column="2" bordered>
        <a-descriptions-item label="任务编号">{{ detail.taskId }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusLabel(detail.status) }}</a-descriptions-item>
        <a-descriptions-item label="文件名称" :span="2">{{ detail.fileName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="总数量">{{ detail.totalCount || 0 }}</a-descriptions-item>
        <a-descriptions-item label="已处理">{{ detail.processedCount || 0 }}</a-descriptions-item>
        <a-descriptions-item label="成功数量">{{ detail.successCount || 0 }}</a-descriptions-item>
        <a-descriptions-item label="失败数量">{{ detail.failureCount || 0 }}</a-descriptions-item>
        <a-descriptions-item label="错误信息" :span="2">{{ detail.errorMessage || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
  import { onBeforeUnmount, onMounted, reactive, ref } from 'vue';
  import { Message, Modal } from '@arco-design/web-vue';
  import {
    cancelExportTask,
    downloadExportTask,
    getExportTask,
    listExportTasks,
    submitExportTask,
  } from '@/api/monitor/export-task';
  import type { TaskRecord } from '@/api/monitor/import-task';

  const statusOptions = [
    { value: 'PENDING', label: '等待执行' },
    { value: 'RUNNING', label: '执行中' },
    { value: 'CANCEL_REQUESTED', label: '取消中' },
    { value: 'CANCELLED', label: '已取消' },
    { value: 'SUCCESS', label: '成功' },
    { value: 'PARTIAL_SUCCESS', label: '部分成功' },
    { value: 'FAILED', label: '失败' },
  ];
  const query = reactive({ status: '' });
  const exportQuery = reactive<{
    userName: string;
    phonenumber: string;
    status: string;
    deptId?: number;
    dateRange: string[];
  }>({ userName: '', phonenumber: '', status: '', deptId: undefined, dateRange: [] });
  const rows = ref<TaskRecord[]>([]);
  const detail = ref<TaskRecord>();
  const page = ref(1);
  const pageSize = ref(10);
  const total = ref(0);
  const loading = ref(true);
  const submitting = ref(false);
  const submitVisible = ref(false);
  const detailVisible = ref(false);
  const downloadingId = ref<number>();
  let refreshTimer: number | undefined;

  function statusLabel(status: string) {
    return statusOptions.find((item) => item.value === status)?.label || status;
  }

  function statusColor(status: string) {
    if (status === 'SUCCESS') return 'success';
    if (status === 'PARTIAL_SUCCESS') return 'warning';
    if (status === 'FAILED' || status === 'CANCELLED') return 'danger';
    if (status === 'RUNNING' || status === 'CANCEL_REQUESTED') return 'info';
    return 'neutral';
  }

  function progress(record: TaskRecord) {
    return record.totalCount > 0 ? Math.min(record.processedCount / record.totalCount, 1) : 0;
  }

  function isCancellable(status: string) {
    return status === 'PENDING' || status === 'RUNNING';
  }

  function isDownloadable(status: string) {
    return status === 'SUCCESS' || status === 'PARTIAL_SUCCESS';
  }

  async function loadData() {
    loading.value = true;
    try {
      const response = await listExportTasks({
        ...query,
        pageNum: page.value,
        pageSize: pageSize.value,
      });
      rows.value = response.rows || [];
      total.value = response.total || 0;
    } finally {
      loading.value = false;
    }
  }

  function handleQuery() {
    page.value = 1;
    loadData();
  }

  function resetQuery() {
    query.status = '';
    handleQuery();
  }

  function handlePageSizeChange(value: number) {
    pageSize.value = value;
    page.value = 1;
    loadData();
  }

  function openSubmit() {
    Object.assign(exportQuery, {
      userName: '', phonenumber: '', status: '', deptId: undefined, dateRange: [],
    });
    submitVisible.value = true;
  }

  async function submitTask() {
    submitting.value = true;
    try {
      await submitExportTask({
        userName: exportQuery.userName || undefined,
        phonenumber: exportQuery.phonenumber || undefined,
        status: exportQuery.status || undefined,
        deptId: exportQuery.deptId,
        params: {
          beginTime: exportQuery.dateRange[0],
          endTime: exportQuery.dateRange[1],
        },
      });
      Message.success('导出任务已提交');
      submitVisible.value = false;
      page.value = 1;
      await loadData();
      return true;
    } finally {
      submitting.value = false;
    }
  }

  async function showDetail(taskId: number) {
    const response = await getExportTask(taskId);
    detail.value = response.data;
    detailVisible.value = true;
  }

  async function downloadTask(record: TaskRecord) {
    downloadingId.value = record.taskId;
    try {
      const response = await downloadExportTask(record.taskId);
      const url = URL.createObjectURL(new Blob([response as unknown as BlobPart]));
      const link = document.createElement('a');
      link.href = url;
      link.download = record.fileName || `export-${record.taskId}.xlsx`;
      link.click();
      URL.revokeObjectURL(url);
      Message.success('文件下载已开始');
    } finally {
      downloadingId.value = undefined;
    }
  }

  function cancelTask(record: TaskRecord) {
    Modal.confirm({
      title: '取消导出任务',
      content: `确认取消任务 ${record.taskId} 吗？`,
      onOk: async () => {
        await cancelExportTask(record.taskId);
        Message.success('取消请求已提交');
        await loadData();
      },
    });
  }

  onMounted(() => {
    loadData();
    refreshTimer = window.setInterval(() => {
      if (rows.value.some((item) => ['PENDING', 'RUNNING', 'CANCEL_REQUESTED'].includes(item.status))) {
        loadData();
      }
    }, 5000);
  });

  onBeforeUnmount(() => {
    if (refreshTimer) window.clearInterval(refreshTimer);
  });
</script>

<style scoped lang="less">
  .task-page { min-height: 100%; padding: 20px; background: var(--color-fill-2); }
  .ui-filter-card { margin-bottom: 16px; padding: 18px 20px 2px; border: 1px solid var(--color-border-2); border-radius: 6px; background: var(--color-bg-2); }
  .task-workspace { overflow: hidden; border: 1px solid var(--color-border-2); border-radius: 6px; background: var(--color-bg-2); }
  .ui-action-bar { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; border-bottom: 1px solid var(--color-border-2); }
  .ui-table-card { min-width: 0; }
  .progress-cell { display: grid; grid-template-columns: minmax(90px, 1fr) 72px; align-items: center; gap: 10px; }
  .task-pagination { display: flex; justify-content: flex-end; padding: 14px 20px; border-top: 1px solid var(--color-border-2); }
  @media (max-width: 640px) { .task-page { padding: 12px; } .ui-filter-card { padding: 14px 14px 2px; } .ui-filter-card :deep(.arco-form-item) { width: 100%; } .ui-action-bar { align-items: flex-start; gap: 12px; padding: 12px 14px; } .task-pagination { justify-content: center; padding: 12px; overflow-x: auto; } }
</style>
