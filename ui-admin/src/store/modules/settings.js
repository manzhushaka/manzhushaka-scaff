import defaultSettings from '@/settings'
import { useDynamicTitle } from '@/utils/dynamicTitle'
import { applyUiTheme, getStoredUiTheme, normalizeUiTheme } from '@/utils/uiTheme'

const UNIFIED_NAV_TYPE = 1
const ARCO_ORANGE_PRIMARY = '#f76823'
const COOL_TOWER_SIDE_THEME = 'theme-dark'

const { showSettings, tagsView, tagsViewPersist, tagsIcon, tagsViewStyle, fixedHeader, sidebarLogo, dynamicTitle, footerVisible, footerContent } = defaultSettings

function getStorageSetting() {
  try {
    return JSON.parse(localStorage.getItem('layout-setting')) || {}
  } catch (error) {
    return {}
  }
}

const storageSetting = getStorageSetting()

const useSettingsStore = defineStore(
  'settings',
  {
    state: () => ({
      title: '',
      theme: ARCO_ORANGE_PRIMARY,
      uiTheme: getStoredUiTheme(),
      sideTheme: COOL_TOWER_SIDE_THEME,
      showSettings: showSettings,
      navType: UNIFIED_NAV_TYPE,
      tagsView: storageSetting.tagsView === undefined ? tagsView : storageSetting.tagsView,
      tagsViewPersist: storageSetting.tagsViewPersist === undefined ? tagsViewPersist : storageSetting.tagsViewPersist,
      tagsIcon: storageSetting.tagsIcon === undefined ? tagsIcon : storageSetting.tagsIcon,
      tagsViewStyle: storageSetting.tagsViewStyle === undefined ? tagsViewStyle : storageSetting.tagsViewStyle,
      fixedHeader: storageSetting.fixedHeader === undefined ? fixedHeader : storageSetting.fixedHeader,
      sidebarLogo: storageSetting.sidebarLogo === undefined ? sidebarLogo : storageSetting.sidebarLogo,
      dynamicTitle: storageSetting.dynamicTitle === undefined ? dynamicTitle : storageSetting.dynamicTitle,
      footerVisible: storageSetting.footerVisible === undefined ? footerVisible : storageSetting.footerVisible,
      footerContent: footerContent,
      isDark: false
    }),
    actions: {
      // 修改布局设置
      changeSetting(data) {
        const { key, value } = data
        if (key === 'navType') {
          this.navType = UNIFIED_NAV_TYPE
          return
        }
        if (key === 'uiTheme') {
          this.setUiTheme(value)
          return
        }
        if (key === 'theme' || key === 'sideTheme' || key === 'isDark') {
          return
        }
        if (this.hasOwnProperty(key)) {
          this[key] = value
        }
      },
      // 设置网页标题
      setTitle(title) {
        this.title = title
        useDynamicTitle()
      },
      /**
       * 切换并持久化 Arco 界面主题。
       *
       * @param {string} uiTheme 主题名称
       */
      setUiTheme(uiTheme) {
        this.uiTheme = applyUiTheme(normalizeUiTheme(uiTheme))
        const layoutSetting = getStorageSetting()
        localStorage.setItem('layout-setting', JSON.stringify({
          ...layoutSetting,
          uiTheme: this.uiTheme
        }))
      }
    }
  })

export default useSettingsStore
