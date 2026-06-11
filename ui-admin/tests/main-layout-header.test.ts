import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const mainLayoutSource = readFileSync(
  resolve(import.meta.dirname, '../src/layout/MainLayout.vue'),
  'utf8',
);

test('main layout keeps header and visited tabs on the same top rail height', () => {
  assert.match(mainLayoutSource, /--top-rail-height:\s*42px/);
  assert.match(mainLayoutSource, /\.header-copy\s*\{[\s\S]*?min-height:\s*var\(--top-rail-height\)/);
  assert.match(mainLayoutSource, /\.visited-tab\s*\{[\s\S]*?min-height:\s*var\(--top-rail-height\)/);
});

test('main layout uses a compact non-circular profile avatar treatment', () => {
  assert.match(mainLayoutSource, /\.profile-avatar\s*\{[^}]*border-radius:\s*(?!50%)[^;]+;/);
  assert.doesNotMatch(mainLayoutSource, /\.profile-avatar\s*\{[^}]*border-radius:\s*50%/);
});
