import { DirectiveBinding } from 'vue';
import { useUserStore } from '@/store';

function checkPermission(el: HTMLElement, binding: DirectiveBinding) {
  const { value } = binding;
  const userStore = useUserStore();
  const { role, roles, permissions } = userStore;

  if (Array.isArray(value)) {
    if (value.length > 0) {
      const permissionValues = value;

      const hasPermission = permissionValues.some(
        (permission: string) =>
          permission === '*' ||
          permissions.includes(permission) ||
          roles.includes(permission) ||
          permission === role
      );
      if (!hasPermission && el.parentNode) {
        el.parentNode.removeChild(el);
      }
    }
  } else {
    throw new Error(`need permissions! Like v-has-permi="['system:user:list']"`);
  }
}

export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding);
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding);
  },
};
