<template>
  <a-drawer title="用户信息详情" v-model:visible="visible" size="68%" render-to-body :footer="false" @before-close="handleClose" class="detail-drawer">
    <a-spin :loading="loading" class="drawer-content">
      <!-- 基本信息 -->
      <h4 class="section-header">基本信息</h4>
      <a-row :gutter="20" class="mb8">
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">用户名称：</label>
            <span class="info-value plaintext">{{ info.nickName }}</span>
          </div>
        </a-col>
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">归属部门：</label>
            <span class="info-value plaintext">{{ (info.dept && info.dept.deptName) }}</span>
          </div>
        </a-col>
      </a-row>
      <a-row :gutter="20" class="mb8">
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">手机号码：</label>
            <span class="info-value plaintext">{{ info.phonenumber }}</span>
          </div>
        </a-col>
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">邮箱：</label>
            <span class="info-value plaintext">{{ info.email }}</span>
          </div>
        </a-col>
      </a-row>
      <a-row :gutter="20" class="mb8">
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">登录账号：</label>
            <span class="info-value plaintext">{{ info.userName }}</span>
          </div>
        </a-col>
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">用户状态：</label>
            <span class="info-value plaintext">
              <a-tag size="small" :color="info.status === '0' ? 'green' : 'red'">{{ info.status === '0' ? '正常' : '停用' }}</a-tag>
            </span>
          </div>
        </a-col>
      </a-row>
      <a-row :gutter="20" class="mb8">
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">用户性别：</label>
            <span class="info-value plaintext">{{ sexLabel }}</span>
          </div>
        </a-col>
      </a-row>
      <a-row :gutter="20" class="mb8">
        <a-col :span="24">
          <div class="info-item full-width">
            <label class="info-label">角色：</label>
            <span class="info-value plaintext">{{ roleNames || '无角色' }}</span>
          </div>
        </a-col>
      </a-row>
      <!-- 其他信息 -->
      <h4 class="section-header">其他信息</h4>
      <a-row :gutter="20" class="mb8">
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">创建者：</label>
            <span class="info-value plaintext">{{ info.createBy }}</span>
          </div>
        </a-col>
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">创建时间：</label>
            <span class="info-value plaintext">{{ info.createTime }}</span>
          </div>
        </a-col>
      </a-row>
      <a-row :gutter="20" class="mb8">
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">更新者：</label>
            <span class="info-value plaintext">{{ info.updateBy }}</span>
          </div>
        </a-col>
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">更新时间：</label>
            <span class="info-value plaintext">{{ info.updateTime }}</span>
          </div>
        </a-col>
      </a-row>
      <a-row :gutter="20" class="mb8">
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">最后登录IP：</label>
            <span class="info-value plaintext">{{ info.loginIp }}</span>
          </div>
        </a-col>
        <a-col :span="12">
          <div class="info-item">
            <label class="info-label">最后登录时间：</label>
            <span class="info-value plaintext">{{ info.loginDate }}</span>
          </div>
        </a-col>
      </a-row>
      <a-row :gutter="20" class="mb8">
        <a-col :span="24">
          <div class="info-item full-width">
            <label class="info-label">备注：</label>
            <span class="info-value plaintext">{{ info.remark }}</span>
          </div>
        </a-col>
      </a-row>
    </a-spin>
  </a-drawer>
</template>

<script setup>
import { getUser } from '@/api/system/user'

const visible = ref(false)
const loading = ref(false)
const info = reactive({})
const roleOptions = ref([])

const { sys_user_sex } = useDict("sys_user_sex")

const sexLabel = computed(() => selectDictLabel(sys_user_sex.value, info.sex) || '-')

const roleNames = computed(() => {
  if (!roleOptions.value.length || !info.roleIds) return ''
  return roleOptions.value.filter(r => info.roleIds?.includes(r.roleId)).map(r => r.roleName).join('、') || ''
})

const open = async (userId) => {
  visible.value = true
  loading.value = true
  try {
    const res = await getUser(userId)
    Object.assign(info, res.data || {})
    roleOptions.value = res.roles || []
    info.roleIds = res.roleIds || []
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

function handleClose() {
  visible.value = false
}

defineExpose({
  open
})
</script>
