<template>
  <section class="app-main">
    <router-view v-slot="{ Component, route }">
      <transition name="fade-transform" mode="out-in">
        <keep-alive :include="tagsViewStore.cachedViews">
          <component v-if="!route.meta.link" :is="Component" :key="route.path"/>
        </keep-alive>
      </transition>
    </router-view>
    <iframe-toggle />
    <copyright />
  </section>
</template>

<script setup>
import copyright from "./Copyright/index"
import iframeToggle from "./IframeToggle/index"
import useTagsViewStore from '@/store/modules/tagsView'

const route = useRoute()
const tagsViewStore = useTagsViewStore()

onMounted(() => {
  addIframe()
})

watchEffect(() => {
  addIframe()
})

function addIframe() {
  if (route.meta.link) {
    useTagsViewStore().addIframeView(route)
  }
}
</script>

<style lang="scss" scoped>
$navbar-height: var(--ui-layout-topbar-height, 52px);
$tags-height: var(--ui-layout-tags-height, 36px);
$navbar-tags-height: calc($navbar-height + $tags-height);

.app-main {
  min-height: calc(100vh - $navbar-height);
  width: 100%;
  position: relative;
  overflow: hidden;
  padding-bottom: 36px;
  background: var(--ui-bg-content);
}

.fixed-header + .app-main {
  overflow-y: auto;
  scrollbar-gutter: auto;
  height: calc(100vh - $navbar-height);
  min-height: 0px;
  margin-top: $navbar-height;
}

.hasTagsView {
  .app-main {
    min-height: calc(100vh - $navbar-tags-height);
  }

  .fixed-header + .app-main {
    margin-top: $navbar-tags-height;
    height: calc(100vh - $navbar-tags-height);
    min-height: 0px;
  }
}

/* 移动端fixed-header优化 */
@media screen and (max-width: 991px) {
  .fixed-header + .app-main {
    padding-bottom: max(60px, calc(constant(safe-area-inset-bottom) + 40px));
    padding-bottom: max(60px, calc(env(safe-area-inset-bottom) + 40px));
    overscroll-behavior-y: none;
  }

  .hasTagsView .fixed-header + .app-main {
    padding-bottom: max(60px, calc(constant(safe-area-inset-bottom) + 40px));
    padding-bottom: max(60px, calc(env(safe-area-inset-bottom) + 40px));
    overscroll-behavior-y: none;
  }
}

@supports (-webkit-touch-callout: none) {
  @media screen and (max-width: 991px) {
    .fixed-header + .app-main {
      padding-bottom: max(17px, calc(constant(safe-area-inset-bottom) + 10px));
      padding-bottom: max(17px, calc(env(safe-area-inset-bottom) + 10px));
      height: calc(100svh - $navbar-height);
      height: calc(100dvh - $navbar-height);
    }

    .hasTagsView .fixed-header + .app-main {
      padding-bottom: max(17px, calc(constant(safe-area-inset-bottom) + 10px));
      padding-bottom: max(17px, calc(env(safe-area-inset-bottom) + 10px));
      height: calc(100svh - $navbar-tags-height);
      height: calc(100dvh - $navbar-tags-height);
    }
  }
}
</style>

<style lang="scss">
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background-color: var(--ui-bg-panel-soft);
}

::-webkit-scrollbar-thumb {
  background-color: color-mix(in srgb, var(--ui-text-secondary) 34%, transparent);
  border: 2px solid var(--ui-bg-panel-soft);
  border-radius: 999px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: color-mix(in srgb, var(--ui-primary) 54%, transparent);
}
</style>
