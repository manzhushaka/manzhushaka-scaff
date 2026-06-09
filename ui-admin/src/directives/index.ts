import type { App } from 'vue';
import { permissionDirective } from './permission';

export function setupDirectives(app: App) {
  app.directive('permission', permissionDirective);
}
