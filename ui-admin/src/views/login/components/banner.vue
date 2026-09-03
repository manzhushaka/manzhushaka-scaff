<template>
  <div class="banner">
    <div class="brand-heading">
      <img class="brand-icon" :src="brandLogo" alt="manzhushaka 标识" />
      <div>
        <div class="brand-name">manzhushaka</div>
        <div class="brand-type">管理系统</div>
      </div>
    </div>
    <div class="brand-visual" aria-hidden="true">
      <img :src="brandLogo" alt="" />
    </div>
    <div class="brand-caption">
      <h1>manzhushaka</h1>
      <p>管理系统</p>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import brandLogo from '@/assets/brand-logo.png';
</script>

<style lang="less" scoped>
  .banner {
    position: relative;
    display: flex;
    min-height: 100vh;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    padding: 96px 56px 64px;
    background: var(--color-fill-1);
    border-right: 1px solid var(--color-border-2);
  }

  .brand-heading {
    position: absolute;
    top: 28px;
    left: 32px;
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .brand-icon {
    width: 44px;
    height: 44px;
    border-radius: 8px;
    object-fit: cover;
  }

  .brand-name {
    color: var(--color-text-1);
    font-weight: 600;
    font-size: 18px;
    line-height: 24px;
  }

  .brand-type {
    color: var(--color-text-3);
    font-size: 12px;
    line-height: 18px;
  }

  .brand-visual {
    width: min(100%, 430px);
    aspect-ratio: 1;
  }

  .brand-visual img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain;
  }

  .brand-caption {
    margin-top: 16px;
    text-align: center;
  }

  .brand-caption h1 {
    margin: 0;
    color: var(--color-text-1);
    font-size: 24px;
    line-height: 32px;
  }

  .brand-caption p {
    margin: 8px 0 0;
    color: var(--color-text-3);
    font-size: 14px;
    line-height: 22px;
  }

  @media (max-width: 640px) {
    .banner {
      min-height: 132px;
      flex-direction: row;
      justify-content: flex-start;
      padding: 24px;
      border-right: 0;
      border-bottom: 1px solid var(--color-border-2);
    }

    .brand-heading {
      position: static;
    }

    .brand-icon {
      width: 52px;
      height: 52px;
    }

    .brand-visual,
    .brand-caption {
      display: none;
    }
  }
</style>
