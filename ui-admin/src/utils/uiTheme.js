export const UI_THEME_ORANGE = 'arco-orange'
export const UI_THEME_PURPLE = 'arco-purple'

export const UI_THEME_OPTIONS = [
  { value: UI_THEME_ORANGE, label: '橙白', color: '#f76823' },
  { value: UI_THEME_PURPLE, label: '紫白', color: '#722ed1' }
]

const UI_THEME_VALUES = new Set(UI_THEME_OPTIONS.map(item => item.value))

/**
 * 规范化界面主题名称。
 *
 * @param {string} themeName 主题名称
 * @return {string} 有效的主题名称
 */
export function normalizeUiTheme(themeName) {
  return UI_THEME_VALUES.has(themeName) ? themeName : UI_THEME_ORANGE
}

/**
 * 读取本地持久化的界面主题。
 *
 * @return {string} 当前主题名称
 */
export function getStoredUiTheme() {
  if (typeof localStorage === 'undefined') {
    return UI_THEME_ORANGE
  }
  try {
    const setting = JSON.parse(localStorage.getItem('layout-setting')) || {}
    return normalizeUiTheme(setting.uiTheme)
  } catch (error) {
    return UI_THEME_ORANGE
  }
}

/**
 * 将主题属性同步到页面根节点。
 *
 * @param {string} themeName 主题名称
 * @return {string} 实际应用的主题名称
 */
export function applyUiTheme(themeName) {
  const normalizedTheme = normalizeUiTheme(themeName)
  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('data-ui-theme', normalizedTheme)
    document.body?.setAttribute('data-ui-theme', normalizedTheme)
  }
  return normalizedTheme
}
