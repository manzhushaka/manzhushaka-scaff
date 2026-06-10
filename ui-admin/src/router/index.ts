import { createRouter, createWebHistory } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { useAuthStore } from '@/store/auth';
import { SESSION_EXPIRED_MESSAGE } from '@/api/request-client';
import { buildDynamicRoutes } from './dynamic';
import { staticRoutes, WHITE_LIST } from './routes';

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes,
});

let routesMounted = false;
const dynamicRouteNames = new Set<string>();

export function resetDynamicRoutes() {
  dynamicRouteNames.forEach((name) => {
    if (router.hasRoute(name)) {
      router.removeRoute(name);
    }
  });
  dynamicRouteNames.clear();
  routesMounted = false;
}

function mountDynamicRoutes() {
  const authStore = useAuthStore();
  const rootName = 'Root';
  for (const route of buildDynamicRoutes(authStore.menus)) {
    router.addRoute(rootName, route);
    if (route.name) {
      dynamicRouteNames.add(String(route.name));
    }
  }
  routesMounted = true;
}

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore();

  if (!authStore.token && !WHITE_LIST.includes(to.path)) {
    next('/login');
    return;
  }

  if (authStore.token && to.path === '/login') {
    next('/');
    return;
  }

  if (authStore.token && !authStore.initialized) {
    try {
      await authStore.bootstrap();
      if (!routesMounted) {
        mountDynamicRoutes();
      }
      next({ ...to, replace: true });
      return;
    } catch (_error) {
      authStore.logout();
      resetDynamicRoutes();
      Message.error(SESSION_EXPIRED_MESSAGE);
      next('/login');
      return;
    }
  }

  next();
});

export default router;
