<template>
  <a-dropdown trigger="click">
    <a-avatar :size="32" class="user-avatar">
      <img v-if="avatar" alt="用户头像" :src="avatar" />
      <icon-user v-else />
    </a-avatar>
    <template #content>
      <a-doption>
        <a-space @click="$emit('switchRoles')">
          <icon-tag />
          <span>{{ $t('messageBox.switchRoles') }}</span>
        </a-space>
      </a-doption>
      <a-doption>
        <a-space @click="navigate('Info')">
          <icon-user />
          <span>{{ $t('messageBox.userCenter') }}</span>
        </a-space>
      </a-doption>
      <a-doption>
        <a-space @click="navigate('Setting')">
          <icon-settings />
          <span>{{ $t('messageBox.userSettings') }}</span>
        </a-space>
      </a-doption>
      <a-doption>
        <a-space @click="navigate('AccountProfile')">
          <icon-user-group />
          <span>个人资料</span>
        </a-space>
      </a-doption>
      <a-doption>
        <a-space @click="$emit('lock')">
          <icon-lock />
          <span>锁定屏幕</span>
        </a-space>
      </a-doption>
      <a-doption>
        <a-space @click="$emit('logout')">
          <icon-export />
          <span>{{ $t('messageBox.logout') }}</span>
        </a-space>
      </a-doption>
    </template>
  </a-dropdown>
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';

  defineProps<{ avatar?: string }>();
  defineEmits<{
    (event: 'switchRoles'): void;
    (event: 'lock'): void;
    (event: 'logout'): void;
  }>();

  const router = useRouter();

  function navigate(name: string) {
    router.push({ name });
  }
</script>

<style scoped lang="less">
  .user-avatar {
    margin-right: var(--ui-space-2);
    cursor: pointer;
  }
</style>
