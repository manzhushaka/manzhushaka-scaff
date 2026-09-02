<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :title="$t('multiDAnalysis.card.title.userActions')"
    >
      <Chart height="122px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { queryUserActions } from '@/api/visualization';
  import useLoading from '@/hooks/loading';
  import useChartOption from '@/hooks/chart-option';

  const labels = ref<string[]>([]);
  const values = ref<number[]>([]);
  const { loading, setLoading } = useLoading(true);
  const { chartOption } = useChartOption((isDark) => ({
    grid: { left: 44, right: 20, top: 0, bottom: 20 },
    xAxis: {
      type: 'value',
      axisLabel: {
        show: true,
        formatter(value: number, idx: number) {
          if (idx === 0) return String(value);
          return `${Number(value) / 1000}k`;
        },
      },
      splitLine: { lineStyle: { color: isDark ? '#484849' : '#E5E8EF' } },
    },
    yAxis: {
      type: 'category',
      data: labels.value,
      axisLabel: { show: true, color: '#4E5969' },
      axisTick: { show: true, length: 2, alignWithLabel: true },
      axisLine: { lineStyle: { color: isDark ? '#484849' : '#A9AEB8' } },
    },
    tooltip: { show: true, trigger: 'axis' },
    series: [{
      data: values.value,
      type: 'bar',
      barWidth: 7,
      itemStyle: { color: '#4086FF', borderRadius: 4 },
    }],
  }));

  /** 加载 Java 操作类型统计。 */
  async function loadData() {
    try {
      const response = await queryUserActions();
      labels.value = response.data.labels;
      values.value = response.data.values;
    } finally {
      setLoading(false);
    }
  }

  onMounted(loadData);
</script>
