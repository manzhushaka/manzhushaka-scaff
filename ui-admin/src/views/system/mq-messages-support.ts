import type { MqMessageQuery, MqMessageRow, MqMessageVO } from '@/types/system';
import { formatStandardDateTime } from '@/utils/date-time';

export const mqMessageKeywordFields = ['streamKey', 'eventType', 'bizKey', 'traceId'] as const;

export const mqMessageStatusOptions = [
  { label: '初始化', value: 'INIT' },
  { label: '待消费', value: 'PUBLISHED' },
  { label: '消费中', value: 'PROCESSING' },
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAIL' },
];

export function toMqMessageStatusText(status: string | null | undefined) {
  switch (status) {
    case 'INIT':
      return '初始化';
    case 'PUBLISHED':
      return '待消费';
    case 'PROCESSING':
      return '消费中';
    case 'SUCCESS':
      return '成功';
    case 'FAIL':
      return '失败';
    default:
      return '--';
  }
}

export function canRetryMqMessage(input: Pick<MqMessageVO, 'status' | 'processingTimedOut'>) {
  if (input.status === 'INIT' || input.status === 'FAIL') {
    return true;
  }
  return input.processingTimedOut === true
    && (input.status === 'PROCESSING' || input.status === 'PUBLISHED');
}

export function buildMqMessageListQuery(input: {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  status?: string;
  source?: string;
}): MqMessageQuery {
  const query: MqMessageQuery = {
    pageNum: input.pageNum,
    pageSize: input.pageSize,
    status: input.status,
    source: input.source?.trim() || undefined,
  };
  const keyword = input.keyword?.trim();
  if (!keyword) {
    return query;
  }
  for (const field of mqMessageKeywordFields) {
    query[field] = keyword;
  }
  return query;
}

function formatDateTime(value: string | null | undefined) {
  return formatStandardDateTime(value);
}

export function mapMqMessageRow(message: MqMessageVO): MqMessageRow {
  const createTimeText = formatDateTime(message.createTime);
  const processingDeadlineAtText = formatDateTime(message.processingDeadlineAt);
  const publishedAtText = formatDateTime(message.publishedAt);
  const consumeStartedAtText = formatDateTime(message.consumeStartedAt);
  const consumedAtText = formatDateTime(message.consumedAt);
  return {
    id: message.id,
    eventId: message.eventId,
    streamKey: message.streamKey,
    eventType: message.eventType,
    bizKey: message.bizKey ?? '--',
    traceId: message.traceId ?? '--',
    source: message.source ?? '--',
    statusText: toMqMessageStatusText(message.status),
    statusValue: message.status ?? '',
    retryCountText: `${message.retryCount ?? 0} 次`,
    retryCountValue: message.retryCount ?? 0,
    lastError: message.lastError ?? '--',
    processingDeadlineAtText,
    payloadSnapshot: message.payloadSnapshot ?? '--',
    processingTimedOut: message.processingTimedOut === true,
    canRetry: canRetryMqMessage(message),
    createTimeText,
    publishedAtText,
    consumeStartedAtText,
    consumedAtText,
    timelineText: `创建 ${createTimeText} / 发布 ${publishedAtText} / 开始 ${consumeStartedAtText} / 完成 ${consumedAtText}`,
  };
}
