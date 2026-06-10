import test from 'node:test';
import assert from 'node:assert/strict';
import { AUTH_BOOTSTRAP_REQUEST_OPTIONS, createAuthApi } from '../src/api/auth.ts';

test('auth bootstrap requests use silent error handling', async () => {
  const calls: Array<{ url: string; config?: unknown }> = [];
  const api = createAuthApi({
    get: async <T>(url: string, config?: unknown) => {
      calls.push({ url, config });
      return null as T;
    },
    post: async <T>() => null as T,
  });

  await api.fetchProfile();
  await api.fetchMenus();
  await api.fetchPermissions();

  assert.deepEqual(calls, [
    { url: '/auth/me', config: AUTH_BOOTSTRAP_REQUEST_OPTIONS },
    { url: '/auth/menus', config: AUTH_BOOTSTRAP_REQUEST_OPTIONS },
    { url: '/auth/permissions', config: AUTH_BOOTSTRAP_REQUEST_OPTIONS },
  ]);
});
