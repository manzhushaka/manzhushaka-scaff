import test from 'node:test';
import assert from 'node:assert/strict';
import { formatDateTime } from '../src/views/system/shared.ts';

test('formats iso date time strings as yyyy-MM-dd HH:mm:ss', () => {
  assert.equal(formatDateTime('2026-06-09T19:00:00'), '2026-06-09 19:00:00');
});

test('formats jackson local date time arrays as yyyy-MM-dd HH:mm:ss', () => {
  assert.equal(formatDateTime([2026, 6, 9, 0, 28, 46]), '2026-06-09 00:28:46');
});

test('returns fallback for empty values', () => {
  assert.equal(formatDateTime(null), '--');
  assert.equal(formatDateTime(undefined), '--');
});
