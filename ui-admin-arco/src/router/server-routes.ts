import { defineAsyncComponent, defineComponent, h } from 'vue';
import type { Component, AsyncComponentLoader } from 'vue';
import type { RouteRecordRaw } from 'vue-router';
import type { BackendRoute } from '@/api/admin';
import { DEFAULT_LAYOUT, PARENT_VIEW } from './routes/base';

const viewModules = import.meta.glob('../views/**/*.vue');

const iconAliases: Record<string, string> = {
  '#': 'icon-menu',
  system: 'icon-settings',
  monitor: 'icon-dashboard',
  people: 'icon-user-group',
  user: 'icon-user',
  peoples: 'icon-user-group',
  'tree-table': 'icon-unordered-list',
  tree: 'icon-mind-mapping',
  dict: 'icon-book',
  edit: 'icon-edit',
  log: 'icon-file',
  druid: 'icon-bar-chart',
  message: 'icon-message',
  online: 'icon-user-group',
  job: 'icon-calendar-clock',
  server: 'icon-computer',
  redis: 'icon-storage',
  'redis-list': 'icon-list',
};

function normalizeViewPath(component: string) {
  return component.replace(/^\//, '').replace(/\.vue$/, '');
}

function loadView(component?: string) {
  if (!component || component === 'Layout') return DEFAULT_LAYOUT;
  if (component === 'ParentView') return PARENT_VIEW;
  if (component === 'InnerLink') return () => import('@/views/not-found/index.vue');

  const normalized = normalizeViewPath(component);
  const kebabCase = normalized.replace(/[A-Z]/g, (letter) => `-${letter.toLowerCase()}`);
  const aliases: Record<string, string> = {
    'monitor/job/log': 'monitor/jobLog/index',
  };
  const candidates = [normalized, kebabCase, aliases[normalized]].filter(Boolean);
  const entry = Object.entries(viewModules).find(([path]) => {
    return candidates.some((candidate) => path.endsWith(`/${candidate}.vue`));
  });
  return entry?.[1] || (() => import('@/views/not-found/index.vue'));
}

function namedRouteComponent(component: Component, name: string): Component {
  const resolvedComponent =
    typeof component === 'function'
      ? defineAsyncComponent(component as AsyncComponentLoader)
      : component;

  return defineComponent({
    name,
    setup(_, { attrs, slots }) {
      return () => h(resolvedComponent, attrs, slots);
    },
  });
}

function routeName(name: string | undefined, path: string) {
  const rawName = name || path;
  return `Server${rawName.charAt(0).toUpperCase()}${rawName.slice(1)}`;
}

function normalizeRoutePath(path: string, depth: number) {
  const normalized = path || '';
  if (depth > 0 && normalized.includes('/')) {
    return `/${normalized.replace(/^\/+/, '')}`;
  }
  return normalized;
}

function convertRoute(route: BackendRoute, depth = 0): RouteRecordRaw {
  const routePath = normalizeRoutePath(route.path, depth);
  const name = routeName(route.name, routePath);
  const children = route.children?.map((child) => convertRoute(child, depth + 1));
  const icon = route.meta?.icon;
  const meta = {
    ...(route.meta || {}),
    locale: route.meta?.title || route.name || routePath,
    title: route.meta?.title || route.name || routePath,
    icon: iconAliases[icon || ''] || 'icon-menu',
    requiresAuth: true,
    hideInMenu: Boolean(route.hidden),
    ignoreCache: Boolean(route.meta?.noCache),
  };

  const result: RouteRecordRaw = {
    path: routePath,
    name,
    component: namedRouteComponent(loadView(route.component), name),
    redirect: route.redirect === 'noRedirect' ? undefined : route.redirect,
    meta,
  };
  if (children?.length) result.children = children;
  return result;
}

export default function convertServerRoutes(routes: BackendRoute[]) {
  return routes.map(convertRoute);
}
