import test from 'node:test';
import assert from 'node:assert/strict';
import { buildUserDeptTreeData, buildUserListQuery } from '../src/views/system/users-support.ts';

test('includes the selected department id in the user list query', () => {
  assert.deepEqual(
    buildUserListQuery({
      pageNum: 1,
      pageSize: 10,
      keyword: 'admin',
      status: 1,
      deptId: 100,
    }),
    {
      pageNum: 1,
      pageSize: 10,
      username: 'admin',
      nickname: 'admin',
      status: 1,
      deptId: 100,
    },
  );
});

test('maps department tree data for the user page sidebar', () => {
  assert.deepEqual(
    buildUserDeptTreeData([
      {
        id: 100,
        parentId: 0,
        deptName: 'manzhushaka',
        ancestorPath: ',',
        sort: 0,
        status: 1,
        children: [
          {
            id: 101,
            parentId: 100,
            deptName: '平台研发部',
            ancestorPath: ',100,',
            sort: 1,
            status: 1,
            children: [],
          },
        ],
      },
    ]),
    [
      {
        key: 100,
        title: 'manzhushaka',
        children: [
          {
            key: 101,
            title: '平台研发部',
            children: [],
          },
        ],
      },
    ],
  );
});
