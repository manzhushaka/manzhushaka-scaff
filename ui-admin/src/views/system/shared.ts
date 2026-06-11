import type {
  ConfigRow,
  DeptRow,
  DeptTreeVO,
  DictItemRow,
  DictItemVO,
  DictTypeRow,
  DictTypeVO,
  PlatformJobLogRow,
  PlatformJobLogVO,
  PlatformJobRow,
  PlatformJobVO,
  LoginLogRow,
  LoginLogVO,
  MenuRow,
  MenuVO,
  OpLogRow,
  OpLogVO,
  RoleRow,
  RoleVO,
  UserRow,
  UserVO,
} from '@/types/system';
import { formatStandardDateTime } from '@/utils/date-time';

export const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

export const yesNoOptions = [
  { label: '显示', value: 1 },
  { label: '隐藏', value: 0 },
];

export const keepAliveOptions = [
  { label: '缓存', value: 1 },
  { label: '不缓存', value: 0 },
];

export const menuTypeOptions = [
  { label: '目录', value: 'DIR' },
  { label: '菜单', value: 'MENU' },
  { label: '按钮', value: 'BUTTON' },
];

export const dataScopeOptions = [
  { label: '仅本人', value: 'SELF' },
  { label: '本部门', value: 'DEPT' },
  { label: '本部门及子部门', value: 'DEPT_AND_CHILD' },
  { label: '全部数据', value: 'ALL' },
];

export const loginStatusOptions = [
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAIL' },
];

export const opSuccessOptions = [
  { label: '成功', value: 'true' },
  { label: '失败', value: 'false' },
];

export const platformJobRunStatusOptions = [
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAIL' },
  { label: '执行中', value: 'RUNNING' },
  { label: '已跳过', value: 'SKIPPED' },
];

export const platformJobTriggerTypeOptions = [
  { label: '定时触发', value: 'SCHEDULE' },
  { label: '手动触发', value: 'MANUAL' },
];

export function toStatusText(status: number | null | undefined) {
  return status === 1 ? '启用' : '停用';
}

export function toVisibleText(visible: number | null | undefined) {
  return visible === 1 ? '显示' : '隐藏';
}

export function toLoginStatusText(status: string | null | undefined) {
  return status === 'SUCCESS' ? '成功' : status === 'FAIL' ? '失败' : '--';
}

export function toPlatformJobRunStatusText(status: string | null | undefined) {
  return platformJobRunStatusOptions.find((item) => item.value === status)?.label ?? '--';
}

export function toPlatformJobTriggerTypeText(triggerType: string | null | undefined) {
  return platformJobTriggerTypeOptions.find((item) => item.value === triggerType)?.label ?? '--';
}

export function toDataScopeText(value: RoleVO['dataScope']) {
  return dataScopeOptions.find((item) => item.value === value)?.label ?? '--';
}

export function formatDateTime(value: string | number[] | null | undefined) {
  return formatStandardDateTime(value);
}

export function mapUserRow(user: UserVO): UserRow {
  return {
    id: user.id,
    username: user.username,
    nickname: user.nickname,
    deptName: user.deptName ?? '--',
    statusText: toStatusText(user.status),
    statusValue: user.status ?? 0,
    roleCodesText: user.roleCodes.length ? user.roleCodes.join(', ') : '--',
    createTimeText: formatDateTime(user.createTime),
  };
}

export function mapRoleRow(role: RoleVO): RoleRow {
  return {
    id: role.id,
    roleCode: role.roleCode,
    roleName: role.roleName,
    dataScopeText: toDataScopeText(role.dataScope),
    statusText: toStatusText(role.status),
    statusValue: role.status ?? 0,
    createTimeText: formatDateTime(role.createTime),
  };
}

export function mapDeptRow(dept: DeptTreeVO): DeptRow {
  return {
    id: dept.id,
    parentId: dept.parentId,
    deptName: dept.deptName,
    sort: dept.sort ?? 0,
    statusText: toStatusText(dept.status),
    statusValue: dept.status ?? 0,
    children: dept.children.map(mapDeptRow),
  };
}

export function mapMenuRow(menu: MenuVO): MenuRow {
  return {
    id: menu.id,
    parentId: menu.parentId,
    menuType: menu.menuType,
    menuName: menu.menuName,
    routePath: menu.routePath ?? '--',
    component: menu.component ?? '--',
    perms: menu.perms ?? '--',
    visibleText: toVisibleText(menu.visible),
    visibleValue: menu.visible ?? 0,
    statusText: toStatusText(menu.status),
    statusValue: menu.status ?? 0,
    sort: menu.sort ?? 0,
    createTimeText: formatDateTime(menu.createTime),
  };
}

export function mapDictTypeRow(dictType: DictTypeVO): DictTypeRow {
  return {
    id: dictType.id,
    dictName: dictType.dictName,
    dictCode: dictType.dictCode,
    statusText: toStatusText(dictType.status),
    statusValue: dictType.status ?? 0,
    createTimeText: formatDateTime(dictType.createTime),
  };
}

export function mapDictItemRow(item: DictItemVO): DictItemRow {
  return {
    id: item.id,
    itemLabel: item.itemLabel,
    itemValue: item.itemValue,
    sort: item.sort ?? 0,
    statusText: toStatusText(item.status),
    statusValue: item.status ?? 0,
  };
}

export function mapConfigRow(config: import('@/types/system').ConfigVO): ConfigRow {
  return {
    id: config.id,
    configName: config.configName,
    configKey: config.configKey,
    configValue: config.configValue ?? '',
    statusText: toStatusText(config.status),
    statusValue: config.status ?? 0,
    createTimeText: formatDateTime(config.createTime),
  };
}

export function mapLoginLogRow(log: LoginLogVO): LoginLogRow {
  return {
    id: log.id,
    username: log.username,
    loginStatus: toLoginStatusText(log.loginStatus),
    loginStatusValue: log.loginStatus ?? '',
    ip: log.ip ?? '--',
    userAgent: log.userAgent ?? '--',
    message: log.message ?? '--',
    createTimeText: formatDateTime(log.createTime),
  };
}

export function mapOpLogRow(log: OpLogVO): OpLogRow {
  return {
    id: log.id,
    module: log.module ?? '--',
    action: log.action ?? '--',
    operatorName: log.operatorName ?? '--',
    successText: log.success === true ? '成功' : log.success === false ? '失败' : '--',
    successValue: log.success,
    costMsText: log.costMs == null ? '--' : `${log.costMs} ms`,
    requestMethod: log.requestMethod ?? '--',
    requestUri: log.requestUri ?? '--',
    errorMsg: log.errorMsg ?? '--',
    createTimeText: formatDateTime(log.createTime),
  };
}

export function mapPlatformJobRow(job: PlatformJobVO): PlatformJobRow {
  return {
    id: job.id,
    jobName: job.jobName,
    handlerName: job.handlerName,
    handlerLabel: job.handlerLabel || job.handlerName,
    cronExpression: job.cronExpression,
    statusText: toStatusText(job.status),
    statusValue: job.status ?? 0,
    lastRunStatusText: toPlatformJobRunStatusText(job.lastRunStatus),
    lastRunStatusValue: job.lastRunStatus ?? '',
    lastTriggerTimeText: formatDateTime(job.lastTriggerTime),
    nextTriggerTimeText: formatDateTime(job.nextTriggerTime),
    createTimeText: formatDateTime(job.createTime),
    remark: job.remark ?? '--',
  };
}

export function mapPlatformJobLogRow(log: PlatformJobLogVO): PlatformJobLogRow {
  return {
    id: log.id,
    jobId: log.jobId,
    jobNameSnapshot: log.jobNameSnapshot,
    handlerNameSnapshot: log.handlerNameSnapshot,
    triggerTypeText: toPlatformJobTriggerTypeText(log.triggerType),
    triggerTypeValue: log.triggerType ?? '',
    runStatusText: toPlatformJobRunStatusText(log.runStatus),
    runStatusValue: log.runStatus ?? '',
    executorHost: log.executorHost ?? '--',
    errorMsg: log.errorMsg ?? '--',
    costMsText: log.costMs == null ? '--' : `${log.costMs} ms`,
    startTimeText: formatDateTime(log.startTime),
    endTimeText: formatDateTime(log.endTime),
    createTimeText: formatDateTime(log.createTime),
  };
}
