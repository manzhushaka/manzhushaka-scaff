<template>
  <a-config-provider :locale="locale">
    <PageLoading v-if="!routerReady" />
    <router-view v-else />
    <global-setting />
  </a-config-provider>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import enUS from '@arco-design/web-vue/es/locale/lang/en-us';
  import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn';
  import GlobalSetting from '@/components/global-setting/index.vue';
  import PageLoading from '@/components/page-loading/index.vue';
  import router from '@/router';
  import useLocale from '@/hooks/locale';

  const { currentLocale } = useLocale();
  const routerReady = ref(false);
  const locale = computed(() => {
    switch (currentLocale.value) {
      case 'zh-CN':
        return zhCN;
      case 'en-US':
        return enUS;
      default:
        return enUS;
    }
  });

  router.isReady().then(
    () => {
      routerReady.value = true;
    },
    () => {
      routerReady.value = true;
    }
  );
</script>
