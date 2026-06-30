<template>
  <div class="app-container log-center-page">
    <section class="log-center-hero">
      <div>
        <p class="log-center-kicker">Log Center</p>
        <h2 class="log-center-title">统一日志</h2>
        <p class="log-center-subtitle">统一查看操作日志与登录日志，入口收敛后排查链路更直接。</p>
      </div>
    </section>

    <section class="log-center-card">
      <el-empty
        v-if="!canViewOperlog && !canViewLogininfor"
        description="当前账号暂未分配日志查看权限"
      />
      <el-tabs v-else v-model="activeTab" class="log-center-tabs">
        <el-tab-pane v-if="canViewOperlog" label="操作日志" name="operlog" lazy>
          <operlog-panel />
        </el-tab-pane>
        <el-tab-pane v-if="canViewLogininfor" label="登录日志" name="logininfor" lazy>
          <logininfor-panel />
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script setup name="LogCenter">
import { ref } from "vue"
import auth from "@/plugins/auth"
import OperlogPanel from "../operlog/index.vue"
import LogininforPanel from "../logininfor/index.vue"

const canViewOperlog = auth.hasPermi("monitor:operlog:list")
const canViewLogininfor = auth.hasPermi("monitor:logininfor:list")
const activeTab = ref(canViewOperlog ? "operlog" : "logininfor")
</script>

<style lang="scss" scoped>
.log-center-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.log-center-hero {
  padding: 20px 22px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(28, 126, 214, 0.12), rgba(28, 126, 214, 0.02) 52%),
    linear-gradient(180deg, #ffffff, #f8fbff);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.log-center-kicker {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #1c7ed6;
}

.log-center-title {
  margin: 0;
  font-size: 24px;
  line-height: 1.3;
  color: #1f2937;
}

.log-center-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  line-height: 1.7;
  color: #64748b;
}

.log-center-card {
  overflow: hidden;
  border: 1px solid var(--el-border-color-light);
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
}

.log-center-tabs {
  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    background: linear-gradient(180deg, #f8fbff, #ffffff);
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    height: 52px;
    font-size: 14px;
    font-weight: 600;
  }

  :deep(.el-tabs__content) {
    background: #ffffff;
  }

  :deep(.app-container) {
    padding: 18px 20px 20px;
  }
}

@media (max-width: 768px) {
  .log-center-hero {
    padding: 18px;
  }

  .log-center-title {
    font-size: 20px;
  }

  .log-center-tabs {
    :deep(.el-tabs__header) {
      padding: 0 14px;
    }

    :deep(.app-container) {
      padding: 14px;
    }
  }
}
</style>
