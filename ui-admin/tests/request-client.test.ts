import test from 'node:test';
import assert from 'node:assert/strict';
import {
  SESSION_EXPIRED_MESSAGE,
  normalizeRequestError,
  shouldNotifyRequestError,
} from '../src/api/request-client.ts';

test('normalizes invalid token errors to a friendly expired-session message', () => {
  const error = normalizeRequestError(new Error('token 无效：f9ff2c8f-bb64-4d6e-8aa6-b940da0c605b'));

  assert.equal(error.message, SESSION_EXPIRED_MESSAGE);
});

test('suppresses request toasts when the caller opts into silent errors', () => {
  assert.equal(shouldNotifyRequestError({ silentError: true }), false);
  assert.equal(shouldNotifyRequestError({ silentError: false }), true);
  assert.equal(shouldNotifyRequestError(), true);
});
