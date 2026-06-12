import { defineStore } from 'pinia';
import { fetchCaptcha as fetchCaptchaApi, fetchMenus, fetchPermissions, fetchProfile, login as loginApi, logout as logoutApi } from '@/api/auth';
import type { MenuItem, UserProfile } from '@/types/auth';
import type { CaptchaPayload } from '@/types/auth';
import { extractPermissionCodes } from '@/router/dynamic';

interface AuthState {
  profile: UserProfile | null;
  menus: MenuItem[];
  permissions: string[];
  initialized: boolean;
  sessionReady: boolean;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    profile: null,
    menus: [],
    permissions: [],
    initialized: false,
    sessionReady: false,
  }),
  getters: {
    isLoggedIn: (state) => state.sessionReady,
  },
  actions: {
    async fetchCaptcha(): Promise<CaptchaPayload> {
      return fetchCaptchaApi();
    },
    async login(username: string, password: string, captchaKey: string, captchaCode: string) {
      const result = await loginApi({ username, password, captchaKey, captchaCode });
      this.profile = result.userInfo;
      this.sessionReady = true;
      this.initialized = false;
      this.menus = [];
      this.permissions = [];
    },
    async bootstrap() {
      const [profile, menus, permissions] = await Promise.all([fetchProfile(), fetchMenus(), fetchPermissions()]);
      this.profile = profile;
      this.menus = menus;
      this.permissions = Array.from(new Set([...permissions, ...extractPermissionCodes(menus)]));
      this.initialized = true;
      this.sessionReady = true;
    },
    async logout() {
      if (this.sessionReady) {
        try {
          await logoutApi();
        } catch {
          // Ignore logout request failures and continue clearing local state.
        }
      }
      this.profile = null;
      this.menus = [];
      this.permissions = [];
      this.initialized = false;
      this.sessionReady = false;
    },
  },
});
