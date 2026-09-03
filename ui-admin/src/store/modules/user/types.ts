export type RoleType = string;
export interface UserState {
  id?: number;
  name?: string;
  userName?: string;
  nickName?: string;
  avatar?: string;
  job?: string;
  organization?: string;
  location?: string;
  email?: string;
  introduction?: string;
  personalWebsite?: string;
  jobName?: string;
  organizationName?: string;
  locationName?: string;
  phone?: string;
  registrationDate?: string;
  accountId?: string;
  certification?: number;
  role: RoleType;
  roles: string[];
  permissions: string[];
  forceChangePassword: boolean;
  passwordExpired: boolean;
  initialized: boolean;
}
