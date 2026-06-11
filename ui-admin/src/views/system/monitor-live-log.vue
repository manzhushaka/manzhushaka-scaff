<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-button v-permission="'system:monitor:refresh'" :loading="loading || logLoading" @click="refreshAll(true)">刷新日志</a-button>
        <a-select v-model="logLineLimit" :options="logLineOptions" style="width: 140px" />
        <a-switch v-model="autoRefreshLogs" type="round">
          <template #checked>自动刷新</template>
          <template #unchecked>手动刷新</template>
        </a-switch>
        <a-button v-permission="'system:monitor:view'" @click="openRoute('/monitor/services')">服务监控</a-button>
        <a-tag bordered color="arcoblue">总览刷新 {{ lastUpdatedText }}</a-tag>
      </a-space>
    </PageHeaderCard>

    <a-spin :loading="loading" class="monitor-spin">
      <div class="monitor-grid">
        <a-card class="page-card section-card" :bordered="false">
          <div class="section-header">
            <div>
              <div class="section-title">在线日志</div>
              <div class="section-description">保留最近运行片段，适合联调、值班和刚发生故障时的即时回看。</div>
            </div>
          </div>

          <div class="ops-summary-grid ops-summary-grid--inline">
            <div class="mini-kpi">
              <div class="mini-kpi__label">缓冲条数</div>
              <div class="mini-kpi__value">{{ logTail?.lines.length ?? 0 }}</div>
              <div class="mini-kpi__note">当前 tail 返回的日志行数。</div>
            </div>
            <div class="mini-kpi">
              <div class="mini-kpi__label">缓冲容量</div>
              <div class="mini-kpi__value">{{ monitor?.logTail.capacity ?? '--' }}</div>
              <div class="mini-kpi__note">超过容量的旧日志会被滚动丢弃。</div>
            </div>
            <div class="mini-kpi">
              <div class="mini-kpi__label">生成时间</div>
              <div class="mini-kpi__value">{{ formatDateTime(logTail?.generatedAt) }}</div>
              <div class="mini-kpi__note">本次 tail 响应生成的时间点。</div>
            </div>
            <div class="mini-kpi">
              <div class="mini-kpi__label">最后一条</div>
              <div class="mini-kpi__value">{{ formatDateTime(logTail?.lastEntryAt) }}</div>
              <div class="mini-kpi__note">方便判断日志是否还在持续推进。</div>
            </div>
          </div>

          <div class="log-meta">
            <span>生成时间：{{ formatDateTime(logTail?.generatedAt) }}</span>
            <span>最后一条：{{ formatDateTime(logTail?.lastEntryAt) }}</span>
            <span>当前条数：{{ logTail?.lines.length ?? 0 }}</span>
          </div>

          <a-textarea
            class="log-console"
            :model-value="logTailText"
            readonly
            :auto-size="{ minRows: 18, maxRows: 26 }"
            placeholder="等待日志返回"
          />
        </a-card>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { formatDateTime } from './shared';
import {
  logLineOptions,
  useMonitorLiveLogPage,
} from './monitor-support';

const {
  loading,
  lastUpdatedText,
  monitor,
  openRoute,
  refreshAll,
  logLoading,
  logLineLimit,
  logTail,
  logTailText,
  fetchLogTail,
} = useMonitorLiveLogPage();

const autoRefreshLogs = ref(false);
let logTimer: ReturnType<typeof setInterval> | undefined;

/**
 * 启动在线日志自动刷新。
 *
 * @param 无入参
 * @returns 无返回值
 */
function startLogAutoRefresh() {
  stopLogAutoRefresh();
  if (!autoRefreshLogs.value) {
    return;
  }
  logTimer = setInterval(() => {
    void fetchLogTail();
  }, 5000);
}

/**
 * 停止在线日志自动刷新。
 *
 * @param 无入参
 * @returns 无返回值
 */
function stopLogAutoRefresh() {
  if (!logTimer) {
    return;
  }
  clearInterval(logTimer);
  logTimer = undefined;
}

watch(autoRefreshLogs, () => {
  startLogAutoRefresh();
});

watch(logLineLimit, () => {
  void fetchLogTail();
});

onBeforeUnmount(() => {
  stopLogAutoRefresh();
});
</script>

<style scoped src="./monitor-theme.css"></style>
