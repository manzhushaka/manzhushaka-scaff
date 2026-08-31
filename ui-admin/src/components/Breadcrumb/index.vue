<template>
  <a-breadcrumb class="app-breadcrumb">
    <a-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
      <span v-if="item.redirect === 'noRedirect' || index == levelList.length - 1" class="no-redirect">{{ item.meta.title }}</span>
      <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
    </a-breadcrumb-item>
  </a-breadcrumb>
</template>

<script setup>
import usePermissionStore from '@/store/modules/permission'

const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const levelList = ref([])

function getBreadcrumb() {
  // only show routes with meta.title
  let matched = []
  const pathNum = findPathNum(route.path)
  // multi-level menu
  if (pathNum > 2) {
    const reg = /\/\w+/gi
    const pathList = route.path.match(reg).map((item, index) => {
      if (index !== 0) item = item.slice(1)
      return item
    })
    getMatched(pathList, permissionStore.defaultRoutes, matched)
  } else {
    matched = route.matched.filter((item) => item.meta && item.meta.title)
  }
  // 判断是否为首页
  if (!isDashboard(matched[0])) {
    matched = [{ path: "/index", meta: { title: "首页" } }].concat(matched)
  }
  levelList.value = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
}
function findPathNum(str, char = "/") {
  let index = str.indexOf(char)
  let num = 0
  while (index !== -1) {
    num++
    index = str.indexOf(char, index + 1)
  }
  return num
}
function getMatched(pathList, routeList, matched) {
  let data = routeList.find(item => item.path == pathList[0] || (item.name += '').toLowerCase() == pathList[0])
  if (data) {
    matched.push(data)
    if (data.children && pathList.length) {
      pathList.shift()
      getMatched(pathList, data.children, matched)
    }
  }
}
function isDashboard(route) {
  const name = route && route.name
  if (!name) {
    return false
  }
  return name.trim() === 'Index'
}
function handleLink(item) {
  const { redirect, path } = item
  if (redirect) {
    router.push(redirect)
    return
  }
  router.push(path)
}

watchEffect(() => {
  // if you go to the redirect page, do not update the breadcrumbs
  if (route.path.startsWith('/redirect/')) {
    return
  }
  getBreadcrumb()
})
getBreadcrumb()
</script>

<style lang='scss' scoped>
.app-breadcrumb.arco-breadcrumb {
  display: flex;
  align-items: center;
  width: 100%;
  min-width: 0;
  height: 100%;
  font-size: 14px;
  line-height: 1;
  white-space: nowrap;
  overflow: hidden;

  :deep(.arco-breadcrumb-item) {
    flex-shrink: 0;
    white-space: nowrap;
  }

  :deep(.arco-breadcrumb-item-link),
  :deep(.arco-breadcrumb-item-separator) {
    white-space: nowrap;
  }

  .no-redirect {
    color: var(--ui-text-muted);
    cursor: text;
  }
}
</style>
