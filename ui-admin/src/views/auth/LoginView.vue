<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-hero">
        <div class="hero-geometry hero-geometry-panel"></div>
        <div class="hero-geometry hero-geometry-shadow"></div>
        <div class="hero-geometry hero-geometry-accent"></div>

        <div class="hero-content">
          <div class="brand-row">
            <span class="brand-mark"></span>
            <span class="brand-name">manzhushaka Admin</span>
          </div>
          <h1>欢迎回来</h1>
          <p>登录管理系统，继续高效管理</p>
          <span class="hero-divider"></span>
        </div>

        <div class="hero-note">© {{ currentYear }} manzhushaka. 保留所有权利。</div>
      </section>

      <section class="login-panel">
        <a-card class="login-card" :bordered="false">
          <div class="card-header">
            <h2>登录</h2>
            <p>请输入您的账号、密码和图片验证码</p>
          </div>

          <a-form :model="form" class="login-form" @submit-success="handleSubmit">
            <a-form-item field="username" hide-label>
              <a-input v-model="form.username" class="login-input" placeholder="用户名 / 邮箱" allow-clear>
                <template #prefix>
                  <icon-user />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item field="password" hide-label>
              <a-input-password
                v-model="form.password"
                class="login-input"
                placeholder="密码"
                :default-visibility="false"
              >
                <template #prefix>
                  <icon-lock />
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item field="captchaCode" hide-label>
              <div class="captcha-row">
                <a-input
                  v-model="form.captchaCode"
                  class="login-input captcha-input"
                  placeholder="请输入图片验证码"
                  allow-clear
                />
                <a-button
                  class="captcha-trigger"
                  type="outline"
                  :loading="captchaLoading"
                  @click="refreshCaptcha"
                >
                  <img v-if="captchaImage" :src="captchaImage" alt="登录验证码" />
                  <span v-else class="captcha-placeholder">点击刷新</span>
                </a-button>
              </div>
            </a-form-item>

            <div class="form-meta">
              <a-checkbox v-model="form.remember">记住我</a-checkbox>
              <a-link class="assist-link" @click="handleAssist('找回密码')">忘记密码？</a-link>
            </div>

            <a-button class="login-submit" type="primary" long html-type="submit" :loading="loading">
              登录
            </a-button>
          </a-form>

          <a-divider class="divider-row">其他登录方式</a-divider>

          <a-space class="social-row" size="large">
            <a-button
              v-for="item in socialProviders"
              :key="item.name"
              :class="['social-button', `social-button--${item.className}`]"
              type="outline"
              shape="circle"
              @click="handleAssist(`${item.name} 登录`)"
            >
              <component :is="item.icon" />
            </a-button>
          </a-space>

          <a-alert v-if="showDemoHint" class="login-hint" type="info" :show-icon="false">
            默认演示账号：admin，默认密码：Admin@123456，验证码可点击图片刷新。
          </a-alert>
        </a-card>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import { useRouter } from 'vue-router';
import {
  IconGoogle,
  IconLock,
  IconQqCircleFill,
  IconUser,
  IconWechat,
} from '@arco-design/web-vue/es/icon';
import { useAuthStore } from '@/store/auth';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const captchaLoading = ref(false);
const captchaImage = ref('');
const currentYear = new Date().getFullYear();
const showDemoHint = import.meta.env.DEV;
const form = reactive({
  username: '',
  password: '',
  captchaKey: '',
  captchaCode: '',
  remember: true,
});
const socialProviders = [
  { name: 'QQ', icon: IconQqCircleFill, className: 'qq' },
  { name: '微信', icon: IconWechat, className: 'wechat' },
  { name: 'Google', icon: IconGoogle, className: 'google' },
];

async function handleSubmit() {
  loading.value = true;
  try {
    await authStore.login(form.username, form.password, form.captchaKey, form.captchaCode);
    Message.success('登录成功');
    router.replace('/');
  } catch (error) {
    try {
      await refreshCaptcha();
    } catch {
      // Request layer already surfaces the refresh error.
    }
    Message.error((error as Error).message || '登录失败');
  } finally {
    loading.value = false;
  }
}

async function refreshCaptcha() {
  captchaLoading.value = true;
  try {
    const captcha = await authStore.fetchCaptcha();
    form.captchaKey = captcha.key;
    form.captchaCode = '';
    captchaImage.value = captcha.imageBase64;
  } finally {
    captchaLoading.value = false;
  }
}

function handleAssist(feature: string) {
  Message.info(`${feature}功能将在后续版本开放`);
}

onMounted(() => {
  void refreshCaptcha();
});
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 20%, rgba(117, 163, 255, 0.18), transparent 28%),
    radial-gradient(circle at 78% 16%, rgba(104, 154, 255, 0.12), transparent 24%),
    linear-gradient(135deg, #f7faff 0%, #f2f6ff 42%, #fbfdff 100%);
}

.login-page::before,
.login-page::after {
  position: absolute;
  inset: auto;
  content: '';
  pointer-events: none;
}

.login-page::before {
  top: -18%;
  left: 25%;
  width: 540px;
  height: 540px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.78), rgba(225, 235, 255, 0.18));
  border: 1px solid rgba(167, 193, 255, 0.3);
  border-radius: 36px;
  transform: rotate(20deg);
  box-shadow: 0 48px 120px rgba(142, 170, 225, 0.2);
}

.login-page::after {
  right: -8%;
  bottom: -20%;
  width: 520px;
  height: 520px;
  background: radial-gradient(circle, rgba(74, 131, 255, 0.18), rgba(74, 131, 255, 0));
}

.login-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 460px;
  gap: clamp(48px, 7vw, 104px);
  align-items: center;
  max-width: 1220px;
  min-height: 100vh;
  margin: 0 auto;
  padding: 40px 48px;
}

.login-hero {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 640px;
  padding: 64px 24px 72px 80px;
  color: #18243d;
}

.hero-geometry {
  position: absolute;
  pointer-events: none;
}

.hero-geometry-panel {
  top: 34px;
  left: 8px;
  width: 330px;
  height: 480px;
  border: 1px solid rgba(184, 203, 246, 0.46);
  border-radius: 32px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.72), rgba(225, 235, 255, 0.34));
  box-shadow: 0 36px 70px rgba(132, 154, 200, 0.18);
  transform: rotate(20deg);
}

.hero-geometry-shadow {
  bottom: 86px;
  left: 4px;
  width: 650px;
  height: 200px;
  border-radius: 34px;
  background: linear-gradient(135deg, rgba(31, 99, 255, 0.24), rgba(104, 163, 255, 0.04));
  box-shadow: 0 18px 60px rgba(83, 124, 216, 0.18);
  transform: rotate(32deg);
}

.hero-geometry-accent {
  top: -50px;
  left: 185px;
  width: 4px;
  height: 760px;
  background: linear-gradient(180deg, rgba(163, 191, 255, 0.12), rgba(255, 255, 255, 0.96), rgba(127, 163, 255, 0.06));
  box-shadow: 0 0 12px rgba(255, 255, 255, 0.5);
  transform: rotate(20deg);
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 320px;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.brand-mark {
  position: relative;
  width: 22px;
  height: 22px;
  border-radius: 7px;
  background: linear-gradient(180deg, #69a0ff 0%, #256bff 100%);
  box-shadow: 0 14px 28px rgba(49, 102, 255, 0.28);
}

.brand-mark::before,
.brand-mark::after {
  position: absolute;
  content: '';
  background: inherit;
  border-radius: inherit;
}

.brand-mark::before {
  top: 4px;
  right: -8px;
  width: 14px;
  height: 14px;
  opacity: 0.7;
}

.brand-mark::after {
  top: 5px;
  left: 5px;
  width: 12px;
  height: 12px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: inset 0 0 0 1px rgba(129, 166, 255, 0.42);
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.01em;
}

h1 {
  margin: 0 0 18px;
  color: #1a2742;
  font-size: clamp(44px, 6vw, 58px);
  line-height: 1.06;
  letter-spacing: -0.03em;
}

p {
  margin: 0;
  color: #6b7892;
  font-size: 18px;
  line-height: 1.8;
}

.hero-divider {
  display: inline-block;
  width: 34px;
  height: 4px;
  margin-top: 38px;
  border-radius: 999px;
  background: linear-gradient(90deg, #256bff 0%, #6a9cff 100%);
  box-shadow: 0 10px 22px rgba(37, 107, 255, 0.18);
}

.hero-note {
  position: absolute;
  bottom: 18px;
  left: 0;
  color: rgba(135, 146, 170, 0.78);
  font-size: 12px;
}

.login-panel {
  display: flex;
  justify-content: center;
}

.login-card {
  width: 100%;
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow:
    0 32px 72px rgba(118, 145, 201, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
}

.login-card :deep(.arco-card-body) {
  padding: 40px 42px 28px;
}

.card-header {
  margin-bottom: 26px;
}

.card-header h2 {
  margin: 0;
  color: #1f2f52;
  font-size: 42px;
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.card-header p {
  margin-top: 12px;
  color: #98a2b6;
  font-size: 14px;
  line-height: 1.6;
}

.login-form {
  margin-top: 10px;
}

.login-form :deep(.arco-form-item) {
  margin-bottom: 16px;
}

.login-form :deep(.arco-input-wrapper),
.login-form :deep(.arco-input-password) {
  height: 48px;
  border: 1px solid rgba(196, 209, 232, 0.82);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: inset 0 1px 2px rgba(12, 36, 86, 0.03);
}

.login-form :deep(.arco-input-wrapper:hover),
.login-form :deep(.arco-input-password:hover) {
  border-color: rgba(120, 154, 229, 0.92);
}

.login-form :deep(.arco-input-wrapper.arco-input-focus),
.login-form :deep(.arco-input-password.arco-input-focus) {
  border-color: #2c6bff;
  box-shadow: 0 0 0 3px rgba(44, 107, 255, 0.12);
}

.login-form :deep(.arco-input-prefix),
.login-form :deep(.arco-input-password .arco-input-prefix) {
  margin-right: 10px;
  color: #9eaac1;
  font-size: 16px;
}

.login-form :deep(.arco-input),
.login-form :deep(.arco-input-password input) {
  color: #20304d;
  font-size: 14px;
}

.login-form :deep(.arco-input::placeholder),
.login-form :deep(.arco-input-password input::placeholder) {
  color: #a4aec1;
}

.captcha-row {
  display: flex;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

.captcha-trigger {
  width: 148px;
  height: 48px;
  padding: 0;
  overflow: hidden;
  border-color: rgba(196, 209, 232, 0.82);
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(248, 251, 255, 0.98), rgba(240, 245, 255, 0.92));
  box-shadow: inset 0 1px 2px rgba(12, 36, 86, 0.03);
}

.captcha-trigger :deep(.arco-btn-content) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.captcha-trigger img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #7f8ca5;
  font-size: 13px;
  font-weight: 600;
}

.form-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0 22px;
}

.form-meta :deep(.arco-checkbox-label) {
  color: #77839a;
  font-size: 13px;
}

.assist-link {
  font-size: 13px;
  font-weight: 600;
}

.login-submit {
  height: 48px;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.04em;
  background: linear-gradient(90deg, #2c6bff 0%, #4384ff 100%);
  box-shadow: 0 16px 28px rgba(44, 107, 255, 0.26);
}

.login-submit:hover {
  background: linear-gradient(90deg, #1f5ef6 0%, #3778f5 100%);
}

.divider-row {
  margin: 28px 0 22px;
  color: #b0b8c7;
  font-size: 12px;
}

.divider-row :deep(.arco-divider-text) {
  color: inherit;
}

.social-row {
  display: flex;
  justify-content: center;
  width: 100%;
}

.social-button {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 12px 24px rgba(166, 180, 208, 0.14);
}

.social-button--qq {
  color: #4f84ff;
}

.social-button--wechat {
  color: #17b26a;
}

.social-button--google {
  color: #f04438;
}

.login-hint {
  margin-top: 22px;
  border-radius: 14px;
  background: rgba(247, 250, 255, 0.92);
}

.login-hint :deep(.arco-alert-content) {
  color: #7a88a0;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 1080px) {
  .login-shell {
    grid-template-columns: 1fr;
    gap: 18px;
    padding: 24px;
  }

  .login-hero {
    min-height: 420px;
    padding: 48px 0 66px 24px;
  }

  .hero-note {
    left: 24px;
  }

  .login-panel {
    justify-content: stretch;
  }

  .login-card {
    max-width: 520px;
    margin: 0 auto;
  }
}

@media (max-width: 720px) {
  .login-page::before {
    top: -8%;
    left: 50%;
    width: 320px;
    height: 420px;
    transform: translateX(-50%) rotate(20deg);
  }

  .login-shell {
    padding: 18px;
  }

  .login-hero {
    min-height: 320px;
    padding: 24px 0 54px;
  }

  .hero-geometry-panel {
    top: 34px;
    left: -16px;
    width: 240px;
    height: 290px;
  }

  .hero-geometry-shadow {
    bottom: 58px;
    left: -32px;
    width: 360px;
    height: 120px;
  }

  .hero-geometry-accent {
    top: -12px;
    left: 120px;
    height: 420px;
  }

  .brand-row {
    margin-bottom: 20px;
  }

  h1 {
    font-size: 42px;
  }

  .hero-note {
    bottom: 8px;
    left: 0;
  }

  .login-card :deep(.arco-card-body) {
    padding: 30px 22px 24px;
  }

  .login-card {
    border-radius: 24px;
  }

  .card-header h2 {
    font-size: 34px;
  }

  .captcha-row {
    flex-direction: column;
  }

  .captcha-trigger {
    width: 100%;
  }
}
</style>
