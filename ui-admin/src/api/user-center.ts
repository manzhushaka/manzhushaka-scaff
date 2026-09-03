import { getProfile, updateProfile, uploadUserAvatar } from './admin';
import { queryAdminSnapshot } from './analytics';

export interface MyProjectRecord {
  id: number;
  name: string;
  description: string;
  peopleNumber: number;
  contributors: {
    name: string;
    email: string;
    avatar: string;
  }[];
}

export interface MyTeamRecord {
  id: number;
  avatar: string;
  name: string;
  peopleNumber: number;
}

export interface LatestActivity {
  id: number;
  title: string;
  description: string;
  avatar: string;
}

export interface BasicInfoModel {
  email: string;
  nickname: string;
  countryRegion: string;
  area: string;
  address: string;
  profile: string;
}

export interface EnterpriseCertificationModel {
  accountType: number;
  status: number;
  time: string;
  legalPerson: string;
  certificateType: string;
  authenticationNumber: string;
  enterpriseName: string;
  enterpriseCertificateType: string;
  organizationCode: string;
}

export type CertificationRecord = Array<{
  certificationType: number;
  certificationContent: string;
  status: number;
  time: string;
}>;

export interface UnitCertification {
  enterpriseInfo: EnterpriseCertificationModel;
  record: CertificationRecord;
}

/** 查询当前用户可见的 Java 菜单资源。 */
export async function queryMyProjectList() {
  const snapshot = await queryAdminSnapshot();
  const data: MyProjectRecord[] = snapshot.menus.rows.slice(0, 8).map((record, index) => ({
    id: Number(record.menuId) || index + 1,
    name: String(record.menuName || '未命名菜单'),
    description: String(record.remark || record.path || 'Java 菜单资源'),
    peopleNumber: 0,
    contributors: [],
  }));
  return { data };
}

/** 查询当前用户可见的 Java 部门资源。 */
export async function queryMyTeamList() {
  const snapshot = await queryAdminSnapshot();
  const data: MyTeamRecord[] = snapshot.departments.rows.slice(0, 8).map((record, index) => ({
    id: Number(record.deptId) || index + 1,
    avatar: '',
    name: String(record.deptName || '未命名部门'),
    peopleNumber: 0,
  }));
  return { data };
}

/** 查询 Java 操作日志并转换为用户中心活动记录。 */
export async function queryLatestActivity() {
  const snapshot = await queryAdminSnapshot();
  const data: LatestActivity[] = snapshot.operationLogs.rows.slice(0, 8).map((record, index) => ({
    id: Number(record.operId) || index + 1,
    title: String(record.title || '系统操作'),
    description: `${record.requestMethod || '-'} ${record.operUrl || ''}`.trim(),
    avatar: '',
  }));
  return { data };
}

/** 保存当前用户资料。 */
export function saveUserInfo(data: Record<string, unknown>) {
  return updateProfile(data);
}

/** 查询实名认证信息。Java 管理端当前未提供该业务模型，因此返回空结果。 */
export async function queryCertification() {
  return {
    data: {
      enterpriseInfo: {} as EnterpriseCertificationModel,
      record: [] as CertificationRecord,
    },
  };
}

/** 上传当前用户头像。 */
export function userUploadApi(data: FormData, config: {
  controller: AbortController;
  onUploadProgress?: (progressEvent: unknown) => void;
}) {
  return uploadUserAvatar(data);
}

/** 查询当前用户资料，供认证页及其他用户中心组件复用。 */
export function queryUserProfile() {
  return getProfile();
}
