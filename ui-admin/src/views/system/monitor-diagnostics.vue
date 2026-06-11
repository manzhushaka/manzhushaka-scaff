<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-button v-permission="'system:monitor:refresh'" :loading="loading" @click="refreshAll(true)">刷新服务态</a-button>
        <a-button v-permission="'system:monitor:view'" @click="openRoute('/monitor/hardware')">硬件监控</a-button>
        <a-tag bordered :color="serviceHealth.color">{{ serviceHealth.label }}</a-tag>
        <a-tag bordered color="arcoblue">总览刷新 {{ lastUpdatedText }}</a-tag>
      </a-space>
    </PageHeaderCard>

    <a-spin :loading="loading" class="monitor-spin">
      <div class="monitor-grid">
        <a-card class="page-card service-command" :bordered="false">
          <div class="service-command__intro">
            <div class="hero-eyebrow">Service Watch</div>
            <div class="service-command__headline">
              <div>
                <div class="service-command__title-row">
                  <div class="hero-title">{{ monitor?.applicationName || '服务监控' }}</div>
                  <a-tag bordered size="large" :color="serviceHealth.color">
                    {{ serviceHealth.label }}
                  </a-tag>
                </div>
                <div class="hero-description">
                  这一页只盯服务侧信号：Redis、任务、消息、慢 SQL 和在线日志。进程 CPU、JVM 堆和线程规模已经回收到硬件监控里，避免两页重复。
                </div>
              </div>
            </div>

            <div class="service-chip-grid">
              <div v-for="item in serviceOverviewItems" :key="item.label" class="service-chip">
                <div class="service-chip__label">{{ item.label }}</div>
                <div class="service-chip__value">{{ item.value }}</div>
              </div>
            </div>
          </div>

          <div class="service-command__rail">
            <div
              v-for="item in serviceStatusItems"
              :key="item.label"
              class="status-pill"
              :class="`status-pill--${item.tone}`"
            >
              <div class="status-pill__label">{{ item.label }}</div>
              <div class="status-pill__value">{{ item.value }}</div>
              <div class="status-pill__detail">{{ item.detail }}</div>
            </div>
          </div>
        </a-card>

        <div class="overview-domain-grid overview-domain-grid--triple">
          <a-card
            v-for="item in serviceDomainCards"
            :key="item.key"
            class="page-card domain-card"
            :class="`domain-card--${item.tone}`"
            :bordered="false"
          >
            <div class="domain-card__header">
              <div class="domain-card__icon" :class="`domain-card__icon--${item.tone}`">
                <component :is="item.icon" />
              </div>
              <div>
                <div class="metric-title">{{ item.title }}</div>
                <div class="domain-card__description">{{ item.description }}</div>
              </div>
            </div>

            <div class="domain-card__label">{{ item.label }}</div>
            <div class="domain-card__value">{{ item.value }}</div>
            <div class="domain-card__note">{{ item.note }}</div>

            <div class="domain-card__stats">
              <div v-for="stat in item.stats" :key="stat.label" class="mini-kpi">
                <div class="mini-kpi__label">{{ stat.label }}</div>
                <div class="mini-kpi__value">{{ stat.value }}</div>
                <div class="mini-kpi__note">{{ stat.note }}</div>
              </div>
            </div>

            <a-button v-permission="item.permission" :type="item.tone === 'danger' ? 'primary' : 'outline'" @click="openRoute(item.path)">
              {{ item.actionLabel }}
            </a-button>
          </a-card>
        </div>

        <div class="diagnostic-grid">
          <a-card class="page-card section-card" :bordered="false">
            <div class="section-header">
              <div>
                <div class="section-title">慢 SQL 观察窗</div>
                <div class="section-description">服务页直接承接数据库热点信号，便于判断接口变慢是不是已经落到 SQL 层。</div>
              </div>
              <a-space wrap>
                <a-tag bordered color="arcoblue">阈值 {{ monitor?.slowSql.thresholdMs ?? '--' }} ms</a-tag>
                <a-button v-permission="'system:monitor:refresh'" size="small" :loading="slowSqlLoading" @click="fetchSlowSql(true)">
                  刷新慢 SQL
                </a-button>
              </a-space>
            </div>

            <a-table
              class="ops-table"
              :data="slowSqlRows"
              :loading="slowSqlLoading"
              :pagination="false"
              :columns="slowSqlColumns"
              row-key="statementId"
            >
              <template #costCell="{ record }">
                <a-tag :color="toSlowSqlColor(record.costMs)">{{ formatCost(record.costMs) }}</a-tag>
              </template>
            </a-table>
          </a-card>

          <div class="tab-grid">
            <a-card class="page-card section-card" :bordered="false">
              <div class="section-header">
                <div>
                  <div class="section-title">在线日志</div>
                  <div class="section-description">保留最近运行片段，更适合联调和刚发生故障时的即时回看。</div>
                </div>
                <a-space wrap>
                  <a-select v-model="logLineLimit" :options="logLineOptions" style="width: 140px" />
                  <a-switch v-model="autoRefreshLogs" type="round">
                    <template #checked>自动刷新</template>
                    <template #unchecked>手动刷新</template>
                  </a-switch>
                  <a-button v-permission="'system:monitor:refresh'" size="small" :loading="logLoading" @click="fetchLogTail(true)">
                    刷新日志
                  </a-button>
                </a-space>
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

            <a-card class="page-card section-card" :bordered="false">
              <div class="section-header">
                <div>
                  <div class="section-title">服务联动</div>
                  <div class="section-description">把诊断信号和后续处理入口放在一起，减少排查到一半再翻页找功能。</div>
                </div>
              </div>

              <div class="ops-summary-grid--inline">
                <div v-for="item in serviceActionStats" :key="item.label" class="mini-kpi">
                  <div class="mini-kpi__label">{{ item.label }}</div>
                  <div class="mini-kpi__value">{{ item.value }}</div>
                  <div class="mini-kpi__note">{{ item.hint }}</div>
                </div>
              </div>

              <a-space wrap>
                <a-button v-permission="'system:job:query'" @click="openRoute('/system/jobs')">去任务页</a-button>
                <a-button v-permission="'system:mq-message:query'" @click="openRoute('/logs/mq-messages')">去消息台账</a-button>
                <a-button v-permission="'system:cache:query'" @click="openRoute('/system/cache')">去缓存页</a-button>
              </a-space>
            </a-card>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue';
import { Message } from '@arco-design/web-vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { formatDateTime } from './shared';
import {
  formatCost,
  logLineOptions,
  slowSqlColumns,
  toSlowSqlColor,
  useMonitorDiagnostics,
  useServiceMonitorViewModel,
} from './monitor-support';

const {
  loading,
  lastUpdatedText,
  monitor,
  serviceHealth,
  serviceOverviewItems,
  serviceStatusItems,
  serviceDomainCards,
  serviceActionStats,
  refreshMonitor,
  openRoute,
} = useServiceMonitorViewModel();

const {
  slowSqlLoading,
  logLoading,
  logLineLimit,
  slowSqlRows,
  logTail,
  logTailText,
  fetchSlowSql,
  fetchLogTail,
} = useMonitorDiagnostics();

const autoRefreshLogs = ref(false);
let logTimer: ReturnType<typeof setInterval> | undefined;

/**
 * 同步刷新服务监控页依赖的总览、慢 SQL 和在线日志。
 *
 * @param showSuccess 是否显示成功提示
 * @returns 全部刷新完成后的 Promise
 */
async function refreshAll(showSuccess = false) {
  await Promise.all([
    refreshMonitor(false),
    fetchSlowSql(false),
    fetchLogTail(false),
  ]);
  if (showSuccess) {
    Message.success('服务监控数据已刷新');
  }
}

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

void fetchSlowSql();
void fetchLogTail();
</script>

<style scoped src="./monitor-theme.css"></style>
