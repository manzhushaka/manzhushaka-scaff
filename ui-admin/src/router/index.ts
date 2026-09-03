import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import NProgress from 'nprogress'; // progress bar
import 'nprogress/nprogress.css';

import { appRoutes } from './routes';
import { DEFAULT_LAYOUT, REDIRECT_MAIN, NOT_FOUND_ROUTE } from './routes/base';
import createRouteGuard from './guard';

const hiddenRoutes: RouteRecordRaw[] = [
  {
    path: '/lock',
    name: 'Lock',
    component: () => import('@/views/lock/index.vue'),
    meta: {
      requiresAuth: true,
      hideInMenu: true,
      noAffix: true,
    },
  },
  {
    path: '/system/user-auth',
    component: DEFAULT_LAYOUT,
    meta: { requiresAuth: true, hideInMenu: true },
    children: [
      {
        path: 'role/:userId',
        name: 'AuthRole',
        component: () => import('@/views/system/user/auth-role.vue'),
        meta: {
          title: '分配角色',
          requiresAuth: true,
          hideInMenu: true,
          activeMenu: 'ServerUser',
        },
      },
    ],
  },
  {
    path: '/system/role-auth',
    component: DEFAULT_LAYOUT,
    meta: { requiresAuth: true, hideInMenu: true },
    children: [
      {
        path: 'user/:roleId',
        name: 'AuthUser',
        component: () => import('@/views/system/role/auth-user.vue'),
        meta: {
          title: '分配用户',
          requiresAuth: true,
          hideInMenu: true,
          activeMenu: 'ServerRole',
        },
      },
    ],
  },
  {
    path: '/system/dict-data',
    component: DEFAULT_LAYOUT,
    meta: { requiresAuth: true, hideInMenu: true },
    children: [
      {
        path: 'index/:dictId',
        name: 'DictData',
        component: () => import('@/views/system/dict/data.vue'),
        meta: {
          title: '字典数据',
          requiresAuth: true,
          hideInMenu: true,
          activeMenu: 'ServerDict',
        },
      },
    ],
  },
  {
    path: '/monitor/job-log',
    component: DEFAULT_LAYOUT,
    meta: { requiresAuth: true, hideInMenu: true },
    children: [
      {
        path: 'index/:jobId',
        name: 'JobLog',
        component: () => import('@/views/monitor/jobLog/index.vue'),
        meta: {
          title: '调度日志',
          requiresAuth: true,
          hideInMenu: true,
          activeMenu: 'ServerJob',
        },
      },
    ],
  },
];

NProgress.configure({ showSpinner: false }); // NProgress Configuration

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: 'login',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: {
        requiresAuth: false,
        noAffix: true,
      },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/register/index.vue'),
      meta: {
        requiresAuth: false,
        noAffix: true,
      },
    },
    ...appRoutes,
    ...hiddenRoutes,
    REDIRECT_MAIN,
    NOT_FOUND_ROUTE,
  ],
  scrollBehavior() {
    return { top: 0 };
  },
});

createRouteGuard(router);

export default router;
