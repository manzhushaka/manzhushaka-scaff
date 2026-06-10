<template>
  <div class="dashboard-page">
    <a-row :gutter="[18, 18]">
      <a-col :xs="24" :lg="16">
        <a-card class="page-card dashboard-card" :bordered="false">
          <template #title>
            <div class="dashboard-card-title">欢迎回来</div>
          </template>
          <template #extra>
            <a-space wrap size="small">
              <a-tag color="green">Vue 3</a-tag>
              <a-tag color="arcoblue">Pinia</a-tag>
              <a-tag color="orangered">Arco Design</a-tag>
            </a-space>
          </template>

          <div class="dashboard-kicker">当前身份</div>
          <div class="dashboard-intro">
            {{ authStore.profile?.nickname ?? '管理员' }}，当前会话已经接入菜单、权限、日志和系统配置能力。
          </div>

          <a-descriptions class="identity-descriptions" :column="3" layout="vertical" bordered>
            <a-descriptions-item label="当前用户">
              {{ authStore.profile?.nickname ?? '--' }}
            </a-descriptions-item>
            <a-descriptions-item label="所属部门">
              {{ authStore.profile?.deptName ?? '--' }}
            </a-descriptions-item>
            <a-descriptions-item label="角色编码">
              <span class="code-text">{{ authStore.profile?.roleCodes?.join(', ') ?? '--' }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <div class="stats-panel">
          <a-card class="page-card statistic-card" :bordered="false">
            <a-statistic title="菜单总数" :value="authStore.menus.length" />
            <div class="statistic-note">已挂载的功能入口</div>
          </a-card>
          <a-card class="page-card statistic-card" :bordered="false">
            <a-statistic title="权限点" :value="authStore.permissions.length" />
            <div class="statistic-note">前端已识别权限编码</div>
          </a-card>
        </div>
      </a-col>
    </a-row>

    <a-card class="page-card quick-entry-card" :bordered="false" title="常用入口">
      <div class="quick-entry-description">把高频系统维护页集中展示，减少来回寻找导航的成本。</div>
      <a-list class="quick-entry-list" :bordered="false" size="large">
        <a-list-item v-for="item in quickEntries" :key="item.title">
          <div class="quick-entry-item">
            <div>
              <div class="quick-entry-title">{{ item.title }}</div>
              <div class="quick-entry-desc">{{ item.description }}</div>
            </div>
            <a-tag bordered>{{ item.category }}</a-tag>
          </div>
        </a-list-item>
      </a-list>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const quickEntries = [
  { title: '用户管理', description: '维护账号、昵称、状态和所属部门。', category: '账号' },
  { title: '角色管理', description: '梳理角色编码与数据权限范围。', category: '权限' },
  { title: '菜单管理', description: '控制路由、组件映射和前端权限标识。', category: '导航' },
  { title: '日志管理', description: '查看登录结果与关键操作留痕。', category: '审计' },
];
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 18px;
}

.dashboard-card-title {
  color: #17233c;
  font-weight: 700;
}

.dashboard-kicker {
  color: #7b89a2;
  font-size: 12px;
  font-weight: 600;
}

.dashboard-intro {
  margin-top: 8px;
  color: #627188;
  font-size: 14px;
  line-height: 1.7;
}

.identity-descriptions {
  margin-top: 18px;
}

.identity-descriptions :deep(.arco-descriptions-item-value) {
  color: #17233c;
  font-weight: 600;
}

.stats-panel {
  display: grid;
  gap: 18px;
}

.statistic-card :deep(.arco-statistic) {
  display: grid;
  gap: 6px;
}

.statistic-card :deep(.arco-statistic-title) {
  color: #75839a;
  font-size: 12px;
}

.statistic-card :deep(.arco-statistic-content-value) {
  color: #17233c;
  font-size: 34px;
  font-weight: 700;
}

.statistic-note {
  margin-top: 10px;
  color: #75839a;
  font-size: 12px;
}

.quick-entry-description {
  margin-bottom: 12px;
  color: #6b7b93;
  font-size: 14px;
  line-height: 1.6;
}

.quick-entry-list :deep(.arco-list-item) {
  padding-left: 0;
  padding-right: 0;
}

.quick-entry-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
}

.quick-entry-title {
  color: #17233c;
  font-weight: 700;
}

.quick-entry-desc {
  margin-top: 6px;
  color: #66768f;
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .quick-entry-item {
    flex-direction: column;
  }
}
</style>
