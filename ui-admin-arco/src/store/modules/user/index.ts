import { defineStore } from 'pinia';
import {
  login as userLogin,
  logout as userLogout,
  getUserInfo,
  LoginData,
} from '@/api/user';
import { setToken, clearToken } from '@/utils/auth';
import { removeRouteListener } from '@/utils/route-listener';
import { UserState } from './types';
import useAppStore from '../app';

const useUserStore = defineStore('user', {
  state: (): UserState => ({
    id: undefined,
    name: undefined,
    userName: undefined,
    nickName: undefined,
    avatar: undefined,
    job: undefined,
    organization: undefined,
    location: undefined,
    email: undefined,
    introduction: undefined,
    personalWebsite: undefined,
    jobName: undefined,
    organizationName: undefined,
    locationName: undefined,
    phone: undefined,
    registrationDate: undefined,
    accountId: undefined,
    certification: undefined,
    role: '',
    roles: [],
    permissions: [],
    forceChangePassword: false,
    passwordExpired: false,
    initialized: false,
  }),

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state };
    },
  },

  actions: {
    switchRoles() {
      return new Promise((resolve) => {
        resolve(this.role);
      });
    },
    // Set user's information
    setInfo(partial: Partial<UserState>) {
      this.$patch(partial);
    },

    // Reset user's information
    resetInfo() {
      this.$reset();
    },

    // Get user's information
    async info() {
      const res = await getUserInfo();
      const data = (res.data || res) as any;
      if (!data) {
        throw new Error('用户信息为空');
      }
      const { user, roles = [], permissions = [] } = data;
      const avatar = user.avatar || '';
      this.setInfo({
        id: user.userId,
        name: user.nickName || user.userName,
        userName: user.userName,
        nickName: user.nickName,
        avatar,
        email: user.email,
        phone: user.phonenumber,
        organizationName: user.dept?.deptName,
        roles,
        permissions,
        role: roles.includes('admin') ? 'admin' : roles[0] || '',
        forceChangePassword: Boolean(data.isDefaultModifyPwd),
        passwordExpired: Boolean(data.isPasswordExpired),
        initialized: true,
      });
      return data;
    },

    // Login
    async login(loginForm: LoginData) {
      try {
        const res = await userLogin(loginForm);
        const token = (res as any).token || (res as any).data?.token;
        if (!token) {
          throw new Error('登录响应缺少令牌');
        }
        setToken(token);
      } catch (err) {
        clearToken();
        throw err;
      }
    },
    logoutCallBack() {
      const appStore = useAppStore();
      this.resetInfo();
      clearToken();
      removeRouteListener();
      appStore.clearServerMenu();
    },

    hasPermission(permission: string) {
      return (
        this.permissions.includes('*') || this.permissions.includes(permission)
      );
    },
    // Logout
    async logout() {
      try {
        await userLogout();
      } finally {
        this.logoutCallBack();
      }
    },
  },
});

export default useUserStore;
