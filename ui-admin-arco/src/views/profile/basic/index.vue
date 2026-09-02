<template>
  <div class="container">
    <a-space direction="vertical" :size="16" fill>
      <a-card class="general-card" :title="$t('basicProfile.title.form')">
        <template #extra>
          <a-button type="outline" @click="goBack">
            <template #icon><icon-arrow-left /></template>
            {{ $t('basicProfile.goBack') }}
          </a-button>
        </template>
        <a-alert type="info">
          {{ $t('basicProfile.detail.source') }}
        </a-alert>
      </a-card>
      <a-card class="general-card" :title="$t('basicProfile.title.currentParams')">
        <ProfileItem :loading="loading" :render-data="currentData" />
      </a-card>
      <OperationLog />
    </a-space>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import useLoading from '@/hooks/loading';
  import { queryProfileBasic, ProfileBasicRes } from '@/api/profile';
  import ProfileItem from './components/profile-item.vue';
  import OperationLog from './components/operation-log.vue';

  const router = useRouter();
  const { loading, setLoading } = useLoading(true);
  const currentData = ref<ProfileBasicRes>({});

  /** 加载当前登录用户资料。 */
  async function fetchCurrentData() {
    try {
      const response = await queryProfileBasic();
      currentData.value = response.data || {};
    } finally {
      setLoading(false);
    }
  }

  /** 返回上一页。 */
  function goBack() {
    router.back();
  }

  fetchCurrentData();
</script>

<script lang="ts">
  export default {
    name: 'Basic',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 20px;
  }
</style>
