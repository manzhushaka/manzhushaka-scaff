import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const systemApiSource = readFileSync(resolve(import.meta.dirname, '../src/api/system.ts'), 'utf8');

test('system api reuses the shared request client instead of defining another axios pipeline', () => {
  assert.match(systemApiSource, /import request from '\.\/request';/);
  assert.doesNotMatch(systemApiSource, /import axios from 'axios';/);
  assert.doesNotMatch(systemApiSource, /dispatchMockRequest/);
  assert.doesNotMatch(systemApiSource, /axios\.create/);
  assert.doesNotMatch(systemApiSource, /interceptors/);
});
