<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: 0 }"
      :body-style="{ padding: '20px' }"
      title="菜单类型分布"
    >
      <Chart v-if="total" height="310px" :option="chartOption" />
      <a-empty v-else description="暂无菜单数据" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import useChartOption from '@/hooks/chart-option';
  import { queryDashboardData } from '@/api/dashboard';

  const { loading, setLoading } = useLoading(true);
  const counts = ref({ M: 0, C: 0, F: 0 });
  const total = computed(() => counts.value.M + counts.value.C + counts.value.F);
  const { chartOption } = useChartOption((isDark) => ({
    legend: {
      left: 'center',
      bottom: 0,
      icon: 'circle',
      data: ['目录', '菜单', '按钮'],
      textStyle: { color: isDark ? '#C9CDD4' : '#4E5969' },
    },
    tooltip: { trigger: 'item' },
    graphic: {
      elements: [
        {
          type: 'text',
          left: 'center',
          top: '40%',
          style: { text: '菜单总数', textAlign: 'center', fill: isDark ? '#C9CDD4' : '#4E5969', fontSize: 14 },
        },
        {
          type: 'text',
          left: 'center',
          top: '50%',
          style: { text: String(total.value), textAlign: 'center', fill: isDark ? '#F7F8FA' : '#1D2129', fontSize: 16, fontWeight: 500 },
        },
      ],
    },
    series: [
      {
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '45%'],
        label: { formatter: '{d}%', color: isDark ? '#C9CDD4' : '#4E5969' },
        data: [
          { value: counts.value.M, name: '目录', itemStyle: { color: '#165DFF' } },
          { value: counts.value.C, name: '菜单', itemStyle: { color: '#14C9C9' } },
          { value: counts.value.F, name: '按钮', itemStyle: { color: '#F7BA1E' } },
        ],
      },
    ],
  }));

  /** 加载 Java 菜单数据并统计类型。 */
  async function loadData() {
    setLoading(true);
    try {
      const snapshot = await queryDashboardData();
      const next = { M: 0, C: 0, F: 0 };
      snapshot.menus.rows.forEach((record) => {
        const type = record.menuType as keyof typeof next;
        if (type in next) next[type] += 1;
      });
      counts.value = next;
    } finally {
      setLoading(false);
    }
  }

  onMounted(loadData);
</script>
