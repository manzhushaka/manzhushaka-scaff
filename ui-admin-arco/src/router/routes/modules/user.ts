import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const USER: AppRouteRecordRaw = {
  path: '/user',
  name: 'user',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.user',
    icon: 'icon-user',
    requiresAuth: true,
    order: 7,
  },
  children: [
    {
      path: 'info',
      name: 'Info',
      component: () => import('@/views/user/info/index.vue'),
      meta: {
        locale: 'menu.user.info',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'setting',
      name: 'Setting',
      component: () => import('@/views/user/setting/index.vue'),
      meta: {
        locale: 'menu.user.setting',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'profile',
      name: 'AccountProfile',
      component: () => import('@/views/user/account-profile.vue'),
      meta: {
        locale: 'menu.user.profile',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
      },
    },
  ],
};

export default USER;
