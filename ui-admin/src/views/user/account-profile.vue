<template>
  <div class="account-page">
    <a-grid :cols="24" :col-gap="16" :row-gap="16">
      <a-grid-item :span="{ xs: 24, lg: 8 }"><a-card :loading="loading" :bordered="false" class="profile-card"><a-avatar :size="88"><img v-if="profile.avatar" :src="profile.avatar" alt="用户头像" /></a-avatar><a-typography-title :heading="5">{{ profile.nickName || profile.userName || userStore.name || '-' }}</a-typography-title><a-typography-text type="secondary">{{ profile.dept?.deptName || '系统用户' }}</a-typography-text><a-divider /><a-descriptions :column="1" bordered><a-descriptions-item label="用户名">{{ profile.userName || '-' }}</a-descriptions-item><a-descriptions-item label="用户编号">{{ profile.userId || '-' }}</a-descriptions-item><a-descriptions-item label="角色组">{{ profile.roleGroup || '-' }}</a-descriptions-item><a-descriptions-item label="创建时间">{{ profile.createTime || '-' }}</a-descriptions-item></a-descriptions></a-card></a-grid-item>
      <a-grid-item :span="{ xs: 24, lg: 16 }"><a-card title="基本资料" :loading="loading" :bordered="false"><a-form ref="profileFormRef" :model="profileForm" :label-col-props="{ span: 5 }" :wrapper-col-props="{ span: 18 }"><a-form-item field="nickName" label="用户昵称" :rules="requiredRule"><a-input v-model="profileForm.nickName" /></a-form-item><a-form-item field="email" label="邮箱"><a-input v-model="profileForm.email" /></a-form-item><a-form-item field="phonenumber" label="手机号码"><a-input v-model="profileForm.phonenumber" /></a-form-item><a-form-item field="sex" label="性别"><a-radio-group v-model="profileForm.sex"><a-radio value="0">男</a-radio><a-radio value="1">女</a-radio><a-radio value="2">未知</a-radio></a-radio-group></a-form-item><a-form-item><a-button type="primary" :loading="saving" @click="saveProfile"><template #icon><icon-save /></template>保存资料</a-button></a-form-item></a-form></a-card><a-card title="安全设置" :bordered="false" class="password-card"><a-alert type="info" show-icon>修改密码后需要使用新密码重新登录。</a-alert><a-form ref="passwordFormRef" :model="passwordForm" :label-col-props="{ span: 5 }" :wrapper-col-props="{ span: 18 }" class="password-form"><a-form-item field="oldPassword" label="当前密码" :rules="requiredRule"><a-input-password v-model="passwordForm.oldPassword" /></a-form-item><a-form-item field="newPassword" label="新密码" :rules="requiredRule"><a-input-password v-model="passwordForm.newPassword" /></a-form-item><a-form-item><a-button type="outline" :loading="passwordSaving" @click="savePassword"><template #icon><icon-safe /></template>更新密码</a-button></a-form-item></a-form></a-card></a-grid-item>
    </a-grid>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { FormInstance } from '@arco-design/web-vue/es/form';
  import { getProfile, updateProfile, updateProfilePassword } from '@/api/admin';
  import useUserStore from '@/store/modules/user';

  const userStore = useUserStore(); const loading = ref(true); const saving = ref(false); const passwordSaving = ref(false); const profileFormRef = ref<FormInstance>(); const passwordFormRef = ref<FormInstance>(); const profile = ref<Record<string, any>>({}); const profileForm = reactive<Record<string, any>>({ nickName: '', email: '', phonenumber: '', sex: '2' }); const passwordForm = reactive<Record<string, any>>({ oldPassword: '', newPassword: '' }); const requiredRule = [{ required: true, message: '该字段不能为空' }];
  async function loadData() { loading.value = true; try { const response = await getProfile(); profile.value = response.data || {}; Object.assign(profileForm, { nickName: profile.value.nickName || '', email: profile.value.email || '', phonenumber: profile.value.phonenumber || '', sex: profile.value.sex || '2' }); } finally { loading.value = false; } }
  async function saveProfile() { if (await profileFormRef.value?.validate()) return; saving.value = true; try { await updateProfile(profileForm); userStore.setInfo({ name: profileForm.nickName, nickName: profileForm.nickName, email: profileForm.email, phone: profileForm.phonenumber }); profile.value = { ...profile.value, ...profileForm }; Message.success('资料保存成功'); } finally { saving.value = false; } }
  async function savePassword() { if (await passwordFormRef.value?.validate()) return; passwordSaving.value = true; try { await updateProfilePassword(passwordForm); Object.assign(passwordForm, { oldPassword: '', newPassword: '' }); Message.success('密码更新成功'); } finally { passwordSaving.value = false; } }
  onMounted(loadData);
</script>

<script lang="ts">
  export default { name: 'AccountProfile' };
</script>

<style scoped lang="less">
  .account-page { min-height: 100%; padding: 20px; background: var(--color-fill-2); }
  .profile-card, .password-card { border-radius: 6px; } .profile-card { text-align: center; } .profile-card :deep(.arco-divider) { margin: 20px 0; } .profile-card :deep(.arco-descriptions) { text-align: left; } .password-card { margin-top: 16px; } .password-form { margin-top: 20px; }
  @media (max-width: 640px) { .account-page { padding: 12px; } }
</style>
