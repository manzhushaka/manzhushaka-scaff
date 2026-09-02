<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-descriptions
      class="profile-details"
      :data="items"
      :column="2"
      bordered
    >
      <template #label="{ label }">{{ label }}</template>
      <template #value="{ value }">{{ value || '-' }}</template>
    </a-descriptions>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, PropType } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { ProfileBasicRes } from '@/api/profile';

  const props = defineProps({
    renderData: {
      type: Object as PropType<ProfileBasicRes>,
      required: true,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  });
  const { t } = useI18n();
  const display = (value: unknown) => (value === undefined || value === null || value === '' ? '-' : String(value));

  const items = computed(() => [
    { label: t('basicProfile.detail.userId'), value: display(props.renderData.userId) },
    { label: t('basicProfile.detail.userName'), value: display(props.renderData.userName) },
    { label: t('basicProfile.detail.nickName'), value: display(props.renderData.nickName) },
    { label: t('basicProfile.detail.email'), value: display(props.renderData.email) },
    { label: t('basicProfile.detail.phonenumber'), value: display(props.renderData.phonenumber) },
    { label: t('basicProfile.detail.sex'), value: display(props.renderData.sex) },
    { label: t('basicProfile.detail.status'), value: display(props.renderData.status) },
    { label: t('basicProfile.detail.deptName'), value: display(props.renderData.deptName) },
    { label: t('basicProfile.detail.roleGroup'), value: display(props.renderData.roleGroup) },
    { label: t('basicProfile.detail.loginIp'), value: display(props.renderData.loginIp) },
    { label: t('basicProfile.detail.loginDate'), value: display(props.renderData.loginDate) },
    { label: t('basicProfile.detail.createTime'), value: display(props.renderData.createTime) },
    { label: t('basicProfile.detail.updateTime'), value: display(props.renderData.updateTime) },
    { label: t('basicProfile.detail.remark'), value: display(props.renderData.remark) },
  ]);
</script>

<style scoped lang="less">
  .profile-details {
    padding: 20px;
  }

  :deep(.arco-descriptions-item-label) {
    font-weight: normal;
  }

  :deep(.arco-skeleton) {
    min-height: 20px;
  }
</style>
