import defaultSettings from '@/settings'
import { useDynamicTitle } from '@/utils/dynamicTitle'

const UNIFIED_NAV_TYPE = 1
const COOL_TOWER_THEME = '#0ea5e9'
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
      theme: COOL_TOWER_THEME,
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
      }
    }
  })

export default useSettingsStore
