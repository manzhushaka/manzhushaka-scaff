import { h } from 'vue';
import type { RouteRecordRaw } from 'vue-router';
import { RouterView } from 'vue-router';
import type { MenuItem } from '@/types/auth';
import { componentMap } from './component-map';

export function buildDynamicRoutes(menus: MenuItem[]): RouteRecordRaw[] {
  return menus
    .filter((menu) => menu.type !== 'BUTTON')
    .map((menu) => {
      const route = {
        path: menu.path,
        name: menu.name,
        meta: {
          title: menu.title,
          icon: menu.icon,
          hidden: menu.hidden,
          tab: Boolean(menu.component),
        },
      } as unknown as RouteRecordRaw;

      if (menu.component) {
        route.component = componentMap[menu.component as keyof typeof componentMap];
      }

      if (!menu.component && menu.children?.length) {
        route.component = { render: () => h(RouterView) };
      }

      if (menu.redirect) {
        route.redirect = menu.redirect;
      }

      if (menu.children?.length) {
        route.children = buildDynamicRoutes(menu.children);
      }

      return route;
    });
}

export function extractSidebarMenus(menus: MenuItem[]): MenuItem[] {
  return menus
    .filter((menu) => menu.type !== 'BUTTON' && !menu.hidden)
    .map((menu) => ({
      ...menu,
      children: menu.children ? extractSidebarMenus(menu.children) : undefined,
    }));
}

export function extractPermissionCodes(menus: MenuItem[]): string[] {
  const codes: string[] = [];

  const walk = (items: MenuItem[]) => {
    for (const item of items) {
      if (item.type === 'BUTTON' && item.permission) {
        codes.push(item.permission);
      }
      if (item.children?.length) {
        walk(item.children);
      }
    }
  };

  walk(menus);
  return codes;
}
