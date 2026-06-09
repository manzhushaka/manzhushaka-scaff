export interface LoginPayload {
  username: string;
  password: string;
}

export interface UserProfile {
  userId: number;
  username: string;
  nickname: string;
  deptId: number;
  deptName?: string;
  roleCodes: string[];
  permCodes: string[];
}

export type MenuType = 'DIR' | 'MENU' | 'BUTTON';

export interface MenuItem {
  id: number;
  name: string;
  type: MenuType;
  path: string;
  component?: string;
  title: string;
  icon?: string;
  hidden?: boolean;
  redirect?: string;
  permission?: string;
  children?: MenuItem[];
}
