import type { PlatformConfigVO } from '@/types/system';

export const DEFAULT_PLATFORM_NAME = 'manzhushaka 管理台';
export const DEFAULT_PLATFORM_SUBTITLE = 'PLATFORM CONSOLE';

export function normalizePlatformConfig(config?: Partial<PlatformConfigVO> | null) {
  const platformName = config?.platformName?.trim() || DEFAULT_PLATFORM_NAME;
  const platformSubtitle = config?.platformSubtitle?.trim() || DEFAULT_PLATFORM_SUBTITLE;
  const logoUrl = config?.logoUrl?.trim() || '';
  return {
    platformName,
    platformSubtitle,
    logoUrl,
  };
}
