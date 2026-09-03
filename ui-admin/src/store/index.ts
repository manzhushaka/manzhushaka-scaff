import { createPinia } from 'pinia';
import useAppStore from './modules/app';
import useUserStore from './modules/user';
import useTabBarStore from './modules/tab-bar';
import useLockStore from './modules/lock';

const pinia = createPinia();

export { useAppStore, useUserStore, useTabBarStore, useLockStore };
export default pinia;
