export type SystemApiEnvelope<T> = {
  code: number;
  message: string;
  data: T;
};

export function buildSystemAuthHeader(token: string | undefined) {
  return token || undefined;
}

export function unwrapSystemResponse<T>(payload: SystemApiEnvelope<T>) {
  if (payload.code !== 0) {
    throw new Error(payload.message || '请求失败，请稍后重试');
  }
  return payload.data;
}
