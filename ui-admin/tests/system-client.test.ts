import test from 'node:test';
import assert from 'node:assert/strict';
import { buildSystemAuthHeader, unwrapSystemResponse } from '../src/api/system-client.ts';

test('keeps the raw system token header value', () => {
  assert.equal(buildSystemAuthHeader('plain-token'), 'plain-token');
  assert.equal(buildSystemAuthHeader(''), undefined);
});

test('throws when the system api envelope reports a business error', () => {
  assert.throws(
    () => unwrapSystemResponse({ code: 500, message: 'No static resource api/system/dicts/types.', data: null }),
    /No static resource api\/system\/dicts\/types\./,
  );
});

test('unwraps the system api payload when the response succeeds', () => {
  assert.deepEqual(
    unwrapSystemResponse({
      code: 0,
      message: 'ok',
      data: {
        total: 1,
        records: [{ id: 100, dictName: '用户状态' }],
      },
    }),
    {
      total: 1,
      records: [{ id: 100, dictName: '用户状态' }],
    },
  );
});
