import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildMqMessageListQuery,
  canRetryMqMessage,
  mapMqMessageRow,
  mqMessageKeywordFields,
  toMqMessageStatusText,
} from '../src/views/system/mq-messages-support.ts';

test('formats mq message statuses for the ledger page', () => {
  assert.equal(toMqMessageStatusText('INIT'), '初始化');
  assert.equal(toMqMessageStatusText('PUBLISHED'), '待消费');
  assert.equal(toMqMessageStatusText('PROCESSING'), '消费中');
  assert.equal(toMqMessageStatusText('SUCCESS'), '成功');
  assert.equal(toMqMessageStatusText('FAIL'), '失败');
});

test('exposes the keyword fields covered by the mq ledger search box', () => {
  assert.deepEqual(mqMessageKeywordFields, ['streamKey', 'eventType', 'bizKey', 'traceId']);
});

test('builds the mq ledger list query with keyword mapped to all supported fields', () => {
  assert.deepEqual(
    buildMqMessageListQuery({
      pageNum: 3,
      pageSize: 20,
      keyword: 'TRACE-9527',
      status: 'PROCESSING',
      source: 'system',
    }),
    {
      pageNum: 3,
      pageSize: 20,
      streamKey: 'TRACE-9527',
      eventType: 'TRACE-9527',
      bizKey: 'TRACE-9527',
      traceId: 'TRACE-9527',
      status: 'PROCESSING',
      source: 'system',
    },
  );
});

test('allows retry for init fail and timed out published or processing messages', () => {
  assert.equal(canRetryMqMessage({ status: 'INIT', processingTimedOut: false }), true);
  assert.equal(canRetryMqMessage({ status: 'FAIL', processingTimedOut: false }), true);
  assert.equal(canRetryMqMessage({ status: 'PROCESSING', processingTimedOut: true }), true);
  assert.equal(canRetryMqMessage({ status: 'PUBLISHED', processingTimedOut: true }), true);
  assert.equal(canRetryMqMessage({ status: 'PROCESSING', processingTimedOut: false }), false);
  assert.equal(canRetryMqMessage({ status: 'PUBLISHED', processingTimedOut: false }), false);
  assert.equal(canRetryMqMessage({ status: 'SUCCESS', processingTimedOut: true }), false);
});

test('maps mq message rows for the ledger table', () => {
  assert.deepEqual(
    mapMqMessageRow({
      id: 8,
      eventId: '1749555555000-0',
      streamKey: 'stream:system:user',
      eventType: 'USER_CREATED',
      bizKey: 'user:9527',
      traceId: 'TRACE-9527',
      source: 'system',
      status: 'PUBLISHED',
      retryCount: 2,
      lastError: 'consumer timeout',
      processingDeadlineAt: '2026-06-10T09:10:30',
      processingTimedOut: true,
      publishedAt: '2026-06-10T09:10:15',
      consumeStartedAt: null,
      consumedAt: null,
      createTime: '2026-06-10T09:10:11',
      payloadSnapshot: '{"userId":9527}',
    }),
    {
      id: 8,
      eventId: '1749555555000-0',
      streamKey: 'stream:system:user',
      eventType: 'USER_CREATED',
      bizKey: 'user:9527',
      traceId: 'TRACE-9527',
      source: 'system',
      statusText: '待消费',
      statusValue: 'PUBLISHED',
      retryCountText: '2 次',
      retryCountValue: 2,
      lastError: 'consumer timeout',
      processingDeadlineAtText: '2026-06-10 09:10:30',
      payloadSnapshot: '{"userId":9527}',
      processingTimedOut: true,
      canRetry: true,
      createTimeText: '2026-06-10 09:10:11',
      publishedAtText: '2026-06-10 09:10:15',
      consumeStartedAtText: '--',
      consumedAtText: '--',
      timelineText: '创建 2026-06-10 09:10:11 / 发布 2026-06-10 09:10:15 / 开始 -- / 完成 --',
    },
  );
});
