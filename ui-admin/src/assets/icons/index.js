const SVG_NAMESPACE = 'http://www.w3.org/2000/svg'
const SPRITE_ID = 'ui-svg-icon-sprite'
const iconModules = import.meta.glob('./svg/*.svg', {
  eager: true,
  query: '?raw',
  import: 'default'
})

/**
 * 注册本地 SVG 图标精灵。
 */
export function registerSvgIcons() {
  if (typeof document === 'undefined' || document.getElementById(SPRITE_ID)) {
    return
  }

  const sprite = document.createElementNS(SVG_NAMESPACE, 'svg')
  sprite.id = SPRITE_ID
  sprite.setAttribute('aria-hidden', 'true')
  sprite.style.position = 'absolute'
  sprite.style.width = '0'
  sprite.style.height = '0'
  sprite.style.overflow = 'hidden'

  Object.entries(iconModules).forEach(([path, source]) => {
    sprite.appendChild(createSymbol(path, source))
  })
  document.body.prepend(sprite)
}

/**
 * 将单个 SVG 源文件转换为安全的 symbol。
 *
 * @param {string} path SVG 文件路径
 * @param {string} source SVG 文件内容
 * @returns {SVGSymbolElement} 图标 symbol
 */
function createSymbol(path, source) {
  const parsedDocument = new DOMParser().parseFromString(source, 'text/html')
  const sourceSvg = parsedDocument.querySelector('svg')
  if (!sourceSvg) {
    throw new Error(`无法解析 SVG 图标: ${path}`)
  }

  sanitizeSvg(sourceSvg)
  const symbol = document.createElementNS(SVG_NAMESPACE, 'symbol')
  symbol.id = `icon-${resolveIconName(path)}`
  symbol.setAttribute('viewBox', resolveViewBox(sourceSvg))
  while (sourceSvg.firstChild) {
    symbol.appendChild(sourceSvg.firstChild)
  }
  return symbol
}

/**
 * 移除 SVG 中不需要的可执行或外部资源内容。
 *
 * @param {SVGElement} sourceSvg SVG 根节点
 */
function sanitizeSvg(sourceSvg) {
  sourceSvg.querySelectorAll('script, foreignObject, style').forEach(element => element.remove())
  sourceSvg.querySelectorAll('*').forEach(element => {
    Array.from(element.attributes).forEach(attribute => {
      const attributeName = attribute.name.toLowerCase()
      if (attributeName.startsWith('on') || attributeName === 'href' || attributeName === 'xlink:href') {
        element.removeAttribute(attribute.name)
      }
    })
  })
}

/**
 * 解析图标 viewBox，兼容仅声明宽高的旧 SVG。
 *
 * @param {SVGElement} sourceSvg SVG 根节点
 * @returns {string} viewBox
 */
function resolveViewBox(sourceSvg) {
  const viewBox = sourceSvg.getAttribute('viewBox')
  if (viewBox) {
    return viewBox
  }
  const width = Number.parseFloat(sourceSvg.getAttribute('width'))
  const height = Number.parseFloat(sourceSvg.getAttribute('height'))
  if (!Number.isFinite(width) || !Number.isFinite(height)) {
    throw new Error('SVG 图标缺少有效的 viewBox 或宽高')
  }
  return `0 0 ${width} ${height}`
}

/**
 * 从文件路径提取图标名称。
 *
 * @param {string} path SVG 文件路径
 * @returns {string} 图标名称
 */
function resolveIconName(path) {
  return path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.svg'))
}
