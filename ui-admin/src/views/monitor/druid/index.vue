<template>
  <div class="druid-page">
    <div class="druid-toolbar">
      <a-space>
        <a-tag color="arcoblue"><icon-bar-chart /> Druid 数据监控</a-tag>
        <a-typography-text type="secondary">由 Java 端 Druid StatView 提供</a-typography-text>
      </a-space>
      <a-button type="outline" :loading="checking" @click="reload">
        <template #icon><icon-refresh /></template>
        重新加载
      </a-button>
    </div>
    <div class="druid-content">
      <a-spin :loading="checking" style="width: 100%; height: 100%">
        <iframe
          v-if="status === 'available'"
          :key="frameKey"
          class="druid-frame"
          :src="url"
          title="Druid 数据监控"
          @load="loaded = true"
          @error="loaded = false"
        />
        <a-empty
          v-else-if="status === 'unavailable'"
          description="Java 服务未启用 Druid 监控"
        />
      </a-spin>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';

  type DruidStatus = 'checking' | 'available' | 'unavailable';

  const frameKey = ref(0);
  const loaded = ref(false);
  const checking = ref(true);
  const status = ref<DruidStatus>('checking');
  const url = computed(() => {
    const baseUrl = import.meta.env.DEV ? '' : import.meta.env.VITE_API_BASE_URL || '';
    return `${baseUrl}/druid/login.html`;
  });

  /** 探测 Java Druid StatView 是否已启用。 */
  async function checkService() {
    checking.value = true;
    loaded.value = false;
    if (!import.meta.env.DEV) {
      status.value = 'available';
      checking.value = false;
      return;
    }

    try {
      const response = await fetch(url.value, { credentials: 'include' });
      const contentType = response.headers.get('content-type') || '';
      status.value = response.ok && contentType.includes('text/html') ? 'available' : 'unavailable';
    } catch {
      status.value = 'unavailable';
    } finally {
      checking.value = false;
    }
  }

  /** 重新探测并刷新 Druid 页面。 */
  function reload() {
    frameKey.value += 1;
    checkService();
  }

  onMounted(checkService);
</script>

<style scoped lang="less">
  .druid-page {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 100px);
    padding: 20px;
    background: var(--ui-bg-content);
  }

  .druid-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  .druid-content {
    display: flex;
    flex: 1;
    min-height: 520px;
    align-items: center;
    justify-content: center;
    background: var(--color-bg-2);
    border: 1px solid var(--ui-border);
  }

  .druid-frame {
    display: block;
    width: 100%;
    height: 100%;
    min-height: 520px;
    border: 0;
  }

  @media (max-width: 640px) {
    .druid-page {
      height: calc(100vh - 84px);
      padding: 12px;
    }

    .druid-toolbar {
      align-items: flex-start;
      gap: 8px;
    }

    .druid-toolbar :deep(.arco-space) {
      flex-wrap: wrap;
    }

    .druid-content,
    .druid-frame {
      min-height: 460px;
    }
  }
</style>
