<template>
  <a-list :bordered="false">
    <a-list-item>
      <a-list-item-meta title="登录密码" description="定期修改密码可以提升账号安全性。" />
      <template #extra>
        <a-button type="text" @click="visible = true">修改</a-button>
      </template>
    </a-list-item>
    <a-list-item>
      <a-list-item-meta title="安全邮箱" description="当前 Java 用户资料中的邮箱地址。" />
      <template #extra><a-tag :class="['status-tag', email ? 'status-tag--success' : 'status-tag--neutral']">{{ email ? '已设置' : '未设置' }}</a-tag></template>
    </a-list-item>
    <a-list-item>
      <a-list-item-meta title="安全手机" description="当前 Java 用户资料中的手机号码。" />
      <template #extra><a-tag :class="['status-tag', phone ? 'status-tag--success' : 'status-tag--neutral']">{{ phone ? '已设置' : '未设置' }}</a-tag></template>
    </a-list-item>
  </a-list>

  <a-modal v-model:visible="visible" title="修改登录密码" :ok-loading="saving" @ok="save">
    <a-form ref="formRef" :model="formData" :rules="rules" layout="vertical">
      <a-form-item field="oldPassword" label="当前密码"><a-input-password v-model="formData.oldPassword" /></a-form-item>
      <a-form-item field="newPassword" label="新密码"><a-input-password v-model="formData.newPassword" /></a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { FormInstance } from '@arco-design/web-vue/es/form';
  import { updateProfilePassword } from '@/api/admin';
  import { useUserStore } from '@/store';

  const userStore = useUserStore();
  const visible = ref(false);
  const saving = ref(false);
  const formRef = ref<FormInstance>();
  const formData = reactive({ oldPassword: '', newPassword: '' });
  const email = computed(() => userStore.email || '');
  const phone = computed(() => userStore.phone || '');
  const rules = {
    oldPassword: [{ required: true, message: '请输入当前密码' }],
    newPassword: [{ required: true, message: '请输入新密码' }, { minLength: 6, message: '密码至少 6 位' }],
  };

  /** 提交密码修改。 */
  async function save() {
    if (await formRef.value?.validate()) return;
    saving.value = true;
    try {
      await updateProfilePassword(formData);
      Object.assign(formData, { oldPassword: '', newPassword: '' });
      visible.value = false;
      Message.success('密码更新成功');
    } finally {
      saving.value = false;
    }
  }
</script>
