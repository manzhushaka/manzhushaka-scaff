import type { Component } from 'vue';
import {
  IconApps,
  IconBook,
  IconBranch,
  IconClockCircle,
  IconDashboard,
  IconFile,
  IconHistory,
  IconMenu,
  IconSafe,
  IconSettings,
  IconStorage,
  IconUser,
} from '@arco-design/web-vue/es/icon/index.js';

const iconMap = {
  'icon-book': IconBook,
  'icon-branch': IconBranch,
  'icon-clock-circle': IconClockCircle,
  'icon-dashboard': IconDashboard,
  'icon-file': IconFile,
  'icon-history': IconHistory,
  'icon-menu': IconMenu,
  'icon-safe': IconSafe,
  'icon-settings': IconSettings,
  'icon-storage': IconStorage,
  'icon-user': IconUser,
} as const;

export function resolveMenuIcon(icon?: string): Component {
  const resolvedIcon = icon?.trim();
  if (!resolvedIcon) {
    return IconApps;
  }
  return iconMap[resolvedIcon as keyof typeof iconMap] ?? IconApps;
}
