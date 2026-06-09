<template>
  <div class="page-card header-card" :class="`header-card--${mode}`">
    <div v-if="showHeading" class="header-content">
      <div v-if="eyebrow" class="eyebrow">{{ eyebrow }}</div>
      <div v-if="title" class="title-row">
        <div class="title">{{ title }}</div>
        <div class="title-accent"></div>
      </div>
      <div v-if="description" class="description">{{ description }}</div>
    </div>
    <div class="header-actions" :class="{ 'header-actions--stretch': !showHeading }">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    title?: string;
    description?: string;
    eyebrow?: string;
    mode?: 'header' | 'toolbar';
  }>(),
  {
    eyebrow: 'Management Module',
    mode: 'header',
  },
);

const showHeading = computed(() => props.mode === 'header' && Boolean(props.title || props.description || props.eyebrow));
</script>

<style scoped>
.header-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
  padding: 22px 24px;
}

.header-card--toolbar {
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(251, 253, 255, 0.94));
  border-color: rgba(15, 23, 42, 0.06);
  border-radius: 20px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.header-card--toolbar::before {
  content: none;
}

.header-content {
  min-width: 0;
}

.eyebrow {
  color: #7888a0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.title {
  color: #162033;
  font-size: 22px;
  font-weight: 700;
}

.title-accent {
  width: 44px;
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(36, 91, 219, 0.95), rgba(93, 176, 255, 0.36));
}

.description {
  max-width: 640px;
  margin-top: 8px;
  color: #627188;
  line-height: 1.65;
}

.header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-height: 52px;
}

.header-card--toolbar .header-actions,
.header-actions--stretch {
  width: 100%;
  min-height: 0;
  justify-content: flex-start;
}

.header-card--toolbar :deep(.arco-space) {
  row-gap: 12px;
}

@media (max-width: 768px) {
  .header-card {
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
