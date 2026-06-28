<template>
  <div :data-ui-theme="computedTheme">
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'

const route = useRoute()
const settingsStore = useSettingsStore()

// 登录页固定使用 cool-tower，不跟随用户切换后的 brandTheme
const computedTheme = computed(() => {
  if (route.path === '/login') {
    return 'cool-tower'
  }
  return settingsStore.brandTheme
})

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(settingsStore.theme)
  })
})
</script>
