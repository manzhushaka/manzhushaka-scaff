<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :title="$t('multiDAnalysis.card.title.contentTypeDistribution')"
      :header-style="{ paddingBottom: 0 }"
    >
      <Chart style="height: 222px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { queryContentTypeDistribution, ContentTypeDistributionRes } from '@/api/visualization';
  import useLoading from '@/hooks/loading';
  import useChartOption from '@/hooks/chart-option';

  const { loading, setLoading } = useLoading(true);
  const renderData = ref<ContentTypeDistributionRes>({ labels: [], series: [] });
  const { chartOption } = useChartOption((isDark) => {
    const values = renderData.value.series[0]?.value || [];
    const max = Math.max(...values, 1);
    return {
      legend: {
        show: true,
        top: 'center',
        right: '0',
        orient: 'vertical',
        icon: 'circle',
        itemWidth: 10,
        itemHeight: 10,
        itemGap: 20,
        textStyle: { color: isDark ? '#ffffff' : '#4E5969' },
      },
      radar: {
        center: ['40%', '50%'],
        radius: 80,
        indicator: renderData.value.labels.map((name) => ({ name, max })),
        axisName: { color: isDark ? '#ffffff' : '#1D2129' },
        axisLine: { lineStyle: { color: isDark ? '#484849' : '#E5E6EB' } },
        splitLine: { lineStyle: { color: isDark ? '#484849' : '#E5E6EB' } },
        splitArea: { areaStyle: { color: [] } },
      },
      series: [{
        type: 'radar',
        areaStyle: { opacity: 0.2 },
        data: renderData.value.series.map((series, index) => ({
          ...series,
          symbol: 'none',
          itemStyle: { color: index ? '#33D1C9' : '#249EFF' },
        })),
      }],
    };
  });

  /** 加载 Java 菜单资源类型统计。 */
  async function loadData() {
    try {
      const response = await queryContentTypeDistribution();
      renderData.value = response.data;
    } finally {
      setLoading(false);
    }
  }

  onMounted(loadData);
</script>
