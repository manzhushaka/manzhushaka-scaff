<template>
  <div class="app-container log-center-page">
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
  gap: 0;
}

.log-center-card {
  overflow: hidden;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
  box-shadow: var(--ui-shadow-panel);
}

.log-center-tabs {
  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    background: var(--ui-bg-panel-muted);
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
    background: var(--ui-bg-panel);
  }

  :deep(.app-container) {
    min-height: 0;
    margin: 0;
    padding: 18px 20px 20px;
    border: 0;
    border-radius: 0;
    background: var(--ui-bg-panel);
    box-shadow: none;
  }
}

@media (max-width: 768px) {
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
