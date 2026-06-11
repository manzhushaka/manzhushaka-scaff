<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-button v-permission="'system:monitor:refresh'" :loading="loading" @click="refreshMonitor(true)">刷新</a-button>
        <a-button v-permission="'system:monitor:view'" type="primary" @click="openRoute('/monitor/services')">
          服务监控
        </a-button>
        <a-tag bordered :color="hardwareHealth.color">{{ hardwareHealth.label }}</a-tag>
        <a-tag bordered color="arcoblue">最后更新 {{ lastUpdatedText }}</a-tag>
      </a-space>
    </PageHeaderCard>

    <a-spin :loading="loading" class="monitor-spin">
      <div class="monitor-grid">
        <a-card class="page-card hero-card hero-card--hardware" :bordered="false">
          <div class="hero-shell">
            <div class="hero-main">
              <div class="hero-eyebrow">Hardware Console</div>
              <div class="hero-heading">
                <div>
                  <div class="hero-title">硬件监控</div>
                  <div class="hero-description">
                    这一页只回答宿主机还有多少余量、是不是机器层面已经吃紧。先看资源，再决定是否需要进入服务监控页确认是我们自己的进程在拉高负载。
                  </div>
                </div>
                <a-tag bordered size="large" :color="hardwareHealth.color">
                  {{ hardwareHealth.label }}
                </a-tag>
              </div>

              <div class="hero-overview">
                <div v-for="item in hardwareOverviewItems" :key="item.label" class="overview-item">
                  <div class="overview-label">{{ item.label }}</div>
                  <div class="overview-value">{{ item.value }}</div>
                </div>
              </div>
            </div>

            <div class="hero-side">
              <div
                v-for="item in hardwareStatusItems"
                :key="item.label"
                class="status-pill"
                :class="`status-pill--${item.tone}`"
              >
                <div class="status-pill__label">{{ item.label }}</div>
                <div class="status-pill__value">{{ item.value }}</div>
                <div class="status-pill__detail">{{ item.detail }}</div>
              </div>
            </div>
          </div>
        </a-card>

        <a-card class="page-card section-card" :bordered="false">
          <div class="section-header">
            <div>
              <div class="section-title">进程与 JVM 视角</div>
              <div class="section-description">把当前应用对宿主机的资源占用也放在硬件页里看，方便区分“机器紧张”还是“我们的进程在吃资源”。</div>
            </div>
          </div>

          <div class="metric-grid metric-grid--inside">
            <a-card
              v-for="item in hardwareRuntimeCards"
              :key="item.key"
              class="page-card metric-card"
              :class="`metric-card--${item.tone}`"
              :bordered="false"
            >
              <div class="metric-head">
                <div class="metric-heading">
                  <div class="metric-icon" :class="`metric-icon--${item.tone}`">
                    <component :is="item.icon" />
                  </div>
                  <div>
                    <div class="metric-title">{{ item.title }}</div>
                    <div class="metric-subtitle">{{ item.subtitle }}</div>
                  </div>
                </div>
                <a-tag bordered size="small" :color="item.tagColor">{{ item.tagText }}</a-tag>
              </div>

              <div class="metric-value">{{ item.value }}</div>
              <a-progress :percent="item.percent" :status="item.progressStatus" :stroke-width="10" :show-text="false" />
              <div class="metric-note">{{ item.note }}</div>
            </a-card>
          </div>
        </a-card>

      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { useHardwareMonitorViewModel } from './monitor-support';

const {
  loading,
  lastUpdatedText,
  hardwareHealth,
  hardwareOverviewItems,
  hardwareStatusItems,
  hardwareRuntimeCards,
  refreshMonitor,
  openRoute,
} = useHardwareMonitorViewModel();
</script>

<style scoped src="./monitor-theme.css"></style>
