<template>
  <el-drawer v-model="showSettings" :withHeader="false" :lock-scroll="false" direction="rtl" size="300px">

    <h3 class="drawer-title">系统布局配置</h3>

    <div class="drawer-item">
      <span>开启页签</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.tagsView" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>持久化标签页</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.tagsViewPersist" :disabled="!settingsStore.tagsView" @change="tagsViewPersistChange" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>显示页签图标</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.tagsIcon" :disabled="!settingsStore.tagsView" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>标签页样式</span>
      <span class="comp-style">
        <el-radio-group v-model="settingsStore.tagsViewStyle" :disabled="!settingsStore.tagsView" size="small">
          <el-radio-button label="card">卡片</el-radio-button>
          <el-radio-button label="chrome">谷歌</el-radio-button>
        </el-radio-group>
      </span>
    </div>

    <div class="drawer-item">
      <span>固定 Header</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.fixedHeader" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>显示 Logo</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.sidebarLogo" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>动态标题</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.dynamicTitle" @change="dynamicTitleChange" class="drawer-switch" />
      </span>
    </div>

    <div class="drawer-item">
      <span>底部版权</span>
      <span class="comp-style">
        <el-switch v-model="settingsStore.footerVisible" class="drawer-switch" />
      </span>
    </div>

    <el-divider />

    <el-button type="primary" plain icon="DocumentAdd" @click="saveSetting">保存配置</el-button>
    <el-button plain icon="Refresh" @click="resetSetting">重置配置</el-button>
  </el-drawer>

</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'

const { proxy } = getCurrentInstance()
const settingsStore = useSettingsStore()
const showSettings = ref(false)
const tagsViewPersist = ref(settingsStore.tagsViewPersist)
const storeSettings = computed(() => settingsStore)

/** 是否需要dynamicTitle */
function dynamicTitleChange() {
  useSettingsStore().setTitle(useSettingsStore().title)
}

function tagsViewPersistChange(val) {
  settingsStore.tagsViewPersist = val
  tagsViewPersist.value = val
}

function saveSetting() {
  proxy.$modal.loading("正在保存到本地，请稍候...")
  if (!tagsViewPersist.value) {
    proxy.$cache.local.remove('tags-view-visited')
  }
  let layoutSetting = {
    "tagsView": storeSettings.value.tagsView,
    "tagsIcon": storeSettings.value.tagsIcon,
    "tagsViewStyle": storeSettings.value.tagsViewStyle,
    "tagsViewPersist": storeSettings.value.tagsViewPersist,
    "fixedHeader": storeSettings.value.fixedHeader,
    "sidebarLogo": storeSettings.value.sidebarLogo,
    "dynamicTitle": storeSettings.value.dynamicTitle,
    "footerVisible": storeSettings.value.footerVisible,
    "navType": storeSettings.value.navType
  }
  localStorage.setItem("layout-setting", JSON.stringify(layoutSetting))
  setTimeout(proxy.$modal.closeLoading(), 1000)
}

function resetSetting() {
  proxy.$cache.local.remove('tags-view-visited')
  proxy.$modal.loading("正在清除设置缓存并刷新，请稍候...")
  localStorage.removeItem("layout-setting")
  setTimeout("window.location.reload()", 1000)
}

function openSetting() {
  showSettings.value = true
}

defineExpose({
  openSetting
})
</script>

<style lang='scss' scoped>
.drawer-item {
  color: var(--el-text-color-regular, rgba(0, 0, 0, 0.65));
  padding: 12px 0;
  font-size: 14px;

  .comp-style {
    float: right;
    margin: -3px 8px 0px 0px;
  }
}

// 导航模式
.nav-wrap {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 10px;
  margin-bottom: 20px;

  .activeItem {
    border: 2px solid var(--el-color-primary) !important;
  }

  .item {
    position: relative;
    margin-right: 16px;
    cursor: pointer;
    width: 56px;
    height: 48px;
    border-radius: 4px;
    background: #f0f2f5;
    border: 2px solid transparent;
  }

  .left {
    b:first-child {
      display: block;
      height: 30%;
      background: #fff;
    }
    b:last-child {
      width: 30%;
      background: #1b2a47;
      position: absolute;
      height: 100%;
      top: 0;
      border-radius: 4px 0 0 4px;
    }
  }
  .mix {
    b:first-child {
      border-radius: 4px 4px 0 0;
      display: block;
      height: 30%;
      background: #1b2a47;
    }
    b:last-child {
      width: 30%;
      background: #1b2a47;
      position: absolute;
      height: 70%;
      border-radius: 0 0 0 4px;
    }
  }
  .top {
    b:first-child {
      display: block;
      height: 30%;
      background: #1b2a47;
      border-radius: 4px 4px 0 0;
    }
  }
}
</style>
