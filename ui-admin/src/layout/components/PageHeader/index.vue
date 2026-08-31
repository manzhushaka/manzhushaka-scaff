<template>
  <section v-if="header.visible" class="page-header" aria-labelledby="page-header-title">
    <div class="page-header__content">
      <div class="page-header__copy">
        <div class="page-header__eyebrow">{{ header.eyebrow }}</div>
        <h1 id="page-header-title" class="page-header__title">{{ header.title }}</h1>
        <p class="page-header__description">{{ header.description }}</p>
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
}

.page-header__content {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 74px;
  padding: 8px 0 12px;
}

.page-header__copy {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.page-header__eyebrow {
  color: var(--ui-primary-active);
  font-family: var(--ui-font-family-mono);
  font-size: 11px;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: 0;
  text-transform: uppercase;
}

.page-header__title {
  margin: 6px 0 0;
  color: var(--ui-text-primary);
  font-family: var(--ui-font-family);
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

@media screen and (max-width: 991px) {
  .page-header {
    margin: 12px 12px 0;
  }

  .page-header__content {
    min-height: 68px;
    padding: 6px 0 10px;
  }

  .page-header__title {
    font-size: 20px;
  }

}
</style>
