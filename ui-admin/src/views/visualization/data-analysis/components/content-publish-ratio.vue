<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card class="general-card" :header-style="{ paddingBottom: '14px' }">
      <template #title>
        {{ $t('dataAnalysis.contentPublishRatio') }}
      </template>
      <template #extra>
        <a-link>{{ $t('workplace.viewMore') }}</a-link>
      </template>
      <Chart style="width: 100%; height: 347px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { ToolTipFormatterParams } from '@/types/echarts';
  import useLoading from '@/hooks/loading';
  import {
    queryContentPublish,
    ContentPublishRecord,
  } from '@/api/visualization';
  import useChartOption from '@/hooks/chart-option';
  import { getChartTheme } from '@/utils/chart-theme';

  const tooltipItemsHtmlString = (items: ToolTipFormatterParams[]) => {
    return items
      .map(
        (el) => `<div class="content-panel">
    <p>
      <span style="background-color: ${
        el.color
      }" class="tooltip-item-icon"></span>
      <span>
      ${el.seriesName}
      </span>
    </p>
    <span class="tooltip-value">
      ${Number(el.value).toLocaleString()}
    </span>
  </div>`
      )
      .join('');
  };

  const { loading, setLoading } = useLoading(true);
  const xAxis = ref<string[]>([]);
  const textChartsData = ref<number[]>([]);
  const imgChartsData = ref<number[]>([]);
  const videoChartsData = ref<number[]>([]);
  const { chartOption } = useChartOption((isDark) => {
    const chartTheme = getChartTheme(isDark);
    return {
      grid: {
        left: '4%',
        right: 0,
        top: '20',
        bottom: '60',
      },
      legend: {
        bottom: 0,
        icon: 'circle',
        textStyle: {
          color: chartTheme.text,
        },
      },
      xAxis: {
        type: 'category',
        data: xAxis.value,
        axisLine: {
          lineStyle: {
            color: chartTheme.axis,
          },
        },
        axisTick: {
          show: true,
          alignWithLabel: true,
          lineStyle: {
            color: chartTheme.text,
          },
        },
        axisLabel: {
          color: chartTheme.text,
        },
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: chartTheme.text,
          formatter(value: number, idx: number) {
            if (idx === 0) return `${value}`;
            return `${value / 1000}k`;
          },
        },
        splitLine: {
          lineStyle: {
            color: chartTheme.grid,
          },
        },
      },
      tooltip: {
        show: true,
        trigger: 'axis',
        formatter(params) {
          const [firstElement] = params as ToolTipFormatterParams[];
          return `<div>
            <p class="tooltip-title">${firstElement.axisValueLabel}</p>
            ${tooltipItemsHtmlString(params as ToolTipFormatterParams[])}
          </div>`;
        },
        className: 'echarts-tooltip-diy',
      },
      series: [
        {
          name: '纯文本',
          data: textChartsData.value,
          stack: 'one',
          type: 'bar',
          barWidth: 16,
          color: isDark ? '#4A7FF7' : '#246EFF',
        },
        {
          name: '图文类',
          data: imgChartsData.value,
          stack: 'one',
          type: 'bar',
          color: isDark ? '#085FEF' : '#00B2FF',
        },
        {
          name: '视频类',
          data: videoChartsData.value,
          stack: 'one',
          type: 'bar',
          color: isDark ? '#01349F' : '#81E2FF',
          itemStyle: {
            borderRadius: 2,
          },
        },
      ],
    };
  });
  const fetchData = async () => {
    setLoading(true);
    try {
      const { data: chartData } = await queryContentPublish();
      xAxis.value = chartData[0]?.x || [];
      chartData.forEach((el: ContentPublishRecord) => {
        if (el.name === '新增操作') {
          textChartsData.value = el.y;
        } else if (el.name === '修改操作') {
          imgChartsData.value = el.y;
        } else if (el.name === '其他操作') {
          videoChartsData.value = el.y;
        }
      });
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  };
  fetchData();
</script>

<style scoped lang="less"></style>
