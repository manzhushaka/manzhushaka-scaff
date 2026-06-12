import axios from 'axios';
import { Message } from '@arco-design/web-vue';
import { dispatchMockRequest } from './mock';
import { normalizeRequestError, shouldNotifyRequestError, unwrapResponse, type ApiEnvelope, type RequestOptions } from './request-client';

const service = axios.create({
  baseURL: '/api',
  timeout: 8000,
  withCredentials: true,
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

const request = {
  async get<T>(url: string, config?: RequestOptions): Promise<T> {
    try {
      const response = await service.get<ApiEnvelope<T> | string>(url, config);
      return unwrapResponse(response.data);
    } catch (error) {
      const normalized = normalizeRequestError(error);
      if (shouldNotifyRequestError(config)) {
        Message.error(normalized.message);
      }
      throw normalized;
    }
  },
  async post<T>(url: string, data?: unknown, config?: RequestOptions): Promise<T> {
    try {
      const response = await service.post<ApiEnvelope<T> | string>(url, data, config);
      return unwrapResponse(response.data);
    } catch (error) {
      const normalized = normalizeRequestError(error);
      if (shouldNotifyRequestError(config)) {
        Message.error(normalized.message);
      }
      throw normalized;
    }
  },
  async put<T>(url: string, data?: unknown, config?: RequestOptions): Promise<T> {
    try {
      const response = await service.put<ApiEnvelope<T> | string>(url, data, config);
      return unwrapResponse(response.data);
    } catch (error) {
      const normalized = normalizeRequestError(error);
      if (shouldNotifyRequestError(config)) {
        Message.error(normalized.message);
      }
      throw normalized;
    }
  },
  async delete<T>(url: string, config?: RequestOptions): Promise<T> {
    try {
      const response = await service.delete<ApiEnvelope<T> | string>(url, config);
      return unwrapResponse(response.data);
    } catch (error) {
      const normalized = normalizeRequestError(error);
      if (shouldNotifyRequestError(config)) {
        Message.error(normalized.message);
      }
      throw normalized;
    }
  },
};

export default request;
