import type { RouteRecordRaw } from 'vue-router';
import request, { HttpResponse } from './interceptor';

export interface LoginData {
  username: string;
  password: string;
  code?: string;
  uuid?: string;
}

export interface LoginRes {
  token: string;
}

export interface UserProfile {
  userId: number;
  userName: string;
  nickName: string;
  avatar?: string;
  email?: string;
  phonenumber?: string;
  sex?: string;
  dept?: { deptName?: string };
}

export interface UserInfoResponse {
  user: UserProfile;
  roles: string[];
  permissions: string[];
  isDefaultModifyPwd?: boolean;
  isPasswordExpired?: boolean;
}

export interface CaptchaResponse extends HttpResponse {
  captchaEnabled: boolean;
  img?: string;
  uuid?: string;
}

export function login(data: LoginData): Promise<HttpResponse<LoginRes>> {
  return request.post('/login', data, {
    headers: { isToken: 'false', repeatSubmit: 'false' },
  });
}

export function logout(): Promise<HttpResponse> {
  return request.post('/logout');
}

export function getUserInfo(): Promise<HttpResponse<UserInfoResponse>> {
  return request.get('/getInfo');
}

export function getMenuList(): Promise<HttpResponse<RouteRecordRaw[]>> {
  return request.get('/getRouters');
}

export function getCaptcha(): Promise<CaptchaResponse> {
  return request.get('/captchaImage', {
    headers: { isToken: 'false' },
  }) as unknown as Promise<CaptchaResponse>;
}
