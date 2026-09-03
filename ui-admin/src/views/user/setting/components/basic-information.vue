<template>
  <a-form
    ref="formRef"
    :model="formData"
    class="form"
    :label-col-props="{ span: 6 }"
    :wrapper-col-props="{ span: 18 }"
  >
    <a-form-item field="nickName" label="昵称" :rules="requiredRule">
      <a-input v-model="formData.nickName" placeholder="请输入昵称" />
    </a-form-item>
    <a-form-item field="email" label="邮箱">
      <a-input v-model="formData.email" placeholder="请输入邮箱" />
    </a-form-item>
    <a-form-item field="phonenumber" label="手机号码">
      <a-input v-model="formData.phonenumber" placeholder="请输入手机号码" />
    </a-form-item>
    <a-form-item field="sex" label="性别">
      <a-radio-group v-model="formData.sex">
        <a-radio value="0">男</a-radio>
        <a-radio value="1">女</a-radio>
        <a-radio value="2">未知</a-radio>
      </a-radio-group>
    </a-form-item>
    <a-form-item>
      <a-space>
        <a-button type="primary" :loading="saving" @click="save">保存</a-button>
        <a-button @click="reset">重置</a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script lang="ts" setup>
  import { onMounted, reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { FormInstance } from '@arco-design/web-vue/es/form';
  import { getProfile, updateProfile } from '@/api/admin';
  import { useUserStore } from '@/store';

  const userStore = useUserStore();
  const formRef = ref<FormInstance>();
  const saving = ref(false);
  const formData = reactive({ nickName: '', email: '', phonenumber: '', sex: '2' });
  const initialData = reactive({ ...formData });
  const requiredRule = [{ required: true, message: '请输入昵称' }];

  function fillForm(data: Record<string, any>) {
    Object.assign(formData, {
      nickName: data.nickName || userStore.nickName || '',
      email: data.email || userStore.email || '',
      phonenumber: data.phonenumber || userStore.phone || '',
      sex: data.sex || '2',
    });
    Object.assign(initialData, formData);
  }

  /** 加载当前用户资料。 */
  async function loadData() {
    const response = await getProfile();
    fillForm(response.data || {});
  }

  /** 保存当前用户资料。 */
  async function save() {
    if (await formRef.value?.validate()) return;
    saving.value = true;
    try {
      await updateProfile(formData);
      userStore.setInfo({ name: formData.nickName, nickName: formData.nickName, email: formData.email, phone: formData.phonenumber });
      Object.assign(initialData, formData);
      Message.success('资料保存成功');
    } finally {
      saving.value = false;
    }
  }

  /** 恢复最近一次已保存的资料。 */
  function reset() {
    Object.assign(formData, initialData);
  }

  onMounted(loadData);
</script>

<style scoped lang="less">
  .form {
    width: min(540px, 100%);
    margin: 0 auto;
  }
</style>
