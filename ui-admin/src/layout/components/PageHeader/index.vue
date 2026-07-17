<template>
  <section v-if="header.visible" class="page-header" aria-labelledby="page-header-title">
    <div class="page-header__content">
      <div class="page-header__copy">
        <div class="page-header__eyebrow">{{ header.eyebrow }}</div>
        <h1 id="page-header-title" class="page-header__title">{{ header.title }}</h1>
        <p class="page-header__description">{{ header.description }}</p>
      </div>
      <div class="page-header__visual" aria-hidden="true">
        <span class="page-header__visual-ring"></span>
        <el-icon><Tools /></el-icon>
      </div>
    </div>
  </section>
</template>

<script setup>
import { buildPageHeader } from '@/utils/pageHeader'

const route = useRoute()
const header = computed(() => buildPageHeader(route))
</script>

<style lang="scss" scoped>
.page-header {
  position: relative;
  margin: var(--ui-layout-content-padding) var(--ui-layout-content-padding) 0;
  overflow: hidden;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
  box-shadow: var(--ui-shadow-panel);
}

.page-header__content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 128px;
  padding: 24px 32px;
  gap: 32px;
}

.page-header__copy {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.page-header__eyebrow {
  color: var(--ui-primary-active);
  font-family: "IBM Plex Mono", "SFMono-Regular", Consolas, monospace;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: 0;
  text-transform: uppercase;
}

.page-header__title {
  margin: 6px 0 0;
  color: var(--ui-text-primary);
  font-size: 22px;
  font-weight: 750;
  line-height: 1.3;
}

.page-header__description {
  max-width: 860px;
  margin: 6px 0 0;
  color: var(--ui-text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

.page-header__visual {
  position: relative;
  display: grid;
  flex: 0 0 148px;
  width: 148px;
  height: 88px;
  place-items: center;
  color: var(--ui-primary);
  background: var(--ui-primary-soft);
  border: 1px solid color-mix(in srgb, var(--ui-primary) 18%, transparent);
  border-radius: var(--ui-radius-panel);
  transform: rotate(-4deg);
}

.page-header__visual::before,
.page-header__visual::after {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  border: 1px solid color-mix(in srgb, var(--ui-primary) 26%, transparent);
  background: var(--ui-bg-panel);
  transform: rotate(18deg);
}

.page-header__visual::before {
  top: -10px;
  left: 14px;
}

.page-header__visual::after {
  right: 18px;
  bottom: -8px;
}

.page-header__visual .el-icon {
  position: relative;
  z-index: 1;
  font-size: 42px;
}

.page-header__visual-ring {
  position: absolute;
  width: 68px;
  height: 68px;
  border: 1px solid color-mix(in srgb, var(--ui-primary) 22%, transparent);
  border-radius: 50%;
}

@media screen and (max-width: 991px) {
  .page-header {
    margin: 12px 12px 0;
  }

  .page-header__content {
    min-height: 112px;
    padding: 18px 20px;
  }

  .page-header__title {
    font-size: 20px;
  }

  .page-header__visual {
    flex-basis: 96px;
    width: 96px;
    height: 72px;
  }
}

@media screen and (max-width: 640px) {
  .page-header__content {
    min-height: 0;
  }

  .page-header__visual {
    display: none;
  }
}
</style>
