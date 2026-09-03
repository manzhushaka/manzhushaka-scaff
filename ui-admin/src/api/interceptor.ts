import axios from 'axios';
import type { AxiosRequestConfig, AxiosResponse } from 'axios';
import { Message, Modal } from '@arco-design/web-vue';
import { clearToken, getToken } from '@/utils/auth';

export interface HttpResponse<T = unknown> {
  code: number;
  msg: string;
  data?: T;
  rows?: T[];
  total?: number;
  [key: string]: unknown;
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
});

interface RequestService {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<HttpResponse<T>>;
  post<T = any>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<HttpResponse<T>>;
  put<T = any>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<HttpResponse<T>>;
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<HttpResponse<T>>;
}

function isSilentRequest(config?: AxiosRequestConfig) {
  const headers = (config?.headers || {}) as Record<string, unknown>;
  return headers.silent === true || headers.silent === 'true';
}

service.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token = getToken();
    const headers = (config.headers || {}) as Record<string, unknown>;
    const tokenDisabled = headers.isToken === false || headers.isToken === 'false';
    if (token && !tokenDisabled && !config.url?.includes('/anon/')) {
      headers.Authorization = `Bearer ${token}`;
    }
    config.headers = headers as AxiosRequestConfig['headers'];
    return config;
  },
  (error) => Promise.reject(error)
);

service.interceptors.response.use(
  (response: AxiosResponse<HttpResponse>) => {
    if (response.config.responseType === 'blob') {
      return response.data;
    }

    const result = response.data;
    if (result.code !== 200) {
      if (result.code === 401) {
        clearToken();
        Modal.error({
          title: '登录状态已过期',
          content: '请重新登录后继续操作。',
          okText: '重新登录',
          onOk: () => window.location.assign('/login'),
        });
      } else if (!isSilentRequest(response.config)) {
        Message.error({ content: result.msg || '请求失败', duration: 5000 });
      }
      return Promise.reject(new Error(result.msg || '请求失败'));
    }
    return result;
  },
  (error) => {
    const message = error?.response?.data?.msg || error?.message || '网络请求失败';
    if (!isSilentRequest(error?.config)) {
      Message.error({ content: message, duration: 5000 });
    }
    return Promise.reject(error);
  }
);

export async function downloadFile(
  url: string,
  data: Record<string, unknown>,
  filename: string
) {
  const response = await service.post(url, data, {
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    transformRequest: [(params) => {
      const searchParams = new URLSearchParams();
      Object.entries(params || {}).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          searchParams.append(key, String(value));
        }
      });
      return searchParams.toString();
    }],
  });
  const blob = new Blob([response as unknown as BlobPart]);
  const objectUrl = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = objectUrl;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(objectUrl);
}

const request = service as unknown as RequestService;

export default request;
