import { defineStore } from 'pinia';

interface LockState {
  isLock: boolean;
  lockPath: string;
}

const LOCK_KEY = 'screen-lock';
const LOCK_PATH_KEY = 'screen-lock-path';

const useLockStore = defineStore('lock', {
  state: (): LockState => ({
    isLock: localStorage.getItem(LOCK_KEY) === 'true',
    lockPath: localStorage.getItem(LOCK_PATH_KEY) || '/dashboard/workplace',
  }),
  actions: {
    lockScreen(currentPath?: string) {
      this.lockPath = currentPath || '/dashboard/workplace';
      this.isLock = true;
      localStorage.setItem(LOCK_KEY, 'true');
      localStorage.setItem(LOCK_PATH_KEY, this.lockPath);
    },
    unlockScreen() {
      this.isLock = false;
      this.lockPath = '/dashboard/workplace';
      localStorage.setItem(LOCK_KEY, 'false');
      localStorage.setItem(LOCK_PATH_KEY, this.lockPath);
    },
  },
});

export default useLockStore;
