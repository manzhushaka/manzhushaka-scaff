import { getProfile, listOperationLogs } from './admin';
import type { HttpResponse } from './interceptor';

export interface ProfileBasicRes {
  userId?: number;
  userName?: string;
  nickName?: string;
  email?: string;
  phonenumber?: string;
  sex?: string;
  avatar?: string;
  status?: string;
  loginIp?: string;
  loginDate?: string;
  createTime?: string;
  updateTime?: string;
  remark?: string;
  deptName?: string;
  roleGroup?: string;
}

export type operationLogRes = Array<{
  key: string;
  contentNumber: string;
  updateContent: string;
  status: number;
  updateTime: string;
}>;

function asRecord(value: unknown): Record<string, any> {
  return value && typeof value === 'object' ? (value as Record<string, any>) : {};
}

function formatDate(value: unknown) {
  return value ? String(value) : '-';
}

/** 查询当前登录用户的 Java 资料详情。 */
export async function queryProfileBasic(): Promise<HttpResponse<ProfileBasicRes>> {
  const response = await getProfile();
  const profile = asRecord(response.data);
  const roles = Array.isArray(profile.roles) ? profile.roles : [];
  return {
    code: response.code,
    msg: response.msg,
    data: {
      userId: profile.userId,
      userName: profile.userName,
      nickName: profile.nickName,
      email: profile.email,
      phonenumber: profile.phonenumber,
      sex: profile.sex,
      avatar: profile.avatar,
      status: profile.status,
      loginIp: profile.loginIp,
      loginDate: formatDate(profile.loginDate),
      createTime: formatDate(profile.createTime),
      updateTime: formatDate(profile.updateTime),
      remark: profile.remark,
      deptName: profile.dept?.deptName,
      roleGroup: String(response.roleGroup || roles.map((role: Record<string, any>) => role.roleName).join('、')),
    },
  };
}

/** 查询 Java 操作日志并转换为基础详情页的表格结构。 */
export async function queryOperationLog(): Promise<HttpResponse<operationLogRes>> {
  const response = await listOperationLogs({ pageNum: 1, pageSize: 10 });
  const data = (response.rows || []).map((record, index) => ({
    key: String(record.operId || index + 1),
    contentNumber: String(record.operId || index + 1),
    updateContent: `${record.title || '系统操作'} ${record.operUrl || ''}`.trim(),
    status: Number(record.status) || 0,
    updateTime: formatDate(record.operTime),
  }));
  return { code: response.code, msg: response.msg, data };
}
