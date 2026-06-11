<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-button v-permission="'system:monitor:refresh'" :loading="loading" @click="refreshMonitor(true)">刷新服务态</a-button>
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
                  这一页只盯服务侧信号：Redis、任务和消息链路，方便值班时先做一轮应用侧健康判断。
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
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { useServiceMonitorViewModel } from './monitor-support';

const {
  loading,
  lastUpdatedText,
  monitor,
  serviceHealth,
  serviceOverviewItems,
  serviceStatusItems,
  serviceDomainCards,
  refreshMonitor,
  openRoute,
} = useServiceMonitorViewModel();
</script>

<style scoped src="./monitor-theme.css"></style>
