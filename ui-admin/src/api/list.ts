import type { DescData } from '@arco-design/web-vue/es/descriptions/interface';
import type { HttpResponse } from './interceptor';
import { listConfigs, QueryParams } from './admin';
import { queryAdminSnapshot } from './analytics';

export interface PolicyRecord {
  id: string;
  number: number;
  name: string;
  contentType: 'img' | 'horizontalVideo' | 'verticalVideo';
  filterType: 'artificial' | 'rules';
  count: number;
  status: 'online' | 'offline';
  createdTime: string;
}

export interface PolicyParams extends Partial<PolicyRecord> {
  current: number;
  pageSize: number;
}

export interface PolicyListRes {
  list: PolicyRecord[];
  total: number;
}

export interface ServiceRecord {
  id: number;
  title: string;
  description: string;
  name?: string;
  actionType?: string;
  icon?: string;
  data?: DescData[];
  enable?: boolean;
  expires?: boolean;
}

function response<T>(data: T): HttpResponse<T> & { data: T } {
  return { code: 200, msg: '操作成功', data };
}

function valueOf(record: QueryParams, key: string) {
  const value = record[key];
  return value === undefined || value === null ? '' : String(value);
}

function configToPolicy(record: QueryParams, index: number): PolicyRecord {
  const configValue = valueOf(record, 'configValue');
  const configType = valueOf(record, 'configType');
  return {
    id: valueOf(record, 'configId') || String(index + 1),
    number: Number(record.configId) || index + 1,
    name: valueOf(record, 'configName') || '系统参数',
    contentType: configValue.startsWith('http') ? 'horizontalVideo' : 'img',
    filterType: configType === 'Y' ? 'rules' : 'artificial',
    count: configValue.length,
    status: configValue ? 'online' : 'offline',
    createdTime: valueOf(record, 'createTime') || '-',
  };
}

function menuCards(rows: QueryParams[]): ServiceRecord[] {
  return rows
    .filter((record) => valueOf(record, 'menuType') === 'C')
    .slice(0, 12)
    .map((record, index) => ({
      id: Number(record.menuId) || index + 1,
      title: valueOf(record, 'menuName') || '未命名菜单',
      description: valueOf(record, 'remark') || valueOf(record, 'path') || 'Java 菜单资源',
      actionType: 'button',
      icon: valueOf(record, 'icon'),
      enable: valueOf(record, 'status') !== '1',
      data: [
        { label: '权限', value: valueOf(record, 'perms') || '-' },
        { label: '路径', value: valueOf(record, 'path') || '-' },
      ],
    }));
}

function roleCards(rows: QueryParams[]): ServiceRecord[] {
  return rows.slice(0, 12).map((record, index) => ({
    id: Number(record.roleId) || index + 1,
    title: valueOf(record, 'roleName') || '未命名角色',
    description: valueOf(record, 'remark') || valueOf(record, 'roleKey') || 'Java 角色资源',
    actionType: 'switch',
    icon: 'peoples',
    enable: valueOf(record, 'status') !== '1',
    data: [{ label: '角色标识', value: valueOf(record, 'roleKey') || '-' }],
  }));
}

function departmentCards(rows: QueryParams[]): ServiceRecord[] {
  return rows.slice(0, 12).map((record, index) => ({
    id: Number(record.deptId) || index + 1,
    title: valueOf(record, 'deptName') || '未命名部门',
    description: valueOf(record, 'remark') || 'Java 部门资源',
    actionType: 'button',
    icon: 'tree',
    enable: valueOf(record, 'status') !== '1',
    data: [
      { label: '负责人', value: valueOf(record, 'leader') || '-' },
      { label: '电话', value: valueOf(record, 'phone') || '-' },
    ],
  }));
}

/** 查询 Java 参数配置并转换为 Arco 查询表格示例的数据结构。 */
export async function queryPolicyList(params: PolicyParams) {
  const query: QueryParams = {
    pageNum: params.current,
    pageSize: params.pageSize,
    configName: params.name || undefined,
    configKey: params.number ? String(params.number) : undefined,
  };
  const result = await listConfigs(query);
  const rows = (result.rows || []).map((record, index) => configToPolicy(record, index));
  const filtered = rows.filter((record) => {
    const contentTypeMatch = !params.contentType || record.contentType === params.contentType;
    const filterTypeMatch = !params.filterType || record.filterType === params.filterType;
    const statusMatch = !params.status || record.status === params.status;
    return contentTypeMatch && filterTypeMatch && statusMatch;
  });
  return response<PolicyListRes>({
    list: filtered,
    total: result.total || filtered.length,
  });
}

/** 查询 Java 菜单资源，用于卡片列表页。 */
export async function queryInspectionList() {
  const snapshot = await queryAdminSnapshot();
  return response(menuCards(snapshot.menus.rows));
}

/** 查询 Java 角色资源，用于卡片列表页。 */
export async function queryTheServiceList() {
  const snapshot = await queryAdminSnapshot();
  return response(roleCards(snapshot.roles.rows));
}

/** 查询 Java 部门资源，用于卡片列表页。 */
export async function queryRulesPresetList() {
  const snapshot = await queryAdminSnapshot();
  return response(departmentCards(snapshot.departments.rows));
}
