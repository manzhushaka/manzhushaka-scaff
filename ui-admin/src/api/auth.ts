import type { LoginPayload } from '@/types/auth';
import type { MenuItem, UserProfile } from '@/types/auth';
import request from './request';

export function login(payload: LoginPayload) {
  return request.post<{ token: string; userInfo: UserProfile }>('/auth/login', payload);
}

export function fetchProfile() {
  return request.get<UserProfile>('/auth/me');
}

export function fetchMenus() {
  return request.get<MenuItem[]>('/auth/menus');
}

export function fetchPermissions() {
  return request.get<string[]>('/auth/permissions');
}
