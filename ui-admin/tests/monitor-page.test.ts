import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const monitorPageSource = readFileSync(
  resolve(import.meta.dirname, '../src/views/system/monitor.vue'),
  'utf8',
);
const serviceMonitorPageSource = readFileSync(
  resolve(import.meta.dirname, '../src/views/system/monitor-diagnostics.vue'),
  'utf8',
);
const componentMapSource = readFileSync(
  resolve(import.meta.dirname, '../src/router/component-map.ts'),
  'utf8',
);
const mockSource = readFileSync(
  resolve(import.meta.dirname, '../src/api/mock.ts'),
  'utf8',
);

test('hardware monitor page does not render removed duplicated sections', () => {
  assert.doesNotMatch(monitorPageSource, /资源底座/);
  assert.doesNotMatch(monitorPageSource, /值班提示/);
  assert.doesNotMatch(monitorPageSource, /CPU 余量/);
  assert.doesNotMatch(monitorPageSource, /内存余量/);
  assert.doesNotMatch(monitorPageSource, /资源得分/);
  assert.doesNotMatch(monitorPageSource, /hardwareMetricCards/);
  assert.doesNotMatch(monitorPageSource, /hardwareBaseStats/);
  assert.doesNotMatch(monitorPageSource, /hardwareGuideItems/);
  assert.doesNotMatch(monitorPageSource, /hardwareSpotlightItems/);
});

test('service monitor page does not inline slow sql and log detail panels anymore', () => {
  assert.doesNotMatch(serviceMonitorPageSource, /a-tab-pane key="slow-sql"/);
  assert.doesNotMatch(serviceMonitorPageSource, /a-tab-pane key="live-log"/);
  assert.doesNotMatch(serviceMonitorPageSource, /logTailText/);
  assert.doesNotMatch(serviceMonitorPageSource, /useMonitorDiagnostics/);
});

test('monitor routes and mock menus include standalone slow sql and live log pages', () => {
  assert.match(componentMapSource, /'system\/monitor-slow-sql'/);
  assert.match(componentMapSource, /'system\/monitor-live-log'/);
  assert.match(mockSource, /menuName: '慢 SQL'/);
  assert.match(mockSource, /component: 'system\/monitor-slow-sql'/);
  assert.match(mockSource, /menuName: '在线日志'/);
  assert.match(mockSource, /component: 'system\/monitor-live-log'/);
});
