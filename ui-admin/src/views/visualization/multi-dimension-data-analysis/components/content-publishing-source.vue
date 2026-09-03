<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :title="$t('multiDAnalysis.card.title.contentPublishingSource')"
    >
      <Chart style="width: 100%; height: 300px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { queryResourceDistribution, ResourceDistributionRes } from '@/api/visualization';
  import useLoading from '@/hooks/loading';
  import useChartOption from '@/hooks/chart-option';

  const { loading, setLoading } = useLoading(true);
  const renderData = ref<ResourceDistributionRes>({ labels: [], values: [] });
  const { chartOption } = useChartOption((isDark) => ({
    legend: {
      left: 'center',
      data: renderData.value.labels,
      bottom: 0,
      icon: 'circle',
      itemWidth: 8,
      textStyle: { color: isDark ? 'rgba(255,255,255,0.7)' : '#4E5969' },
    },
    tooltip: { show: true, trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['45%', '68%'],
      center: ['50%', '45%'],
      label: { formatter: '{b}: {d}%', color: isDark ? 'rgba(255,255,255,0.7)' : '#4E5969' },
      itemStyle: {
        borderColor: isDark ? '#17171A' : '#fff',
        borderWidth: 1,
      },
      data: renderData.value.labels.map((name, index) => ({
        name,
        value: renderData.value.values[index] || 0,
      })),
    }],
  }));

  /** 加载 Java 菜单创建者分布。 */
  async function loadData() {
    try {
      const response = await queryResourceDistribution();
      renderData.value = response.data;
    } finally {
      setLoading(false);
    }
  }

  onMounted(loadData);
</script>
