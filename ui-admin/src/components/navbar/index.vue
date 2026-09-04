<template>
  <div class="navbar">
    <div class="left-side">
      <div
        class="brand-group"
        :class="{ 'brand-group-collapsed': brandGroupCollapsed }"
        :style="brandGroupStyle"
      >
        <img class="brand-logo" :src="brandLogo" alt="manzhushaka 标识" />
        <a-typography-title
          v-if="showBrandTitle"
          :style="{ margin: 0, fontSize: '18px' }"
          :heading="5"
        >
          manzhushaka
        </a-typography-title>
      </div>
      <div class="nav-tools">
        <a-tooltip v-if="showMenuToggle" :content="menuToggleLabel">
          <a-button
            class="nav-btn nav-tool-btn"
            type="text"
            shape="square"
            :aria-label="menuToggleLabel"
            @click="toggleMenu"
          >
            <template #icon>
              <icon-menu-unfold
                v-if="appStore.device === 'mobile' || isMenuCollapsed"
              />
              <icon-menu-fold v-else />
            </template>
          </a-button>
        </a-tooltip>
        <a-tooltip :content="$t('settings.navbar.refresh')">
          <a-button
            class="nav-btn nav-tool-btn"
            type="text"
            shape="square"
            :aria-label="$t('settings.navbar.refresh')"
            @click="refreshPage"
          >
            <template #icon><icon-refresh /></template>
          </a-button>
        </a-tooltip>
      </div>
      <a-breadcrumb
        v-if="breadcrumbItems.length"
        class="route-breadcrumb"
        aria-label="当前页面路径"
      >
        <a-breadcrumb-item><icon-apps /></a-breadcrumb-item>
        <a-breadcrumb-item
          v-for="(item, index) in visibleBreadcrumbItems"
          :key="`${item}-${index}`"
        >
          {{ item }}
        </a-breadcrumb-item>
      </a-breadcrumb>
    </div>
    <div class="center-side">
      <Menu v-if="topMenu" />
    </div>
    <ul class="right-side">
      <li>
        <a-tooltip :content="$t('settings.search')">
          <a-button class="nav-btn" type="outline" :shape="'circle'">
            <template #icon>
              <icon-search />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="$t('settings.language')">
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="setDropDownVisible"
          >
            <template #icon>
              <icon-language />
            </template>
          </a-button>
        </a-tooltip>
        <a-dropdown trigger="click" @select="changeLocale as any">
          <div ref="triggerBtn" class="trigger-btn"></div>
          <template #content>
            <a-doption
              v-for="item in locales"
              :key="item.value"
              :value="item.value"
            >
              <template #icon>
                <icon-check v-show="item.value === currentLocale" />
              </template>
              {{ item.label }}
            </a-doption>
          </template>
        </a-dropdown>
      </li>
      <li>
        <a-tooltip
          :content="
            theme === 'light'
              ? $t('settings.navbar.theme.toDark')
              : $t('settings.navbar.theme.toLight')
          "
        >
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="handleToggleTheme"
          >
            <template #icon>
              <icon-moon-fill v-if="theme === 'dark'" />
              <icon-sun-fill v-else />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="$t('settings.navbar.alerts')">
          <div class="message-box-trigger">
            <a-badge :count="9" dot>
              <a-button
                class="nav-btn"
                type="outline"
                :shape="'circle'"
                @click="setPopoverVisible"
              >
                <icon-notification />
              </a-button>
            </a-badge>
          </div>
        </a-tooltip>
        <a-popover
          trigger="click"
          :arrow-style="{ display: 'none' }"
          :content-style="{ padding: 0, minWidth: '400px' }"
          content-class="message-popover"
        >
          <div ref="refBtn" class="ref-btn"></div>
          <template #content>
            <message-box />
          </template>
        </a-popover>
      </li>
      <li>
        <a-tooltip
          :content="
            isFullscreen
              ? $t('settings.navbar.screen.toExit')
              : $t('settings.navbar.screen.toFull')
          "
        >
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="toggleFullScreen"
          >
            <template #icon>
              <icon-fullscreen-exit v-if="isFullscreen" />
              <icon-fullscreen v-else />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="$t('settings.title')">
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="setVisible"
          >
            <template #icon>
              <icon-settings />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-dropdown trigger="click">
          <a-avatar
            :size="32"
            :style="{ marginRight: '8px', cursor: 'pointer' }"
          >
            <img v-if="avatar" alt="用户头像" :src="avatar" />
            <icon-user v-else />
          </a-avatar>
          <template #content>
            <a-doption>
              <a-space @click="switchRoles">
                <icon-tag />
                <span>
                  {{ $t('messageBox.switchRoles') }}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="$router.push({ name: 'Info' })">
                <icon-user />
                <span>
                  {{ $t('messageBox.userCenter') }}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="$router.push({ name: 'Setting' })">
                <icon-settings />
                <span>
                  {{ $t('messageBox.userSettings') }}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="$router.push({ name: 'AccountProfile' })">
                <icon-user-group />
                <span>个人资料</span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="handleLock">
                <icon-lock />
                <span>锁定屏幕</span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="handleLogout">
                <icon-export />
                <span>
                  {{ $t('messageBox.logout') }}
                </span>
              </a-space>
            </a-doption>
          </template>
        </a-dropdown>
      </li>
    </ul>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, inject } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useI18n } from 'vue-i18n';
  import { Message } from '@arco-design/web-vue';
  import { useDark, useToggle, useFullscreen } from '@vueuse/core';
  import { useAppStore, useUserStore } from '@/store';
  import { LOCALE_OPTIONS } from '@/locale';
  import useLocale from '@/hooks/locale';
  import useUser from '@/hooks/user';
  import useLockStore from '@/store/modules/lock';
  import Menu from '@/components/menu/index.vue';
  import brandLogo from '@/assets/brand-logo.png';
  import MessageBox from '../message-box/index.vue';

  const appStore = useAppStore();
  const userStore = useUserStore();
  const lockStore = useLockStore();
  const router = useRouter();
  const route = useRoute();
  const { t } = useI18n();
  const { logout } = useUser();
  const { changeLocale, currentLocale } = useLocale();
  const { isFullscreen, toggle: toggleFullScreen } = useFullscreen();
  const locales = [...LOCALE_OPTIONS];
  const avatar = computed(() => {
    return userStore.avatar;
  });
  const theme = computed(() => {
    return appStore.theme;
  });
  const topMenu = computed(() => appStore.topMenu && appStore.menu);
  const showMenuToggle = computed(() => appStore.menu && !topMenu.value);
  const alignBrandWithSider = computed(
    () => appStore.menu && !topMenu.value && !appStore.hideMenu
  );
  const brandGroupCollapsed = computed(
    () => alignBrandWithSider.value && appStore.menuCollapse
  );
  const brandGroupStyle = computed(() => {
    if (!alignBrandWithSider.value) return undefined;
    const width = appStore.menuCollapse ? 48 : appStore.menuWidth;
    return { width: `${width}px` };
  });
  const showBrandTitle = computed(() => !brandGroupCollapsed.value);
  /**
   * 将路由元数据转换为当前语言下的面包屑标题。
   *
   * @param title 路由元数据中的标题或国际化键。
   * @return 当前语言下的标题。
   */
  const resolveRouteTitle = (title?: string) => {
    if (!title) return '';
    return title.startsWith('menu.') ? t(title) : title;
  };
  const breadcrumbItems = computed(() => {
    return route.matched
      .map((matchedRoute) =>
        resolveRouteTitle(matchedRoute.meta.locale || matchedRoute.meta.title)
      )
      .filter((title) => title.length > 0);
  });
  const visibleBreadcrumbItems = computed(() => {
    if (appStore.device === 'mobile') return breadcrumbItems.value.slice(-1);
    return breadcrumbItems.value;
  });
  const isMenuCollapsed = computed(() => {
    return appStore.device === 'desktop' && appStore.menuCollapse;
  });
  const menuToggleLabel = computed(() => {
    if (appStore.device === 'mobile') return t('settings.navbar.menuExpand');
    return isMenuCollapsed.value
      ? t('settings.navbar.menuExpand')
      : t('settings.navbar.menuCollapse');
  });
  const toggleDrawerMenu = inject('toggleDrawerMenu') as () => void;
  /**
   * 切换桌面端侧栏状态，或在移动端打开菜单抽屉。
   */
  const toggleMenu = () => {
    if (appStore.device === 'mobile') {
      toggleDrawerMenu();
      return;
    }
    appStore.updateSettings({ menuCollapse: !appStore.menuCollapse });
  };
  /**
   * 重新加载当前路由页面。
   */
  const refreshPage = () => {
    router.go(0);
  };
  const isDark = useDark({
    selector: 'body',
    attribute: 'arco-theme',
    valueDark: 'dark',
    valueLight: 'light',
    storageKey: 'arco-theme',
    onChanged(dark: boolean) {
      // overridden default behavior
      appStore.toggleTheme(dark);
    },
  });
  const toggleTheme = useToggle(isDark);
  const handleToggleTheme = () => {
    toggleTheme();
  };
  const setVisible = () => {
    appStore.updateSettings({ globalSettings: true });
  };
  const refBtn = ref();
  const triggerBtn = ref();
  const setPopoverVisible = () => {
    const event = new MouseEvent('click', {
      view: window,
      bubbles: true,
      cancelable: true,
    });
    refBtn.value.dispatchEvent(event);
  };
  const handleLogout = () => {
    logout();
  };
  const handleLock = () => {
    lockStore.lockScreen(router.currentRoute.value.fullPath);
    router.push({ name: 'Lock' });
  };
  const setDropDownVisible = () => {
    const event = new MouseEvent('click', {
      view: window,
      bubbles: true,
      cancelable: true,
    });
    triggerBtn.value.dispatchEvent(event);
  };
  const switchRoles = async () => {
    const res = await userStore.switchRoles();
    Message.success(res as string);
  };
</script>

<style scoped lang="less">
  .navbar {
    display: flex;
    justify-content: space-between;
    height: 100%;
    min-height: 60px;
    background-color: transparent;
  }

  .left-side {
    display: flex;
    align-items: center;
    height: 100%;
    min-width: 0;

    .brand-group {
      display: flex;
      align-items: center;
      flex-shrink: 0;
      gap: 10px;
      height: 100%;
      padding: 0 20px;
      transition: width var(--ui-motion-standard) var(--ui-motion-ease);
    }

    .brand-group-collapsed {
      justify-content: center;
      padding: 0 8px;
    }

    .nav-tools {
      display: flex;
      align-items: center;
      flex-shrink: 0;
      gap: 2px;
      padding: 0 8px;

      .nav-tool-btn {
        color: var(--color-text-2);
        font-size: 18px;
        transition:
          color var(--ui-motion-fast) ease,
          background-color var(--ui-motion-fast) ease,
          transform var(--ui-motion-fast) var(--ui-motion-ease);

        &:hover {
          color: rgb(var(--primary-6));
          background-color: var(--color-primary-light-1);
          transform: translateY(-1px);
        }
      }
    }

    .route-breadcrumb {
      min-width: 0;
      overflow: hidden;
      color: var(--color-text-3);
      font-size: var(--ui-font-size-sm);
      line-height: var(--line-height-control);

      :deep(.arco-breadcrumb-item) {
        max-width: 180px;
        overflow: hidden;
        color: var(--color-text-3);
        text-overflow: ellipsis;
        white-space: nowrap;

        &:last-child {
          color: var(--color-text-1);
          font-weight: 500;
        }
      }
    }
  }

  .brand-logo {
    width: 32px;
    height: 32px;
    border-radius: 6px;
    object-fit: cover;
  }

  .center-side {
    flex: 1;
  }

  .right-side {
    display: flex;
    align-items: center;
    height: 100%;
    margin: 0;
    padding-right: 20px;
    list-style: none;
    :deep(.locale-select) {
      border-radius: 20px;
    }
    li {
      display: flex;
      align-items: center;
      height: 100%;
      padding: 0 8px;
    }

    a {
      color: var(--color-text-1);
      text-decoration: none;
    }
    .nav-btn {
      border-color: rgb(var(--gray-2));
      color: rgb(var(--gray-8));
      font-size: var(--ui-font-size-md);
    }
    .trigger-btn,
    .ref-btn {
      position: absolute;
      bottom: 14px;
    }
    .trigger-btn {
      margin-left: 14px;
    }
  }

  @media (max-width: 640px) {
    .left-side {
      flex-shrink: 0;
    }

    .left-side .brand-group {
      padding: 0 12px;
    }

    .left-side :deep(.arco-typography) {
      display: none;
    }

    .left-side .nav-tools {
      padding: 0 4px;
    }

    .left-side .route-breadcrumb {
      max-width: 150px;

      :deep(.arco-breadcrumb-item) {
        max-width: 110px;
      }
    }

    .right-side {
      flex-shrink: 0;
      padding-right: 12px;
    }

    .right-side > li {
      padding: 0 4px;
    }

    .right-side > li:nth-child(1),
    .right-side > li:nth-child(2),
    .right-side > li:nth-child(5),
    .right-side > li:nth-child(6) {
      display: none;
    }
  }
</style>

<style lang="less">
  .message-popover {
    .arco-popover-content {
      margin-top: 0;
    }
  }
</style>
