export interface ChartTheme {
  text: string;
  grid: string;
  axis: string;
  panel: string;
}

export function getChartTheme(isDark: boolean): ChartTheme {
  return {
    text: isDark ? '#C9CDD4' : '#4E5969',
    grid: isDark ? '#3F3F3F' : '#E5E6EB',
    axis: isDark ? '#A9AEB8' : '#A9AEB8',
    panel: isDark ? '#313132' : '#F2F3F5',
  };
}
