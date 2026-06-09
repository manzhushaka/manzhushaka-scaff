<template>
  <div class="page-card header-card">
    <div>
      <div class="title">{{ title }}</div>
      <div class="description">{{ description }}</div>
    </div>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  description: string;
}>();
</script>

<style scoped>
.header-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 20px 24px;
}

.title {
  font-size: 20px;
  font-weight: 700;
}

.description {
  margin-top: 6px;
  color: #6b7280;
}
</style>
