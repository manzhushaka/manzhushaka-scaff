import type { HttpResponse } from './interceptor';
import { queryAdminSnapshot, AdminRecord, AdminSnapshot } from './analytics';

export interface ChartDataRecord {
  x: string;
  y: number;
  name: string;
}

export interface DataChainGrowth {
  quota: string;
}

export interface DataChainGrowthRes {
  count: number;
  growth: number;
  chartData: {
    xAxis: string[];
    data: { name: string; value: number[] };
  };
}

export interface PublicOpinionAnalysis {
  quota: string;
}

export interface PublicOpinionAnalysisRes {
  count: number;
  growth: number;
  chartData: ChartDataRecord[];
}

export interface PopularAuthorRes {
  list: {
    ranking: number;
    author: string;
    contentCount: number;
    clickCount: number;
  }[];
}

export interface ContentPublishRecord {
  x: string[];
  y: number[];
  name: string;
}

export interface GeneralChart {
  xAxis: string[];
  data: Array<{ name: string; value: number[] }>;
}

export interface DataOverviewRes {
  xAxis: string[];
  data: Array<{ name: string; value: number[]; count: number }>;
}

export interface UserActionsRes {
  labels: string[];
  values: number[];
}

export interface ResourceDistributionRes {
  labels: string[];
  values: number[];
}

export interface ContentTypeDistributionRes {
  labels: string[];
  series: Array<{ name: string; value: number[] }>;
}

function response<T>(data: T): HttpResponse<T> & { data: T } {
  return { code: 200, msg: '操作成功', data };
}

function dateKeys(count = 7) {
  const result: string[] = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  for (let index = count - 1; index >= 0; index -= 1) {
    const date = new Date(today);
    date.setDate(today.getDate() - index);
    result.push(date.toISOString().slice(0, 10));
  }
  return result;
}

function dayOf(value: unknown) {
  if (!value) return '';
  const date = new Date(String(value));
  return Number.isNaN(date.getTime()) ? '' : date.toISOString().slice(0, 10);
}

function countsByDay(rows: AdminRecord[], field: string, keys: string[]) {
  const counts = new Map(keys.map((key) => [key, 0]));
  rows.forEach((row) => {
    const key = dayOf(row[field]);
    if (counts.has(key)) counts.set(key, (counts.get(key) || 0) + 1);
  });
  return keys.map((key) => counts.get(key) || 0);
}

function metricRows(snapshot: AdminSnapshot, quota: string) {
  if (quota === 'visitors' || quota === 'allVisitors' || quota === 'userRetention') {
    return snapshot.loginLogs.rows.filter((row) => String(row.status) === '0');
  }
  if (quota === 'published' || quota === 'contentConsumption' || quota === 'contentConsumptionTrends') {
    return snapshot.operationLogs.rows.filter((row) => Number(row.businessType) === 1);
  }
  if (quota === 'comment') {
    return snapshot.operationLogs.rows.filter((row) => Number(row.businessType) === 2);
  }
  return snapshot.operationLogs.rows;
}

function growthOf(values: number[]) {
  const current = values[values.length - 1] || 0;
  const previous = values[values.length - 2] || 0;
  if (!previous) return current ? 100 : 0;
  return Number((((current - previous) / previous) * 100).toFixed(2));
}

function operationTypeRows(snapshot: AdminSnapshot, businessType: number) {
  return snapshot.operationLogs.rows.filter((row) => Number(row.businessType) === businessType);
}

function resourceCounts(rows: AdminRecord[], field: string, labels: string[]) {
  const map = new Map(labels.map((label) => [label, 0]));
  rows.forEach((row) => {
    const key = String(row[field] || '其他');
    map.set(key, (map.get(key) || 0) + 1);
  });
  return labels.map((label) => map.get(label) || 0);
}

/** 查询 Java 数据并生成趋势卡片数据。 */
export async function queryDataChainGrowth(data: DataChainGrowth) {
  const snapshot = await queryAdminSnapshot();
  const keys = dateKeys();
  const rows = metricRows(snapshot, data.quota);
  const values = countsByDay(rows, 'operTime', keys);
  return response<DataChainGrowthRes>({
    count: rows.length,
    growth: growthOf(values),
    chartData: { xAxis: keys, data: { name: data.quota, value: values } },
  });
}

/** 查询 Java 操作记录并生成热门操作人员排行。 */
export async function queryPopularAuthor() {
  const snapshot = await queryAdminSnapshot();
  const counts = new Map<string, number>();
  snapshot.operationLogs.rows.forEach((row) => {
    const name = String(row.operName || '系统用户');
    counts.set(name, (counts.get(name) || 0) + 1);
  });
  const list = Array.from(counts.entries())
    .sort((left, right) => right[1] - left[1])
    .slice(0, 10)
    .map(([author, count], index) => ({
      ranking: index + 1,
      author,
      contentCount: count,
      clickCount: snapshot.operationLogs.rows.filter((row) => String(row.operName || '系统用户') === author).length,
    }));
  return response<PopularAuthorRes>({ list });
}

/** 查询 Java 操作日志的按日分布。 */
export async function queryContentPublish() {
  const snapshot = await queryAdminSnapshot();
  const keys = dateKeys();
  return response<ContentPublishRecord[]>([
    {
      name: '新增操作',
      x: keys,
      y: countsByDay(operationTypeRows(snapshot, 1), 'operTime', keys),
    },
    {
      name: '修改操作',
      x: keys,
      y: countsByDay(operationTypeRows(snapshot, 2), 'operTime', keys),
    },
    {
      name: '其他操作',
      x: keys,
      y: countsByDay(snapshot.operationLogs.rows.filter((row) => ![1, 2].includes(Number(row.businessType))), 'operTime', keys),
    },
  ]);
}

/** 查询 Java 操作日志在一天内的时段分布。 */
export async function queryContentPeriodAnalysis() {
  const snapshot = await queryAdminSnapshot();
  const xAxis = Array.from({ length: 24 }, (_, index) => `${String(index).padStart(2, '0')}:00`);
  const { rows } = snapshot.operationLogs;
  const series = [
    { name: '新增操作', rows: operationTypeRows(snapshot, 1) },
    { name: '修改操作', rows: operationTypeRows(snapshot, 2) },
    { name: '其他操作', rows: rows.filter((row) => ![1, 2].includes(Number(row.businessType))) },
  ].map(({ name, rows: selectedRows }) => {
    const values = xAxis.map(() => 0);
    selectedRows.forEach((row) => {
      const date = new Date(String(row.operTime));
      if (!Number.isNaN(date.getTime())) values[date.getHours()] += 1;
    });
    const total = values.reduce((sum, value) => sum + value, 0) || 1;
    return { name, value: values.map((value) => Number(((value / total) * 100).toFixed(2))) };
  });
  return response<GeneralChart>({ xAxis, data: series });
}

/** 查询 Java 登录和操作记录的汇总趋势。 */
export async function queryPublicOpinionAnalysis(data: PublicOpinionAnalysis) {
  const snapshot = await queryAdminSnapshot();
  const keys = dateKeys();
  if (data.quota === 'share') {
    const labels = ['用户操作', '新增操作', '修改操作', '其他操作'];
    const values = [
      snapshot.operationLogs.rows.length,
      operationTypeRows(snapshot, 1).length,
      operationTypeRows(snapshot, 2).length,
      snapshot.operationLogs.rows.filter((row) => ![1, 2].includes(Number(row.businessType))).length,
    ];
    return response<PublicOpinionAnalysisRes>({
      count: values.reduce((sum, value) => sum + value, 0),
      growth: 0,
      chartData: labels.map((name, index) => ({ x: name, y: values[index], name })),
    });
  }
  const rows = metricRows(snapshot, data.quota);
  const values = countsByDay(rows, 'operTime', keys);
  return response<PublicOpinionAnalysisRes>({
    count: rows.length,
    growth: growthOf(values),
    chartData: keys.map((x, index) => ({ x, y: values[index], name: data.quota })),
  });
}

/** 查询 Java 用户、菜单和日志的多维趋势。 */
export async function queryDataOverview() {
  const snapshot = await queryAdminSnapshot();
  const xAxis = dateKeys();
  const activeUsers = xAxis.map((key) => {
    const names = new Set<string>();
    snapshot.operationLogs.rows.forEach((row) => {
      if (dayOf(row.operTime) === key) names.add(String(row.operName || '系统用户'));
    });
    return names.size;
  });
  const data = [
    {
      name: '内容生产量',
      value: countsByDay(operationTypeRows(snapshot, 1), 'operTime', xAxis),
      count: operationTypeRows(snapshot, 1).length,
    },
    {
      name: '内容点击量',
      value: countsByDay(operationTypeRows(snapshot, 2), 'operTime', xAxis),
      count: operationTypeRows(snapshot, 2).length,
    },
    {
      name: '内容曝光量',
      value: countsByDay(snapshot.loginLogs.rows.filter((row) => String(row.status) === '0'), 'loginTime', xAxis),
      count: snapshot.loginLogs.rows.filter((row) => String(row.status) === '0').length,
    },
    { name: '活跃用户数', value: activeUsers, count: new Set(snapshot.operationLogs.rows.map((row) => row.operName)).size },
  ];
  return response<DataOverviewRes>({ xAxis, data });
}

/** 查询 Java 操作类型统计。 */
export async function queryUserActions() {
  const snapshot = await queryAdminSnapshot();
  const labels = ['新增', '修改', '删除'];
  const values = [1, 2, 3].map((type) => operationTypeRows(snapshot, type).length);
  return response<UserActionsRes>({ labels, values });
}

/** 查询 Java 菜单类型分布。 */
export async function queryContentTypeDistribution() {
  const snapshot = await queryAdminSnapshot();
  const labels = ['目录', '页面', '按钮'];
  const values = ['M', 'C', 'F'].map((type) => snapshot.menus.rows.filter((row) => String(row.menuType) === type).length);
  const series = [
    { name: '可见资源', value: values },
    { name: '启用资源', value: labels.map((_, index) => values[index]) },
  ];
  return response<ContentTypeDistributionRes>({ labels, series });
}

/** 查询 Java 菜单创建者分布。 */
export async function queryResourceDistribution() {
  const snapshot = await queryAdminSnapshot();
  const labels = Array.from(new Set(snapshot.menus.rows.map((row) => String(row.createBy || '系统')))).slice(0, 6);
  const normalizedLabels = labels.length ? labels : ['暂无数据'];
  const values = labels.length ? resourceCounts(snapshot.menus.rows, 'createBy', normalizedLabels) : [0];
  return response<ResourceDistributionRes>({ labels: normalizedLabels, values });
}
