<template>
  <div class="log-center-page">
    <a-empty
      v-if="!canViewOperlog && !canViewLogininfor"
      class="log-center-empty"
      description="当前账号暂未分配日志查看权限"
    />
    <a-tabs v-else v-model:active-key="activeTab" class="log-center-tabs" lazy-load>
      <a-tab-pane v-if="canViewOperlog" title="操作日志" key="operlog">
        <operlog-panel />
      </a-tab-pane>
      <a-tab-pane v-if="canViewLogininfor" title="登录日志" key="logininfor">
        <logininfor-panel />
      </a-tab-pane>
    </a-tabs>
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
  min-height: 0;
  background: var(--ui-bg-content);
}

.log-center-tabs {
  :deep(.arco-tabs-nav) {
    margin: 0;
    padding: 0 var(--ui-layout-content-padding);
    border-bottom: 1px solid var(--ui-divider);
    background: var(--ui-bg-panel);
  }

  :deep(.arco-tabs-nav::before) {
    display: none;
  }

  :deep(.arco-tabs-tab) {
    height: 52px;
    font-size: 14px;
    font-weight: 600;
  }

  :deep(.arco-tabs-content) {
    background: transparent;
  }

  :deep(.app-container) {
    min-height: 0;
    margin: var(--ui-layout-content-padding);
    padding: 0;
    background: transparent;
  }
}

.log-center-empty {
  margin: var(--ui-layout-content-padding);
  padding: 40px 20px;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
}

@media (max-width: 768px) {
  .log-center-tabs {
    :deep(.arco-tabs-nav) {
      padding: 0 12px;
    }

    :deep(.app-container) {
      margin: 12px;
    }
  }

  .log-center-empty {
    margin: 12px;
  }
}
</style>
