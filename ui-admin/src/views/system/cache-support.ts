import type {
  CacheEntryQuery,
  CacheEntryRow,
  CacheEntryVO,
} from '@/types/system';
import { formatDateTime } from './shared';

export const cacheLimitOptions = [
  { label: '20 条', value: 20 },
  { label: '50 条', value: 50 },
  { label: '100 条', value: 100 },
];

export function buildCacheEntryQuery(payload: {
  keyword: string;
  limit: number;
}): CacheEntryQuery {
  return {
    keyword: payload.keyword.trim(),
    limit: payload.limit,
  };
}

export function formatCacheTtl(ttlSeconds: number | null | undefined) {
  if (ttlSeconds == null) {
    return '--';
  }
  if (ttlSeconds === -1) {
    return '永久';
  }
  if (ttlSeconds === -2) {
    return '已过期';
  }
  return `${ttlSeconds}s`;
}

export function stringifyCacheValue(value: unknown) {
  if (value == null) {
    return '--';
  }
  if (typeof value === 'string') {
    return value;
  }
  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return String(value);
  }
}

export function mapCacheEntryRow(entry: CacheEntryVO): CacheEntryRow {
  return {
    key: entry.key,
    type: (entry.type || '--').toUpperCase(),
    ttlSeconds: entry.ttlSeconds ?? null,
    ttlText: formatCacheTtl(entry.ttlSeconds),
    expireAtText: formatDateTime(entry.expireAt),
    valuePreview: entry.valuePreview || '--',
  };
}
