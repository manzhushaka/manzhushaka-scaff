<template>
  <div class="system-page">
    <PageHeaderCard
      title="定时任务"
      description="基于 Quartz 管理平台定时任务。支持任务注册、启停、手动执行，并可查看每次执行的详细日志。"
    />

    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索任务名称或处理器"
          style="width: 280px"
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
        <a-select
          v-model="runStatusFilter"
          :options="runStatusFilterOptions"
          allow-clear
          placeholder="全部结果"
          style="width: 140px"
          @change="handleSearch"
        />
        <a-button v-permission="'system:job:query'" @click="fetchRows">刷新</a-button>
        <a-button type="primary" v-permission="'system:job:add'" @click="openCreate">新增任务</a-button>
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
        <template #jobCell="{ record }">
          <div class="primary-cell">
            <div class="primary-cell-title">{{ record.jobName }}</div>
            <div class="primary-cell-sub">{{ record.remark }}</div>
          </div>
        </template>
        <template #statusCell="{ record }">
          <a-tag :color="record.statusValue === 1 ? 'green' : 'red'">{{ record.statusText }}</a-tag>
        </template>
        <template #runStatusCell="{ record }">
          <a-tag :color="runStatusColorMap[record.lastRunStatusValue] ?? 'gray'">{{ record.lastRunStatusText }}</a-tag>
        </template>
        <template #cronCell="{ record }">
          <span class="code-text">{{ record.cronExpression }}</span>
        </template>
        <template #actionsCell="{ record }">
          <a-space wrap>
            <a-button size="mini" v-permission="'system:job:update'" @click="openEdit(record.id)">编辑</a-button>
            <a-popconfirm
              :content="record.statusValue === 1 ? '确认暂停该任务吗？' : '确认恢复该任务吗？'"
              @ok="handleToggleStatus(record.id, record.statusValue)"
            >
              <a-button
                size="mini"
                :status="record.statusValue === 1 ? 'warning' : 'normal'"
                v-permission="record.statusValue === 1 ? 'system:job:pause' : 'system:job:resume'"
              >
                {{ record.statusValue === 1 ? '暂停' : '恢复' }}
              </a-button>
            </a-popconfirm>
            <a-popconfirm content="确认立即执行一次该任务吗？" @ok="handleTrigger(record.id)">
              <a-button size="mini" v-permission="'system:job:trigger'">执行一次</a-button>
            </a-popconfirm>
            <a-button size="mini" v-permission="'system:job:log'" @click="openLogs(record.id, record.jobName)">日志</a-button>
            <a-popconfirm content="确认删除该任务吗？" @ok="handleDelete(record.id)">
              <a-button size="mini" status="danger" v-permission="'system:job:delete'">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </div>

    <a-modal v-model:visible="visible" :title="editingId ? '编辑任务' : '新增任务'" @before-ok="submitForm">
      <a-form :model="form" layout="vertical">
        <a-form-item field="jobName" label="任务名称">
          <a-input v-model="form.jobName" placeholder="请输入任务名称" />
        </a-form-item>
        <a-form-item field="handlerName" label="处理器">
          <a-select v-model="form.handlerName" :options="handlerOptions" placeholder="请选择任务处理器" />
        </a-form-item>
        <a-form-item field="cronExpression" label="Cron 表达式">
          <a-input v-model="form.cronExpression" placeholder="例如：0 0/5 * * * ?" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item field="jobParam" label="任务参数">
          <a-textarea v-model="form.jobParam" :auto-size="{ minRows: 3, maxRows: 5 }" placeholder="可选，支持 JSON 或普通文本" />
        </a-form-item>
        <a-form-item field="remark" label="备注">
          <a-textarea v-model="form.remark" :auto-size="{ minRows: 2, maxRows: 4 }" placeholder="可选，填写任务说明" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer v-model:visible="logDrawerVisible" :width="1080" :title="`执行日志 - ${activeLogJobName}`" unmount-on-close>
      <div class="log-drawer-content">
        <div class="page-card log-toolbar-card">
          <a-space wrap>
            <a-select
              v-model="logRunStatusFilter"
              :options="runStatusFilterOptions"
              allow-clear
              placeholder="全部结果"
              style="width: 140px"
              @change="handleLogSearch"
            />
            <a-select
              v-model="logTriggerTypeFilter"
              :options="triggerTypeFilterOptions"
              allow-clear
              placeholder="全部触发方式"
              style="width: 160px"
              @change="handleLogSearch"
            />
            <a-button @click="fetchLogs">刷新日志</a-button>
          </a-space>
        </div>

        <div class="page-card table-card">
          <a-table
            :data="logRows"
            :loading="logLoading"
            row-key="id"
            :pagination="logPagination"
            :columns="logColumns"
            @page-change="handleLogPageChange"
          >
            <template #runStatusCell="{ record }">
              <a-tag :color="runStatusColorMap[record.runStatusValue] ?? 'gray'">{{ record.runStatusText }}</a-tag>
            </template>
            <template #triggerTypeCell="{ record }">
              <a-tag>{{ record.triggerTypeText }}</a-tag>
            </template>
            <template #logActionsCell="{ record }">
              <a-button size="mini" @click="viewLogDetail(record.id)">查看详情</a-button>
            </template>
          </a-table>
        </div>

        <div class="page-card log-detail-card">
          <div class="log-detail-header">
            <div>
              <div class="section-title">日志详情</div>
              <div class="detail-tip">{{ activeLogMeta }}</div>
            </div>
          </div>
          <a-textarea
            :model-value="activeLogContent"
            readonly
            :auto-size="{ minRows: 18, maxRows: 28 }"
            placeholder="请选择一条执行记录查看日志详情"
          />
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { Message, type TableColumnData } from '@arco-design/web-vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import type {
  PlatformJobForm,
  PlatformJobLogDetailVO,
  PlatformJobLogRow,
  PlatformJobRow,
  SelectOption,
} from '@/types/system';
import {
  mapPlatformJobLogRow,
  mapPlatformJobRow,
  platformJobRunStatusOptions,
  platformJobTriggerTypeOptions,
  statusOptions,
} from './shared';

const loading = ref(false);
const visible = ref(false);
const keyword = ref('');
const statusFilter = ref<number | undefined>();
const runStatusFilter = ref<string | undefined>();
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const editingId = ref<number | null>(null);
const rows = ref<PlatformJobRow[]>([]);
const handlerOptions = ref<SelectOption[]>([]);

const logDrawerVisible = ref(false);
const logLoading = ref(false);
const logCurrent = ref(1);
const logPageSize = ref(8);
const logTotal = ref(0);
const logRows = ref<PlatformJobLogRow[]>([]);
const activeLogJobId = ref<number | null>(null);
const activeLogJobName = ref('');
const activeLogDetail = ref<PlatformJobLogDetailVO | null>(null);
const logRunStatusFilter = ref<string | undefined>();
const logTriggerTypeFilter = ref<string | undefined>();

const form = reactive<Required<PlatformJobForm>>({
  jobName: '',
  handlerName: '',
  cronExpression: '',
  status: 1,
  jobParam: '',
  remark: '',
});

const runStatusColorMap: Record<string, string> = {
  SUCCESS: 'green',
  FAIL: 'red',
  RUNNING: 'arcoblue',
  SKIPPED: 'gold',
};

const statusFilterOptions = [
  { label: '全部状态', value: undefined },
  ...statusOptions,
];

const runStatusFilterOptions = [
  { label: '全部结果', value: undefined },
  ...platformJobRunStatusOptions,
];

const triggerTypeFilterOptions = [
  { label: '全部触发方式', value: undefined },
  ...platformJobTriggerTypeOptions,
];

const columns: TableColumnData[] = [
  { dataIndex: 'jobName', title: '任务', width: 220, slotName: 'jobCell' },
  { dataIndex: 'handlerLabel', title: '处理器', width: 160 },
  { dataIndex: 'cronExpression', title: 'Cron', width: 170, slotName: 'cronCell' },
  { dataIndex: 'statusText', title: '状态', width: 110, slotName: 'statusCell' },
  { dataIndex: 'lastRunStatusText', title: '最近结果', width: 120, slotName: 'runStatusCell' },
  { dataIndex: 'lastTriggerTimeText', title: '上次执行', width: 180 },
  { dataIndex: 'nextTriggerTimeText', title: '下次执行', width: 180 },
  { dataIndex: 'createTimeText', title: '创建时间', width: 180 },
  { dataIndex: 'actions', title: '操作', width: 400, slotName: 'actionsCell' },
];

const logColumns: TableColumnData[] = [
  { dataIndex: 'triggerTypeText', title: '触发方式', width: 120, slotName: 'triggerTypeCell' },
  { dataIndex: 'runStatusText', title: '结果', width: 120, slotName: 'runStatusCell' },
  { dataIndex: 'costMsText', title: '耗时', width: 110 },
  { dataIndex: 'executorHost', title: '执行节点', width: 150 },
  { dataIndex: 'errorMsg', title: '错误摘要' },
  { dataIndex: 'startTimeText', title: '开始时间', width: 180 },
  { dataIndex: 'endTimeText', title: '结束时间', width: 180 },
  { dataIndex: 'actions', title: '操作', width: 120, slotName: 'logActionsCell' },
];

const pagination = computed(() => ({
  current: current.value,
  pageSize: pageSize.value,
  total: total.value,
  showTotal: true,
}));

const logPagination = computed(() => ({
  current: logCurrent.value,
  pageSize: logPageSize.value,
  total: logTotal.value,
  showTotal: true,
}));

const activeLogContent = computed(() => activeLogDetail.value?.logContent ?? '');
const activeLogMeta = computed(() => {
  if (!activeLogDetail.value) {
    return '请选择一条执行记录查看详细日志';
  }
  return `${activeLogDetail.value.runStatus} / ${activeLogDetail.value.triggerType} / ${activeLogDetail.value.startTime ?? '--'}`;
});

function resetForm() {
  form.jobName = '';
  form.handlerName = handlerOptions.value[0]?.value ? String(handlerOptions.value[0].value) : '';
  form.cronExpression = '0 0/5 * * * ?';
  form.status = 1;
  form.jobParam = '';
  form.remark = '';
  editingId.value = null;
}

async function fetchHandlerOptions() {
  handlerOptions.value = await systemApi.listPlatformJobHandlerOptions();
  if (!form.handlerName && handlerOptions.value.length > 0) {
    form.handlerName = String(handlerOptions.value[0].value);
  }
}

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listPlatformJobs({
      pageNum: current.value,
      pageSize: pageSize.value,
      jobName: keyword.value || undefined,
      handlerName: keyword.value || undefined,
      status: statusFilter.value,
      lastRunStatus: runStatusFilter.value,
    });
    rows.value = response.records.map(mapPlatformJobRow);
    total.value = response.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  current.value = 1;
  void fetchRows();
}

function handlePageChange(page: number) {
  current.value = page;
  void fetchRows();
}

function openCreate() {
  resetForm();
  visible.value = true;
}

async function openEdit(id: number) {
  const detail = await systemApi.getPlatformJob(id);
  editingId.value = id;
  form.jobName = detail.jobName;
  form.handlerName = detail.handlerName;
  form.cronExpression = detail.cronExpression;
  form.status = detail.status ?? 1;
  form.jobParam = detail.jobParam ?? '';
  form.remark = detail.remark ?? '';
  visible.value = true;
}

async function submitForm() {
  if (!form.jobName.trim() || !form.handlerName.trim() || !form.cronExpression.trim()) {
    Message.warning('任务名称、处理器和 Cron 表达式不能为空');
    return false;
  }

  const payload: PlatformJobForm = {
    jobName: form.jobName.trim(),
    handlerName: form.handlerName.trim(),
    cronExpression: form.cronExpression.trim(),
    status: form.status,
    jobParam: form.jobParam.trim() || undefined,
    remark: form.remark.trim() || undefined,
  };

  if (editingId.value) {
    await systemApi.updatePlatformJob(editingId.value, payload);
    Message.success('任务已更新');
  } else {
    await systemApi.createPlatformJob(payload);
    Message.success('任务已创建');
  }

  visible.value = false;
  resetForm();
  await fetchRows();
  return true;
}

async function handleToggleStatus(id: number, status: number) {
  if (status === 1) {
    await systemApi.pausePlatformJob(id);
    Message.success('任务已暂停');
  } else {
    await systemApi.resumePlatformJob(id);
    Message.success('任务已恢复');
  }
  await fetchRows();
}

async function handleTrigger(id: number) {
  await systemApi.triggerPlatformJob(id);
  Message.success('任务已触发');
  await fetchRows();
}

async function handleDelete(id: number) {
  await systemApi.deletePlatformJob(id);
  Message.success('任务已删除');
  if (rows.value.length === 1 && current.value > 1) {
    current.value -= 1;
  }
  await fetchRows();
}

function openLogs(jobId: number, jobName: string) {
  activeLogJobId.value = jobId;
  activeLogJobName.value = jobName;
  activeLogDetail.value = null;
  logRunStatusFilter.value = undefined;
  logTriggerTypeFilter.value = undefined;
  logCurrent.value = 1;
  logDrawerVisible.value = true;
  void fetchLogs();
}

async function fetchLogs() {
  if (activeLogJobId.value == null) {
    return;
  }
  logLoading.value = true;
  try {
    const response = await systemApi.listPlatformJobLogs(activeLogJobId.value, {
      pageNum: logCurrent.value,
      pageSize: logPageSize.value,
      runStatus: logRunStatusFilter.value,
      triggerType: logTriggerTypeFilter.value,
    });
    logRows.value = response.records.map(mapPlatformJobLogRow);
    logTotal.value = response.total;
    if (!response.records.length) {
      activeLogDetail.value = null;
      return;
    }
    const firstLogId = response.records[0].id;
    if (!activeLogDetail.value || !response.records.some((item) => item.id === activeLogDetail.value?.id)) {
      await viewLogDetail(firstLogId);
    }
  } finally {
    logLoading.value = false;
  }
}

function handleLogSearch() {
  logCurrent.value = 1;
  void fetchLogs();
}

function handleLogPageChange(page: number) {
  logCurrent.value = page;
  void fetchLogs();
}

async function viewLogDetail(logId: number) {
  activeLogDetail.value = await systemApi.getPlatformJobLogDetail(logId);
}

Promise.all([fetchHandlerOptions(), fetchRows()]);
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

.log-drawer-content {
  display: grid;
  gap: 18px;
}

.log-toolbar-card {
  padding-bottom: 0;
}

.log-detail-header {
  margin-bottom: 12px;
}

.section-title {
  color: #17233c;
  font-weight: 700;
}

.detail-tip {
  margin-top: 4px;
  color: #74839a;
  font-size: 12px;
}
</style>
