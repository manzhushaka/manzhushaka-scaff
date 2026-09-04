<script lang="tsx">
  import { defineComponent, ref, h, computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute, useRouter, RouteRecordRaw } from 'vue-router';
  import type { RouteMeta } from 'vue-router';
  import { useAppStore } from '@/store';
  import { listenerRouteChange } from '@/utils/route-listener';
  import { openWindow, regexUrl } from '@/utils';
  import * as ArcoIcons from '@arco-design/web-vue/es/icon';
  import useMenuTree from './use-menu-tree';

  const iconMap: Record<string, any> = Object.fromEntries(Object.entries(ArcoIcons)
    .filter(([name]) => /^Icon[A-Z]/.test(name))
    .map(([name, component]) => [`icon-${name.slice(4).replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase()}`, component]));

  export default defineComponent({
    emit: ['collapse'],
    setup() {
      const { t } = useI18n();
      const appStore = useAppStore();
      const router = useRouter();
      const route = useRoute();
      const { menuTree } = useMenuTree();
      const collapsed = computed({
        get() {
          if (appStore.device === 'desktop') return appStore.menuCollapse;
          return false;
        },
        set(value: boolean) {
          appStore.updateSettings({ menuCollapse: value });
        },
      });

      const topMenu = computed(() => appStore.topMenu);
      const openKeys = ref<string[]>([]);
      const selectedKey = ref<string[]>([]);

      // Keep one active branch open so sibling menus collapse smoothly.
      const menuStructure = computed(() => {
        const parents = new Map<string, string | undefined>();
        const subMenus = new Set<string>();
        const walk = (routes: RouteRecordRaw[], parent?: string) => {
          routes?.forEach((item) => {
            const key = String(item.name);
            parents.set(key, parent);
            if (item.children?.length) {
              subMenus.add(key);
              walk(item.children, key);
            }
          });
        };
        walk(menuTree.value);
        return { parents, subMenus };
      });

      const syncOpenKeys = (keys: string[]) => {
        const { parents, subMenus } = menuStructure.value;
        const lastKey = [...keys].reverse().find((key) => subMenus.has(key));
        if (!lastKey) return [];

        const branch: string[] = [];
        let current: string | undefined = lastKey;
        while (current) {
          if (subMenus.has(current)) branch.unshift(current);
          current = parents.get(current);
        }
        return branch;
      };

      const goto = (item: RouteRecordRaw) => {
        // Open external link
        if (regexUrl.test(item.path)) {
          openWindow(item.path);
          selectedKey.value = [item.name as string];
          return;
        }
        // Eliminate external link side effects
        const { hideInMenu, activeMenu } = item.meta as RouteMeta;
        if (route.name === item.name && !hideInMenu && !activeMenu) {
          selectedKey.value = [item.name as string];
          return;
        }
        // Trigger router change
        router.push({
          name: item.name,
        });
      };
      const findMenuOpenKeys = (target: string) => {
        const result: string[] = [];
        let isFind = false;
        const backtrack = (item: RouteRecordRaw, keys: string[]) => {
          if (item.name === target) {
            isFind = true;
            result.push(...keys);
            return;
          }
          if (item.children?.length) {
            item.children.forEach((el) => {
              backtrack(el, [...keys, el.name as string]);
            });
          }
        };
        menuTree.value.forEach((el: RouteRecordRaw) => {
          if (isFind) return; // Performance optimization
          backtrack(el, [el.name as string]);
        });
        return result;
      };
      listenerRouteChange((newRoute) => {
        const { requiresAuth, activeMenu, hideInMenu } = newRoute.meta;
        if (requiresAuth && (!hideInMenu || activeMenu)) {
          const menuOpenKeys = findMenuOpenKeys(
            (activeMenu || newRoute.name) as string
          );

          openKeys.value = syncOpenKeys(menuOpenKeys);

          selectedKey.value = [
            activeMenu || menuOpenKeys[menuOpenKeys.length - 1],
          ];
        }
      }, true);
      const setCollapse = (val: boolean) => {
        if (appStore.device === 'desktop')
          appStore.updateSettings({ menuCollapse: val });
      };

      const updateOpenKeys = (keys: string[]) => {
        openKeys.value = syncOpenKeys(keys);
      };

      const renderSubMenu = () => {
        function travel(_route: RouteRecordRaw[], nodes = []) {
          if (_route) {
            _route.forEach((element) => {
              // This is demo, modify nodes as needed
              const icon = element?.meta?.icon
                ? () => h(iconMap[element.meta?.icon as string] || ArcoIcons.IconMenu)
                : null;
              const label = String(
                element?.meta?.title || element?.meta?.locale || ''
              );
              const node =
                element?.children && element?.children.length !== 0 ? (
                  <a-sub-menu
                    key={element?.name}
                    v-slots={{
                      icon,
                      title: () => h('span', t(label)),
                    }}
                  >
                    {travel(element?.children)}
                  </a-sub-menu>
                ) : (
                  <a-menu-item
                    key={element?.name}
                    v-slots={{ icon }}
                    onClick={() => goto(element)}
                  >
                    {t(label)}
                  </a-menu-item>
                );
              nodes.push(node as never);
            });
          }
          return nodes;
        }
        return travel(menuTree.value);
      };

      return () => (
        <a-menu
          class="app-menu"
          mode={topMenu.value ? 'horizontal' : 'vertical'}
          v-model:collapsed={collapsed.value}
          v-model:open-keys={openKeys.value}
          show-collapse-button={false}
          auto-open={false}
          selected-keys={selectedKey.value}
          auto-open-selected={true}
          level-indent={34}
          style="height: 100%;width:100%;"
          onCollapse={setCollapse}
          onUpdateOpenKeys={updateOpenKeys}
        >
          {renderSubMenu()}
        </a-menu>
      );
    },
  });
</script>

<style lang="less" scoped>
  :deep(.arco-menu-inner) {
    .arco-menu-inline-header {
      display: flex;
      align-items: center;
    }
    .arco-icon {
      &:not(.arco-icon-down) {
        font-size: 18px;
      }
    }
  }

  :deep(.app-menu) {
    .arco-menu-item,
    .arco-menu-inline-header {
      margin-inline: var(--ui-space-1);
      font-size: var(--ui-font-size-base);
      line-height: 40px;
      transition:
        color 0.2s var(--ui-motion-ease),
        background-color 0.2s var(--ui-motion-ease),
        transform 0.2s var(--ui-motion-ease);

      &::before {
        position: absolute;
        top: 8px;
        bottom: 8px;
        left: 0;
        width: 3px;
        border-radius: 0 3px 3px 0;
        background-color: var(--ui-primary);
        content: '';
        opacity: 0;
        transform: scaleY(0.35);
        transition:
          opacity 0.2s ease,
          transform 0.2s cubic-bezier(0.23, 1, 0.32, 1);
      }

      &:hover {
        transform: translateX(1px);
      }
    }

    .arco-menu-item.arco-menu-selected::before,
    .arco-menu-inline-header.arco-menu-selected::before {
      opacity: 1;
      transform: scaleY(1);
    }

    .arco-menu-icon-suffix {
      transition: transform 0.22s cubic-bezier(0.23, 1, 0.32, 1);
    }

    .arco-menu-inline-content {
      transition: height 0.24s cubic-bezier(0.23, 1, 0.32, 1);
    }
  }

  @media (prefers-reduced-motion: reduce) {
    :deep(.app-menu),
    :deep(.app-menu *) {
      transition-duration: 0.01ms !important;
      animation-duration: 0.01ms !important;
    }
  }
</style>
