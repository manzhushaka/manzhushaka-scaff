<template>
  <a-card class="general-card" title="我的部门" :body-style="{ paddingBottom: '12px' }">
    <a-list v-if="departments.length" :bordered="false">
      <a-list-item v-for="department in departments" :key="department.deptId">
        <a-list-item-meta :title="department.deptName || '-'">
          <template #avatar>
            <a-avatar><icon-mind-mapping /></a-avatar>
          </template>
          <template #description>
            {{ department.leader ? `负责人：${department.leader}` : '未设置负责人' }}
          </template>
        </a-list-item-meta>
      </a-list-item>
    </a-list>
    <a-empty v-else description="暂无部门数据" />
  </a-card>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { queryAdminSnapshot } from '@/api/analytics';

  const departments = ref<Record<string, any>[]>([]);

  /** 加载 Java 部门数据。 */
  async function loadData() {
    const snapshot = await queryAdminSnapshot();
    departments.value = snapshot.departments.rows.slice(0, 6);
  }

  onMounted(loadData);
</script>

<style scoped lang="less">
  .general-card {
    min-height: 356px;
  }

  :deep(.arco-list-item) {
    padding-left: 0;
  }
</style>
