<template>
  <a-card v-if="shouldRender" class="page-card page-header-card" :bordered="false">
    <div class="page-header-card__inner" :class="{ 'page-header-card__inner--toolbar': useToolbarLayout }">
      <div v-if="showHeading" class="header-content">
        <div v-if="eyebrow" class="eyebrow">{{ eyebrow }}</div>
        <div v-if="title" class="title-row">
          <div class="title">{{ title }}</div>
        </div>
        <div v-if="description" class="description">{{ description }}</div>
      </div>
      <div v-if="hasActions" class="header-actions" :class="{ 'header-actions--stretch': !showHeading }">
        <slot />
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue';

const props = withDefaults(
  defineProps<{
    title?: string;
    description?: string;
    eyebrow?: string;
    mode?: 'header' | 'toolbar';
  }>(),
  {
    eyebrow: '',
    mode: 'header',
  },
);

const slots = useSlots();
const hasActions = computed(() => Boolean(slots.default));

// Intro header blocks are disabled globally; keep the API for backward compatibility.
const showHeading = computed(() => false);
const useToolbarLayout = computed(() => props.mode === 'toolbar' || !showHeading.value);
const shouldRender = computed(() => showHeading.value || hasActions.value);
</script>

<style scoped>
.page-header-card {
  margin-bottom: 18px;
}

.page-header-card :deep(.arco-card-body) {
  padding: 0;
}

.page-header-card__inner {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 24px;
}

.page-header-card__inner--toolbar {
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
}

.header-content {
  min-width: 0;
}

.eyebrow {
  color: #7888a0;
  font-size: 12px;
  font-weight: 600;
}

.title-row {
  display: flex;
  align-items: center;
  margin-top: 4px;
}

.title {
  color: #162033;
  font-size: 22px;
  font-weight: 700;
}

.description {
  max-width: 720px;
  margin-top: 8px;
  color: #627188;
  font-size: 14px;
  line-height: 1.6;
}

.header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-height: 52px;
}

.page-header-card__inner--toolbar .header-actions,
.header-actions--stretch {
  width: 100%;
  min-height: 0;
  justify-content: flex-start;
}

.page-header-card__inner--toolbar :deep(.arco-space) {
  row-gap: 12px;
}

@media (max-width: 768px) {
  .page-header-card__inner {
    flex-direction: column;
    padding: 18px;
  }

  .title {
    font-size: 20px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
