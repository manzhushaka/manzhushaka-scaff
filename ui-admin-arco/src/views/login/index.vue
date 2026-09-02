<template>
  <main class="login-page">
    <LoginBanner />
    <section class="login-workspace" aria-label="登录工作区">
      <div class="login-content">
        <LoginForm />
      </div>
      <div class="login-footer">
        <Footer />
      </div>
    </section>
  </main>
</template>

<script lang="ts" setup>
  import Footer from '@/components/footer/index.vue';
  import LoginBanner from './components/banner.vue';
  import LoginForm from './components/login-form.vue';
</script>

<style lang="less" scoped>
  .login-page {
    display: grid;
    min-height: 100vh;
    grid-template-columns: minmax(420px, 44%) minmax(0, 1fr);
    background: var(--color-bg-2);
  }

  .login-workspace {
    position: relative;
    display: flex;
    min-width: 0;
    align-items: center;
    justify-content: center;
    padding: 48px 40px 72px;
  }

  .login-content {
    width: min(100%, 360px);
  }

  .login-footer {
    position: absolute;
    right: 0;
    bottom: 8px;
    left: 0;
  }

  @media (max-width: @screen-lg) {
    .login-page {
      grid-template-columns: minmax(320px, 38%) minmax(0, 1fr);
    }
  }

  @media (max-width: 640px) {
    .login-page {
      display: flex;
      min-height: 100dvh;
      flex-direction: column;
    }

    .login-workspace {
      flex: 1;
      align-items: flex-start;
      padding: 32px 24px 64px;
    }

    .login-content {
      width: 100%;
    }
  }
</style>
