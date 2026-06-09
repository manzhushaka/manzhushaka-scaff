<template>
  <div class="login-shell">
    <div class="login-hero">
      <div class="hero-badge">Task 5 / Frontend Admin Scaffold</div>
      <h1>满洲沙卡后台管理台</h1>
      <p>这一版优先打通登录、布局、动态菜单、权限与系统管理主链路，直接对接当前 Spring Boot 后端。</p>
    </div>
    <div class="page-card login-card">
      <a-form :model="form" layout="vertical" @submit-success="handleSubmit">
        <a-form-item field="username" label="用户名">
          <a-input v-model="form.username" placeholder="请输入 admin" />
        </a-form-item>
        <a-form-item field="password" label="密码">
          <a-input-password v-model="form.password" placeholder="请输入默认密码 Admin@123456" />
        </a-form-item>
        <a-button type="primary" long html-type="submit" :loading="loading">
          登录
        </a-button>
      </a-form>
      <div class="login-hint">默认演示账号：`admin`，默认密码：`Admin@123456`。</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const form = reactive({
  username: 'admin',
  password: 'Admin@123456',
});

async function handleSubmit() {
  loading.value = true;
  try {
    await authStore.login(form.username, form.password);
    Message.success('登录成功');
    router.replace('/');
  } catch (error) {
    Message.error((error as Error).message || '登录失败');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-shell {
  display: grid;
  grid-template-columns: 1.2fr 420px;
  gap: 32px;
  align-items: center;
  min-height: 100vh;
  padding: 48px;
}

.login-hero {
  padding: 48px;
  color: #0f172a;
}

.hero-badge {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 999px;
  color: #165dff;
  background: rgba(22, 93, 255, 0.1);
}

h1 {
  margin: 20px 0 12px;
  font-size: 44px;
  line-height: 1.1;
}

p {
  max-width: 640px;
  color: #475569;
  font-size: 18px;
  line-height: 1.7;
}

.login-card {
  padding: 32px;
}

.login-hint {
  margin-top: 16px;
  color: #6b7280;
  font-size: 13px;
}

@media (max-width: 960px) {
  .login-shell {
    grid-template-columns: 1fr;
    padding: 20px;
  }

  .login-hero {
    padding: 12px 0;
  }

  h1 {
    font-size: 32px;
  }
}
</style>
