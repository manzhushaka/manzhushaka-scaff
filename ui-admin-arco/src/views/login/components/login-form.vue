<template>
  <div class="login-form-wrapper">
    <div class="login-form-title">{{ $t('login.form.title') }}</div>
    <div class="login-form-sub-title">登录 manzhushaka</div>
    <div class="login-form-error-msg">{{ errorMessage }}</div>
    <a-form
      ref="loginForm"
      :model="userInfo"
      class="login-form"
      layout="vertical"
      @submit="handleSubmit"
    >
      <a-form-item
        field="username"
        :rules="[{ required: true, message: $t('login.form.userName.errMsg') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input
          v-model="userInfo.username"
          :placeholder="$t('login.form.userName.placeholder')"
        >
          <template #prefix>
            <icon-user />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item
        field="password"
        :rules="[{ required: true, message: $t('login.form.password.errMsg') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input-password
          v-model="userInfo.password"
          :placeholder="$t('login.form.password.placeholder')"
          allow-clear
        >
          <template #prefix>
            <icon-lock />
          </template>
        </a-input-password>
      </a-form-item>
      <a-form-item
        v-if="captchaEnabled"
        field="code"
        :rules="[{ required: true, message: '请输入验证码' }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <div class="captcha-row">
          <a-input
            v-model="userInfo.code"
            autocomplete="off"
            placeholder="请输入验证码"
          >
            <template #prefix><icon-safe /></template>
          </a-input>
          <a-button
            class="captcha-button"
            type="outline"
            :loading="captchaLoading"
            aria-label="刷新验证码"
            @click="loadCaptcha"
          >
            <img v-if="codeUrl" :src="codeUrl" alt="验证码" />
            <icon-refresh v-else />
          </a-button>
        </div>
      </a-form-item>
      <a-space :size="16" direction="vertical">
        <div class="login-form-password-actions">
          <a-checkbox
            :model-value="loginConfig.rememberPassword"
            @change="setRememberPassword"
          >
            记住账号
          </a-checkbox>
          <a-link @click="showForgotPassword">忘记密码？</a-link>
        </div>
        <a-button type="primary" html-type="submit" long :loading="loading">
          登录
        </a-button>
        <a-button type="text" long class="login-form-register-btn" @click="router.push({ name: 'register' })">
          注册账号
        </a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
  /* eslint-disable no-use-before-define */
  import { ref, reactive, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { ValidatedError } from '@arco-design/web-vue/es/form/interface';
  import { useI18n } from 'vue-i18n';
  import { useStorage } from '@vueuse/core';
  import { useUserStore } from '@/store';
  import useLoading from '@/hooks/loading';
  import { getCaptcha } from '@/api/user';
  import type { LoginData } from '@/api/user';

  const router = useRouter();
  const { t } = useI18n();
  const errorMessage = ref('');
  const { loading, setLoading } = useLoading();
  const userStore = useUserStore();

  const loginConfig = useStorage('login-config', {
    rememberPassword: true,
    username: '',
  });
  const userInfo = reactive({
    username: loginConfig.value.username,
    password: '',
    code: '',
    uuid: '',
  });
  const captchaEnabled = ref(true);
  const captchaLoading = ref(false);
  const codeUrl = ref('');

  const handleSubmit = async ({
    errors,
    values,
  }: {
    errors: Record<string, ValidatedError> | undefined;
    values: Record<string, any>;
  }) => {
    if (loading.value) return;
    if (!errors) {
      setLoading(true);
      try {
        await userStore.login({ ...values, code: userInfo.code, uuid: userInfo.uuid } as LoginData);
        const { redirect, ...othersQuery } = router.currentRoute.value.query;
        const destination = redirect as string;
        if (destination?.startsWith('/')) {
          await router.replace({
            path: destination,
            query: { ...othersQuery },
          });
        } else {
          await router.replace({
            name: destination || 'Workplace',
            query: { ...othersQuery },
          });
        }
        Message.success(t('login.form.login.success'));
        const { rememberPassword } = loginConfig.value;
        const { username } = values;
        loginConfig.value.username = rememberPassword ? username : '';
      } catch (err) {
        errorMessage.value = (err as Error).message;
        await loadCaptcha();
      } finally {
        setLoading(false);
      }
    }
  };
  const setRememberPassword = (value: boolean | Array<string | number | boolean>) => {
    loginConfig.value.rememberPassword = Array.isArray(value) ? value.includes(true) : value;
  };
  const showForgotPassword = () => {
    Message.info('请联系系统管理员重置密码');
  };
  const loadCaptcha = async () => {
    captchaLoading.value = true;
    try {
      const response = await getCaptcha();
      captchaEnabled.value = response.captchaEnabled ?? true;
      if (captchaEnabled.value) {
        codeUrl.value = response.img ? `data:image/png;base64,${response.img}` : '';
        userInfo.uuid = response.uuid || '';
      } else {
        codeUrl.value = '';
        userInfo.code = '';
        userInfo.uuid = '';
      }
    } finally {
      captchaLoading.value = false;
    }
  };

  onMounted(loadCaptcha);
</script>

<style lang="less" scoped>
  .login-form {
    &-wrapper {
      width: 320px;
    }

    &-title {
      color: var(--color-text-1);
      font-weight: 500;
      font-size: 24px;
      line-height: 32px;
    }

    &-sub-title {
      color: var(--color-text-3);
      font-size: 16px;
      line-height: 24px;
    }

    &-error-msg {
      height: 32px;
      color: rgb(var(--red-6));
      line-height: 32px;
    }

    &-password-actions {
      display: flex;
      justify-content: space-between;
    }

    .captcha-row {
      display: flex;
      width: 100%;
      gap: 10px;
    }

    .captcha-row .arco-input-wrapper {
      flex: 1;
    }

    .captcha-button {
      width: 112px;
      height: 40px;
      padding: 0;
      overflow: hidden;
    }

    .captcha-button img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    &-register-btn {
      color: var(--color-text-3) !important;
    }
  }
</style>
