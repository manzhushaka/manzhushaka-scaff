import type { DeptTreeVO, UserQuery } from '@/types/system';

export interface UserDeptTreeNode {
  key: number;
  title: string;
  children?: UserDeptTreeNode[];
}

interface BuildUserListQueryInput {
  pageNum: number;
  pageSize: number;
  keyword: string;
  status?: number;
  deptId?: number;
}

export function buildUserListQuery(input: BuildUserListQueryInput): UserQuery {
  return {
    pageNum: input.pageNum,
    pageSize: input.pageSize,
    username: input.keyword || undefined,
    nickname: input.keyword || undefined,
    status: input.status,
    deptId: input.deptId,
  };
}

export function buildUserDeptTreeData(depts: DeptTreeVO[]): UserDeptTreeNode[] {
  return depts.map((dept) => ({
    key: dept.id,
    title: dept.deptName,
    children: buildUserDeptTreeData(dept.children ?? []),
  }));
}
