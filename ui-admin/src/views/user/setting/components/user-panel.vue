<template>
  <a-card :bordered="false">
    <a-space :size="36" wrap>
      <a-upload
        :custom-request="customRequest"
        :file-list="fileList"
        :show-upload-button="true"
        :show-file-list="false"
        accept="image/*"
        @change="uploadChange"
      >
        <template #upload-button>
          <a-avatar :size="100" class="info-avatar">
            <template #trigger-icon><icon-camera /></template>
            <img v-if="fileList[0]?.url" :src="fileList[0].url" alt="用户头像" />
            <icon-user v-else />
          </a-avatar>
        </template>
      </a-upload>
      <a-descriptions
        :data="renderData"
        :column="2"
        align="right"
        layout="inline-horizontal"
        :label-style="{ width: '140px', fontWeight: 'normal', color: 'rgb(var(--gray-8))' }"
        :value-style="{ width: '200px', paddingLeft: '8px', textAlign: 'left' }"
      >
        <template #label="{ label }">{{ label }}：</template>
        <template #value="{ value }">{{ value || '-' }}</template>
      </a-descriptions>
    </a-space>
  </a-card>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import type { FileItem, RequestOption } from '@arco-design/web-vue/es/upload/interfaces';
  import { Message } from '@arco-design/web-vue';
  import { getProfile, uploadUserAvatar } from '@/api/admin';
  import { useUserStore } from '@/store';

  const userStore = useUserStore();
  const profile = ref<Record<string, any>>({});
  const fileList = ref<FileItem[]>([]);
  const renderData = computed(() => [
    { label: '用户名', value: profile.value.userName || userStore.userName },
    { label: '用户编号', value: profile.value.userId || userStore.id },
    { label: '部门', value: profile.value.dept?.deptName || userStore.organizationName },
    { label: '角色组', value: profile.value.roleGroup || userStore.roles.join('、') },
  ]);

  function renderProfile(data: Record<string, any>) {
    profile.value = data;
    fileList.value = data.avatar
      ? [{ uid: '-1', name: 'avatar', url: data.avatar }]
      : [];
  }

  /** 加载当前账号资料。 */
  async function loadData() {
    const response = await getProfile();
    renderProfile(response.data || {});
  }

  function uploadChange(items: FileItem[], item: FileItem) {
    fileList.value = item ? [item] : items;
  }

  function customRequest(options: RequestOption) {
    const controller = new AbortController();
    const request = async () => {
      const { onProgress, onError, onSuccess, fileItem } = options;
      const formData = new FormData();
      formData.append('avatarfile', fileItem.file as Blob);
      try {
        const response = await uploadUserAvatar(formData);
        const avatar = response.data?.imgUrl || '';
        if (avatar) {
          fileList.value = [{ ...fileItem, url: avatar }];
          userStore.setInfo({ avatar });
        }
        onProgress(100);
        onSuccess(response);
        Message.success('头像更新成功');
      } catch (error) {
        onError(error);
      }
    };
    request();
    return { abort: () => controller.abort() };
  }

  onMounted(loadData);
</script>

<style scoped lang="less">
  .arco-card {
    padding: 14px 0 4px 4px;
  }

  .info-avatar {
    color: rgb(var(--arcoblue-6));
    background-color: rgb(var(--arcoblue-1));
  }
</style>
