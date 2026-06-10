<template>
  <div class="system-page">
    <div class="page-card config-card">
      <div class="config-header">
        <div>
          <div class="section-title">平台展示配置</div>
          <div class="config-description">维护左侧导航顶部展示的系统名称、副标题和 Logo，保存后会同步到当前管理台。</div>
        </div>
        <a-space wrap class="config-actions">
          <a-button :loading="loading" @click="fetchPlatformConfig(true)">重新加载</a-button>
          <a-button
            type="primary"
            v-permission="'system:config:update'"
            :loading="saving"
            :disabled="loading"
            @click="savePlatformConfig"
          >
            保存配置
          </a-button>
        </a-space>
      </div>

      <a-form :model="form" layout="vertical" class="config-form">
        <a-form-item
          field="platformName"
          label="系统名称"
          extra="用于左侧导航顶部展示；留空时会恢复默认名称。"
        >
          <a-input v-model="form.platformName" allow-clear placeholder="请输入系统名称，例如：满朱砂管理后台" />
        </a-form-item>
        <a-form-item
          field="platformSubtitle"
          label="系统副标题"
          extra="展示在系统名称下方；留空时会恢复默认副标题。"
        >
          <a-input v-model="form.platformSubtitle" allow-clear placeholder="请输入系统副标题，例如：PLATFORM CONSOLE" />
        </a-form-item>
        <a-form-item
          field="logoUrl"
          label="Logo 图片地址"
          extra="支持填写可访问的图片 URL；留空时使用系统默认标识。"
        >
          <a-input v-model="form.logoUrl" allow-clear placeholder="请输入 Logo 图片 URL" />
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import { systemApi } from '@/api/system';
import { normalizePlatformConfig } from '@/platform-config';
import { usePlatformStore } from '@/store/platform';

const loading = ref(false);
const saving = ref(false);
const platformStore = usePlatformStore();
const form = reactive({
  platformName: '',
  platformSubtitle: '',
  logoUrl: '',
});

async function fetchPlatformConfig(force = false) {
  loading.value = true;
  try {
    const config = normalizePlatformConfig(await systemApi.getPlatformConfig());
    form.platformName = config.platformName;
    form.platformSubtitle = config.platformSubtitle;
    form.logoUrl = config.logoUrl;
    if (force) {
      Message.success('平台配置已重新加载');
    }
    platformStore.applyPlatformConfig(config);
  } finally {
    loading.value = false;
  }
}

async function savePlatformConfig() {
  saving.value = true;
  try {
    const payload = normalizePlatformConfig(form);
    await systemApi.updatePlatformConfig(payload);
    platformStore.applyPlatformConfig(payload);
    form.platformName = payload.platformName;
    form.platformSubtitle = payload.platformSubtitle;
    form.logoUrl = payload.logoUrl;
    Message.success('平台配置已更新');
  } finally {
    saving.value = false;
  }
}

void fetchPlatformConfig();
</script>

<style scoped>
.system-page {
  display: grid;
  gap: 18px;
}

.config-card {
  display: grid;
  gap: 20px;
  padding: 24px;
}

.config-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.config-description {
  margin-top: 6px;
  color: var(--text-muted);
  line-height: 1.6;
}

.config-form {
  max-width: 760px;
}

.config-form :deep(.arco-form-item) {
  margin-bottom: 20px;
}

.config-form :deep(.arco-form-item-extra) {
  margin-top: 8px;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .config-card {
    padding: 18px;
  }

  .config-header {
    flex-direction: column;
  }
}
</style>
