import type { Router, LocationQueryRaw } from 'vue-router';
import NProgress from 'nprogress'; // progress bar

import { useUserStore } from '@/store';
import { isLogin } from '@/utils/auth';
import useLockStore from '@/store/modules/lock';

export default function setupUserLoginInfoGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    NProgress.start();
    const userStore = useUserStore();
    const lockStore = useLockStore();
    if (isLogin()) {
      if (to.name === 'login' || to.name === 'register') {
        next({ name: 'Workplace' });
        return;
      }
      if (lockStore.isLock && to.name !== 'Lock') {
        next({ name: 'Lock' });
        return;
      }
      if (userStore.initialized) {
        next();
      } else {
        try {
          await userStore.info();
          next();
        } catch (error) {
          await userStore.logout();
          next({
            name: 'login',
            query: {
              redirect: to.name,
              ...to.query,
            } as LocationQueryRaw,
          });
        }
      }
    } else {
      if (to.meta.requiresAuth === false || to.name === 'login' || to.name === 'register') {
        next();
        return;
      }
      next({
        name: 'login',
        query: {
          redirect: to.name,
          ...to.query,
        } as LocationQueryRaw,
      });
    }
  });
}
