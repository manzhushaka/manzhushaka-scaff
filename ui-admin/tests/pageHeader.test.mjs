import assert from 'node:assert/strict'
import { buildPageHeader } from '../src/utils/pageHeader.js'

const logHeader = buildPageHeader({
  path: '/log/logCenter',
  meta: {
    title: '统一日志'
  }
})

assert.equal(logHeader.visible, true)
assert.equal(logHeader.eyebrow, 'LOG CENTER')
assert.equal(logHeader.title, '统一日志')
assert.equal(logHeader.description, '统一查看操作日志与登录日志，入口收敛后排查链路更直接。')

const customHeader = buildPageHeader({
  path: '/custom/page',
  meta: {
    title: '业务看板',
    description: '查看业务指标、处理进度和异常预警。'
  }
})

assert.equal(customHeader.visible, true)
assert.equal(customHeader.eyebrow, 'PAGE CENTER')
assert.equal(customHeader.description, '查看业务指标、处理进度和异常预警。')

const fallbackHeader = buildPageHeader({
  path: '/custom/list',
  meta: {
    title: '业务列表'
  }
})

assert.equal(fallbackHeader.visible, true)
assert.equal(fallbackHeader.description, '业务列表用于查看和维护相关业务数据，帮助快速完成当前页面的查询与处理。')

const hiddenHeader = buildPageHeader({
  path: '/index',
  meta: {
    title: '首页'
  }
})

assert.equal(hiddenHeader.visible, false)
