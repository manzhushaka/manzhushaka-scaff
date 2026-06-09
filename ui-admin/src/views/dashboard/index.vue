<template>
  <div class="dashboard-page">
    <div class="dashboard-overview">
      <section class="page-card identity-panel">
        <div class="identity-topbar">
          <div class="panel-kicker">当前身份</div>
          <a-space wrap size="small">
            <a-tag color="green">Vue 3</a-tag>
            <a-tag color="arcoblue">Pinia</a-tag>
            <a-tag color="orangered">Arco Design</a-tag>
          </a-space>
        </div>
        <h2>{{ authStore.profile?.nickname ?? '管理员' }}，欢迎回来</h2>
        <p>当前会话已经接入菜单、权限、日志和系统配置能力，可直接从左侧导航进入对应模块。</p>
        <div class="identity-grid">
          <div class="identity-card">
            <div class="identity-label">当前用户</div>
            <div class="identity-value">{{ authStore.profile?.nickname ?? '--' }}</div>
            <div class="identity-note">当前后台操作账号</div>
          </div>
          <div class="identity-card">
            <div class="identity-label">所属部门</div>
            <div class="identity-value">{{ authStore.profile?.deptName ?? '--' }}</div>
            <div class="identity-note">用于数据权限与归属展示</div>
          </div>
          <div class="identity-card">
            <div class="identity-label">角色编码</div>
            <div class="identity-value code-value">{{ authStore.profile?.roleCodes?.join(', ') ?? '--' }}</div>
            <div class="identity-note">决定可见菜单与可执行操作</div>
          </div>
        </div>
      </section>

      <section class="stats-panel">
        <div class="page-card matrix-card matrix-card-primary">
          <div class="matrix-label">菜单总数</div>
          <div class="matrix-value">{{ authStore.menus.length }}</div>
          <div class="matrix-hint">已挂载的功能入口</div>
        </div>
        <div class="page-card matrix-card matrix-card-muted">
          <div class="matrix-label">权限点</div>
          <div class="matrix-value">{{ authStore.permissions.length }}</div>
          <div class="matrix-hint">前端已识别权限编码</div>
        </div>
      </section>
    </div>

    <div class="page-card quick-entry-card">
      <div class="section-header">
        <div>
          <div class="section-title">常用入口</div>
          <div class="section-description">把高频系统维护页集中展示，减少来回寻找导航的成本。</div>
        </div>
      </div>
      <div class="quick-entry-grid">
        <div v-for="item in quickEntries" :key="item.title" class="quick-entry-item">
          <div class="quick-entry-title">{{ item.title }}</div>
          <div class="quick-entry-desc">{{ item.description }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const quickEntries = [
  { title: '用户管理', description: '维护账号、昵称、状态和所属部门。' },
  { title: '角色管理', description: '梳理角色编码与数据权限范围。' },
  { title: '菜单管理', description: '控制路由、组件映射和前端权限标识。' },
  { title: '日志管理', description: '查看登录结果与关键操作留痕。' },
];
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 18px;
}

.dashboard-overview {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(280px, 0.85fr);
  gap: 18px;
}

.panel-kicker {
  color: #7b89a2;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.identity-panel {
  padding: 24px;
}

.identity-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.identity-panel h2 {
  margin: 10px 0 0;
  color: #17233c;
  font-size: 30px;
  line-height: 1.18;
}

.identity-panel p {
  max-width: 620px;
  margin: 12px 0 0;
  color: #607089;
  line-height: 1.7;
}

.identity-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 20px;
}

.identity-card {
  padding: 14px 16px;
  background: rgba(247, 250, 254, 0.92);
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 16px;
}

.identity-label {
  color: #7b89a2;
  font-size: 12px;
}

.identity-value {
  margin-top: 6px;
  color: #1b2a45;
  font-size: 24px;
  font-weight: 700;
}

.identity-note {
  margin-top: 8px;
  color: #718099;
  font-size: 12px;
}

.stats-panel {
  display: grid;
  gap: 14px;
}

.matrix-card {
  padding: 20px;
  border-radius: 22px;
}

.matrix-card-primary {
  background: linear-gradient(180deg, #1f4fbe 0%, #173c8f 100%);
  color: #fff;
  box-shadow: 0 20px 38px rgba(28, 67, 153, 0.24);
}

.matrix-card-muted {
  background: linear-gradient(180deg, #f7f9fd 0%, #eef4fb 100%);
  color: #183153;
}

.matrix-label {
  font-size: 13px;
  opacity: 0.78;
}

.matrix-value {
  margin-top: 8px;
  font-size: 36px;
  font-weight: 700;
}

.matrix-hint {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.72;
}

.code-value {
  font-family: 'SFMono-Regular', 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 18px;
  line-height: 1.5;
}

.quick-entry-card {
  padding: 22px 24px;
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.section-description {
  margin-top: 6px;
  color: #6b7b93;
  line-height: 1.6;
}

.quick-entry-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.quick-entry-item {
  padding: 18px;
  background: rgba(248, 250, 254, 0.94);
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 18px;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.quick-entry-item:hover {
  transform: translateY(-2px);
  border-color: rgba(36, 91, 219, 0.16);
  box-shadow: 0 16px 28px rgba(15, 23, 42, 0.08);
}

.quick-entry-title {
  color: #17233c;
  font-weight: 700;
}

.quick-entry-desc {
  margin-top: 8px;
  color: #66768f;
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 1024px) {
  .dashboard-overview {
    grid-template-columns: 1fr;
  }

  .identity-topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .identity-grid,
  .quick-entry-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .identity-panel,
  .quick-entry-card {
    padding: 18px;
  }

  .identity-panel h2 {
    font-size: 24px;
  }

  .identity-grid,
  .quick-entry-grid {
    grid-template-columns: 1fr;
  }
}
</style>
