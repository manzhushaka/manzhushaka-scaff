<template>
  <div data-ui-theme="cool-tower">
    <router-view />
  </div>
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'

const settingsStore = useSettingsStore()

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(settingsStore.theme)
  })
})
</script>
