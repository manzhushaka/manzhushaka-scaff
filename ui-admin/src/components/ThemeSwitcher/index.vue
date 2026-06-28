<template>
  <el-tooltip content="主题切换" effect="dark" placement="bottom">
    <div class="theme-switcher">
      <button
        v-for="theme in themes"
        :key="theme.id"
        class="theme-swatch"
        :class="{ active: settingsStore.brandTheme === theme.id }"
        :aria-pressed="(settingsStore.brandTheme === theme.id).toString()"
        :style="{ background: theme.gradient }"
        @click="switchTheme(theme.id)"
      >
        <el-tooltip :content="theme.label" effect="dark" placement="top" :popper-options="{
          modifiers: [{ name: 'offset', options: { offset: [0, 4] } }]
        }">
          <span class="theme-swatch-inner"></span>
        </el-tooltip>
      </button>
    </div>
  </el-tooltip>
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'

const settingsStore = useSettingsStore()

const themes = [
  {
    id: 'cool-tower',
    label: '冷感控制塔',
    gradient: 'linear-gradient(135deg, #0f3b60 0%, #0ea5e9 62%, #e0f2fe 100%)'
  },
  {
    id: 'amber-command',
    label: '琥珀指挥舱',
    gradient: 'linear-gradient(135deg, #0e1218 0%, #ffb74d 100%)'
  },
  {
    id: 'gold-ledger',
    label: '米金账本台',
    gradient: 'linear-gradient(135deg, #fffdf8 0%, #c88b3a 100%)'
  }
]

function switchTheme(themeId) {
  settingsStore.setBrandTheme(themeId)
}
</script>

<style lang="scss" scoped>
.theme-switcher {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 4px;
  background: var(--ui-bg-panel, #ffffff);
  border: 1px solid var(--ui-border, #d8e6ef);
  border-radius: 8px;
  gap: 4px;
}

.theme-swatch {
  position: relative;
  width: 26px;
  height: 26px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: transform 160ms ease, box-shadow 160ms ease;
  flex-shrink: 0;
  outline: none;

  &:hover {
    transform: translateY(-1px);
  }

  &.active {
    box-shadow: 0 0 0 2px var(--ui-primary, #0ea5e9), inset 0 0 0 1px rgba(255, 255, 255, 0.5);
  }
}

.theme-swatch-inner {
  display: block;
  width: 100%;
  height: 100%;
  border-radius: inherit;
}
</style>