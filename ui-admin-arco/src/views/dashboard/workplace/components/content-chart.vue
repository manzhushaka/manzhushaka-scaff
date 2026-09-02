<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: 0 }"
      :body-style="{ paddingTop: '20px' }"
      title="操作日志趋势"
    >
      <template #extra>
        <a-link @click="loadData">刷新</a-link>
      </template>
      <Chart v-if="xAxis.length" height="289px" :option="chartOption" />
      <a-empty v-else description="暂无操作日志" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import { queryDashboardData, DashboardSnapshot } from '@/api/dashboard';
  import useChartOption from '@/hooks/chart-option';

  const { loading, setLoading } = useLoading(true);
  const xAxis = ref<string[]>([]);
  const chartsData = ref<number[]>([]);
  const { chartOption } = useChartOption((isDark) => ({
    grid: { left: '2.6%', right: '0', top: '10', bottom: '30' },
    xAxis: {
      type: 'category',
      data: xAxis.value,
      boundaryGap: false,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: isDark ? '#C9CDD4' : '#4E5969' },
      splitLine: { show: true, lineStyle: { color: isDark ? '#3F3F3F' : '#E5E6EB' } },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisLabel: { color: isDark ? '#C9CDD4' : '#4E5969' },
      splitLine: { lineStyle: { type: 'dashed', color: isDark ? '#3F3F3F' : '#E5E6EB' } },
    },
    tooltip: { trigger: 'axis' },
    series: [
      {
        name: '操作次数',
        data: chartsData.value,
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: '#165DFF' },
        areaStyle: { color: 'rgba(22, 93, 255, 0.12)' },
      },
    ],
  }));

  function formatDay(value: unknown) {
    const date = new Date(String(value));
    return Number.isNaN(date.getTime())
      ? String(value || '').slice(0, 10)
      : date.toISOString().slice(0, 10);
  }

  function renderSnapshot(snapshot: DashboardSnapshot) {
    const countMap = new Map<string, number>();
    snapshot.operationLogs.rows.forEach((record) => {
      const day = formatDay(record.operTime);
      if (day) countMap.set(day, (countMap.get(day) || 0) + 1);
    });
    const entries = [...countMap.entries()].sort(([left], [right]) => left.localeCompare(right));
    xAxis.value = entries.map(([day]) => day);
    chartsData.value = entries.map(([, count]) => count);
  }

  /** 查询并整理 Java 审计日志。 */
  async function loadData() {
    setLoading(true);
    try {
      const snapshot = await queryDashboardData();
      renderSnapshot(snapshot);
    } finally {
      setLoading(false);
    }
  }

  onMounted(loadData);
</script>
