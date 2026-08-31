<template>
  <a-config-provider :locale="zhCN" size="medium" global>
    <div class="app-theme-root" :data-ui-theme="settingsStore.uiTheme">
      <router-view />
    </div>
  </a-config-provider>
</template>

<script setup>
import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn'
import useSettingsStore from '@/store/modules/settings'
import { applyUiTheme } from '@/utils/uiTheme'

const settingsStore = useSettingsStore()

watch(() => settingsStore.uiTheme, uiTheme => applyUiTheme(uiTheme), { immediate: true })
</script>

<style scoped>
.app-theme-root {
  min-height: 100%;
}
</style>
