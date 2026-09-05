import request from './interceptor';
import type { QueryParams } from './types';
/** 查询个人资料。 */
export function getProfile() {
  return request.get('/system/user/profile');
}

/** 修改个人资料。 */
export function updateProfile(data: QueryParams) {
  return request.put('/system/user/profile', data);
}

/** 修改个人密码。 */
export function updateProfilePassword(data: QueryParams) {
  return request.put('/system/user/profile/updatePwd', data);
}
