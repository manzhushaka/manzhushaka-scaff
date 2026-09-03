<template>
  <main class="register-page">
    <section class="register-panel" aria-labelledby="register-title">
      <div class="brand-mark" aria-hidden="true">
        <img :src="logo" alt="" />
      </div>
      <a-link class="back-link" @click="router.push({ name: 'login' })">
        <template #icon><icon-arrow-left /></template>
        返回登录
      </a-link>
      <div class="register-heading">
        <a-typography-text type="secondary">ACCESS REQUEST</a-typography-text>
        <a-typography-title id="register-title" :heading="3">创建系统账号</a-typography-title>
        <a-typography-paragraph type="secondary">
          注册信息将按 Java 端的账号规则校验。
        </a-typography-paragraph>
      </div>

      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical" size="large" @submit="handleSubmit">
        <a-form-item field="username" label="账号">
          <a-input v-model="form.username" autocomplete="username" placeholder="请输入 2-20 位账号">
            <template #prefix><icon-user /></template>
          </a-input>
        </a-form-item>
        <a-form-item field="password" label="密码">
          <a-input-password v-model="form.password" autocomplete="new-password" placeholder="请输入 5-32 位密码">
            <template #prefix><icon-lock /></template>
          </a-input-password>
        </a-form-item>
        <a-form-item field="confirmPassword" label="确认密码">
          <a-input-password v-model="form.confirmPassword" autocomplete="new-password" placeholder="请再次输入密码">
            <template #prefix><icon-safe /></template>
          </a-input-password>
        </a-form-item>
        <a-form-item v-if="captchaEnabled" field="code" label="验证码">
          <a-space class="captcha-row" fill>
            <a-input v-model="form.code" autocomplete="off" placeholder="请输入验证码" />
            <a-button class="captcha-button" type="outline" :loading="captchaLoading" aria-label="刷新验证码" @click="loadCaptcha">
              <img v-if="codeUrl" :src="codeUrl" alt="验证码" />
              <icon-refresh v-else />
            </a-button>
          </a-space>
        </a-form-item>
        <a-button type="primary" html-type="submit" long :loading="loading">注册账号</a-button>
      </a-form>
    </section>
  </main>
</template>

<script lang="ts" setup>
  import { onMounted, reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { FormInstance } from '@arco-design/web-vue/es/form';
  import { useRouter } from 'vue-router';
  import logo from '@/assets/brand-logo.png';
  import { getCaptcha } from '@/api/user';
  import { register as registerAccount } from '@/api/admin';

  const router = useRouter();
  const formRef = ref<FormInstance>();
  const form = reactive({ username: '', password: '', confirmPassword: '', code: '', uuid: '' });
  const loading = ref(false);
  const captchaLoading = ref(false);
  const captchaEnabled = ref(true);
  const codeUrl = ref('');
  const rules = {
    username: [{ required: true, message: '请输入账号' }],
    password: [{ required: true, message: '请输入密码' }],
    confirmPassword: [{ required: true, message: '请确认密码' }, { validator: (value: string, callback: (error?: string) => void) => { callback(value === form.password ? undefined : '两次输入的密码不一致'); } }],
    code: [{ required: true, message: '请输入验证码' }],
  };

  async function loadCaptcha() {
    captchaLoading.value = true;
    try {
      const response = await getCaptcha();
      captchaEnabled.value = response.captchaEnabled ?? true;
      if (captchaEnabled.value) {
        codeUrl.value = response.img ? `data:image/png;base64,${response.img}` : '';
        form.uuid = response.uuid || '';
      } else {
        codeUrl.value = '';
        form.code = '';
        form.uuid = '';
      }
    } finally {
      captchaLoading.value = false;
    }
  }

  async function handleSubmit({ errors }: { errors?: Record<string, unknown> }) {
    if (errors || loading.value) return;
    loading.value = true;
    try {
      await registerAccount({ username: form.username, password: form.password, code: form.code, uuid: form.uuid });
      Message.success('注册成功，请使用新账号登录');
      router.push({ name: 'login' });
    } finally {
      loading.value = false;
    }
  }

  onMounted(loadCaptcha);
</script>

<style lang="less" scoped>
  .register-page { display: flex; min-height: 100vh; align-items: center; justify-content: center; padding: 24px; background: var(--color-fill-2); }
  .register-panel { width: min(100%, 440px); padding: 32px; border: 1px solid var(--color-border-2); border-radius: 8px; background: var(--color-bg-2); box-shadow: 0 12px 32px rgb(0 0 0 / 8%); }
  .brand-mark { display: flex; width: 44px; height: 44px; align-items: center; justify-content: center; margin-bottom: 20px; border: 1px solid var(--color-border-2); border-radius: 8px; overflow: hidden; background: var(--color-fill-2); }
  .brand-mark img { width: 100%; height: 100%; object-fit: cover; }
  .back-link { margin-bottom: 22px; }
  .register-heading { margin-bottom: 24px; }
  .register-heading .arco-typography-title { margin: 8px 0; }
  .captcha-row { width: 100%; }
  .captcha-button { width: 116px; height: 40px; padding: 0; overflow: hidden; }
  .captcha-button img { width: 100%; height: 100%; object-fit: cover; }
  @media (max-width: 480px) { .register-page { padding: 12px; } .register-panel { padding: 24px 18px; } .captcha-button { width: 96px; } }
</style>
