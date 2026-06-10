import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildCacheEntryQuery,
  formatCacheTtl,
  mapCacheEntryRow,
  stringifyCacheValue,
} from '../src/views/system/cache-support.ts';

test('formatCacheTtl distinguishes persistent and expiring keys', () => {
  assert.equal(formatCacheTtl(-1), '永久');
  assert.equal(formatCacheTtl(45), '45s');
  assert.equal(formatCacheTtl(null), '--');
});

test('buildCacheEntryQuery trims keyword', () => {
  assert.deepEqual(buildCacheEntryQuery({ keyword: ' auth:* ', limit: 50 }), {
    keyword: 'auth:*',
    limit: 50,
  });
});

test('stringifyCacheValue pretty prints structured values', () => {
  assert.equal(
    stringifyCacheValue({ token: 'abc', loginTime: '2026-06-10 09:00:00' }),
    '{\n  "token": "abc",\n  "loginTime": "2026-06-10 09:00:00"\n}',
  );
});

test('mapCacheEntryRow formats ttl and expire time text', () => {
  const row = mapCacheEntryRow({
    key: 'auth:captcha:test',
    type: 'string',
    ttlSeconds: 120,
    expireAt: '2026-06-10T09:12:00',
    valuePreview: 'ABCD',
  });

  assert.equal(row.key, 'auth:captcha:test');
  assert.equal(row.type, 'STRING');
  assert.equal(row.ttlText, '120s');
  assert.equal(row.expireAtText, '2026-06-10 09:12:00');
  assert.equal(row.valuePreview, 'ABCD');
});
