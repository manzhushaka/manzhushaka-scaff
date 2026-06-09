import axios from 'axios';
import { Message } from '@arco-design/web-vue';
import { getToken } from '@/utils/storage';
import { dispatchMockRequest } from './mock';

type ApiEnvelope<T> = {
  code: number;
  message: string;
  data: T;
};

const service = axios.create({
  baseURL: '/api',
  timeout: 8000,
});

service.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = token;
  }
  return config;
});

service.interceptors.request.use(async (config) => {
  const useMock = import.meta.env.VITE_USE_MOCK === 'true';
  if (!useMock) {
    return config;
  }

  const result = await dispatchMockRequest({
    url: config.url,
    method: config.method,
    params: config.params as Record<string, string> | undefined,
    data: config.data as string | undefined,
  });

  config.adapter = async () => ({
    data: result,
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
    request: {},
  });

  return config;
});

function unwrapResponse<T>(payload: ApiEnvelope<T> | string): T {
  const normalized = typeof payload === 'string' ? (JSON.parse(payload) as ApiEnvelope<T>) : payload;
  if (normalized.code !== 0) {
    throw new Error(normalized.message || '请求失败');
  }
  return normalized.data;
}

function normalizeRequestError(error: unknown) {
  if (axios.isAxiosError(error) && !error.response) {
    return new Error('无法连接后端服务，请确认后端服务和前端代理已启动');
  }
  return error instanceof Error ? error : new Error('请求失败，请稍后重试');
}

const request = {
  async get<T>(url: string, config?: object) {
    try {
      const response = await service.get<ApiEnvelope<T> | string>(url, config);
      return unwrapResponse(response.data);
    } catch (error) {
      const normalized = normalizeRequestError(error);
      Message.error(normalized.message);
      throw normalized;
    }
  },
  async post<T>(url: string, data?: unknown, config?: object) {
    try {
      const response = await service.post<ApiEnvelope<T> | string>(url, data, config);
      return unwrapResponse(response.data);
    } catch (error) {
      const normalized = normalizeRequestError(error);
      Message.error(normalized.message);
      throw normalized;
    }
  },
  async put<T>(url: string, data?: unknown, config?: object) {
    try {
      const response = await service.put<ApiEnvelope<T> | string>(url, data, config);
      return unwrapResponse(response.data);
    } catch (error) {
      const normalized = normalizeRequestError(error);
      Message.error(normalized.message);
      throw normalized;
    }
  },
  async delete<T>(url: string, config?: object) {
    try {
      const response = await service.delete<ApiEnvelope<T> | string>(url, config);
      return unwrapResponse(response.data);
    } catch (error) {
      const normalized = normalizeRequestError(error);
      Message.error(normalized.message);
      throw normalized;
    }
  },
};

export default request;
