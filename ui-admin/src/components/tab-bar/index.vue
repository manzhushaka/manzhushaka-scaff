<template>
  <div class="tab-bar-container">
    <a-affix ref="affixRef" :offset-top="offsetTop">
      <div class="tab-bar-box">
        <div class="tab-bar-scroll">
          <TransitionGroup
            ref="tagsWrap"
            class="tags-wrap"
            name="tab-fade"
            tag="div"
          >
            <tab-item
              v-for="(tag, index) in tagList"
              :key="tag.fullPath"
              :index="index"
              :item-data="tag"
            />
          </TransitionGroup>
        </div>
      </div>
    </a-affix>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, watch, nextTick, onUnmounted } from 'vue';
  import type { RouteLocationNormalized } from 'vue-router';
  import { useRoute } from 'vue-router';
  import {
    listenerRouteChange,
    removeRouteListener,
  } from '@/utils/route-listener';
  import { useAppStore, useTabBarStore } from '@/store';
  import tabItem from './tab-item.vue';

  const appStore = useAppStore();
  const tabBarStore = useTabBarStore();
  const route = useRoute();

  const affixRef = ref();
  const tagsWrap = ref<HTMLElement>();
  const tagList = computed(() => {
    return tabBarStore.getTabList;
  });
  const offsetTop = computed(() => {
    return appStore.navbar ? 60 : 0;
  });

  const scrollActiveTabIntoView = async () => {
    await nextTick();
    tagsWrap.value
      ?.querySelector<HTMLElement>('.link-activated')
      ?.scrollIntoView({
        behavior: 'smooth',
        block: 'nearest',
        inline: 'nearest',
      });
  };

  watch([() => route.fullPath, tagList], scrollActiveTabIntoView, {
    flush: 'post',
  });

  watch(
    () => appStore.navbar,
    () => {
      affixRef.value?.updatePosition();
    }
  );

  const handleRouteChange = (changedRoute: RouteLocationNormalized) => {
    if (
      !changedRoute.meta.noAffix &&
      !tagList.value.some((tag) => tag.fullPath === changedRoute.fullPath)
    ) {
      tabBarStore.updateTabList(changedRoute);
    }
  };

  listenerRouteChange(handleRouteChange, true);

  onUnmounted(() => {
    removeRouteListener(handleRouteChange);
  });
</script>

<style scoped lang="less">
  .tab-bar-container {
    position: relative;
    z-index: 2;
    padding: 4px 20px 0;
    background-color: var(--color-fill-2);
    .tab-bar-box {
      display: flex;
      min-height: 32px;
      padding: 0;
      .tab-bar-scroll {
        flex: 1;
        height: 32px;
        overflow: hidden;
        .tags-wrap {
          display: flex;
          align-items: center;
          height: 32px;
          gap: 6px;
          white-space: nowrap;
          overflow-x: auto;

          :deep(.tab-fade-enter-active),
          :deep(.tab-fade-leave-active),
          :deep(.tab-fade-move) {
            transition: opacity var(--ui-motion-fast) var(--ui-motion-ease),
              transform var(--ui-motion-fast) var(--ui-motion-ease);
          }

          :deep(.tab-fade-enter-from),
          :deep(.tab-fade-leave-to) {
            opacity: 0;
            transform: translateX(8px) scale(0.98);
          }

          :deep(.tab-fade-leave-active) {
            position: absolute;
          }

          :deep(.arco-tag) {
            display: inline-flex;
            align-items: center;
            flex-shrink: 0;
            height: 32px;
            margin-right: 0;
            padding: 0 10px;
            font-size: var(--ui-font-size-sm);
            line-height: 30px;
            color: var(--color-text-2);
            background-color: var(--color-bg-2);
            border: 1px solid var(--color-border-2);
            border-radius: 6px;
            cursor: pointer;
            transition: color var(--ui-motion-fast) var(--ui-motion-ease),
              border-color var(--ui-motion-fast) var(--ui-motion-ease),
              background-color var(--ui-motion-fast) var(--ui-motion-ease),
              box-shadow var(--ui-motion-fast) var(--ui-motion-ease),
              transform var(--ui-motion-fast) var(--ui-motion-ease);

            &:first-child {
              .arco-tag-close-btn {
                display: none;
              }
            }

            &:hover {
              color: rgb(var(--primary-6));
              border-color: rgb(var(--primary-3));
              transform: translateY(-1px);
            }

            &.link-activated {
              color: rgb(var(--primary-6));
              background-color: var(--color-bg-2);
              border-color: var(--color-border-2);
              box-shadow: 0 2px 8px rgba(var(--primary-6), 0.12);
            }
          }
        }
      }
    }
  }

  @media (max-width: 640px) {
    .tab-bar-container {
      padding: 4px 12px 0;

      .tab-bar-box {
        min-height: 32px;
        padding: 0;
      }
    }
  }

  @media (prefers-reduced-motion: reduce) {
    :deep(.tab-fade-enter-active),
    :deep(.tab-fade-leave-active),
    :deep(.tab-fade-move) {
      transition-duration: 0.01ms;
    }
  }
</style>
