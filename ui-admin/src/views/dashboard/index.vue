<template>
  <div>
    <PageHeaderCard title="工作台" description="骨架版首页展示当前登录态、菜单能力和后续接线入口。">
      <a-space>
        <a-tag color="green">Vue 3</a-tag>
        <a-tag color="arcoblue">Pinia</a-tag>
        <a-tag color="orangered">Arco Design</a-tag>
      </a-space>
    </PageHeaderCard>

    <a-row :gutter="16">
      <a-col :span="8">
        <div class="page-card stat-card">
          <div class="stat-label">当前用户</div>
          <div class="stat-value">{{ authStore.profile?.nickname }}</div>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="page-card stat-card">
          <div class="stat-label">部门</div>
          <div class="stat-value">{{ authStore.profile?.deptName }}</div>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="page-card stat-card">
          <div class="stat-label">菜单数量</div>
          <div class="stat-value">{{ authStore.menus.length }}</div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/store/auth';
import PageHeaderCard from '@/components/PageHeaderCard.vue';

const authStore = useAuthStore();
</script>

<style scoped>
.stat-card {
  padding: 24px;
}

.stat-label {
  color: #6b7280;
}

.stat-value {
  margin-top: 12px;
  font-size: 28px;
  font-weight: 700;
}
</style>
