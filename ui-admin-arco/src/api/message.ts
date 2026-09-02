import request, { HttpResponse } from './interceptor';

export interface MessageRecord {
  id: number;
  type: string;
  title: string;
  subTitle: string;
  avatar?: string;
  content: string;
  time: string;
  status: 0 | 1;
  messageType?: number;
}

export type MessageListType = MessageRecord[];
type MessageListResponse = Omit<HttpResponse, 'data'> & { data?: MessageListType };

interface MessageStatus {
  ids: number[];
}

export interface ChatRecord {
  id: number;
  username: string;
  content: string;
  time: string;
  isCollect: boolean;
}

const readMessageIds = new Set<number>();

function logRows(response: HttpResponse) {
  return Array.isArray(response.rows) ? response.rows : [];
}

function formatTime(value: unknown) {
  return value ? String(value) : '-';
}

/**
 * 将 Java 操作日志转换为顶部消息盒子的通知模型。
 * 消息盒子本身没有对应的持久化接口，已读状态只保留在当前页面会话中。
 *
 * @return 消息盒子数据
 */
export async function queryMessageList(): Promise<MessageListResponse> {
  const response = (await request.get('/monitor/operlog/list', {
    params: { pageNum: 1, pageSize: 8 },
    headers: { silent: 'true' },
  })) as unknown as HttpResponse;
  const data = logRows(response).map((record: Record<string, any>, index) => {
    const id = Number(record.operId) || index + 1;
    return {
      id,
      type: 'message',
      title: record.title || '系统操作',
      subTitle: record.requestMethod || '',
      content: record.errorMsg || record.operUrl || '已产生一条系统操作记录',
      time: formatTime(record.operTime),
      status: readMessageIds.has(id) ? 1 : 0,
    } as MessageRecord;
  });
  return { ...response, data };
}

/** 将当前消息盒子中的记录标记为已读。 */
export async function setMessageStatus(data: MessageStatus): Promise<void> {
  data.ids.forEach((id) => readMessageIds.add(id));
}

/**
 * 查询实时监控页使用的操作记录。
 *
 * @return 聊天窗口展示数据
 */
export async function queryChatList(): Promise<Omit<HttpResponse, 'data'> & { data?: ChatRecord[] }> {
  const response = (await request.get('/monitor/operlog/list', {
    params: { pageNum: 1, pageSize: 8 },
    headers: { silent: 'true' },
  })) as unknown as HttpResponse;
  const data = logRows(response).map((record: Record<string, any>, index) => ({
    id: Number(record.operId) || index + 1,
    username: record.operName || '系统用户',
    content: record.title || record.operUrl || '系统操作',
    time: formatTime(record.operTime),
    isCollect: false,
  }));
  return { ...response, data };
}
