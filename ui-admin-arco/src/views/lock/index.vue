<template>
  <main class="lock-page">
    <section class="lock-panel" aria-labelledby="lock-title">
      <a-avatar :size="72" class="lock-avatar">
        <img v-if="userStore.avatar" :src="userStore.avatar" alt="当前用户头像" />
        <icon-user v-else />
      </a-avatar>
      <a-typography-title id="lock-title" :heading="4">屏幕已锁定</a-typography-title>
      <a-typography-text type="secondary">{{ userStore.nickName || userStore.userName || '当前用户' }}</a-typography-text>
      <a-form class="unlock-form" :model="form" layout="vertical" @submit="handleUnlock">
        <a-form-item field="password" label="登录密码">
          <a-input-password v-model="form.password" autofocus autocomplete="current-password" placeholder="请输入密码">
            <template #prefix><icon-lock /></template>
          </a-input-password>
        </a-form-item>
        <a-button type="primary" html-type="submit" long :loading="loading">
          <template #icon><icon-unlock /></template>
          解锁并返回工作区
        </a-button>
      </a-form>
      <a-button type="text" status="danger" :loading="loggingOut" @click="logout">
        <template #icon><icon-export /></template>
        退出重新登录
      </a-button>
    </section>
  </main>
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { useRoute, useRouter } from 'vue-router';
  import { unlockScreen } from '@/api/admin';
  import { useUserStore } from '@/store';
  import useLockStore from '@/store/modules/lock';

  const router = useRouter();
  const route = useRoute();
  const userStore = useUserStore();
  const lockStore = useLockStore();
  const form = reactive({ password: '' });
  const loading = ref(false);
  const loggingOut = ref(false);

  async function handleUnlock({ errors }: { errors?: Record<string, unknown> }) {
    if (errors || loading.value) return;
    loading.value = true;
    try {
      const returnPath = lockStore.lockPath || '/dashboard/workplace';
      await unlockScreen(form.password);
      lockStore.unlockScreen();
      form.password = '';
      router.replace(returnPath || (route.query.redirect as string) || '/dashboard/workplace');
    } finally {
      loading.value = false;
    }
  }

  async function logout() {
    loggingOut.value = true;
    try {
      lockStore.unlockScreen();
      await userStore.logout();
      router.replace({ name: 'login' });
    } catch (error) {
      Message.error((error as Error).message || '退出失败');
    } finally {
      loggingOut.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .lock-page { display: flex; min-height: 100vh; align-items: center; justify-content: center; padding: 24px; background: var(--color-fill-2); }
  .lock-panel { display: flex; width: min(100%, 400px); flex-direction: column; align-items: center; padding: 36px 32px 28px; border: 1px solid var(--color-border-2); border-radius: 8px; background: var(--color-bg-2); box-shadow: 0 12px 32px rgb(0 0 0 / 8%); }
  .lock-avatar { margin-bottom: 18px; background: rgb(var(--arcoblue-2)); color: rgb(var(--arcoblue-6)); }
  .lock-panel .arco-typography-title { margin: 0 0 6px; }
  .unlock-form { width: 100%; margin-top: 26px; }
  .unlock-form .arco-form-item { margin-bottom: 18px; }
  @media (max-width: 480px) { .lock-page { padding: 12px; } .lock-panel { padding: 28px 18px 22px; } }
</style>
