<template><div class="assign-page"><a-page-header :subtitle="user.nickName || user.userName || ''" @back="router.back" /><a-card :loading="loading" :bordered="false"><a-checkbox-group v-model="checkedRoleIds" direction="vertical"><a-space direction="vertical"><a-checkbox v-for="role in roles" :key="role.roleId" :value="role.roleId" :disabled="role.status === '1'">{{ role.roleName }}（{{ role.roleKey }}）</a-checkbox></a-space></a-checkbox-group><template #actions><a-button type="primary" :loading="saving" @click="save"><template #icon><icon-save /></template>保存授权</a-button></template></a-card></div></template>
<script lang="ts" setup>
  import { onMounted, ref } from 'vue'; import { useRoute, useRouter } from 'vue-router'; import { Message } from '@arco-design/web-vue'; import { getUserAuthRoles, updateUserAuthRoles } from '@/api/admin';

  const route = useRoute(); const router = useRouter(); const user = ref<Record<string, any>>({}); const roles = ref<Record<string, any>[]>([]); const checkedRoleIds = ref<Array<string | number>>([]); const loading = ref(true); const saving = ref(false);
  async function loadData() { loading.value = true; try { const response = await getUserAuthRoles(route.params.userId as string); const payload = (response as any).data || response; user.value = payload.user || {}; roles.value = payload.roles || []; checkedRoleIds.value = roles.value.filter((item) => item.checked || item.flag || item.userId).map((item) => item.roleId); } finally { loading.value = false; } }
  async function save() { saving.value = true; try { await updateUserAuthRoles({ userId: route.params.userId, roleIds: checkedRoleIds.value }); Message.success('角色授权成功'); router.back(); } finally { saving.value = false; } }
  onMounted(loadData);
</script>
<script lang="ts">
  export default { name: 'AuthRole' };
</script>
<style scoped lang="less">.assign-page { min-height: 100%; padding: 20px; background: var(--color-fill-2); } .arco-card { max-width: 720px; } @media (max-width: 640px) { .assign-page { padding: 12px; } }</style>
