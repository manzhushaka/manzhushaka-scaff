import test from 'node:test';
import assert from 'node:assert/strict';
import { DEFAULT_PLATFORM_NAME, DEFAULT_PLATFORM_SUBTITLE, normalizePlatformConfig } from '../src/platform-config.ts';

test('falls back to the default platform name when api data is empty', () => {
  assert.deepEqual(normalizePlatformConfig({ platformName: '   ', platformSubtitle: '   ', logoUrl: '' }), {
    platformName: DEFAULT_PLATFORM_NAME,
    platformSubtitle: DEFAULT_PLATFORM_SUBTITLE,
    logoUrl: '',
  });
});

test('keeps trimmed platform branding values from the api payload', () => {
  assert.deepEqual(
    normalizePlatformConfig({
      platformName: ' 山海平台 ',
      platformSubtitle: ' Unified Operations Center ',
      logoUrl: ' https://cdn.example.com/logo.png ',
    }),
    {
      platformName: '山海平台',
      platformSubtitle: 'Unified Operations Center',
      logoUrl: 'https://cdn.example.com/logo.png',
    },
  );
});
