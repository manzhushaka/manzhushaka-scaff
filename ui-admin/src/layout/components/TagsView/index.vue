<template>
  <div id="tags-view-container" class="tags-view-container" :class="{ 'tags-view-container--chrome': tagsViewStyle === 'chrome' }">
    <!-- 左切换箭头 -->
    <span class="tags-nav-btn tags-nav-btn--left" :class="{ disabled: !canScrollLeft }" @click="scrollLeft">
      <icon-left />
    </span>

    <!-- 标签滚动区 -->
    <scroll-pane ref="scrollPaneRef" class="tags-view-wrapper" @scroll="handleScroll" @update-arrows="updateArrowState">
      <router-link
        v-for="tag in visitedViews"
        :key="tag.path"
        :data-path="tag.path"
        :class="{ 'active': isActive(tag), 'has-icon': tagsIcon }"
        :to="{ path: tag.path, query: tag.query, fullPath: tag.fullPath }"
        class="tags-view-item"
        @click.middle="!isAffix(tag) ? closeSelectedTag(tag) : ''"
        @contextmenu.prevent="openMenu(tag, $event)"
      >
        <svg-icon v-if="tagsIcon && tag.meta && tag.meta.icon && tag.meta.icon !== '#'" :icon-class="tag.meta.icon" style="margin-right: 3px;" />
        {{ tag.title }}
        <span v-if="!isAffix(tag)" @click.prevent.stop="closeSelectedTag(tag)" class="tags-close-btn">
          <icon-close class="tags-close-icon" />
        </span>
      </router-link>
    </scroll-pane>

    <!-- 右切换箭头 -->
    <span class="tags-nav-btn tags-nav-btn--right" :class="{ disabled: !canScrollRight }" @click="scrollRight">
      <icon-right />
    </span>

    <!-- 下拉操作菜单 -->
    <a-dropdown class="tags-action-dropdown" trigger="click" position="br" @select="handleDropdownCommand">
      <span class="tags-action-btn">
        <icon-down />
      </span>
      <template #content>
        <a-doption v-if="!isAffix(selectedDropdownTag)" value="close"><template #icon><icon-close /></template>关闭当前</a-doption>
        <a-doption value="closeOthers"><template #icon><icon-close-circle /></template>关闭其他</a-doption>
        <a-doption value="closeLeft" :disabled="isFirstView()"><template #icon><icon-to-left /></template>关闭左侧</a-doption>
        <a-doption value="closeRight" :disabled="isLastView()"><template #icon><icon-to-right /></template>关闭右侧</a-doption>
        <a-doption value="closeAll"><template #icon><icon-delete /></template>全部关闭</a-doption>
        <a-divider :margin="4" />
        <a-doption value="fullscreen">
          <template #icon><icon-fullscreen v-if="!isFullscreen" /><icon-fullscreen-exit v-else /></template>
          {{ isFullscreen ? '退出全屏' : '全屏显示' }}
        </a-doption>
      </template>
    </a-dropdown>

    <!-- 刷新按钮 -->
    <span class="tags-action-btn tags-refresh-btn" title="刷新页面" aria-label="刷新页面" @click="refreshSelectedTag(selectedDropdownTag)">
      <icon-refresh />
    </span>

    <!-- 右键上下文菜单 -->
    <ul v-show="visible" :style="{ left: left + 'px', top: top + 'px' }" class="contextmenu">
      <li @click="refreshSelectedTag(selectedTag)"><icon-refresh />刷新页面</li>
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)"><icon-close />关闭当前</li>
      <li @click="closeOthersTags"><icon-close-circle />关闭其他</li>
      <li v-if="!isFirstView()" @click="closeLeftTags"><icon-to-left />关闭左侧</li>
      <li v-if="!isLastView()" @click="closeRightTags"><icon-to-right />关闭右侧</li>
      <li @click="closeAllTags(selectedTag)"><icon-delete />全部关闭</li>
    </ul>
  </div>
</template>

<script setup>
import {
  IconClose,
  IconCloseCircle,
  IconDelete,
  IconDown,
  IconFullscreen,
  IconFullscreenExit,
  IconLeft,
  IconRefresh,
  IconRight,
  IconToLeft,
  IconToRight
} from '@arco-design/web-vue/es/icon'
import ScrollPane from './ScrollPane'
import { getNormalPath } from '@/utils/manzhushaka'
import useTagsViewStore from '@/store/modules/tagsView'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

const visible = ref(false)
const top = ref(0)
const left = ref(0)
const selectedTag = ref({})
const affixTags = ref([])
const scrollPaneRef = ref(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)
const isFullscreen = ref(false)
const hiddenElements = ref([])

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const visitedViews = computed(() => useTagsViewStore().visitedViews)
const routes = computed(() => usePermissionStore().routes)
const tagsIcon = computed(() => useSettingsStore().tagsIcon)
const tagsViewPersist = computed(() => useSettingsStore().tagsViewPersist)
const tagsViewStyle = computed(() => useSettingsStore().tagsViewStyle)

// 下拉菜单针对当前激活的 tag
const selectedDropdownTag = computed(() => visitedViews.value.find(v => isActive(v)) || {})

watch(route, () => {
  addTags()
  moveToCurrentTag()
})

watch(visible, (value) => {
  if (value) {
    document.body.addEventListener('click', closeMenu)
  } else {
    document.body.removeEventListener('click', closeMenu)
  }
})

watch(visitedViews, () => {
  nextTick(() => updateArrowState())
})

onMounted(() => {
  initTags()
  addTags()
  window.addEventListener('resize', updateArrowState)
  window.addEventListener('keydown', handleKeyDown)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateArrowState)
  window.removeEventListener('keydown', handleKeyDown)
})

function handleKeyDown(event) {
  // 当按下Esc键且处于全屏状态时，退出全屏
  if (event.key === 'Escape' && isFullscreen.value) {
    toggleFullscreen()
  }
}

function isActive(r) {
  return r.path === route.path
}

function isAffix(tag) {
  return tag && tag.meta && tag.meta.affix
}

function isFirstView() {
  try {
    const tag = selectedTag.value && selectedTag.value.fullPath ? selectedTag.value : selectedDropdownTag.value
    return tag.fullPath === '/index' || tag.fullPath === visitedViews.value[1].fullPath
  } catch (err) {
    return false
  }
}

function isLastView() {
  try {
    const tag = selectedTag.value && selectedTag.value.fullPath ? selectedTag.value : selectedDropdownTag.value
    return tag.fullPath === visitedViews.value[visitedViews.value.length - 1].fullPath
  } catch (err) {
    return false
  }
}

function filterAffixTags(routes, basePath = '') {
  let tags = []
  routes.forEach(route => {
    if (route.meta && route.meta.affix) {
      const tagPath = getNormalPath(basePath + '/' + route.path)
      tags.push({
        fullPath: tagPath,
        path: tagPath,
        name: route.name,
        meta: { ...route.meta }
      })
    }
    if (route.children) {
      const tempTags = filterAffixTags(route.children, route.path)
      if (tempTags.length >= 1) {
        tags = [...tags, ...tempTags]
      }
    }
  })
  return tags
}

function initTags() {
  if (tagsViewPersist.value) {
    useTagsViewStore().loadPersistedViews()
  }
  const res = filterAffixTags(routes.value)
  affixTags.value = res
  for (const tag of res) {
    if (tag.name) {
      useTagsViewStore().addAffixView(tag)
    }
  }
}

function addTags() {
  const { name } = route
  if (name) {
    useTagsViewStore().addView(route)
  }
}

function moveToCurrentTag() {
  nextTick(() => {
    for (const r of visitedViews.value) {
      if (r.path === route.path) {
        scrollPaneRef.value.moveToTarget(r)
        if (r.fullPath !== route.fullPath) {
          useTagsViewStore().updateVisitedView(route)
        }
      }
    }
  })
}

function scrollLeft() {
  if (!canScrollLeft.value) return
  scrollPaneRef.value.scrollToStart()
}

function scrollRight() {
  if (!canScrollRight.value) return
  scrollPaneRef.value.scrollToEnd()
}

function updateArrowState() {
  nextTick(() => {
    if (scrollPaneRef.value) {
      const state = scrollPaneRef.value.getScrollState()
      canScrollLeft.value = state.canLeft
      canScrollRight.value = state.canRight
    }
  })
}

function toggleFullscreen() {
  const mainContainer = document.querySelector('.main-container')
  const navbar = document.querySelector('.navbar')
  const sidebar = document.querySelector('.sidebar-container')
  if (!mainContainer) return

  if (!isFullscreen.value) {
    mainContainer.classList.add('fullscreen-mode')
    document.body.style.overflow = 'hidden'
    const elementsToHide = [{ el: navbar, originalDisplay: navbar?.style.display || '' }, { el: sidebar, originalDisplay: sidebar?.style.display || '' }]
    elementsToHide.forEach(item => {
      if (item.el && item.el.style.display !== 'none') {
        item.originalDisplay = item.el.style.display
        item.el.style.display = 'none'
        hiddenElements.value.push(item)
      }
    })
    isFullscreen.value = true
  } else {
    mainContainer.classList.remove('fullscreen-mode')
    document.body.style.overflow = ''
    hiddenElements.value.forEach(item => {
      if (item.el) {
        item.el.style.display = item.originalDisplay
      }
    })
    hiddenElements.value = []
    document.querySelector('.tags-action-btn').blur()
    isFullscreen.value = false
  }
}

function handleDropdownCommand(command) {
  const tag = selectedDropdownTag.value
  selectedTag.value = tag
  switch (command) {
    case 'refresh':     refreshSelectedTag(tag); break
    case 'fullscreen':  toggleFullscreen(); break
    case 'close':       closeSelectedTag(tag); break
    case 'closeOthers': closeOthersTags(); break
    case 'closeLeft':   closeLeftTags(); break
    case 'closeRight':  closeRightTags(); break
    case 'closeAll':    closeAllTags(tag); break
  }
}

function refreshSelectedTag(view) {
  proxy.$tab.refreshPage(view)
  if (route.meta.link) {
    useTagsViewStore().delIframeView(route)
  }
}

function closeSelectedTag(view) {
  proxy.$tab.closePage(view).then(({ visitedViews }) => {
    if (isActive(view)) {
      toLastView(visitedViews, view)
    }
  })
}

function closeRightTags() {
  proxy.$tab.closeRightPage(selectedTag.value).then(visitedViews => {
    if (!visitedViews.find(i => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

function closeLeftTags() {
  proxy.$tab.closeLeftPage(selectedTag.value).then(visitedViews => {
    if (!visitedViews.find(i => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

function closeOthersTags() {
  router.push(selectedTag.value).catch(() => { })
  proxy.$tab.closeOtherPage(selectedTag.value).then(() => {
    moveToCurrentTag()
  })
}

function closeAllTags(view) {
  proxy.$tab.closeAllPage().then(({ visitedViews }) => {
    if (affixTags.value.some(tag => tag.path === route.path)) {
      return
    }
    toLastView(visitedViews, view)
  })
}

function toLastView(visitedViews, view) {
  const latestView = visitedViews.slice(-1)[0]
  if (latestView) {
    router.push(latestView.fullPath)
  } else {
    if (view && view.name === 'Dashboard') {
      router.replace({ path: '/redirect' + view.fullPath })
    } else {
      router.push('/')
    }
  }
}

function openMenu(tag, e) {
  left.value = e.clientX
  top.value = e.clientY
  visible.value = true
  selectedTag.value = tag
}

function closeMenu() {
  visible.value = false
}

function handleScroll() {
  closeMenu()
  updateArrowState()
}
</script>

<style lang="scss" scoped>
$tags-bar-height: var(--ui-layout-tags-height, 42px);

.tags-view-container {
  height: $tags-bar-height;
  width: 100%;
  background: var(--ui-bg-tags);
  border-bottom: 1px solid var(--ui-border);
  display: flex;
  align-items: center;
  overflow: hidden;

  $btn-width: 32px;
  $btn-color: var(--ui-text-secondary);
  $btn-hover-bg: var(--ui-primary-soft);
  $btn-hover-color: var(--ui-primary);
  $btn-disabled-color: var(--ui-border);
  $divider: 1px solid var(--ui-border);

  .tags-nav-btn {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: $btn-width;
    height: $btn-width;
    border-radius: 6px;
    cursor: pointer;
    color: $btn-color;
    font-size: 13px;
    user-select: none;
    transition: background 0.15s, color 0.15s;
    margin: 0 2px;

    &:hover:not(.disabled) {
      background: $btn-hover-bg;
      color: $btn-hover-color;
    }

    &.disabled {
      color: $btn-disabled-color;
      cursor: not-allowed;
      width: 0;
      margin: 0;
      overflow: hidden;
    }

    &--left  { border-right: none; }
    &--right { border-left: none; }
  }

  .tags-view-wrapper {
    flex: 1;
    min-width: 0;
    height: $tags-bar-height;
    display: flex;
    align-items: center;

    .tags-view-item {
      display: inline-flex;
      align-items: center;
      position: relative;
      cursor: pointer;
      height: $tags-bar-height;
      line-height: $tags-bar-height;
      border: none;
      color: var(--ui-text-secondary);
      background: transparent;
      padding: 0 14px;
      font-size: 13px;
      margin-left: 0;
      margin-right: 0;
      border-radius: 0;
      text-decoration: none;
      vertical-align: middle;
      transition: background var(--ui-transition-fast), color var(--ui-transition-fast), border-color var(--ui-transition-fast);

      &:first-of-type {
        margin-left: 0;
      }

      &:last-of-type {
        margin-right: 0;
      }

      &:hover {
        background: transparent;
        color: var(--ui-text-primary);
      }
    }
  }

  &:not(.tags-view-container--chrome) .tags-view-wrapper .tags-view-item.active {
    background-color: transparent;
    color: var(--ui-text-primary);
    font-weight: 600;
    box-shadow: none;

    &::before {
      content: none;
    }

    &::after {
      content: '';
      position: absolute;
      right: 14px;
      bottom: 0;
      left: 14px;
      height: 3px;
      background: var(--ui-sidebar-text-active);
    }
  }

  &:not(.tags-view-container--chrome) .tags-view-wrapper .tags-view-item.active.has-icon::before {
    content: none !important;
  }

  .tags-action-dropdown {
    flex-shrink: 0;
    display: flex;
    align-items: center;
  }

  .tags-action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: $btn-width;
    height: $btn-width;
    border-radius: 6px;
    cursor: pointer;
    color: $btn-color;
    font-size: 13px;
    border-left: none;
    user-select: none;
    transition: background 0.15s, color 0.15s;
    margin: 0 2px;

      &:hover {
        background: var(--ui-bg-hover);
        color: $btn-hover-color;
      }
  }

  .tags-action-dropdown,
  .tags-refresh-btn {
    border-left: 1px solid var(--ui-border-subtle);
  }

  .tags-refresh-btn {
    width: $btn-width;
    padding: 0;
  }

  .contextmenu {
    margin: 0;
    background: var(--el-bg-color-overlay, #fff);
    z-index: 3000;
    position: fixed;
    list-style-type: none;
    padding: 5px 0;
    border-radius: var(--ui-radius-popover, 8px);
    font-size: 12px;
    font-weight: 400;
    color: var(--ui-text-primary);
    box-shadow: var(--ui-shadow-popover, 2px 2px 3px 0 rgba(0, 0, 0, .3));
    border: 1px solid var(--ui-border);

    li {
      margin: 0;
      padding: 7px 16px;
      cursor: pointer;

      &:hover {
        background: var(--ui-primary-soft);
        color: var(--ui-primary);
      }
    }
  }

  &.tags-view-container--chrome {
    --chrome-strip-bg: var(--ui-bg-panel);
    --chrome-strip-border: var(--ui-border);
    --chrome-tab-active-bg: var(--ui-primary-soft);
    --chrome-tab-text: var(--ui-text-regular);
    --chrome-tab-text-active: var(--ui-primary);
    --chrome-wing-r: 10px;

    overflow: visible;
    background: var(--chrome-strip-bg);
    border-bottom: 1px solid var(--chrome-strip-border);
    align-items: flex-end;

    .tags-nav-btn {
      align-self: stretch;
      height: auto;
      min-height: $tags-bar-height;
      margin-bottom: 0;
      border-color: var(--chrome-strip-border);
      border-radius: 0;
    }

    .tags-action-dropdown {
      margin-bottom: 0;
    }

    .tags-action-btn {
      border-color: var(--chrome-strip-border);
      border-radius: 0;
    }

    .tags-view-wrapper {
      .tags-view-item {
        display: inline-flex !important;
        align-items: center;
        justify-content: center;
        position: relative;
        z-index: 1;
        height: 32px;
        min-height: 32px;
        margin: 0 0 -1px;
        padding: 0 12px;
        font-size: 13px;
        font-weight: 400;
        line-height: 1.2;
        border: none !important;
        border-radius: 0;
        background: transparent !important;
        color: var(--chrome-tab-text);
        padding-top: 0 !important;
        box-shadow: none !important;
        transition: background 0.12s ease, color 0.12s ease, border-radius 0.12s ease;

        &::before,
        &::after {
          content: '' !important;
          display: block !important;
          position: absolute;
          bottom: 0;
          width: var(--chrome-wing-r);
          height: var(--chrome-wing-r);
          margin: 0 !important;
          pointer-events: none;
          background: transparent !important;
          border-radius: 0 !important;
          transition: box-shadow 0.12s ease;
        }

        &::before {
          left: calc(-1 * var(--chrome-wing-r));
          border-bottom-right-radius: var(--chrome-wing-r) !important;
          box-shadow: none;
        }

        &::after {
          right: calc(-1 * var(--chrome-wing-r));
          border-bottom-left-radius: var(--chrome-wing-r) !important;
          box-shadow: none;
        }

        &:first-of-type {
          margin-left: 6px;
        }

        &:last-of-type {
          margin-right: 10px;
        }

        &:not(.active) + .tags-view-item:not(.active) {
          border-left: 1px solid var(--ui-divider);
          padding-left: 11px;
        }

        &:hover:not(.active) {
          background: var(--ui-bg-hover) !important;
          border-radius: 6px 6px 0 0;
          color: var(--ui-text-primary);
        }

        &.active {
          height: 33px;
          min-height: 33px;
          padding: 0 14px;
          color: var(--chrome-tab-text-active) !important;
          font-weight: 500;
          background: var(--chrome-tab-active-bg) !important;
          border: none !important;
          border-radius: var(--chrome-wing-r) var(--chrome-wing-r) 0 0;
          box-shadow: var(--ui-shadow-panel);

          &::before {
            box-shadow: calc(var(--chrome-wing-r) * 0.5) calc(var(--chrome-wing-r) * 0.5) 0 calc(var(--chrome-wing-r) * 0.5) var(--chrome-tab-active-bg);
          }

          &::after {
            box-shadow: calc(var(--chrome-wing-r) * -0.5) calc(var(--chrome-wing-r) * 0.5) 0 calc(var(--chrome-wing-r) * 0.5) var(--chrome-tab-active-bg);
          }
        }
      }
    }
  }
}
</style>

<style lang="scss">
.tags-view-wrapper {
  .tags-view-item {
    .tags-close-btn {
      position: absolute;
      right: -1px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 14px;
      height: 14px;
      margin-left: 0;
      border-radius: 50%;
      opacity: 0;
      transition: opacity var(--ui-transition-fast), background-color var(--ui-transition-fast);
      cursor: pointer;
      
      .tags-close-icon {
        width: 1em;
        height: 1em;
        vertical-align: 0;
        line-height: 1;
        display: inline-flex;
        align-items: center;
        justify-content: center;
      }
      
      &:hover {
        background-color: color-mix(in srgb, var(--ui-primary) 18%, transparent);
        
        .tags-close-icon {
          color: var(--ui-primary);
        }
      }
    }

    &:hover .tags-close-btn,
    &:focus-visible .tags-close-btn {
      opacity: 1;
    }
  }
}

.tags-view-container--chrome .tags-view-item .tags-close-btn {
  opacity: 1;
}

/* 页签全屏模式样式 */
.main-container.fullscreen-mode {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  margin-left: 0 !important;
  transition: none !important;
}

.main-container.fullscreen-mode .fixed-header {
  display: block !important;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100% !important;
  z-index: 1000;
  transition: none !important;
}

.main-container.fullscreen-mode .fixed-header .navbar {
  display: none !important;
}

.main-container.fullscreen-mode .app-main {
  position: fixed;
  top: var(--ui-layout-tags-height, 42px);
  left: 0;
  right: 0;
  bottom: 0;
  margin: 0 !important;
  padding: 0 !important;
  height: calc(100vh - var(--ui-layout-tags-height, 42px)) !important;
  min-height: calc(100vh - var(--ui-layout-tags-height, 42px)) !important;
  overflow: auto;
}
</style>
