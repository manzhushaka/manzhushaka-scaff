<template>
  <a-drawer
    v-model:visible="showSettings"
    :footer="false"
    :width="340"
    title="界面设置"
    unmount-on-close
  >
    <section class="setting-section" aria-labelledby="theme-setting-title">
      <div id="theme-setting-title" class="setting-section__title">主题色</div>
      <div class="theme-options" role="radiogroup" aria-label="主题色">
        <button
          v-for="themeOption in UI_THEME_OPTIONS"
          :key="themeOption.value"
          type="button"
          :class="['theme-option', { 'is-active': settingsStore.uiTheme === themeOption.value }]"
          role="radio"
          :aria-checked="settingsStore.uiTheme === themeOption.value"
          @click="settingsStore.setUiTheme(themeOption.value)"
        >
          <span class="theme-option__preview" :style="{ '--theme-preview-color': themeOption.color }">
            <span class="theme-option__sidebar" />
            <span class="theme-option__signal" />
          </span>
          <span>{{ themeOption.label }}</span>
          <icon-check v-if="settingsStore.uiTheme === themeOption.value" class="theme-option__check" />
        </button>
      </div>
    </section>

    <a-divider />

    <section class="setting-section" aria-labelledby="layout-setting-title">
      <div id="layout-setting-title" class="setting-section__title">页面布局</div>

      <div class="drawer-item">
        <span>开启页签</span>
        <a-switch v-model="settingsStore.tagsView" size="small" />
      </div>

      <div class="drawer-item">
        <span>持久化标签页</span>
        <a-switch
          v-model="settingsStore.tagsViewPersist"
          :disabled="!settingsStore.tagsView"
          size="small"
          @change="tagsViewPersistChange"
        />
      </div>

      <div class="drawer-item">
        <span>显示页签图标</span>
        <a-switch v-model="settingsStore.tagsIcon" :disabled="!settingsStore.tagsView" size="small" />
      </div>

      <div class="drawer-item drawer-item--stack">
        <span>标签页样式</span>
        <a-radio-group v-model="settingsStore.tagsViewStyle" type="button" size="small" :disabled="!settingsStore.tagsView">
          <a-radio value="card">下划线</a-radio>
          <a-radio value="chrome">浏览器</a-radio>
        </a-radio-group>
      </div>

      <div class="drawer-item">
        <span>固定 Header</span>
        <a-switch v-model="settingsStore.fixedHeader" size="small" />
      </div>

      <div class="drawer-item">
        <span>显示 Logo</span>
        <a-switch v-model="settingsStore.sidebarLogo" size="small" />
      </div>

      <div class="drawer-item">
        <span>动态标题</span>
        <a-switch v-model="settingsStore.dynamicTitle" size="small" @change="dynamicTitleChange" />
      </div>

      <div class="drawer-item">
        <span>底部版权</span>
        <a-switch v-model="settingsStore.footerVisible" size="small" />
      </div>
    </section>

    <a-divider />

    <a-space fill>
      <a-button type="primary" long @click="saveSetting">
        <template #icon><icon-save /></template>
        保存配置
      </a-button>
      <a-button long @click="resetSetting">
        <template #icon><icon-refresh /></template>
        重置
      </a-button>
    </a-space>
  </a-drawer>
</template>

<script setup>
import { IconCheck, IconRefresh, IconSave } from '@arco-design/web-vue/es/icon'
import useSettingsStore from '@/store/modules/settings'
import { UI_THEME_OPTIONS } from '@/utils/uiTheme'

const { proxy } = getCurrentInstance()
const settingsStore = useSettingsStore()
const showSettings = ref(false)
const tagsViewPersist = ref(settingsStore.tagsViewPersist)

/**
 * 同步动态标题设置。
 */
function dynamicTitleChange() {
  settingsStore.setTitle(settingsStore.title)
}

/**
 * 同步标签页持久化开关。
 *
 * @param {boolean} value 是否持久化
 */
function tagsViewPersistChange(value) {
  settingsStore.tagsViewPersist = value
  tagsViewPersist.value = value
}

/**
 * 保存当前界面设置。
 */
function saveSetting() {
  proxy.$modal.loading('正在保存到本地，请稍候...')
  if (!tagsViewPersist.value) {
    proxy.$cache.local.remove('tags-view-visited')
  }
  const layoutSetting = {
    uiTheme: settingsStore.uiTheme,
    tagsView: settingsStore.tagsView,
    tagsIcon: settingsStore.tagsIcon,
    tagsViewStyle: settingsStore.tagsViewStyle,
    tagsViewPersist: settingsStore.tagsViewPersist,
    fixedHeader: settingsStore.fixedHeader,
    sidebarLogo: settingsStore.sidebarLogo,
    dynamicTitle: settingsStore.dynamicTitle,
    footerVisible: settingsStore.footerVisible
  }
  localStorage.setItem('layout-setting', JSON.stringify(layoutSetting))
  setTimeout(() => {
    proxy.$modal.closeLoading()
    proxy.$modal.msgSuccess('界面设置已保存')
  }, 400)
}

/**
 * 清除界面设置并刷新页面。
 */
function resetSetting() {
  proxy.$cache.local.remove('tags-view-visited')
  proxy.$modal.loading('正在重置界面设置...')
  localStorage.removeItem('layout-setting')
  setTimeout(() => window.location.reload(), 500)
}

/**
 * 打开界面设置抽屉。
 */
function openSetting() {
  showSettings.value = true
}

defineExpose({ openSetting })
</script>

<style lang="scss" scoped>
.setting-section__title {
  margin-bottom: 12px;
  color: var(--ui-text-primary);
  font-size: 13px;
  font-weight: 600;
}

.theme-options {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.theme-option {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
  min-width: 0;
  padding: 8px;
  color: var(--ui-text-secondary);
  font: inherit;
  text-align: left;
  border: 1px solid var(--ui-border);
  border-radius: 6px;
  background: var(--ui-bg-panel);
  cursor: pointer;
  transition: border-color var(--ui-transition-fast), box-shadow var(--ui-transition-fast);

  &:hover,
  &.is-active {
    color: var(--ui-text-primary);
    border-color: var(--ui-primary);
  }

  &.is-active {
    box-shadow: var(--ui-focus-ring);
  }
}

.theme-option__preview {
  position: relative;
  display: block;
  height: 52px;
  overflow: hidden;
  border: 1px solid var(--ui-border-subtle);
  border-radius: 4px;
  background: #ffffff;
}

.theme-option__sidebar {
  display: block;
  width: 26%;
  height: 100%;
  background: #1b1820;
}

.theme-option__signal {
  position: absolute;
  top: 9px;
  right: 8px;
  width: 54%;
  height: 8px;
  border-radius: 2px;
  background: var(--theme-preview-color);
  box-shadow: 0 14px 0 color-mix(in srgb, var(--theme-preview-color) 18%, #ffffff);
}

.theme-option__check {
  position: absolute;
  right: 8px;
  bottom: 10px;
  color: var(--ui-primary);
}

.drawer-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 42px;
  color: var(--ui-text-regular);
  font-size: 14px;
  border-bottom: 1px solid var(--ui-border-subtle);

  &:last-child {
    border-bottom: 0;
  }
}

.drawer-item--stack {
  align-items: flex-start;
  flex-direction: column;
  padding: 10px 0;
}

@media (max-width: 390px) {
  .theme-options {
    grid-template-columns: 1fr;
  }
}
</style>
