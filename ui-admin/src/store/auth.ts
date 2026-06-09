import { defineStore } from 'pinia';
import { fetchCaptcha as fetchCaptchaApi, fetchMenus, fetchPermissions, fetchProfile, login as loginApi } from '@/api/auth';
import type { MenuItem, UserProfile } from '@/types/auth';
import type { CaptchaPayload } from '@/types/auth';
import { extractPermissionCodes } from '@/router/dynamic';
import { clearToken, getToken, setToken } from '@/utils/storage';

interface AuthState {
  token: string;
  profile: UserProfile | null;
  menus: MenuItem[];
  permissions: string[];
  initialized: boolean;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: getToken() ?? '',
    profile: null,
    menus: [],
    permissions: [],
    initialized: false,
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
  },
  actions: {
    async fetchCaptcha(): Promise<CaptchaPayload> {
      return fetchCaptchaApi();
    },
    async login(username: string, password: string, captchaKey: string, captchaCode: string) {
      const result = await loginApi({ username, password, captchaKey, captchaCode });
      this.token = result.token;
      this.profile = result.userInfo;
      setToken(result.token);
    },
    async bootstrap() {
      if (!this.token) {
        return;
      }
      const [profile, menus, permissions] = await Promise.all([fetchProfile(), fetchMenus(), fetchPermissions()]);
      this.profile = profile;
      this.menus = menus;
      this.permissions = Array.from(new Set([...permissions, ...extractPermissionCodes(menus)]));
      this.initialized = true;
    },
    logout() {
      this.token = '';
      this.profile = null;
      this.menus = [];
      this.permissions = [];
      this.initialized = false;
      clearToken();
    },
  },
});
