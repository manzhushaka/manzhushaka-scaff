import axios from 'axios';
import type { AxiosRequestConfig } from 'axios';

export type ApiEnvelope<T> = {
  code: number;
  message: string;
  data: T;
};

export type RequestOptions = AxiosRequestConfig & {
  silentError?: boolean;
};

export const SESSION_EXPIRED_MESSAGE = '登录状态已失效，请重新登录';

export function unwrapResponse<T>(payload: ApiEnvelope<T> | string): T {
  const normalized = typeof payload === 'string' ? (JSON.parse(payload) as ApiEnvelope<T>) : payload;
  if (normalized.code !== 0) {
    throw new Error(normalizeErrorMessage(normalized.message));
  }
  return normalized.data;
}

export function normalizeErrorMessage(message: string | undefined) {
  const normalized = message?.trim();
  if (!normalized) {
    return '请求失败，请稍后重试';
  }
  if (
    /^token\s*无效(?:\s*[:：].*)?$/i.test(normalized)
    || normalized.includes('登录已过期')
    || normalized.includes('登录状态已失效')
    || normalized.includes('未登录')
  ) {
    return SESSION_EXPIRED_MESSAGE;
  }
  return normalized;
}

export function normalizeRequestError(error: unknown) {
  if (axios.isAxiosError(error) && !error.response) {
    return new Error('无法连接后端服务，请确认后端服务和前端代理已启动');
  }
  if (error instanceof Error) {
    return new Error(normalizeErrorMessage(error.message));
  }
  return new Error('请求失败，请稍后重试');
}

export function shouldNotifyRequestError(options?: RequestOptions) {
  return !options?.silentError;
}
