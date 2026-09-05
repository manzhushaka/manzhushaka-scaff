<template>
        <a-form-item v-if="resource === 'users' && !form.userId" field="userName" label="用户名称" :rules="requiredRule">
          <a-input v-model="form.userName" placeholder="请输入用户名称" />
        </a-form-item>
        <a-form-item v-if="resource === 'users' && !form.userId" field="password" label="用户密码" :rules="requiredRule">
          <a-input-password v-model="form.password" placeholder="请输入用户密码" />
        </a-form-item>
        <a-form-item v-if="resource === 'users'" field="nickName" label="用户昵称" :rules="requiredRule">
          <a-input v-model="form.nickName" placeholder="请输入用户昵称" />
        </a-form-item>
        <a-form-item v-if="resource === 'users'" field="phonenumber" label="手机号码">
          <a-input v-model="form.phonenumber" placeholder="请输入手机号码" />
        </a-form-item>
        <a-form-item v-if="resource === 'users'" field="email" label="邮箱">
          <a-input v-model="form.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item v-if="resource === 'users'" field="sex" label="性别">
          <a-select v-model="form.sex"><a-option value="0">男</a-option><a-option value="1">女</a-option><a-option value="2">未知</a-option></a-select>
        </a-form-item>
        <a-form-item v-if="resource === 'users'" field="status" label="状态">
          <a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">停用</a-radio></a-radio-group>
        </a-form-item>

        <a-form-item v-if="resource === 'roles'" field="roleName" label="角色名称" :rules="requiredRule">
          <a-input v-model="form.roleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item v-if="resource === 'roles'" field="roleKey" label="角色权限" :rules="requiredRule">
          <a-input v-model="form.roleKey" placeholder="请输入角色权限" />
        </a-form-item>
        <a-form-item v-if="resource === 'roles'" field="roleSort" label="显示顺序" :rules="requiredRule">
          <a-input-number v-model="form.roleSort" :min="0" />
        </a-form-item>
        <a-form-item v-if="resource === 'roles'" field="status" label="状态">
          <a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">停用</a-radio></a-radio-group>
        </a-form-item>
        <a-form-item v-if="resource === 'roles'" field="remark" label="备注"><a-textarea v-model="form.remark" /></a-form-item>

        <a-form-item v-if="resource === 'menus'" field="parentId" label="上级菜单">
          <a-tree-select v-model="form.parentId" :data="menuTree" :field-names="treeFieldNames" allow-clear placeholder="请选择上级菜单" />
        </a-form-item>
        <a-form-item v-if="resource === 'menus'" field="menuName" label="菜单名称" :rules="requiredRule"><a-input v-model="form.menuName" /></a-form-item>
        <a-form-item v-if="resource === 'menus'" field="menuType" label="菜单类型"><a-radio-group v-model="form.menuType"><a-radio value="M">目录</a-radio><a-radio value="C">菜单</a-radio><a-radio value="F">按钮</a-radio></a-radio-group></a-form-item>
        <a-form-item v-if="resource === 'menus' && form.menuType !== 'F'" field="path" label="路由地址"><a-input v-model="form.path" /></a-form-item>
        <a-form-item v-if="resource === 'menus' && form.menuType !== 'F'" field="component" label="组件路径"><a-input v-model="form.component" /></a-form-item>
        <a-form-item v-if="resource === 'menus'" field="orderNum" label="显示排序"><a-input-number v-model="form.orderNum" :min="0" /></a-form-item>
        <a-form-item v-if="resource === 'menus'" field="perms" label="权限标识"><a-input v-model="form.perms" /></a-form-item>
        <a-form-item v-if="resource === 'menus'" field="icon" label="图标">
          <a-input
            :model-value="form.icon"
            readonly
            placeholder="请选择图标"
            aria-label="选择图标"
            @click="$emit('openIcon')"
            @keyup.enter="$emit('openIcon')"
            @keyup.space.prevent="$emit('openIcon')"
          >
            <template #prefix>
              <component :is="selectedIcon?.component || iconMenu" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item v-if="resource === 'departments' && form.parentId !== 0" field="parentId" label="上级部门">
          <a-tree-select v-model="form.parentId" :data="departmentTree" :field-names="deptTreeFieldNames" allow-clear />
        </a-form-item>
        <a-form-item v-if="resource === 'departments'" field="deptName" label="部门名称" :rules="requiredRule"><a-input v-model="form.deptName" /></a-form-item>
        <a-form-item v-if="resource === 'departments'" field="orderNum" label="显示排序"><a-input-number v-model="form.orderNum" :min="0" /></a-form-item>
        <a-form-item v-if="resource === 'departments'" field="leader" label="负责人"><a-input v-model="form.leader" /></a-form-item>
        <a-form-item v-if="resource === 'departments'" field="phone" label="联系电话"><a-input v-model="form.phone" /></a-form-item>
        <a-form-item v-if="resource === 'departments'" field="email" label="邮箱"><a-input v-model="form.email" /></a-form-item>
        <a-form-item v-if="resource === 'departments'" field="status" label="状态"><a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">停用</a-radio></a-radio-group></a-form-item>

        <a-form-item v-if="resource === 'dictTypes'" field="dictName" label="字典名称" :rules="requiredRule"><a-input v-model="form.dictName" /></a-form-item>
        <a-form-item v-if="resource === 'dictTypes'" field="dictType" label="字典类型" :rules="requiredRule"><a-input v-model="form.dictType" /></a-form-item>
        <a-form-item v-if="resource === 'dictTypes'" field="status" label="状态"><a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">停用</a-radio></a-radio-group></a-form-item>
        <a-form-item v-if="resource === 'dictTypes'" field="remark" label="备注"><a-textarea v-model="form.remark" /></a-form-item>

        <a-form-item v-if="resource === 'configs'" field="configName" label="参数名称" :rules="requiredRule"><a-input v-model="form.configName" /></a-form-item>
        <a-form-item v-if="resource === 'configs'" field="configKey" label="参数键名" :rules="requiredRule"><a-input v-model="form.configKey" /></a-form-item>
        <a-form-item v-if="resource === 'configs'" field="configValue" label="参数键值" :rules="requiredRule"><a-textarea v-model="form.configValue" /></a-form-item>
        <a-form-item v-if="resource === 'configs'" field="configType" label="系统内置"><a-radio-group v-model="form.configType"><a-radio value="Y">是</a-radio><a-radio value="N">否</a-radio></a-radio-group></a-form-item>
        <a-form-item v-if="resource === 'configs'" field="remark" label="备注"><a-textarea v-model="form.remark" /></a-form-item>

        <a-form-item v-if="resource === 'jobs'" field="jobName" label="任务名称" :rules="requiredRule"><a-input v-model="form.jobName" /></a-form-item>
        <a-form-item v-if="resource === 'jobs'" field="jobGroup" label="任务组名"><a-input v-model="form.jobGroup" /></a-form-item>
        <a-form-item v-if="resource === 'jobs'" field="invokeTarget" label="调用目标" :rules="requiredRule"><a-input v-model="form.invokeTarget" /></a-form-item>
        <a-form-item v-if="resource === 'jobs'" field="cronExpression" label="Cron 表达式"><a-input v-model="form.cronExpression" /></a-form-item>
        <a-form-item v-if="resource === 'jobs'" field="status" label="状态"><a-radio-group v-model="form.status"><a-radio value="0">正常</a-radio><a-radio value="1">暂停</a-radio></a-radio-group></a-form-item>
</template>

<script lang="ts" setup>
  import { reactive, watch } from 'vue';
  import type { Component } from 'vue';
  import type { Resource } from './resource-config';

  const props = defineProps<{
    resource: Resource;
    formData: Record<string, any>;
    requiredRule: Array<Record<string, unknown>>;
    menuTree: Record<string, any>[];
    departmentTree: Record<string, any>[];
    treeFieldNames: Record<string, string>;
    deptTreeFieldNames: Record<string, string>;
    selectedIcon?: { component: Component };
    iconMenu: Component;
  }>();
  const emit = defineEmits<{
    (event: 'openIcon'): void;
    (event: 'updateForm', value: Record<string, any>): void;
  }>();
  const form = reactive<Record<string, any>>({});

  watch(
    () => props.formData,
    (value) => {
      Object.keys(form).forEach((key) => delete form[key]);
      Object.assign(form, value);
    },
    { deep: true, immediate: true },
  );

  watch(
    form,
    (value) => {
      emit('updateForm', { ...value });
    },
    { deep: true },
  );
</script>
