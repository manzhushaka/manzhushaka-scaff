import { queryAdminSnapshot } from './analytics';

export type DashboardSnapshot = Awaited<ReturnType<typeof queryAdminSnapshot>>;

/**
 * 查询工作台所需的 Java 后台数据。
 *
 * @return 管理数据快照
 */
export function queryDashboardData() {
  return queryAdminSnapshot();
}
