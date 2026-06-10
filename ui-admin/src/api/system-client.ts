export type SystemApiEnvelope<T> = {
  code: number;
  message: string;
  data: T;
};

export function buildSystemAuthHeader(token: string | null | undefined) {
  const trimmed = token?.trim();
  return trimmed ? trimmed : undefined;
}

export function unwrapSystemResponse<T>(payload: SystemApiEnvelope<T>) {
  if (payload.code !== 0) {
    throw new Error(payload.message || '请求失败，请稍后重试');
  }
  return payload.data;
}
