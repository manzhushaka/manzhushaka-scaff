<template>
  <section v-if="header.visible" class="page-header" aria-labelledby="page-header-title">
    <div class="page-header__content">
      <div class="page-header__eyebrow">{{ header.eyebrow }}</div>
      <h1 id="page-header-title" class="page-header__title">{{ header.title }}</h1>
      <p class="page-header__description">{{ header.description }}</p>
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
  margin: 18px 18px 0;
  overflow: hidden;
  border: 1px solid color-mix(in srgb, var(--ui-primary) 14%, var(--ui-border));
  border-radius: 14px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--ui-primary-soft) 68%, #ffffff 32%) 0%, #ffffff 84%),
    var(--ui-bg-panel);
  box-shadow: 0 8px 22px rgba(15, 59, 96, 0.05);
}

.page-header__content {
  position: relative;
  padding: 18px 24px 20px 28px;
}

.page-header__eyebrow {
  color: var(--ui-primary);
  font-size: 13px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 0;
  text-transform: uppercase;
}

.page-header__title {
  margin: 10px 0 0;
  color: var(--ui-text-primary);
  font-size: 24px;
  font-weight: 800;
  line-height: 1.3;
}

.page-header__description {
  max-width: 860px;
  margin: 10px 0 0;
  color: var(--ui-text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

@media screen and (max-width: 991px) {
  .page-header {
    margin: 12px 12px 0;
  }

  .page-header__content {
    padding: 16px 18px 18px 22px;
  }

  .page-header__title {
    font-size: 20px;
  }
}
</style>
