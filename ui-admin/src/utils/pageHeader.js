const HIDDEN_PAGE_PATHS = new Set([
  '/index',
  '/login',
  '/register',
  '/lock',
  '/401',
  '/404'
])

const PAGE_DESCRIPTIONS = {
  个人中心: '查看和维护当前登录账号的资料、安全设置与头像信息。',
  分配角色: '为指定用户分配角色，控制账号可访问的菜单与功能权限。',
  分配用户: '维护指定角色下的授权用户，确保角色权限按需分配。',
  用户管理: '统一维护后台用户、所属部门、角色分配和账号状态。',
  角色管理: '配置系统角色、数据权限和菜单授权范围。',
  菜单管理: '维护系统菜单、按钮权限和前端路由入口。',
  部门管理: '维护组织架构、部门层级和负责人信息。',
  字典管理: '维护系统字典类型，为业务字段提供标准选项。',
  字典数据: '维护指定字典类型下的键值、排序和状态。',
  参数设置: '维护系统运行参数和业务配置项。',
  统一日志: '统一查看操作日志与登录日志，入口收敛后排查链路更直接。',
  运行日志: '查看应用运行日志，支持按级别、时间和关键词定位问题。',
  '慢 SQL 日志': '查看慢 SQL 执行记录，辅助定位数据库性能瓶颈。',
  消息队列台账: '查看消息队列处理台账，追踪消息投递、消费和异常状态。',
  在线用户: '查看当前在线会话，按需执行会话管理操作。',
  定时任务: '维护调度任务、执行策略和运行状态。',
  调度日志: '查看定时任务执行日志和异常信息。',
  数据监控: '查看数据库连接池与 SQL 监控信息。',
  宿主机监控: '查看宿主机 CPU、内存、磁盘和 JVM 运行状态。',
  缓存监控: '查看 Redis 缓存运行指标和命令统计。',
  缓存列表: '查看缓存键名、内容与过期状态。',
  接口文档: '查看后端接口文档、调试入口和请求响应结构。',
  // IIP 发票积分平台（注意：iip 的会员页菜单定名「会员管理」，与系统「用户管理」区分，勿占用「用户管理」条目）
  数据概览: '汇总发票积分平台的核心经营指标与近 7 日趋势，帮助运营快速掌握平台运行状况。',
  会员管理: '查看和维护小程序会员的注册资料、登录状态与账号状态。',
  商户管理: '维护入驻商户的资料与审核状态，支撑发票归属与券核销场景。',
  发票审核: '审核会员上传的消费发票，通过后按活动比例发放积分。',
  积分流水: '查询会员积分的获取、消耗、过期与调整记录，追踪每笔积分变动。',
  券管理: '维护积分商城的券定义、库存、兑换规则与有效期。',
  兑换记录: '查询会员的券兑换记录与核销状态，支撑对账与统计分析。',
  活动管理: '维护发票积分活动的起止时间、积分比例与参与商户、发券配置。'
}

const PAGE_GROUPS = {
  个人中心: 'ACCOUNT CENTER',
  分配角色: 'AUTH CENTER',
  分配用户: 'AUTH CENTER',
  用户管理: 'AUTH CENTER',
  角色管理: 'AUTH CENTER',
  菜单管理: 'AUTH CENTER',
  部门管理: 'AUTH CENTER',
  字典管理: 'SYSTEM CENTER',
  字典数据: 'SYSTEM CENTER',
  参数设置: 'SYSTEM CENTER',
  统一日志: 'LOG CENTER',
  运行日志: 'LOG CENTER',
  '慢 SQL 日志': 'LOG CENTER',
  消息队列台账: 'LOG CENTER',
  调度日志: 'LOG CENTER',
  在线用户: 'MONITOR CENTER',
  定时任务: 'MONITOR CENTER',
  数据监控: 'MONITOR CENTER',
  宿主机监控: 'MONITOR CENTER',
  缓存监控: 'MONITOR CENTER',
  缓存列表: 'MONITOR CENTER',
  接口文档: 'TOOL CENTER',
  数据概览: 'IIP CENTER',
  会员管理: 'IIP CENTER',
  商户管理: 'IIP CENTER',
  发票审核: 'IIP CENTER',
  积分流水: 'IIP CENTER',
  券管理: 'IIP CENTER',
  兑换记录: 'IIP CENTER',
  活动管理: 'IIP CENTER'
}

export function buildPageHeader(route = {}) {
  const title = resolveTitle(route)
  if (!shouldShowPageHeader(route, title)) {
    return {
      visible: false,
      eyebrow: '',
      title: '',
      description: ''
    }
  }

  return {
    visible: true,
    eyebrow: resolveEyebrow(route, title),
    title,
    description: resolveDescription(route, title)
  }
}

export function shouldShowPageHeader(route = {}, title = resolveTitle(route)) {
  const meta = route.meta || {}
  const path = route.path || ''

  if (!title || meta.hidePageHeader || meta.link) {
    return false
  }

  return !HIDDEN_PAGE_PATHS.has(path) && !path.startsWith('/redirect')
}

function resolveTitle(route) {
  const meta = route.meta || {}
  return trimText(meta.pageTitle || meta.title)
}

function resolveDescription(route, title) {
  const meta = route.meta || {}
  return trimText(meta.description || meta.pageDescription || PAGE_DESCRIPTIONS[title])
    || `${title}用于查看和维护相关业务数据，帮助快速完成当前页面的查询与处理。`
}

function resolveEyebrow(route, title) {
  const meta = route.meta || {}
  const customEyebrow = trimText(meta.eyebrow || meta.pageGroup)
  if (customEyebrow) {
    return customEyebrow.toUpperCase()
  }

  return PAGE_GROUPS[title] || 'PAGE CENTER'
}

function trimText(value) {
  return typeof value === 'string' ? value.trim() : ''
}
