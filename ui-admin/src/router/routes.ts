import type { RouteRecordRaw } from 'vue-router';
import MainLayout from '@/layout/MainLayout.vue';
import LoginView from '@/views/auth/LoginView.vue';

export const WHITE_LIST = ['/login'];

export const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { title: '登录' },
  },
  {
    path: '/',
    name: 'Root',
    component: MainLayout,
    redirect: '/dashboard',
    meta: { title: '首页' },
    children: [],
  },
];
