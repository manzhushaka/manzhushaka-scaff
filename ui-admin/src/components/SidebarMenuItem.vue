<template>
  <a-sub-menu v-if="hasVisibleChildren" :key="menu.path">
    <template #icon>
      <component :is="iconComponent" />
    </template>
    <template #title>{{ menu.title }}</template>
    <SidebarMenuItem
      v-for="child in visibleChildren"
      :key="child.id"
      :menu="child"
      :resolve-icon="resolveIcon"
    />
  </a-sub-menu>
  <a-menu-item v-else :key="menu.path">
    <template #icon>
      <component :is="iconComponent" />
    </template>
    {{ menu.title }}
  </a-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import type { MenuItem } from '@/types/auth';

const props = defineProps<{
  menu: MenuItem;
  resolveIcon: (icon?: string) => Component;
}>();

const visibleChildren = computed(() =>
  (props.menu.children ?? []).filter((item) => item.type !== 'BUTTON' && !item.hidden),
);
const hasVisibleChildren = computed(() => visibleChildren.value.length > 0);
const iconComponent = computed(() => props.resolveIcon(props.menu.icon));
</script>
