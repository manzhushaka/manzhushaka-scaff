<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-button v-permission="'system:monitor:refresh'" :loading="loading || slowSqlLoading" @click="refreshAll(true)">刷新慢 SQL</a-button>
        <a-button v-permission="'system:monitor:view'" @click="openRoute('/monitor/services')">服务监控</a-button>
        <a-tag bordered color="arcoblue">总览刷新 {{ lastUpdatedText }}</a-tag>
        <a-tag bordered :color="slowSqlTagColor">阈值 {{ monitor?.slowSql.thresholdMs ?? '--' }} ms</a-tag>
      </a-space>
    </PageHeaderCard>

    <a-spin :loading="loading" class="monitor-spin">
      <div class="monitor-grid">
        <a-card class="page-card section-card" :bordered="false">
          <div class="section-header">
            <div>
              <div class="section-title">慢 SQL 观察窗</div>
              <div class="section-description">单独聚焦数据库热点语句，判断接口变慢是否已经实打实落到 SQL 层。</div>
            </div>
          </div>

          <div class="ops-summary-grid ops-summary-grid--inline">
            <div class="mini-kpi">
              <div class="mini-kpi__label">最近采样</div>
              <div class="mini-kpi__value">{{ monitor?.slowSql.recentCount ?? 0 }}</div>
              <div class="mini-kpi__note">当前缓冲区记录的慢 SQL 数量。</div>
            </div>
            <div class="mini-kpi">
              <div class="mini-kpi__label">最新语句标识</div>
              <div class="mini-kpi__value code-text">{{ monitor?.slowSql.latestStatementId || '--' }}</div>
              <div class="mini-kpi__note">用于快速定位到对应 Mapper 或执行链路。</div>
            </div>
            <div class="mini-kpi">
              <div class="mini-kpi__label">最新耗时</div>
              <div class="mini-kpi__value">{{ formatCost(monitor?.slowSql.latestCostMs) }}</div>
              <div class="mini-kpi__note">和阈值一起看是否只是偶发抖动。</div>
            </div>
            <div class="mini-kpi">
              <div class="mini-kpi__label">最新执行时间</div>
              <div class="mini-kpi__value">{{ monitor?.slowSql.latestExecuteTime || '--' }}</div>
              <div class="mini-kpi__note">适合和日志、任务执行时间交叉对照。</div>
            </div>
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
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import {
  formatCost,
  slowSqlColumns,
  toSlowSqlColor,
  useMonitorSlowSqlPage,
} from './monitor-support';

const {
  loading,
  lastUpdatedText,
  monitor,
  openRoute,
  refreshAll,
  slowSqlLoading,
  slowSqlRows,
} = useMonitorSlowSqlPage();

const slowSqlTagColor = computed(() => ((monitor.value?.slowSql.recentCount ?? 0) > 0 ? 'gold' : 'green'));
</script>

<style scoped src="./monitor-theme.css"></style>
