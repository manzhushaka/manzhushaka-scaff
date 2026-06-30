<template>
  <section v-if="header.visible" class="page-header" aria-labelledby="page-header-title">
    <div class="page-header__eyebrow">{{ header.eyebrow }}</div>
    <h1 id="page-header-title" class="page-header__title">{{ header.title }}</h1>
    <p class="page-header__description">{{ header.description }}</p>
  </section>
</template>

<script setup>
import { buildPageHeader } from '@/utils/pageHeader'

const route = useRoute()
const header = computed(() => buildPageHeader(route))
</script>

<style lang="scss" scoped>
.page-header {
  margin: 18px 18px 0;
  padding: 18px 24px 20px;
  border: 1px solid color-mix(in srgb, var(--ui-primary) 16%, var(--ui-border));
  border-radius: var(--ui-radius-panel);
  background:
    linear-gradient(115deg, color-mix(in srgb, var(--ui-primary-soft) 72%, #ffffff 28%) 0%, #ffffff 82%),
    var(--ui-bg-panel);
  box-shadow: 0 10px 28px rgba(15, 59, 96, 0.06);
}

.page-header__eyebrow {
  color: var(--ui-primary);
  font-size: 12px;
  font-weight: 800;
  line-height: 1.25;
  letter-spacing: 0;
}

.page-header__title {
  margin: 10px 0 0;
  color: var(--ui-text-primary);
  font-size: 22px;
  font-weight: 800;
  line-height: 1.35;
}

.page-header__description {
  max-width: 860px;
  margin: 10px 0 0;
  color: var(--ui-text-secondary);
  font-size: 14px;
  line-height: 1.65;
}

@media screen and (max-width: 991px) {
  .page-header {
    margin: 12px 12px 0;
    padding: 16px 18px 18px;
  }

  .page-header__title {
    font-size: 20px;
  }
}
</style>
