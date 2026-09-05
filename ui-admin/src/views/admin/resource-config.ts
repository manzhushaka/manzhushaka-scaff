export type Resource =
  | 'users'
  | 'roles'
  | 'menus'
  | 'departments'
  | 'dictTypes'
  | 'configs'
  | 'online'
  | 'operationLogs'
  | 'loginLogs'
  | 'slowSql'
  | 'mqLogs'
  | 'jobs'
  | 'jobLogs';

export type QueryFieldType = 'input' | 'status' | 'menuType';

export interface QueryField {
  key: string;
  label: string;
  type: QueryFieldType;
}

export const resourceTitles: Record<Resource, string> = {
  users: '用户管理',
  roles: '角色管理',
  menus: '菜单管理',
  departments: '部门管理',
  dictTypes: '字典管理',
  configs: '参数设置',
  online: '在线用户',
  operationLogs: '操作日志',
  loginLogs: '登录日志',
  slowSql: '慢 SQL 日志',
  mqLogs: '消息队列台账',
  jobs: '定时任务',
  jobLogs: '调度日志',
};

export const resourceRowKeys: Record<Resource, string> = {
  users: 'userId',
  roles: 'roleId',
  menus: 'menuId',
  departments: 'deptId',
  dictTypes: 'dictId',
  configs: 'configId',
  online: 'tokenId',
  operationLogs: 'operId',
  loginLogs: 'infoId',
  slowSql: 'slowSqlId',
  mqLogs: 'messageLogId',
  jobs: 'jobId',
  jobLogs: 'jobLogId',
};

export const resourceQueryFields: Record<Resource, QueryField[]> = {
  users: [
    { key: 'userName', label: '用户名称', type: 'input' },
    { key: 'phonenumber', label: '手机号码', type: 'input' },
    { key: 'status', label: '状态', type: 'status' },
  ],
  roles: [
    { key: 'roleName', label: '角色名称', type: 'input' },
    { key: 'roleKey', label: '角色权限', type: 'input' },
    { key: 'status', label: '状态', type: 'status' },
  ],
  menus: [
    { key: 'menuName', label: '菜单名称', type: 'input' },
    { key: 'menuType', label: '菜单类型', type: 'menuType' },
  ],
  departments: [
    { key: 'deptName', label: '部门名称', type: 'input' },
    { key: 'status', label: '状态', type: 'status' },
  ],
  dictTypes: [
    { key: 'dictName', label: '字典名称', type: 'input' },
    { key: 'dictType', label: '字典类型', type: 'input' },
    { key: 'status', label: '状态', type: 'status' },
  ],
  configs: [
    { key: 'configName', label: '参数名称', type: 'input' },
    { key: 'configKey', label: '参数键名', type: 'input' },
    { key: 'configType', label: '系统内置', type: 'input' },
  ],
  online: [
    { key: 'ipaddr', label: '登录地址', type: 'input' },
    { key: 'userName', label: '用户名称', type: 'input' },
  ],
  operationLogs: [
    { key: 'title', label: '系统模块', type: 'input' },
    { key: 'operName', label: '操作人员', type: 'input' },
  ],
  loginLogs: [
    { key: 'userName', label: '登录名称', type: 'input' },
    { key: 'ipaddr', label: '登录地址', type: 'input' },
  ],
  slowSql: [{ key: 'requestUrl', label: '请求地址', type: 'input' }],
  mqLogs: [{ key: 'messageTopic', label: '消息主题', type: 'input' }],
  jobs: [
    { key: 'jobName', label: '任务名称', type: 'input' },
    { key: 'jobGroup', label: '任务组名', type: 'input' },
  ],
  jobLogs: [
    { key: 'jobName', label: '任务名称', type: 'input' },
    { key: 'jobGroup', label: '任务组名', type: 'input' },
  ],
};

export const resourcePermissions: Record<Resource, string> = {
  users: 'system:user',
  roles: 'system:role',
  menus: 'system:menu',
  departments: 'system:dept',
  dictTypes: 'system:dict',
  configs: 'system:config',
  online: 'monitor:online',
  operationLogs: 'monitor:operlog',
  loginLogs: 'monitor:logininfor',
  slowSql: 'monitor:slowsql',
  mqLogs: 'monitor:mqlog',
  jobs: 'monitor:job',
  jobLogs: 'monitor:job',
};

export const creatableResources: Resource[] = [
  'users',
  'roles',
  'menus',
  'departments',
  'dictTypes',
  'configs',
  'jobs',
];

export const deletableResources: Resource[] = [
  'users',
  'roles',
  'menus',
  'departments',
  'dictTypes',
  'configs',
  'slowSql',
  'operationLogs',
  'loginLogs',
  'jobs',
  'jobLogs',
];
