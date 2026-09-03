import { defineStore } from 'pinia';
import { Notification } from '@arco-design/web-vue';
import type { RouteRecordRaw, Router } from 'vue-router';
import defaultSettings from '@/config/settings.json';
import { getMenuList } from '@/api/user';
import type { BackendRoute } from '@/api/admin';
import convertServerRoutes from '@/router/server-routes';
import { appRoutes } from '@/router/routes';
import { AppState } from './types';

const useAppStore = defineStore('app', {
  state: (): AppState => ({ ...defaultSettings }),

  getters: {
    appCurrentSetting(state: AppState): AppState {
      return { ...state };
    },
    appDevice(state: AppState) {
      return state.device;
    },
    appAsyncMenus(state: AppState): RouteRecordRaw[] {
      const dashboard = appRoutes.find((route) => route.name === 'dashboard');
      const workplace = dashboard?.children?.find(
        (route) => route.name === 'Workplace'
      );
      const homeRoute =
        dashboard && workplace
          ? { ...dashboard, children: [workplace] }
          : undefined;
      return homeRoute ? [homeRoute, ...state.serverMenu] : state.serverMenu;
    },
  },

  actions: {
    // Update app settings
    updateSettings(partial: Partial<AppState>) {
      // @ts-ignore-next-line
      this.$patch(partial);
    },

    // Change theme color
    toggleTheme(dark: boolean) {
      if (dark) {
        this.theme = 'dark';
        document.body.setAttribute('arco-theme', 'dark');
      } else {
        this.theme = 'light';
        document.body.removeAttribute('arco-theme');
      }
    },
    toggleDevice(device: string) {
      this.device = device;
    },
    toggleMenu(value: boolean) {
      this.hideMenu = value;
    },
    async fetchServerMenuConfig() {
      try {
        const { data } = await getMenuList();
        this.serverMenu = convertServerRoutes(
          (data || []) as unknown as BackendRoute[]
        );
      } catch {
        Notification.error({
          id: 'menuNotice',
          content: '菜单加载失败，请刷新页面重试。',
          closable: true,
        });
      }
    },
    clearServerMenu() {
      this.serverMenu = [];
      this.serverRoutesAdded = false;
    },
    addServerRoutes(router: Router) {
      if (this.serverRoutesAdded) return;
      this.serverMenu.forEach((route) => {
        if (route.name && !router.hasRoute(route.name)) {
          router.addRoute(route);
        }
      });
      this.serverRoutesAdded = true;
    },
  },
});

export default useAppStore;
