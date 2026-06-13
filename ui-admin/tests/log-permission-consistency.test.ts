import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const projectRoot = resolve(import.meta.dirname, '..');

function readProjectFile(path: string) {
  return readFileSync(resolve(projectRoot, path), 'utf8');
}

test('log pages use the permission code seeded by the backend menu data', () => {
  const initSql = readProjectFile('../sql/manzhushaka_init.sql');
  const loginLogsPage = readProjectFile('src/views/system/login-logs.vue');
  const opLogsPage = readProjectFile('src/views/system/op-logs.vue');

  assert.match(initSql, /'system:log:view'/);
  assert.match(loginLogsPage, /v-permission="'system:log:view'"/);
  assert.match(opLogsPage, /v-permission="'system:log:view'"/);
  assert.doesNotMatch(loginLogsPage, /system:login-log:query/);
  assert.doesNotMatch(opLogsPage, /system:op-log:query/);
});
