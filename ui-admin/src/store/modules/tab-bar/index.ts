import type { RouteLocationNormalized } from 'vue-router';
import { defineStore } from 'pinia';
import {
  DEFAULT_ROUTE,
  DEFAULT_ROUTE_NAME,
  REDIRECT_ROUTE_NAME,
} from '@/router/constants';
import { isString } from '@/utils/is';
import { TabBarState, TagProps } from './types';

const formatTag = (route: RouteLocationNormalized): TagProps => {
  const { name, meta, fullPath, query } = route;
  return {
    title:
      meta.locale ||
      (typeof meta.title === 'string' ? meta.title : String(name || fullPath)),
    name: String(name),
    fullPath,
    icon: meta.icon,
    query,
    ignoreCache: meta.ignoreCache,
  };
};

const BAN_LIST = [REDIRECT_ROUTE_NAME];

const useAppStore = defineStore('tabBar', {
  state: (): TabBarState => ({
    cacheTabList: new Set([DEFAULT_ROUTE_NAME]),
    tagList: [DEFAULT_ROUTE],
  }),

  getters: {
    getTabList(): TagProps[] {
      return this.tagList;
    },
    getCacheList(): string[] {
      return Array.from(this.cacheTabList);
    },
  },

  actions: {
    updateTabList(route: RouteLocationNormalized) {
      const routeName = String(route.name || '');
      if (!routeName || BAN_LIST.includes(routeName)) return;

      const tag = formatTag(route);
      if (this.tagList.some((item) => item.fullPath === tag.fullPath)) return;

      this.tagList.push(tag);
      if (!route.meta.ignoreCache) {
        this.cacheTabList.add(routeName);
      }
    },
    deleteTag(idx: number, tag: TagProps) {
      this.tagList.splice(idx, 1);
      if (!this.tagList.some((item) => item.name === tag.name)) {
        this.cacheTabList.delete(tag.name);
      }
    },
    addCache(name: string) {
      if (isString(name) && name !== '') this.cacheTabList.add(name);
    },
    deleteCache(tag: TagProps) {
      this.cacheTabList.delete(tag.name);
    },
    freshTabList(tags: TagProps[]) {
      this.tagList = tags;
      this.cacheTabList.clear();
      // 要先判断ignoreCache
      this.tagList
        .filter((el) => !el.ignoreCache)
        .map((el) => el.name)
        .forEach((x) => this.cacheTabList.add(x));
    },
    resetTabList() {
      this.tagList = [DEFAULT_ROUTE];
      this.cacheTabList.clear();
      this.cacheTabList.add(DEFAULT_ROUTE_NAME);
    },
  },
});

export default useAppStore;
