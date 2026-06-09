import { defineStore } from 'pinia';

export interface VisitedTab {
  path: string;
  title: string;
  closable: boolean;
}

interface TabsState {
  items: VisitedTab[];
  activePath: string;
}

export function createTabsManager(initialItems: VisitedTab[] = [], initialActivePath = '') {
  const items = [...initialItems];
  let activePath = initialActivePath || items[items.length - 1]?.path || '';

  const findIndex = (path: string) => items.findIndex((item) => item.path === path);

  return {
    get items() {
      return items;
    },
    get activePath() {
      return activePath;
    },
    visit(tab: VisitedTab) {
      const index = findIndex(tab.path);
      if (index >= 0) {
        items[index] = tab;
      } else {
        items.push(tab);
      }
      activePath = tab.path;
    },
    close(path: string) {
      const index = findIndex(path);
      if (index < 0) {
        return activePath;
      }
      if (!items[index].closable) {
        activePath = items[index].path;
        return activePath;
      }

      const wasActive = activePath === path;
      items.splice(index, 1);

      if (!items.length) {
        activePath = '';
        return activePath;
      }

      if (!wasActive) {
        return activePath;
      }

      const fallback = items[index - 1] ?? items[index] ?? items[0];
      activePath = fallback.path;
      return activePath;
    },
    reset(itemsToKeep: VisitedTab[] = []) {
      items.splice(0, items.length, ...itemsToKeep);
      activePath = itemsToKeep[0]?.path ?? '';
    },
  };
}

export const useTabsStore = defineStore('tabs', {
  state: (): TabsState => ({
    items: [],
    activePath: '',
  }),
  actions: {
    visit(tab: VisitedTab) {
      const manager = createTabsManager(this.items, this.activePath);
      manager.visit(tab);
      this.items = [...manager.items];
      this.activePath = manager.activePath;
    },
    close(path: string) {
      const manager = createTabsManager(this.items, this.activePath);
      const nextPath = manager.close(path);
      this.items = [...manager.items];
      this.activePath = nextPath;
      return nextPath;
    },
    setActive(path: string) {
      this.activePath = path;
    },
    reset(itemsToKeep: VisitedTab[] = []) {
      const manager = createTabsManager();
      manager.reset(itemsToKeep);
      this.items = [...manager.items];
      this.activePath = manager.activePath;
    },
  },
});
