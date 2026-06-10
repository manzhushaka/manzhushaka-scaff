import test from 'node:test';
import assert from 'node:assert/strict';
import { dispatchMockRequest } from '../src/api/mock.ts';

test('dispatchMockRequest supports object payloads for mock post requests', async () => {
  const response = await dispatchMockRequest({
    url: '/auth/login',
    method: 'post',
    data: {
      username: 'admin',
      password: 'Admin@123456',
      captchaKey: 'mock-captcha-key',
      captchaCode: 'ABCD',
    } as never,
  });

  assert.equal(response.code, 0);
  assert.equal(response.data.token, 'mock-token-admin');
  assert.equal(response.data.userInfo.username, 'admin');
});
