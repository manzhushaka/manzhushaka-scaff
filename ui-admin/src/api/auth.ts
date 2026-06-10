import type { CaptchaPayload, LoginPayload } from '@/types/auth';
import type { MenuItem, UserProfile } from '@/types/auth';
import request from './request';
import type { RequestOptions } from './request-client';

type RequestClient = {
  get<T>(url: string, config?: RequestOptions): Promise<T>;
  post<T>(url: string, data?: unknown, config?: RequestOptions): Promise<T>;
};

export const AUTH_BOOTSTRAP_REQUEST_OPTIONS = Object.freeze<RequestOptions>({
  silentError: true,
});

export function createAuthApi(client: RequestClient = request) {
  return {
    fetchCaptcha() {
      return client.get<CaptchaPayload>('/auth/captcha');
    },
    login(payload: LoginPayload) {
      return client.post<{ token: string; userInfo: UserProfile }>('/auth/login', payload);
    },
    fetchProfile() {
      return client.get<UserProfile>('/auth/me', AUTH_BOOTSTRAP_REQUEST_OPTIONS);
    },
    fetchMenus() {
      return client.get<MenuItem[]>('/auth/menus', AUTH_BOOTSTRAP_REQUEST_OPTIONS);
    },
    fetchPermissions() {
      return client.get<string[]>('/auth/permissions', AUTH_BOOTSTRAP_REQUEST_OPTIONS);
    },
  };
}

const authApi = createAuthApi();

export function fetchCaptcha() {
  return authApi.fetchCaptcha();
}

export function login(payload: LoginPayload) {
  return authApi.login(payload);
}

export function fetchProfile() {
  return authApi.fetchProfile();
}

export function fetchMenus() {
  return authApi.fetchMenus();
}

export function fetchPermissions() {
  return authApi.fetchPermissions();
}
