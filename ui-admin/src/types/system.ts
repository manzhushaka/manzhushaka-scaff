export interface PageResult<T> {
  total: number;
  records: T[];
}

export interface SelectOption {
  label: string;
  value: string | number;
}

export interface UserVO {
  id: number;
  username: string;
  nickname: string;
  deptId: number | null;
  deptName: string | null;
  status: number;
  roleCodes: string[];
  createTime: string | null;
}

export interface UserForm {
  username: string;
  password?: string;
  nickname: string;
  deptId: number | null;
  status: number;
}

export interface UserQuery {
  pageNum: number;
  pageSize: number;
  username?: string;
  nickname?: string;
  status?: number;
  deptId?: number;
}

export interface RoleVO {
  id: number;
  roleCode: string;
  roleName: string;
  dataScope: 'SELF' | 'DEPT' | 'DEPT_AND_CHILD' | 'ALL' | null;
  status: number;
  createTime: string | null;
}

export interface RoleForm {
  roleCode: string;
  roleName: string;
  dataScope: 'SELF' | 'DEPT' | 'DEPT_AND_CHILD' | 'ALL';
  status: number;
}

export interface RoleQuery {
  pageNum: number;
  pageSize: number;
  roleCode?: string;
  roleName?: string;
  status?: number;
}

export interface DeptTreeVO {
  id: number;
  parentId: number | null;
  deptName: string;
  ancestorPath: string | null;
  sort: number;
  status: number;
  children: DeptTreeVO[];
}

export interface DeptForm {
  parentId: number | null;
  deptName: string;
  sort: number;
  status: number;
}

export interface DeptQuery {
  deptName?: string;
  status?: number;
}

export interface MenuVO {
  id: number;
  parentId: number | null;
  menuType: 'DIR' | 'MENU' | 'BUTTON' | string;
  menuName: string;
  routePath: string | null;
  routeName: string | null;
  component: string | null;
  icon: string | null;
  sort: number;
  visible: number;
  keepAlive: number;
  perms: string | null;
  status: number;
  createTime: string | null;
}

export interface MenuForm {
  parentId: number | null;
  menuType: 'DIR' | 'MENU' | 'BUTTON';
  menuName: string;
  routePath?: string;
  routeName?: string;
  component?: string;
  icon?: string;
  sort: number;
  visible: number;
  keepAlive: number;
  perms?: string;
  status: number;
}

export interface MenuQuery {
  menuName?: string;
  menuType?: string;
  status?: number;
}

export interface DictTypeVO {
  id: number;
  dictName: string;
  dictCode: string;
  status: number;
  createTime: string | null;
  items?: DictItemVO[];
}

export interface DictTypeForm {
  dictName: string;
  dictCode: string;
  status: number;
}

export interface DictTypeQuery {
  pageNum: number;
  pageSize: number;
  dictName?: string;
  dictCode?: string;
  status?: number;
}

export interface DictItemVO {
  id: number;
  dictTypeId: number;
  itemLabel: string;
  itemValue: string;
  sort: number;
  status: number;
}

export interface DictItemForm {
  dictTypeId: number;
  itemLabel: string;
  itemValue: string;
  sort: number;
  status: number;
}

export interface ConfigVO {
  id: number;
  configName: string;
  configKey: string;
  configValue: string | null;
  status: number;
  createTime: string | null;
}

export interface ConfigForm {
  configName: string;
  configKey: string;
  configValue?: string;
  status: number;
}

export interface ConfigQuery {
  pageNum: number;
  pageSize: number;
  configName?: string;
  configKey?: string;
  status?: number;
}

export interface LoginLogVO {
  id: number;
  username: string;
  loginStatus: string;
  ip: string | null;
  userAgent: string | null;
  message: string | null;
  createTime: string | null;
}

export interface LoginLogQuery {
  pageNum: number;
  pageSize: number;
  username?: string;
  loginStatus?: string;
}

export interface OpLogVO {
  id: number;
  traceId: string | null;
  module: string | null;
  action: string | null;
  businessType: string | null;
  requestUri: string | null;
  requestMethod: string | null;
  operatorId: number | null;
  operatorName: string | null;
  costMs: number | null;
  success: boolean | null;
  errorMsg: string | null;
  requestSnapshot: string | null;
  responseSnapshot: string | null;
  createTime: string | null;
}

export interface OpLogQuery {
  pageNum: number;
  pageSize: number;
  module?: string;
  action?: string;
  operatorName?: string;
  success?: boolean;
}

export interface UserRow {
  id: number;
  username: string;
  nickname: string;
  deptName: string;
  statusText: string;
  roleCodesText: string;
  createTimeText: string;
}

export interface RoleRow {
  id: number;
  roleCode: string;
  roleName: string;
  dataScopeText: string;
  statusText: string;
  createTimeText: string;
}

export interface DeptRow {
  id: number;
  parentId: number | null;
  deptName: string;
  sort: number;
  statusText: string;
  children: DeptRow[];
}

export interface MenuRow {
  id: number;
  parentId: number | null;
  menuType: string;
  menuName: string;
  routePath: string;
  component: string;
  perms: string;
  visibleText: string;
  statusText: string;
  sort: number;
  createTimeText: string;
}

export interface DictTypeRow {
  id: number;
  dictName: string;
  dictCode: string;
  statusText: string;
  createTimeText: string;
}

export interface DictItemRow {
  id: number;
  itemLabel: string;
  itemValue: string;
  sort: number;
  statusText: string;
}

export interface ConfigRow {
  id: number;
  configName: string;
  configKey: string;
  configValue: string;
  statusText: string;
  createTimeText: string;
}

export interface LoginLogRow {
  id: number;
  username: string;
  loginStatus: string;
  ip: string;
  userAgent: string;
  message: string;
  createTimeText: string;
}

export interface OpLogRow {
  id: number;
  module: string;
  action: string;
  operatorName: string;
  successText: string;
  costMsText: string;
  requestMethod: string;
  requestUri: string;
  errorMsg: string;
  createTimeText: string;
}

export interface EntityRecord {
  id: number;
  name: string;
  status: '启用' | '停用';
  remark: string;
  updatedAt: string;
  [key: string]: string | number | boolean | null | undefined | string[] | DepartmentRecord[];
}

export interface DepartmentRecord extends EntityRecord {
  name: string;
  parentId: number | null;
  leader: string;
  orderNum: number;
  status: '启用' | '停用';
  remark: string;
  updatedAt: string;
  children?: DepartmentRecord[];
}

export interface MenuRecord extends EntityRecord {
  name: string;
  menuType: string;
  routePath: string;
  component: string;
  permission: string;
  visible: '显示' | '隐藏';
  status: '启用' | '停用';
  updatedAt: string;
}

export interface DictTypeRecord extends EntityRecord {
  name: string;
  dictType: string;
  status: '启用' | '停用';
  remark: string;
  updatedAt: string;
}

export interface DictItemRecord extends EntityRecord {
  name: string;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  status: '启用' | '停用';
  remark: string;
  updatedAt: string;
}

export interface LogRecord extends EntityRecord {
  name: string;
  operator: string;
  ip: string;
  remark: string;
  updatedAt: string;
}

export interface CrudField {
  field: string;
  label: string;
  placeholder: string;
  type?: 'input' | 'password' | 'number' | 'select' | 'textarea';
  options?: Array<{ label: string; value: number | string }>;
  defaultValue?: string | number | null;
  required?: boolean;
  min?: number;
}

export interface CrudColumn {
  dataIndex: string;
  title: string;
}

export interface CrudMeta {
  key: string;
  title: string;
  permissionPrefix: string;
  columns: CrudColumn[];
  fields: CrudField[];
}
