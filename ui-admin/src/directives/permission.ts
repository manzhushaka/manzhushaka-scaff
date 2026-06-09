import type { Directive } from 'vue';
import { useAuthStore } from '@/store/auth';

export const permissionDirective: Directive<HTMLElement, string | string[]> = {
  mounted(el, binding) {
    const authStore = useAuthStore();
    const required = Array.isArray(binding.value) ? binding.value : [binding.value];
    const granted = required.some((item) => authStore.permissions.includes(item));
    if (!granted) {
      el.remove();
    }
  },
};
