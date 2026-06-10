import { defineStore } from 'pinia';
import { systemApi } from '@/api/system';
import { normalizePlatformConfig } from '@/platform-config';

interface PlatformState {
  platformName: string;
  platformSubtitle: string;
  logoUrl: string;
  loaded: boolean;
}

export const usePlatformStore = defineStore('platform', {
  state: (): PlatformState => {
    const defaultConfig = normalizePlatformConfig();
    return {
      platformName: defaultConfig.platformName,
      platformSubtitle: defaultConfig.platformSubtitle,
      logoUrl: defaultConfig.logoUrl,
      loaded: false,
    };
  },
  actions: {
    async fetchPlatformConfig(force = false) {
      if (this.loaded && !force) {
        return;
      }
      const config = normalizePlatformConfig(await systemApi.getPlatformConfig());
      this.platformName = config.platformName;
      this.platformSubtitle = config.platformSubtitle;
      this.logoUrl = config.logoUrl;
      this.loaded = true;
    },
    applyPlatformConfig(payload: { platformName: string; platformSubtitle: string; logoUrl: string }) {
      const config = normalizePlatformConfig(payload);
      this.platformName = config.platformName;
      this.platformSubtitle = config.platformSubtitle;
      this.logoUrl = config.logoUrl;
      this.loaded = true;
    },
  },
});
