import DashboardView from '@/views/dashboard/index.vue';
import UsersView from '@/views/system/users.vue';
import RolesView from '@/views/system/roles.vue';
import DeptsView from '@/views/system/depts.vue';
import MenusView from '@/views/system/menus.vue';
import DictsView from '@/views/system/dicts.vue';
import ParamsView from '@/views/system/params.vue';
import LoginLogsView from '@/views/system/login-logs.vue';
import OpLogsView from '@/views/system/op-logs.vue';
import MqMessagesView from '@/views/system/mq-messages.vue';

export const componentMap = {
  'dashboard/index': DashboardView,
  'system/users': UsersView,
  'system/roles': RolesView,
  'system/depts': DeptsView,
  'system/menus': MenusView,
  'system/dicts': DictsView,
  'system/params': ParamsView,
  'system/login-logs': LoginLogsView,
  'system/op-logs': OpLogsView,
  'system/mq-messages': MqMessagesView,
} as const;
